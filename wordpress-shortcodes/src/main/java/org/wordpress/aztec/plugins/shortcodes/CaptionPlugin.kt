package org.wordpress.aztec.plugins.shortcodes

import android.text.Editable
import android.text.Spannable
import android.util.ArrayMap
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.plugins.html2visual.IHtmlTextHandler
import org.wordpress.aztec.plugins.shortcodes.spans.CaptionShortcodeSpan
import org.wordpress.aztec.util.SpanWrapper
import org.xml.sax.Parser
import org.xml.sax.helpers.AttributesImpl

class CaptionPlugin : IHtmlTextHandler {
    override val pattern = "(\\[caption.*\\]|.*\\[/caption\\])"

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

    fun getLastSpan(output: Editable): CaptionShortcodeSpan? {
        var span: CaptionShortcodeSpan? = null
        val spans = output.getSpans(0, output.length, CaptionShortcodeSpan::class.java)
        if (spans.isNotEmpty()) {
            span = spans.last()
        }
        return span
    }

    fun isStart(text: String): Boolean {
        return text.startsWith("[caption")
    }

    fun parseAttributes(text: String): Map<String, String> {
        val map = HashMap<String, String>()

        if (isStart(text)) {
            val attrString = text.substring("[caption ".length..text.length-1).trim()
            val pairs = attrString.split(" ")

            pairs.forEach {
                val splitPair = it.split("=")
                if (splitPair.size == 2) {
                    map.put(splitPair[0], splitPair[1])
                }
            }
        }
        return map
    }
}
