package org.wordpress.aztec.util

import android.content.ClipData
import android.content.Context
import android.text.Editable
import android.text.Spannable
import android.text.Spanned
import org.wordpress.aztec.AztecParser

fun Editable.getLast(kind: Class<*>): Any? {
    val spans = this.getSpans(0, this.length, kind)

    if (spans.isEmpty()) {
        return null
    } else {
        return (spans.size downTo 1)
                .firstOrNull { this.getSpanFlags(spans[it - 1]) == Spannable.SPAN_MARK_MARK }
                ?.let { spans[it - 1] }
    }
}

inline fun <reified T> Editable.getLast(): T? {
    val spans = this.getSpans(0, this.length, T::class.java)

    if (spans.isEmpty()) {
        return null
    } else {
        return (spans.size downTo 1)
                .firstOrNull { this.getSpanFlags(spans[it - 1]) == Spannable.SPAN_MARK_MARK }
                ?.let { spans[it - 1] }
    }
}

fun ClipData.Item.coerceToStyledText(context: Context, parser: AztecParser): CharSequence {
    val text = text ?: ""
    if (text is Spanned) {
        return text
    }

    val html = htmlText ?: ""
    return parser.fromHtml(html, context)
}

fun ClipData.Item.coerceToHtmlText(parser: AztecParser): String {
    // If the item has an explicit HTML value, simply return that.
    val htmlText = htmlText
    if (htmlText != null) {
        return htmlText
    }

    // If this Item has a plain text value, return it as HTML.
    val text = text ?: ""
    if (text is Spanned) {
        return parser.toHtml(text)
    }

    return text.toString()
}