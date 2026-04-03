package com.blurtext

import android.text.SpannableStringBuilder
import android.text.Spanned

data class TextFragment(
  val text: String,
  val blurRadius: Float? = null
)

object BlurTextBuilder {

  fun build(fragments: List<TextFragment>): SpannableStringBuilder {
    val spannable = SpannableStringBuilder()

    fragments.forEach { fragment ->
      val start = spannable.length

      spannable.append(fragment.text)

      val end = spannable.length

      fragment.blurRadius?.let { radius ->
        spannable.setSpan(
          BlurReplacementSpan(radius),
          start,
          end,
          Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
      }
    }

    return spannable
  }
}
