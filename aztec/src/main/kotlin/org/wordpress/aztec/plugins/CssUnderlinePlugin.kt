package org.wordpress.aztec.plugins

import android.text.SpannableStringBuilder
import android.text.Spanned
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.plugins.html2visual.ISpanPostprocessor
import org.wordpress.aztec.plugins.visual2html.ISpanPreprocessor
import org.wordpress.aztec.source.InlineCssStyleFormatter
import org.wordpress.aztec.spans.AztecUnderlineSpan
import org.wordpress.aztec.spans.HiddenHtmlSpan

class CssUnderlinePlugin : ISpanPostprocessor, ISpanPreprocessor {

    private val SPAN_TAG = "span"
    private val UNDERLINE_STYLE_VALUE = "underline"

    override fun beforeSpansProcessed(spannable: SpannableStringBuilder) {
        spannable.getSpans(0, spannable.length, AztecUnderlineSpan::class.java).forEach {
            InlineCssStyleFormatter.addStyleAttribute(it.attributes, InlineCssStyleFormatter.CSS_TEXT_DECORATION_ATTRIBUTE, UNDERLINE_STYLE_VALUE)

            val calypsoUnderlineSpan = HiddenHtmlSpan(SPAN_TAG, it.attributes, 0)
            spannable.setSpan(calypsoUnderlineSpan, spannable.getSpanStart(it), spannable.getSpanEnd(it), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.removeSpan(it)
        }
    }

    override fun afterSpansProcessed(spannable: SpannableStringBuilder) {
        spannable.getSpans(0, spannable.length, HiddenHtmlSpan::class.java).forEach {
            if (it.TAG == SPAN_TAG && InlineCssStyleFormatter.containsStyleAttribute(it.attributes, InlineCssStyleFormatter.CSS_TEXT_DECORATION_ATTRIBUTE)) {
                InlineCssStyleFormatter.removeStyleAttribute(it.attributes, InlineCssStyleFormatter.CSS_TEXT_DECORATION_ATTRIBUTE)
                spannable.setSpan(AztecUnderlineSpan(), spannable.getSpanStart(it), spannable.getSpanEnd(it), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                if (it.attributes.isEmpty()) {
                    spannable.removeSpan(it)
                }
            }
        }
    }
}