package org.wordpress.aztec.util

import android.text.Spannable

class SpanWrapper<T>(var spannable: Spannable, var span: T) {
    fun remove() {
        spannable.removeSpan(span)
    }

    var start: Int
        get() { return spannable.getSpanStart(span) }
        set(start) { spannable.setSpan(span, start, end, flags) }

    var end: Int
        get() { return spannable.getSpanEnd(span) }
        set(end) { spannable.setSpan(span, start, end, flags) }

    var flags: Int
        get() { return spannable.getSpanFlags(span) }
        set(flags) { spannable.setSpan(span, start, end, flags) }

    companion object {
        fun <T> getSpans(spannable: Spannable, start: Int, end: Int, type: Class<T>): List<SpanWrapper<T>> {
            return getSpans(spannable, spannable.getSpans(start, end, type))
        }

        fun <T> getSpans(spannable: Spannable, spanObjects: Array<T>): List<SpanWrapper<T>> {
            return spanObjects.map { it -> SpanWrapper(spannable, it) }
        }
    }
}
