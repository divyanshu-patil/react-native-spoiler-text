package com.blurtext

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ReplacementSpan

class BlurReplacementSpan(
  private val radius: Float = 8f
) : ReplacementSpan() {

  override fun getSize(
    paint: Paint,
    text: CharSequence,
    start: Int,
    end: Int,
    fm: Paint.FontMetricsInt?
  ): Int {
    return paint.measureText(text, start, end).toInt()
  }

  override fun draw(
    canvas: Canvas,
    text: CharSequence,
    start: Int,
    end: Int,
    x: Float,
    top: Int,
    y: Int,
    bottom: Int,
    paint: Paint
  ) {
    val original = paint.maskFilter

    paint.maskFilter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)

    canvas.drawText(text, start, end, x, y.toFloat(), paint)

    paint.maskFilter = original
  }
}
