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
  fun setColor(colorInt: Int?) {
    setTextColor(colorInt ?: Color.BLACK)
  }

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

  // ── Layout / lifecycle ──────────────────────────────────────────────────────

  override fun onDraw(canvas: android.graphics.Canvas) {
    val descent = paint.fontMetricsInt.descent
    canvas.save()
    canvas.translate(0f, descent.toFloat())
    super.onDraw(canvas)
    canvas.restore()
  }

  override fun getBaseline(): Int {
    val fm = paint.fontMetricsInt
    // After the onDraw translate, the visual baseline is at (descent + (-ascent)) from top
    return fm.descent + (-fm.ascent)
  }

  fun afterUpdateTransaction() {
    updateTypeface()
    applyLineHeight()
    applyBlur()
    isInitialized = true
  }


  private fun updateTypeface() {
    if (!typefaceDirty) return
    typefaceDirty = false
    val newTypeface = applyStyles(typeface, fontStyle, fontWeight, fontFamily, context.assets)
    typeface = newTypeface
    paint.typeface = newTypeface
  }
}
