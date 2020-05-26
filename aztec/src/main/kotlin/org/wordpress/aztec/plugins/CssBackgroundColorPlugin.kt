package org.wordpress.aztec.plugins

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import org.wordpress.aztec.plugins.html2visual.ISpanPostprocessor
import org.wordpress.aztec.plugins.visual2html.ISpanPreprocessor
import org.wordpress.aztec.source.CssStyleFormatter
import org.wordpress.aztec.spans.AztecBackgroundColorSpan
import org.wordpress.aztec.spans.HiddenHtmlSpan
import org.wordpress.aztec.spans.IAztecNestable
import org.wordpress.aztec.util.ColorConverter
import org.wordpress.aztec.util.SpanWrapper

class CssBackgroundColorPlugin: ISpanPostprocessor, ISpanPreprocessor {

    private val SPAN_TAG = "span"

    override fun beforeSpansProcessed(spannable: SpannableStringBuilder) {
        spannable.getSpans(0, spannable.length, AztecBackgroundColorSpan::class.java).forEach {
            if (!CssStyleFormatter.containsStyleAttribute(it.attributes, CssStyleFormatter.CSS_BACKGROUND_COLOR_ATTRIBUTE)) {
                CssStyleFormatter.addStyleAttribute(it.attributes, CssStyleFormatter.CSS_BACKGROUND_COLOR_ATTRIBUTE, it.bgColorHex)
            }

            val start = spannable.getSpanStart(it)
            val nesting = IAztecNestable.getNestingLevelAt(spannable, start) + 1

            val backgroundSpan = HiddenHtmlSpan(SPAN_TAG, it.attributes, nesting)

            spannable.setSpan(backgroundSpan, start, spannable.getSpanEnd(it), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.removeSpan(it)

            // if a parent of the new hidden span is also a <span> we can merge them
            IAztecNestable.getParent(spannable, SpanWrapper(spannable, backgroundSpan))?.let {
                if (it.span is HiddenHtmlSpan) {
                    val hiddenSpan = it.span as HiddenHtmlSpan
                    if (hiddenSpan.TAG == SPAN_TAG) {
                        val parentStyle = hiddenSpan.attributes.getValue(CssStyleFormatter.STYLE_ATTRIBUTE)
                        val childStyle = backgroundSpan.attributes.getValue(CssStyleFormatter.STYLE_ATTRIBUTE)
                        if (parentStyle != null) {
                            hiddenSpan.attributes.setValue(CssStyleFormatter.STYLE_ATTRIBUTE, CssStyleFormatter.mergeStyleAttributes(parentStyle, childStyle))
                        } else {
                            //adding background to existing span tag
                            hiddenSpan.attributes.setValue(CssStyleFormatter.STYLE_ATTRIBUTE, childStyle)
                        }

                        // remove the extra child span
                        spannable.removeSpan(backgroundSpan)
                    }
                }
            }
        }
    }

    override fun afterSpansProcessed(spannable: Spannable) {
        spannable.getSpans(0, spannable.length, HiddenHtmlSpan::class.java).forEach {
            if (it.TAG == SPAN_TAG && CssStyleFormatter.containsStyleAttribute(it.attributes, CssStyleFormatter.CSS_BACKGROUND_COLOR_ATTRIBUTE)) {
                val att = CssStyleFormatter.getStyleAttribute(it.attributes, CssStyleFormatter.CSS_BACKGROUND_COLOR_ATTRIBUTE)
                val color = ColorConverter.getColorInt(att)
                CssStyleFormatter.removeStyleAttribute(it.attributes, CssStyleFormatter.CSS_BACKGROUND_COLOR_ATTRIBUTE)
                spannable.setSpan(BackgroundColorSpan(color), spannable.getSpanStart(it), spannable.getSpanEnd(it), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                if (it.attributes.isEmpty()) {
                    spannable.removeSpan(it)
                }
            }
        }
    }
}
