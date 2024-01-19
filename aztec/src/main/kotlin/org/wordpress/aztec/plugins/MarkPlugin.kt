package org.wordpress.aztec.plugins

import android.text.SpannableStringBuilder
import org.wordpress.aztec.plugins.visual2html.ISpanPreprocessor
import org.wordpress.aztec.source.CssStyleFormatter
import org.wordpress.aztec.spans.MarkSpan

class MarkPlugin : ISpanPreprocessor {

    override fun beforeSpansProcessed(spannable: SpannableStringBuilder) {
        spannable.getSpans(0, spannable.length, MarkSpan::class.java).forEach {
            if (!CssStyleFormatter.containsStyleAttribute(it.attributes, CssStyleFormatter.CSS_BACKGROUND_COLOR_ATTRIBUTE)) {
                CssStyleFormatter.addStyleAttribute(it.attributes, CssStyleFormatter.CSS_BACKGROUND_COLOR_ATTRIBUTE, "rgba(0, 0, 0, 0)")
            }

            if (!CssStyleFormatter.containsStyleAttribute(it.attributes, CssStyleFormatter.CSS_COLOR_ATTRIBUTE)) {
                CssStyleFormatter.addStyleAttribute(it.attributes, CssStyleFormatter.CSS_COLOR_ATTRIBUTE, it.getTextColor())
            }

            if (!it.attributes.hasAttribute("class")) {
                it.attributes.setValue("class", "has-inline-color")
            }
        }
    }
}
