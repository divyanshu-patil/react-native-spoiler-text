package com.blurtext

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Color
import android.graphics.text.LineBreaker
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import com.facebook.react.common.ReactConstants
import com.facebook.react.uimanager.PixelUtil
import com.facebook.react.uimanager.StateWrapper
import com.facebook.react.views.text.ReactTypefaceUtils.applyStyles
import com.facebook.react.views.text.ReactTypefaceUtils.parseFontStyle
import com.facebook.react.views.text.ReactTypefaceUtils.parseFontWeight
import kotlin.math.ceil

class BlurTextView : AppCompatTextView {
  var stateWrapper: StateWrapper? = null

  lateinit var layoutManager: BlurTextViewLayoutManager

  var fontSize: Float? = null

  private var typefaceDirty = false
  private var fontFamily: String? = null
  private var fontStyle: Int = ReactConstants.UNSET
  private var fontWeight: Int = ReactConstants.UNSET

  private var lineHeightPx: Int? = null
  private var isInitialized = false

  private var isSpoilerActive = false
  private var particleColor: Int = Color.BLACK
  private val particles = mutableListOf<Particle>()
  private val particlePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
  private var isRevealing = false
  private var revealProgress = 0f

  // Blur state
  private var blurRadius: Float = 0f

  constructor(context: Context) : super(context) {
    prepareComponent()
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    prepareComponent()
  }

  constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
    context,
    attrs,
    defStyleAttr
  ) {
    prepareComponent()
  }

  private fun prepareComponent() {
    isVerticalScrollBarEnabled = true
    gravity = Gravity.TOP or Gravity.START
    includeFontPadding = false
    setLineSpacing(0f, 1f)
    isElegantTextHeight = false

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      setFallbackLineSpacing(false)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      breakStrategy = LineBreaker.BREAK_STRATEGY_HIGH_QUALITY
    }

    setPadding(0, 0, 0, 0)
    setBackgroundColor(Color.TRANSPARENT)

    layoutManager = BlurTextViewLayoutManager(this)
  }

  // ── Blur ────────────────────────────────────────────────────────────────────

  /**
   * Sets a Gaussian blur radius on the text paint.
   * Pass 0f (or any non-positive value) to remove the blur.
   *
   * Hardware acceleration must be disabled on this view for BlurMaskFilter
   * to render correctly; the method handles that automatically.
   */
  fun setBlurRadius(radius: Float) {
    blurRadius = radius
    applyBlur()
    requestLayout()
  }

  private fun applyBlur() {
    if (blurRadius > 0f) {
      setLayerType(LAYER_TYPE_SOFTWARE, null)
      paint.maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
    } else {
      paint.maskFilter = null
      setLayerType(LAYER_TYPE_HARDWARE, null)
    }
    invalidate()
    requestLayout()
  }

  // ── Text ────────────────────────────────────────────────────────────────────

  fun setValue(value: CharSequence?) {
    if (value == null) return
    if (text?.toString() == value.toString()) return
    text = value

    requestLayout()
    invalidate()

    layoutManager.invalidateLayout()
  }

  // ── Appearance ──────────────────────────────────────────────────────────────

  fun setFontSize(size: Float) {
    if (size == 0f) return
    val sizePx = ceil(PixelUtil.toPixelFromSP(size))
    fontSize = sizePx
    setTextSize(TypedValue.COMPLEX_UNIT_PX, sizePx)

    requestLayout()
    invalidate()
    layoutManager.invalidateLayout()
  }

  fun setFontFamily(family: String?) {
    if (family != fontFamily) {
      fontFamily = family
      typefaceDirty = true
    }

    requestLayout()
    invalidate()
  }

  fun setFontWeight(weight: String?) {
    val parsed = parseFontWeight(weight)
    if (parsed != fontWeight) {
      fontWeight = parsed
      typefaceDirty = true
    }

    requestLayout()
    invalidate()
  }

  fun setFontStyle(style: String?) {
    val parsed = parseFontStyle(style)
    if (parsed != fontStyle) {
      fontStyle = parsed
      typefaceDirty = true
    }

    requestLayout()
    invalidate()
  }

  fun setLineHeightReact(lineHeight: Float) {
    if (lineHeight <= 0f) return
    lineHeightPx = ceil(PixelUtil.toPixelFromDIP(lineHeight)).toInt()
    applyLineHeight()

    requestLayout()
    invalidate()
  }

  private fun applyLineHeight() {
    val lh = lineHeightPx ?: return

    val fm = paint.fontMetricsInt
    val fontHeight = fm.descent - fm.ascent

    val spacing = lh - fontHeight

    if (spacing >= 0) {
      // RN-like behavior
      setLineSpacing(spacing.toFloat(), 1f)
    } else {
      // If lineHeight < fontHeight, RN still clamps
      setLineSpacing(0f, 1f)
    }
  }

  private val frameCallback = object : android.view.Choreographer.FrameCallback {
    override fun doFrame(frameTimeNanos: Long) {
      if (!isSpoilerActive) return
      updateParticles()
      invalidate()
      android.view.Choreographer.getInstance().postFrameCallback(this)
    }
  }

  private data class Particle(
    var x: Float, var y: Float,
    var vx: Float, var vy: Float,
    var alpha: Int, var radius: Float,
    var life: Float, var maxLife: Float
  )


  fun setSpoiler(active: Boolean) {
    if (!active && isSpoilerActive) {
      // Start reveal animation instead of instant hide
      startReveal()
      return
    }
    isSpoilerActive = active
    if (active) {
      isRevealing = false
      revealProgress = 0f
      setTextColor(Color.TRANSPARENT)
      if (width > 0 && height > 0) spawnParticles()
      android.view.Choreographer.getInstance().postFrameCallback(frameCallback)
    } else {
      android.view.Choreographer.getInstance().removeFrameCallback(frameCallback)
      particles.clear()
      setTextColor(particleColor)
      invalidate()
    }
  }

  private fun startReveal() {
    isRevealing = true
    isSpoilerActive = false
    revealProgress = 0f
    android.view.Choreographer.getInstance().removeFrameCallback(frameCallback)
    // Set text color back so it's ready to draw, alpha controlled by canvas layer
    setTextColor(particleColor)
    android.view.Choreographer.getInstance().postFrameCallback(revealCallback)
  }

  private val revealCallback = object : android.view.Choreographer.FrameCallback {
    override fun doFrame(frameTimeNanos: Long) {
      revealProgress += 0.033f
      if (revealProgress >= 1f) {
        isRevealing = false
        isSpoilerActive = false
        particles.clear()
        setTextColor(particleColor)
        invalidate()
        return
      }
      updateRevealParticles()
      invalidate()
      android.view.Choreographer.getInstance().postFrameCallback(this)
    }
  }



  private fun updateRevealParticles() {
    val w = width.toFloat()
    val revealX = revealProgress * (w + 100f) - 50f

    val iterator = particles.iterator()
    while (iterator.hasNext()) {
      val p = iterator.next()
      val distFromWave = p.x - revealX

      when {
        distFromWave < -40f -> {
          iterator.remove()
        }
        distFromWave < 40f -> {
          val waveProgress = 1f - ((distFromWave + 40f) / 80f)
          p.x -= waveProgress * 3f
          p.vy *= 0.85f
          p.vx *= 0.85f
          p.alpha = ((1f - waveProgress) * 220f).toInt().coerceIn(0, 255)
          if (p.alpha <= 0) iterator.remove()
        }
        else -> {
          p.x += p.vx
          p.y += p.vy
        }
      }
    }
  }

  fun setColor(colorInt: Int?) {
    particleColor = colorInt ?: Color.BLACK
    if (!isSpoilerActive) setTextColor(particleColor)
  }

  private fun spawnParticles() {
    particles.clear()
    val w = width.toFloat()
    val h = height.toFloat()
    if (w == 0f || h == 0f) return
    val count = (w * h / 12f).toInt().coerceIn(20, 800)
    repeat(count) { particles.add(randomParticle(w, h)) }
  }

  private fun randomParticle(w: Float, h: Float): Particle {
    val maxLife = kotlin.random.Random.nextFloat() * 60f + 30f
    return Particle(
      x = kotlin.random.Random.nextFloat() * w,
      y = kotlin.random.Random.nextFloat() * h,
      vx = (kotlin.random.Random.nextFloat() - 0.5f) * 0.8f,
      vy = (kotlin.random.Random.nextFloat() - 0.5f) * 0.8f,
      alpha = kotlin.random.Random.nextInt(80, 200),
      radius = kotlin.random.Random.nextFloat() * 1.2f + 0.4f,
      life = kotlin.random.Random.nextFloat() * maxLife,
      maxLife = maxLife
    )
  }

  private fun updateParticles() {
    val w = width.toFloat()
    val h = height.toFloat()
    if (w == 0f || h == 0f) return
    val toAdd = mutableListOf<Particle>()
    val iterator = particles.iterator()
    while (iterator.hasNext()) {
      val p = iterator.next()
      p.x += p.vx
      p.y += p.vy
      p.life += 1f
      val progress = p.life / p.maxLife
      p.alpha = when {
        progress < 0.2f -> (progress / 0.2f * 200).toInt()
        progress > 0.8f -> ((1f - (progress - 0.8f) / 0.2f) * 200).toInt()
        else -> 200
      }.coerceIn(0, 255)
      if (p.life >= p.maxLife) {
        iterator.remove()
        toAdd.add(randomParticle(w, h))
      }
    }
    particles.addAll(toAdd)
  }

  // ── Layout / lifecycle ──────────────────────────────────────────────────────

  override fun onDraw(canvas: android.graphics.Canvas) {
    when {
      isRevealing -> {
        val textAlpha = (revealProgress * 255f).toInt().coerceIn(0, 255)

        canvas.save()
        canvas.translate(0f, paint.fontMetricsInt.descent.toFloat())
        canvas.saveLayerAlpha(
          0f, -paint.fontMetricsInt.descent.toFloat(),
          width.toFloat(), height.toFloat(),
          textAlpha
        )
        super.onDraw(canvas)
        canvas.restore()
        canvas.restore()

        drawParticles(canvas)
      }

      isSpoilerActive -> {
        drawParticles(canvas)
      }

      else -> {
        canvas.save()
        canvas.translate(0f, paint.fontMetricsInt.descent.toFloat())
        super.onDraw(canvas)
        canvas.restore()
      }
    }
  }

  override fun getBaseline(): Int {
    val fm = paint.fontMetricsInt
    return fm.descent + (-fm.ascent)
  }

  private fun drawParticles(canvas: android.graphics.Canvas) {
    val r = (particleColor shr 16) and 0xFF
    val g = (particleColor shr 8) and 0xFF
    val b = particleColor and 0xFF
    for (p in particles) {
      particlePaint.color = Color.argb(p.alpha, r, g, b)
      canvas.drawCircle(p.x, p.y, p.radius, particlePaint)
    }
  }

  fun afterUpdateTransaction() {
    updateTypeface()
    applyLineHeight()
    applyBlur()
    isInitialized = true
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    android.view.Choreographer.getInstance().removeFrameCallback(frameCallback)
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    if (isSpoilerActive) spawnParticles()
  }

  private fun updateTypeface() {
    if (!typefaceDirty) return
    typefaceDirty = false
    val newTypeface = applyStyles(typeface, fontStyle, fontWeight, fontFamily, context.assets)
    typeface = newTypeface
    paint.typeface = newTypeface
  }
}
