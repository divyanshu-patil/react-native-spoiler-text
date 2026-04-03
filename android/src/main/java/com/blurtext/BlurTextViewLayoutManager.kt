package com.blurtext

import com.facebook.react.bridge.Arguments

class BlurTextViewLayoutManager(private val view: BlurTextView) {
  private var forceHeightRecalculationCounter: Int = 0

  fun invalidateLayout() {
    val text = view.text
    val paint = view.paint

    val needUpdate = MeasurementStore.store(view.id, text, paint)

    val counter = forceHeightRecalculationCounter
    forceHeightRecalculationCounter++

    val lineCount = view.layout?.lineCount ?: 1

    val state = Arguments.createMap()
    state.putInt("forceHeightRecalculationCounter", counter)
    state.putInt("lineCount", lineCount)

    view.stateWrapper?.updateState(state)
    view.requestLayout()
  }

  fun releaseMeasurementStore() {
    MeasurementStore.release(view.id)
  }
}
