package org.wordpress.aztec.plugins.shortcodes

import android.text.Editable
import android.text.Spannable
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import org.wordpress.aztec.plugins.html2visual.IHtmlPreprocessor
import org.wordpress.aztec.plugins.html2visual.IHtmlTagHandler
import org.wordpress.aztec.plugins.shortcodes.handlers.CaptionHandler
import org.wordpress.aztec.plugins.shortcodes.spans.CaptionShortcodeSpan
import org.wordpress.aztec.plugins.shortcodes.watchers.CaptionWatcher
import org.wordpress.aztec.plugins.visual2html.IHtmlPostprocessor
import org.wordpress.aztec.util.SpanWrapper
import org.wordpress.aztec.util.getLast
import org.xml.sax.Attributes

class CaptionShortcodePlugin @JvmOverloads constructor(private val aztecText: AztecText? = null) :
        IHtmlTagHandler, IHtmlPreprocessor, IHtmlPostprocessor {

    companion object {
        val HTML_TAG = "wp-shortcode-caption-html-tag"
        val SHORTCODE_TAG = "caption"
    }

    init {
        aztecText?.let {
            CaptionWatcher(aztecText)
                    .add(CaptionHandler(aztecText))
                    .install(aztecText)
        }
    }

    override fun canHandleTag(tag: String): Boolean {
        return tag == HTML_TAG
    }

    override fun handleTag(opening: Boolean, tag: String, output: Editable, attributes: Attributes, nestingLevel: Int): Boolean {
        if (opening) {
            output.setSpan(CaptionShortcodeSpan(AztecAttributes(attributes), HTML_TAG, nestingLevel, aztecText), output.length, output.length, Spannable.SPAN_MARK_MARK)
        } else {
            val span = output.getLast<CaptionShortcodeSpan>()
            span?.let {
                val wrapper = SpanWrapper<CaptionShortcodeSpan>(output, span)
                if (wrapper.start == output.length) {
                    output.append(Constants.ZWJ_CHAR)
                } else {
                    // remove all newlines from captions
                    while (wrapper.start + 1 < output.length && output[wrapper.start + 1] == '\n') {
                        output.delete(wrapper.start + 1, wrapper.start + 2)
                    }
                    while (output.isNotEmpty() && output[output.length - 1] == '\n') {
                        output.delete(output.length - 1, output.length)
                    }
                    for (i in wrapper.start + 1 until output.length) {
                        if (output[i] == '\n') {
                            output.replace(i, i + 1, " ")
                        }
                    }
                }
                output.setSpan(span, wrapper.start, output.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        return true
    }

    override fun beforeHtmlProcessed(source: String): String {
        return StringBuilder(source)
                .replace(Regex("(?<!\\[)\\[$SHORTCODE_TAG([^\\]]*)\\]"), "<$HTML_TAG$1>")
                .replace(Regex("\\[/$SHORTCODE_TAG\\](?!\\])"), "</$HTML_TAG>")
    }

    override fun onHtmlProcessed(source: String): String {
        return StringBuilder(source)
                .replace(Regex("<$HTML_TAG([^>]*)>"), "[$SHORTCODE_TAG$1]")
                .replace(Regex("</$HTML_TAG>"), "[/$SHORTCODE_TAG]")
    }
}