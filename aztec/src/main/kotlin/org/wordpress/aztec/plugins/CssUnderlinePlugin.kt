package org.wordpress.aztec.plugins

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import org.wordpress.aztec.plugins.html2visual.ISpanPostprocessor
import org.wordpress.aztec.plugins.visual2html.ISpanPreprocessor
import org.wordpress.aztec.source.CssStyleFormatter
import org.wordpress.aztec.spans.AztecUnderlineSpan
import org.wordpress.aztec.spans.HiddenHtmlSpan
import org.wordpress.aztec.spans.IAztecNestable
import org.wordpress.aztec.util.SpanWrapper

class CssUnderlinePlugin : ISpanPostprocessor, ISpanPreprocessor {

    private val SPAN_TAG = "span"
    private val UNDERLINE_STYLE_VALUE = "underline"

    init {
        AztecUnderlineSpan.isCssStyleByDefault = true
    }

    override fun beforeSpansProcessed(spannable: SpannableStringBuilder) {
        spannable.getSpans(0, spannable.length, AztecUnderlineSpan::class.java).filter { it.isCssStyle }.forEach {
            if (!CssStyleFormatter.containsStyleAttribute(it.attributes, CssStyleFormatter.CSS_TEXT_DECORATION_ATTRIBUTE)) {
                CssStyleFormatter.addStyleAttribute(it.attributes, CssStyleFormatter.CSS_TEXT_DECORATION_ATTRIBUTE, UNDERLINE_STYLE_VALUE)
            }

            val start = spannable.getSpanStart(it)
            val nesting = IAztecNestable.getNestingLevelAt(spannable, start) + 1

            val calypsoUnderlineSpan = HiddenHtmlSpan(SPAN_TAG, it.attributes, nesting)

            spannable.setSpan(calypsoUnderlineSpan, start, spannable.getSpanEnd(it), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.removeSpan(it)

            // if a parent of the new hidden span is also a <span> we can merge them
            IAztecNestable.getParent(spannable, SpanWrapper(spannable, calypsoUnderlineSpan))?.let {
                if (it.span is HiddenHtmlSpan) {
                    val hiddenSpan = it.span as HiddenHtmlSpan
                    if (hiddenSpan.TAG == SPAN_TAG) {
                        val parentStyle = hiddenSpan.attributes.getValue(CssStyleFormatter.STYLE_ATTRIBUTE)
                        val childStyle = calypsoUnderlineSpan.attributes.getValue(CssStyleFormatter.STYLE_ATTRIBUTE)
                        if (parentStyle != null && childStyle != null) {
                            hiddenSpan.attributes.setValue(CssStyleFormatter.STYLE_ATTRIBUTE, CssStyleFormatter.mergeStyleAttributes(parentStyle, childStyle))
                        }

                        // remove the extra child span
                        spannable.removeSpan(calypsoUnderlineSpan)
                    }
                }
            }
        }
    }

    override fun afterSpansProcessed(spannable: Spannable) {
        spannable.getSpans(0, spannable.length, HiddenHtmlSpan::class.java).forEach {
            if (it.TAG == SPAN_TAG && CssStyleFormatter.containsStyleAttribute(it.attributes, CssStyleFormatter.CSS_TEXT_DECORATION_ATTRIBUTE)) {
                CssStyleFormatter.removeStyleAttribute(it.attributes, CssStyleFormatter.CSS_TEXT_DECORATION_ATTRIBUTE)
                spannable.setSpan(AztecUnderlineSpan(), spannable.getSpanStart(it), spannable.getSpanEnd(it), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                if (it.attributes.isEmpty()) {
                    spannable.removeSpan(it)
                }
            }
        }
    }
}
