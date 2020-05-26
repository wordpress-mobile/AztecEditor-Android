package org.wordpress.aztec.plugins

import android.text.SpannableStringBuilder
import org.wordpress.aztec.plugins.visual2html.ISpanPreprocessor
import org.wordpress.aztec.source.CssStyleFormatter
import org.wordpress.aztec.spans.AztecBackgroundColorSpan

class CssBackgroundColorPlugin : ISpanPreprocessor {

    override fun beforeSpansProcessed(spannable: SpannableStringBuilder) {
        spannable.getSpans(0, spannable.length, AztecBackgroundColorSpan::class.java).forEach {
            if (!CssStyleFormatter.containsStyleAttribute(it.attributes, CssStyleFormatter.CSS_BACKGROUND_COLOR_ATTRIBUTE)) {
                CssStyleFormatter.addStyleAttribute(it.attributes, CssStyleFormatter.CSS_BACKGROUND_COLOR_ATTRIBUTE, it.getColorHex())
            }
        }
    }
}
