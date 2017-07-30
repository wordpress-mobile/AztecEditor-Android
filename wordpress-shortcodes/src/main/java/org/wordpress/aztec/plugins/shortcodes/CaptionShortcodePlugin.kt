package org.wordpress.aztec.plugins.shortcodes

import android.text.Editable
import android.text.Spannable
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.plugins.html2visual.IHtmlPreprocessor
import org.wordpress.aztec.plugins.html2visual.IHtmlTagHandler
import org.wordpress.aztec.plugins.shortcodes.spans.CaptionShortcodeSpan
import org.wordpress.aztec.plugins.visual2html.IHtmlPostprocessor
import org.wordpress.aztec.util.SpanWrapper
import org.wordpress.aztec.util.getLast
import org.xml.sax.Attributes

class CaptionShortcodePlugin : IHtmlTagHandler, IHtmlPreprocessor, IHtmlPostprocessor {

    // "captio" is used as tag name on purpose because "caption" gets eaten up by the Jsoup parser.
    private val TAG = "captio"

    override fun canHandleTag(tag: String): Boolean {
        return tag == TAG
    }

    override fun handleTag(opening: Boolean, tag: String, output: Editable, attributes: Attributes, nestingLevel: Int): Boolean {
        if (opening) {
            output.setSpan(CaptionShortcodeSpan(AztecAttributes(attributes), TAG, nestingLevel), output.length, output.length, Spannable.SPAN_MARK_MARK)
        } else {
            val span = output.getLast<CaptionShortcodeSpan>()
            span?.let {
                val wrapper = SpanWrapper<CaptionShortcodeSpan>(output, span)
                output.setSpan(span, wrapper.start, output.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        return true
    }

    override fun processHtmlBeforeParsing(source: String): String {
        return StringBuilder(source)
                .replace(Regex("(?<!\\[)\\[${TAG}n([^\\]]*)\\]"), "<$TAG$1>")
                .replace(Regex("\\[/${TAG}n\\](?!\\])"), "</$TAG>")
    }

    override fun processHtmlAfterSerialization(source: String): String {
        return StringBuilder(source)
                .replace(Regex("<$TAG([^>]*)>"), "[${TAG}n$1]")
                .replace(Regex("</$TAG>"), "[/${TAG}n]")
    }
}
