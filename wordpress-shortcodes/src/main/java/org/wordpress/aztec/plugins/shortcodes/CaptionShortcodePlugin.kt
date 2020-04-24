package org.wordpress.aztec.plugins.shortcodes

import android.text.Editable
import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import org.wordpress.aztec.plugins.html2visual.IHtmlPreprocessor
import org.wordpress.aztec.plugins.html2visual.IHtmlTagHandler
import org.wordpress.aztec.plugins.shortcodes.handlers.CaptionHandler
import org.wordpress.aztec.plugins.shortcodes.spans.CaptionShortcodeSpan
import org.wordpress.aztec.plugins.shortcodes.spans.createCaptionShortcodeSpan
import org.wordpress.aztec.plugins.shortcodes.watchers.CaptionWatcher
import org.wordpress.aztec.plugins.visual2html.IHtmlPostprocessor
import org.wordpress.aztec.plugins.visual2html.ISpanPreprocessor
import org.wordpress.aztec.source.CssStyleFormatter
import org.wordpress.aztec.spans.IAztecAlignmentSpan
import org.wordpress.aztec.util.SpanWrapper
import org.wordpress.aztec.util.getLast
import org.xml.sax.Attributes

class CaptionShortcodePlugin @JvmOverloads constructor(private val aztecText: AztecText? = null) :
        IHtmlTagHandler, IHtmlPreprocessor, IHtmlPostprocessor, ISpanPreprocessor {

    companion object {
        val HTML_TAG = "wp-shortcode-caption-html-tag"
        val SHORTCODE_TAG = "caption"
        val ALIGN_ATTRIBUTE = "align"
        val ALIGN_LEFT_ATTRIBUTE_VALUE = "alignleft"
        val ALIGN_RIGHT_ATTRIBUTE_VALUE = "alignright"
        val ALIGN_CENTER_ATTRIBUTE_VALUE = "aligncenter"
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
            val captionShortcodeSpan = createCaptionShortcodeSpan(
                    AztecAttributes(attributes),
                    HTML_TAG,
                    nestingLevel,
                    aztecText)
            output.setSpan(captionShortcodeSpan, output.length, output.length, Spannable.SPAN_MARK_MARK)
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

                if (span is IAztecAlignmentSpan && span.attributes.hasAttribute(ALIGN_ATTRIBUTE)) {
                    when (span.attributes.getValue(ALIGN_ATTRIBUTE)) {
                        ALIGN_RIGHT_ATTRIBUTE_VALUE -> span.align = Layout.Alignment.ALIGN_OPPOSITE
                        ALIGN_CENTER_ATTRIBUTE_VALUE -> span.align = Layout.Alignment.ALIGN_CENTER
                        ALIGN_LEFT_ATTRIBUTE_VALUE -> span.align = Layout.Alignment.ALIGN_NORMAL
                    }
                }

                span.attributes.removeAttribute(CssStyleFormatter.STYLE_ATTRIBUTE)

                if (wrapper.end - wrapper.start == 1) {
                    wrapper.remove()
                }
            }
        }
        return true
    }

    override fun beforeSpansProcessed(spannable: SpannableStringBuilder) {
        spannable.getSpans(0, spannable.length, CaptionShortcodeSpan::class.java).forEach {
            it.attributes.removeAttribute(CaptionShortcodePlugin.ALIGN_ATTRIBUTE)
            if (it is IAztecAlignmentSpan && it.align != null) {
                it.attributes.setValue(CaptionShortcodePlugin.ALIGN_ATTRIBUTE,
                        when (it.align) {
                            Layout.Alignment.ALIGN_NORMAL -> CaptionShortcodePlugin.ALIGN_LEFT_ATTRIBUTE_VALUE
                            Layout.Alignment.ALIGN_CENTER -> CaptionShortcodePlugin.ALIGN_CENTER_ATTRIBUTE_VALUE
                            else -> CaptionShortcodePlugin.ALIGN_RIGHT_ATTRIBUTE_VALUE
                        })
            }
        }
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