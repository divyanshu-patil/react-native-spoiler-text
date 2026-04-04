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
import kotlin.math.sqrt

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

  private var revealTouchX: Float = -1f
  private var revealTouchY: Float = -1f
  private var blurRadius: Float = 0f

  constructor(context: Context) : super(context) { prepareComponent() }
  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { prepareComponent() }
  constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { prepareComponent() }

  private fun prepareComponent() {
    isVerticalScrollBarEnabled = true
    gravity = Gravity.TOP or Gravity.START
    includeFontPadding = false
    setLineSpacing(0f, 1f)
    isElegantTextHeight = false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) setFallbackLineSpacing(false)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) breakStrategy = LineBreaker.BREAK_STRATEGY_HIGH_QUALITY
    setPadding(0, 0, 0, 0)
    setBackgroundColor(Color.TRANSPARENT)
    layoutManager = BlurTextViewLayoutManager(this)
  }

  // ── Blur ─────────────────────────────────────────────────────────────────────

  fun setBlurRadius(radius: Float) {
    blurRadius = radius
    if (!isRevealing) applyBlur()
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
  }

  // ── Text ──────────────────────────────────────────────────────────────────────

  fun setValue(value: CharSequence?) {
    if (value == null) return
    if (text?.toString() == value.toString()) return
    text = value
    requestLayout()
    invalidate()
    layoutManager.invalidateLayout()
  }

  // ── Appearance ────────────────────────────────────────────────────────────────

  fun setFontSize(size: Float) {
    if (size == 0f) return
    val sizePx = ceil(PixelUtil.toPixelFromSP(size))
    fontSize = sizePx
    setTextSize(TypedValue.COMPLEX_UNIT_PX, sizePx)
    requestLayout(); invalidate(); layoutManager.invalidateLayout()
  }

  fun setFontFamily(family: String?) {
    if (family != fontFamily) { fontFamily = family; typefaceDirty = true }
    requestLayout(); invalidate()
  }

  fun setFontWeight(weight: String?) {
    val parsed = parseFontWeight(weight)
    if (parsed != fontWeight) { fontWeight = parsed; typefaceDirty = true }
    requestLayout(); invalidate()
  }

  fun setFontStyle(style: String?) {
    val parsed = parseFontStyle(style)
    if (parsed != fontStyle) { fontStyle = parsed; typefaceDirty = true }
    requestLayout(); invalidate()
  }

  fun setLineHeightReact(lineHeight: Float) {
    if (lineHeight <= 0f) return
    lineHeightPx = ceil(PixelUtil.toPixelFromDIP(lineHeight)).toInt()
    applyLineHeight(); requestLayout(); invalidate()
  }

  private fun applyLineHeight() {
    val lh = lineHeightPx ?: return
    val fm = paint.fontMetricsInt
    val spacing = lh - (fm.descent - fm.ascent)
    setLineSpacing(if (spacing >= 0) spacing.toFloat() else 0f, 1f)
  }

  // ── Particle data class ───────────────────────────────────────────────────────

  private data class Particle(
    var x: Float, var y: Float,
    var vx: Float, var vy: Float,
    var alpha: Int, var radius: Float,
    var life: Float, var maxLife: Float
  )

  // ── Spoiler activate ──────────────────────────────────────────────────────────

  private val frameCallback = object : android.view.Choreographer.FrameCallback {
    override fun doFrame(frameTimeNanos: Long) {
      if (!isSpoilerActive) return
      updateParticles()
      invalidate()
      android.view.Choreographer.getInstance().postFrameCallback(this)
    }
  }

  fun setSpoiler(active: Boolean) {
    if (!active && isSpoilerActive) { startReveal(); return }
    isSpoilerActive = active
    if (active) {
      isRevealing = false; revealProgress = 0f
      setTextColor(Color.TRANSPARENT)
      if (width > 0 && height > 0) spawnParticles()
      android.view.Choreographer.getInstance().postFrameCallback(frameCallback)
    } else {
      android.view.Choreographer.getInstance().removeFrameCallback(frameCallback)
      particles.clear(); setTextColor(particleColor); invalidate()
    }
  }

  fun setRevealTouchPoint(x: Float, y: Float) { revealTouchX = x; revealTouchY = y }

  // ── Reveal ────────────────────────────────────────────────────────────────────

  private fun startReveal() {
    isRevealing = true; isSpoilerActive = false; revealProgress = 0f
    if (revealTouchX < 0f) revealTouchX = width / 2f
    if (revealTouchY < 0f) revealTouchY = height / 2f
    android.view.Choreographer.getInstance().removeFrameCallback(frameCallback)
    setTextColor(particleColor)
    setLayerType(LAYER_TYPE_SOFTWARE, null)
    android.view.Choreographer.getInstance().postFrameCallback(revealCallback)
  }

  private fun maxRevealRadius(): Float {
    val w = width.toFloat(); val h = height.toFloat()
    val cx = revealTouchX; val cy = revealTouchY
    return maxOf(
      sqrt(cx * cx + cy * cy),
      sqrt((w - cx) * (w - cx) + cy * cy),
      sqrt(cx * cx + (h - cy) * (h - cy)),
      sqrt((w - cx) * (w - cx) + (h - cy) * (h - cy))
    )
  }

  private val revealCallback = object : android.view.Choreographer.FrameCallback {
    override fun doFrame(frameTimeNanos: Long) {
      revealProgress += 0.028f
      if (revealProgress >= 1f) { finishReveal(); return }

      updateRevealParticles()

      // Blur dissolves: starts at 14px, fully gone by 60% of reveal
      val blurFade = (1f - (revealProgress / 0.6f)).coerceIn(0f, 1f)
      val dynamicBlur = 14f * blurFade
      paint.maskFilter = if (dynamicBlur > 0.5f) BlurMaskFilter(dynamicBlur, BlurMaskFilter.Blur.NORMAL) else null

      invalidate()
      android.view.Choreographer.getInstance().postFrameCallback(this)
    }
  }

  private fun finishReveal() {
    isRevealing = false; isSpoilerActive = false; particles.clear()
    setTextColor(particleColor)
    paint.maskFilter = if (blurRadius > 0f) BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
    else { setLayerType(LAYER_TYPE_HARDWARE, null); null }
    revealTouchX = -1f; revealTouchY = -1f
    invalidate()
  }

  /**
   * Telegram-style radial wipe:
   *  - A circle expands from the touch point (ease-out curve).
   *  - Particles INSIDE the circle fade out quietly in place — no scatter.
   *  - A 30px feather band at the wave edge softens the boundary.
   *  - Particles OUTSIDE keep their normal idle drift.
   */
  private fun updateRevealParticles() {
    val maxR = maxRevealRadius()
    val eased = 1f - (1f - revealProgress) * (1f - revealProgress)  // ease-out
    val waveRadius = eased * (maxR + 40f)
    val feather = 30f

    val iterator = particles.iterator()
    while (iterator.hasNext()) {
      val p = iterator.next()
      val dist = sqrt((p.x - revealTouchX).let { it * it } + (p.y - revealTouchY).let { it * it })

      when {
        dist < waveRadius - feather -> iterator.remove()           // fully cleared
        dist < waveRadius -> {                                      // feather band: fade in place
          val fadeT = (waveRadius - dist) / feather
          p.alpha = ((1f - fadeT) * p.alpha).toInt().coerceIn(0, 255)
          if (p.alpha < 2) iterator.remove()
        }
        else -> {                                                   // outside: normal drift
          p.x += p.vx; p.y += p.vy; p.life += 1f
          val prog = p.life / p.maxLife
          p.alpha = when {
            prog < 0.2f -> (prog / 0.2f * 200).toInt()
            prog > 0.8f -> ((1f - (prog - 0.8f) / 0.2f) * 200).toInt()
            else -> 200
          }.coerceIn(0, 255)
          if (p.life >= p.maxLife) iterator.remove()
        }
      }
    }
  }

  // ── Particle helpers ──────────────────────────────────────────────────────────

  fun setColor(colorInt: Int?) {
    particleColor = colorInt ?: Color.BLACK
    if (!isSpoilerActive) setTextColor(particleColor)
  }

  private fun spawnParticles() {
    particles.clear()
    val w = width.toFloat(); val h = height.toFloat()
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
    val w = width.toFloat(); val h = height.toFloat()
    if (w == 0f || h == 0f) return
    val toAdd = mutableListOf<Particle>()
    val iterator = particles.iterator()
    while (iterator.hasNext()) {
      val p = iterator.next()
      p.x += p.vx; p.y += p.vy; p.life += 1f
      val progress = p.life / p.maxLife
      p.alpha = when {
        progress < 0.2f -> (progress / 0.2f * 200).toInt()
        progress > 0.8f -> ((1f - (progress - 0.8f) / 0.2f) * 200).toInt()
        else -> 200
      }.coerceIn(0, 255)
      if (p.life >= p.maxLife) { iterator.remove(); toAdd.add(randomParticle(w, h)) }
    }
    particles.addAll(toAdd)
  }

  // ── Drawing ───────────────────────────────────────────────────────────────────

  override fun onDraw(canvas: android.graphics.Canvas) {
    if (!isRevealing) {
      paint.maskFilter =
        if (blurRadius > 0f)
          BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
        else null
    }
    when {
      isRevealing -> {
        val textAlpha = (revealProgress * 255f).toInt().coerceIn(0, 255)
        canvas.save()
        canvas.saveLayerAlpha(0f, -paint.fontMetricsInt.descent.toFloat(), width.toFloat(), height.toFloat(), textAlpha)
        super.onDraw(canvas)
        canvas.restore(); canvas.restore()
        drawParticles(canvas)   // remaining particles drawn on top of fading text
      }
      isSpoilerActive -> drawParticles(canvas)
      else -> {
        canvas.save()
        super.onDraw(canvas)
        canvas.restore()
      }
    }
  }

  override fun layout(l: Int, t: Int, r: Int, b: Int) {
    val fm = paint.fontMetricsInt
    val descent = fm.descent
    // Shift the view up by descent so baseline aligns correctly
    // and descenders have room to render below
    super.layout(l, t + descent, r, b + descent)
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

  // ── Lifecycle ─────────────────────────────────────────────────────────────────

  fun afterUpdateTransaction() {
    updateTypeface(); applyLineHeight(); applyBlur(); isInitialized = true
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    android.view.Choreographer.getInstance().removeFrameCallback(frameCallback)
    android.view.Choreographer.getInstance().removeFrameCallback(revealCallback)
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    if (isSpoilerActive) spawnParticles()
  }

  private fun updateTypeface() {
    if (!typefaceDirty) return
    typefaceDirty = false
    val newTypeface = applyStyles(typeface, fontStyle, fontWeight, fontFamily, context.assets)
    typeface = newTypeface; paint.typeface = newTypeface
  }
}
