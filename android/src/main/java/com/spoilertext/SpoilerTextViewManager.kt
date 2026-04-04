package com.spoilertext

import android.content.Context
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.*
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.viewmanagers.SpoilerTextViewManagerDelegate
import com.facebook.react.viewmanagers.SpoilerTextViewManagerInterface
import com.facebook.yoga.YogaMeasureMode

@ReactModule(name = SpoilerTextViewManager.NAME)
class SpoilerTextViewManager :
  SimpleViewManager<SpoilerTextView>(),
  SpoilerTextViewManagerInterface<SpoilerTextView> {

  private val mDelegate: ViewManagerDelegate<SpoilerTextView> =
    SpoilerTextViewManagerDelegate(this)

  override fun getDelegate(): ViewManagerDelegate<SpoilerTextView>? = mDelegate

  override fun getName(): String = NAME

  override fun createViewInstance(context: ThemedReactContext): SpoilerTextView {
    val view = SpoilerTextView(context)
    view.setOnClickListener {
      val event = com.facebook.react.bridge.Arguments.createMap()
      val reactContext = view.context as ThemedReactContext
      reactContext
        .getJSModule(com.facebook.react.uimanager.events.RCTEventEmitter::class.java)
        .receiveEvent(view.id, "onPress", event)
    }
    return view
  }

  override fun updateState(
    view: SpoilerTextView,
    props: ReactStylesDiffMap?,
    stateWrapper: StateWrapper?
  ): Any? {
    view.stateWrapper = stateWrapper
    return super.updateState(view, props, stateWrapper)
  }

  override fun setText(view: SpoilerTextView?, value: String?) {
    view?.setValue(value)
  }

  @ReactProp(name = "color")
  override fun setColor(view: SpoilerTextView, value: Int?) {
    if (value != null) view.setColor(value)
  }

  @ReactProp(name = "spoiler")
  override fun setSpoiler(view: SpoilerTextView, value: Boolean) {
    view.setSpoiler(value)
  }

  override fun setBlurRadius(view: SpoilerTextView, value: Float) {
    view?.setBlurRadius(value)
  }


  @ReactProp(name = "fontSize", defaultFloat = ViewDefaults.FONT_SIZE_SP)
  override fun setFontSize(view: SpoilerTextView, value: Float) {
    view.setFontSize(value)
  }

  @ReactProp(name = "fontFamily")
  override fun setFontFamily(view: SpoilerTextView?, family: String?) {
    view?.setFontFamily(family)
  }

  @ReactProp(name = "fontWeight")
  override fun setFontWeight(view: SpoilerTextView?, weight: String?) {
    view?.setFontWeight(weight)
  }

  @ReactProp(name = "fontStyle")
  override fun setFontStyle(view: SpoilerTextView?, style: String?) {
    view?.setFontStyle(style)
  }

  @ReactProp(name = "lineHeight")
  override fun setLineHeight(view: SpoilerTextView?, lineHeight: Float) {
    view?.setLineHeightReact(lineHeight)
  }

  override fun getExportedCustomDirectEventTypeConstants(): MutableMap<String, Any> {
    return mutableMapOf(
      "onPress" to mapOf("registrationName" to "onPress")
    )
  }

  override fun onAfterUpdateTransaction(view: SpoilerTextView) {
    super.onAfterUpdateTransaction(view)
    view.afterUpdateTransaction()
  }

  override fun setPadding(
    view: SpoilerTextView?,
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
    const val NAME = "SpoilerTextView"
  }
}
