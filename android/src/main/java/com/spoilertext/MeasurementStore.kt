package com.spoilertext

import android.content.Context
import android.graphics.Typeface
import android.graphics.text.LineBreaker
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.PixelUtil
import com.facebook.react.views.text.ReactTypefaceUtils.applyStyles
import com.facebook.react.views.text.ReactTypefaceUtils.parseFontStyle
import com.facebook.react.views.text.ReactTypefaceUtils.parseFontWeight
import com.facebook.yoga.YogaMeasureOutput
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.ceil

object MeasurementStore {
  data class PaintParams(
    val typeface: Typeface,
    val fontSize: Float,
  )

  data class MeasurementParams(
    val initialized: Boolean,

    val cachedWidth: Float,
    val cachedSize: Long,

    val spannable: CharSequence?,
    val paintParams: PaintParams,
  )

  private val data = ConcurrentHashMap<Int, MeasurementParams>()

  fun store(id: Int, spannable: CharSequence?, paint: TextPaint): Boolean {
    val existing = data[id]

    val paintParams = PaintParams(paint.typeface, paint.textSize)

    data[id] = MeasurementParams(
      initialized = true,
      cachedWidth = existing?.cachedWidth ?: 0f,
      cachedSize = existing?.cachedSize ?: 0L,
      spannable = spannable,
      paintParams = paintParams
    )

    return true // always trigger update
  }

  fun release(id: Int) {
    data.remove(id)
  }

  private fun measure(maxWidth: Float, spannable: CharSequence?, paintParams: PaintParams): Long {
    val paint = TextPaint().apply {
      typeface = paintParams.typeface
      textSize = paintParams.fontSize
    }

    return measure(maxWidth, spannable, paint)
  }

  private fun measure(maxWidth: Float, spannable: CharSequence?, paint: TextPaint): Long {
    val text = spannable ?: ""
    val textLength = text.length

    val naturalWidth = paint.measureText(text, 0, textLength)

    val layoutWidth = if (maxWidth > 0f) {
      // IMPORTANT: constrain, don't force
      minOf(naturalWidth, maxWidth)
    } else {
      naturalWidth
    }

    val builder = StaticLayout.Builder
      .obtain(text, 0, textLength, paint, ceil(layoutWidth).toInt())  // use layoutWidth, not maxWidth
      .setIncludePad(false)   // match React Native Text behavior
      .setLineSpacing(0f, 1f)

    builder.setAlignment(Layout.Alignment.ALIGN_NORMAL)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      builder.setBreakStrategy(LineBreaker.BREAK_STRATEGY_HIGH_QUALITY)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      builder.setUseLineSpacingFromFallbacks(false)
    }

    val staticLayout = builder.build()

    val actualWidth = (0 until staticLayout.lineCount)
      .maxOfOrNull { staticLayout.getLineWidth(it) } ?: layoutWidth

    val widthInSP = PixelUtil.toDIPFromPixel(ceil(actualWidth))

    val heightInSP = PixelUtil.toDIPFromPixel(staticLayout.height.toFloat())
    return YogaMeasureOutput.make(widthInSP, heightInSP)
  }

  // Returns plain text defaultValue, or "I" if no defaultValue
  private fun getInitialText(defaultView: SpoilerTextView, props: ReadableMap?): CharSequence {
    val defaultValue = props?.getString("defaultValue")

    // If there is no default value, assume text is one line, "I" is a good approximation of height
    if (defaultValue == null) return "I"


   return defaultValue

  }

  private fun getInitialFontSize(defaultView: SpoilerTextView, props: ReadableMap?): Float {
    val propsFontSize = props?.getDouble("fontSize")?.toFloat()
    if (propsFontSize == null) return defaultView.textSize

    return ceil(PixelUtil.toPixelFromSP(propsFontSize))
  }

  // Called when view measurements are not available in the store
  // Most likely first measurement, we can use defaultValue, as no native state is set yet
  private fun initialMeasure(context: Context, id: Int?, width: Float, props: ReadableMap?): Long {
    val defaultView = SpoilerTextView(context)

    val text = getInitialText(defaultView, props)
    val fontSize = getInitialFontSize(defaultView, props)

    val fontFamily = props?.getString("fontFamily")
    val fontStyle = parseFontStyle(props?.getString("fontStyle"))
    val fontWeight = parseFontWeight(props?.getString("fontWeight"))

    val typeface = applyStyles(defaultView.typeface, fontStyle, fontWeight, fontFamily, context.assets)
    val paintParams = PaintParams(typeface, fontSize)
    val size = measure(width, text, PaintParams(typeface, fontSize))

    if (id != null) {
      data[id] = MeasurementParams(true, width, size, text, paintParams)
    }

    return size
  }

fun getMeasureById(context: Context, id: Int?, width: Float, props: ReadableMap?): Long {
  val resolvedId = id ?: return initialMeasure(context, id, width, props)
  val value = data[resolvedId] ?: return initialMeasure(context, resolvedId, width, props)

  if (!value.initialized) {
    return initialMeasure(context, resolvedId, width, props)
  }

  if (width == value.cachedWidth) {
    return value.cachedSize
  }

  val paint = TextPaint().apply {
    typeface = value.paintParams.typeface
    textSize = value.paintParams.fontSize
  }

  val size = measure(width, value.spannable, paint)
  data[resolvedId] = MeasurementParams(true, width, size, value.spannable, value.paintParams)
  return size
}

}
