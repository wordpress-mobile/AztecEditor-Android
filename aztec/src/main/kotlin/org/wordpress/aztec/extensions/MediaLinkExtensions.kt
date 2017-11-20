package org.wordpress.aztec.extensions

import android.text.Spanned
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.spans.AztecMediaSpan
import org.wordpress.aztec.spans.AztecURLSpan

fun AztecText.getMediaLink(attributePredicate: AztecText.AttributePredicate): String {
    text.getSpans(0, text.length, AztecMediaSpan::class.java)
            .firstOrNull { attributePredicate.matches(it.attributes) }
            ?.let {
                val start = text.getSpanStart(it)
                val end = text.getSpanEnd(it)

                editableText.getSpans(start, end, AztecURLSpan::class.java).firstOrNull()?.let { return it.url }
            }

    return ""
}

fun AztecText.getMediaLinkAttributes(attributePredicate: AztecText.AttributePredicate): AztecAttributes {
    text.getSpans(0, text.length, AztecMediaSpan::class.java)
            .firstOrNull { attributePredicate.matches(it.attributes) }
            ?.let {
                val start = text.getSpanStart(it)
                val end = text.getSpanEnd(it)

                text.getSpans(start, end, AztecURLSpan::class.java).firstOrNull()?.let { return it.attributes }
            }

    return AztecAttributes()
}

fun AztecText.removeLinkFromMedia(attributePredicate: AztecText.AttributePredicate) {
    text.getSpans(0, text.length, AztecMediaSpan::class.java)
            .filter { attributePredicate.matches(it.attributes) }
            .forEach {
                val start = text.getSpanStart(it)
                val end = text.getSpanEnd(it)

                linkFormatter.removeLink(start, end)
            }
}

fun AztecText.addLinkToMedia(attributePredicate: AztecText.AttributePredicate, link: String, linkAttributes: AztecAttributes = AztecAttributes()) {
    text.getSpans(0, text.length, AztecMediaSpan::class.java)
            .filter { attributePredicate.matches(it.attributes) }
            .forEach {
                val start = text.getSpanStart(it)
                val end = text.getSpanEnd(it)

                removeLinkFromMedia(attributePredicate)
                text.setSpan(AztecURLSpan(link, linkAttributes), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
}
