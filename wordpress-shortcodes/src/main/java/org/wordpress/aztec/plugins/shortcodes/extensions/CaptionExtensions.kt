package org.wordpress.aztec.plugins.shortcodes.extensions

import android.text.Spanned
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.plugins.shortcodes.CaptionShortcodePlugin
import org.wordpress.aztec.plugins.shortcodes.spans.CaptionShortcodeSpan
import org.wordpress.aztec.spans.AztecImageSpan
import org.wordpress.aztec.spans.IAztecNestable
import org.wordpress.aztec.util.SpanWrapper

fun AztecText.getImageCaption(attributePredicate: AztecText.AttributePredicate): String {
    return this.text
            .getSpans(0, this.text.length, AztecImageSpan::class.java)
            .first {
                attributePredicate.matches(it.attributes)
            }
            .getCaption()
}

fun AztecImageSpan.getCaption(): String {
    textView?.text?.let {
        SpanWrapper<AztecImageSpan>(textView!!.text, this).let { span ->
            textView?.text?.getSpans(span.start, span.end, CaptionShortcodeSpan::class.java)?.first().let {
                return it!!.caption
            }
        }
    }
    return ""
}

fun AztecText.setImageCaption(attributePredicate: AztecText.AttributePredicate, value: String) {
    this.text.getSpans(0, this.text.length, AztecImageSpan::class.java)
        .first {
            attributePredicate.matches(it.attributes)
        }
        .setCaption(value)
}

fun AztecImageSpan.setCaption(value: String) {
    textView?.text?.let {
        SpanWrapper<AztecImageSpan>(textView!!.text, this).let { span ->
            var captionSpan = textView?.text?.getSpans(span.start, span.end, CaptionShortcodeSpan::class.java)?.firstOrNull()
            if (captionSpan == null) {
                captionSpan = CaptionShortcodeSpan(AztecAttributes(), CaptionShortcodePlugin.HTML_TAG, IAztecNestable.getNestingLevelAt(textView!!.text, span.start), textView!!.text)
                textView!!.text.setSpan(captionSpan, span.start, span.end, Spanned.SPAN_MARK_MARK)
            }
            captionSpan.caption = value
        }
    }
}