package org.wordpress.aztec.plugins.shortcodes

import android.text.Editable
import android.text.Spannable
import android.text.style.CharacterStyle
import org.wordpress.aztec.plugins.html2visual.IHtmlTextHandler
import org.wordpress.aztec.plugins.shortcodes.spans.CaptionShortcodeSpan
import org.wordpress.aztec.plugins.visual2html.IInlineSpanHandler
import org.wordpress.aztec.util.SpanWrapper

class CaptionPlugin : ShortcodePlugin("caption"), IInlineSpanHandler, IHtmlTextHandler {

    override val pattern = "(\\[$tagName.*\\]|.*\\[/$tagName\\])"

    override fun onHtmlTextMatch(text: String, output: Editable, nestingLevel: Int): Boolean {
        if (isStart(text)) {
            val attrs = parseAttributes(text)
            output.setSpan(CaptionShortcodeSpan(attrs), output.length, output.length, Spannable.SPAN_MARK_MARK)
        } else {
            val caption = text.substring(0..text.indexOf('[')-1)
            val span = getLastSpan(output)

            if (caption.isNotBlank()) {
                span?.let {
                    output.append(caption)

                    span.caption = caption
                    val wrapper = SpanWrapper<CaptionShortcodeSpan>(output, span)
                    output.setSpan(span, wrapper.start, output.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }
        return true
    }

    override fun canHandleSpan(span: CharacterStyle): Boolean {
        return span is CaptionShortcodeSpan
    }

    override fun handleSpanStart(html: StringBuilder, span: CharacterStyle) {
        val captionSpan = span as CaptionShortcodeSpan
        html.append("[$tagName ${joinAttributes(span.attrs)}]")
    }

    override fun handleSpanEnd(html: StringBuilder, span: CharacterStyle) {
        html.append("[/$tagName]")
    }
}
