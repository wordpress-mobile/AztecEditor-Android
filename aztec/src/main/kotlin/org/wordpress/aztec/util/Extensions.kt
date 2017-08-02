package org.wordpress.aztec.util

import android.text.Editable
import android.text.Spannable

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