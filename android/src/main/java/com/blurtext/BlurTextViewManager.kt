package com.blurtext

import android.content.Context
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.*
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.viewmanagers.BlurTextViewManagerDelegate
import com.facebook.react.viewmanagers.BlurTextViewManagerInterface
import com.facebook.yoga.YogaMeasureMode

@ReactModule(name = BlurTextViewManager.NAME)
class BlurTextViewManager :
  SimpleViewManager<BlurTextView>(),
  BlurTextViewManagerInterface<BlurTextView> {

  private val mDelegate: ViewManagerDelegate<BlurTextView> =
    BlurTextViewManagerDelegate(this)

  override fun getDelegate(): ViewManagerDelegate<BlurTextView>? = mDelegate

  override fun getName(): String = NAME

  override fun createViewInstance(context: ThemedReactContext): BlurTextView {
    return BlurTextView(context)
  }

  override fun updateState(
    view: BlurTextView,
    props: ReactStylesDiffMap?,
    stateWrapper: StateWrapper?
  ): Any? {
    view.stateWrapper = stateWrapper
    return super.updateState(view, props, stateWrapper)
  }

  override fun setText(view: BlurTextView?, value: String?) {
    view?.setValue(value)
  }

  @ReactProp(name = "color")
  override fun setColor(view: BlurTextView, value: Int?) {
    if (value != null) view.setColor(value)
  }

  override fun setBlurRadius(view: BlurTextView, value: Float) {
view?.setBlurRadius(value)
  }


  @ReactProp(name = "fontSize", defaultFloat = ViewDefaults.FONT_SIZE_SP)
  override fun setFontSize(view: BlurTextView, value: Float) {
    view.setFontSize(value)
  }

  @ReactProp(name = "fontFamily")
  override fun setFontFamily(view: BlurTextView?, family: String?) {
    view?.setFontFamily(family)
  }

  @ReactProp(name = "fontWeight")
  override fun setFontWeight(view: BlurTextView?, weight: String?) {
    view?.setFontWeight(weight)
  }

  @ReactProp(name = "fontStyle")
  override fun setFontStyle(view: BlurTextView?, style: String?) {
    view?.setFontStyle(style)
  }

  @ReactProp(name = "lineHeight")
  override fun setLineHeight(view: BlurTextView?, lineHeight: Float) {
    view?.setLineHeightReact(lineHeight)
  }

  override fun onAfterUpdateTransaction(view: BlurTextView) {
    super.onAfterUpdateTransaction(view)
    view.afterUpdateTransaction()
  }

  override fun setPadding(
    view: BlurTextView?,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int
  ) {
    super.setPadding(view, left, top, right, bottom)
    view?.setPadding(left, top, right, bottom)
  }


  override fun measure(
    context: Context,
    localData: ReadableMap?,
    props: ReadableMap?,
    state: ReadableMap?,
    width: Float,
    widthMode: YogaMeasureMode?,
    height: Float,
    heightMode: YogaMeasureMode?,
    attachmentsPositions: FloatArray?
  ): Long {
    val id = localData?.getInt("viewTag")
    return MeasurementStore.getMeasureById(context, id, width, props)
  }

  companion object {
    const val NAME = "BlurTextView"
  }
}
