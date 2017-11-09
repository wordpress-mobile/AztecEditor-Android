package org.wordpress.aztec.plugins.shortcodes.extensions

import org.wordpress.aztec.AztecText
import org.wordpress.aztec.plugins.shortcodes.spans.CaptionShortcodeSpan
import org.wordpress.aztec.spans.AztecImageSpan
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
    this.textView?.text?.let {
        SpanWrapper<AztecImageSpan>(this.textView!!.text, this).let { span ->
            this.textView?.text?.getSpans(span.start, span.end, CaptionShortcodeSpan::class.java)?.first().let {
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
    this.textView?.text?.let {
        SpanWrapper<AztecImageSpan>(this.textView!!.text, this).let { span ->
            this.textView?.text?.getSpans(span.start, span.end, CaptionShortcodeSpan::class.java)?.first().let {
                it!!.caption = value
            }
        }
    }
}