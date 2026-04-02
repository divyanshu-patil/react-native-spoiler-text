package com.blurtext

import android.graphics.Color
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.viewmanagers.BlurTextViewManagerInterface
import com.facebook.react.viewmanagers.BlurTextViewManagerDelegate

@ReactModule(name = BlurTextViewManager.NAME)
class BlurTextViewManager : SimpleViewManager<BlurTextView>(),
  BlurTextViewManagerInterface<BlurTextView> {
  private val mDelegate: ViewManagerDelegate<BlurTextView>

  init {
    mDelegate = BlurTextViewManagerDelegate(this)
  }

  override fun getDelegate(): ViewManagerDelegate<BlurTextView>? {
    return mDelegate
  }

  override fun getName(): String {
    return NAME
  }

  public override fun createViewInstance(context: ThemedReactContext): BlurTextView {
    return BlurTextView(context)
  }

  @ReactProp(name = "color")
  override fun setColor(view: BlurTextView?, color: Int?) {
    view?.setBackgroundColor(color ?: Color.TRANSPARENT)
  }

  companion object {
    const val NAME = "BlurTextView"
  }
}
