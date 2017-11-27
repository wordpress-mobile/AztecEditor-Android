package org.wordpress.aztec.util

import android.text.Spannable

class SpanWrapper<T>(var spannable: Spannable, var span: T) {

    // Copied from SpannableStringBuilder
    private val START_MASK = 0xF0
    private val START_SHIFT = 4
    private val PARAGRAPH = 3

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
        set(flags) {
            // Do not set the span if it's a PARAGRAPH that doesn't start at paragraph boundary
            // Copied from SpannableStringBuilder that throws an exception in this case.
            val flagsStart = flags and START_MASK shr START_SHIFT
            if (!isInvalidParagraph(start, flagsStart)) {
                spannable.setSpan(span, start, end, flags)
            }
        }

    private fun isInvalidParagraph(index: Int, flag: Int): Boolean {
        return flag == PARAGRAPH && index != 0 && index != spannable.length && spannable.get(index - 1) != '\n'
    }

    companion object {
        inline fun <reified T : Any> getSpans(spannable: Spannable, start: Int, end: Int): List<SpanWrapper<T>> {
            return getSpans(spannable, spannable.getSpans(start, end, T::class.java))
        }

        fun <T> getSpans(spannable: Spannable, start: Int, end: Int, type: Class<T>): List<SpanWrapper<T>> {
            return getSpans(spannable, spannable.getSpans(start, end, type))
        }

        fun <T> getSpans(spannable: Spannable, spanObjects: Array<T>): List<SpanWrapper<T>> {
            return spanObjects.map { it -> SpanWrapper(spannable, it) }
        }
    }
}
