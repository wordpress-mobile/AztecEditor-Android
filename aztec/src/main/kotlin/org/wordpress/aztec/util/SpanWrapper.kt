package org.wordpress.aztec.util

import android.text.Spannable
import org.wordpress.android.util.AppLog

class SpanWrapper<T>(var spannable: Spannable, var span: T) {

    private var frozenStart: Int = -1
    private var frozenEnd: Int = -1
    private var frozenFlags: Int = -1

    fun remove() {
        frozenStart = start
        frozenEnd = end
        frozenFlags = flags

        spannable.removeSpan(span)
    }

    fun reapply() {
        if (frozenFlags != -1 && frozenEnd != -1 && frozenStart != -1) {
            setSpanOrLogError(span, frozenStart, frozenEnd, frozenFlags)
        }
    }

    var start: Int
        get() { return spannable.getSpanStart(span) }
        set(start) { setSpanOrLogError(span, start, end, flags) }

    var end: Int
        get() { return spannable.getSpanEnd(span) }
        set(end) { setSpanOrLogError(span, start, end, flags) }

    var flags: Int
        get() { return spannable.getSpanFlags(span) }
        set(flags) { setSpanOrLogError(span, start, end, flags) }

    private fun setSpanOrLogError(span: T, start: Int, end: Int, flags: Int) {
        // Silently ignore invalid PARAGRAPH spans that don't start or end at paragraph boundary
        if (isInvalidParagraph(spannable, start, end, flags)) return

        spannable.setSpan(span, start, end, flags)
    }

    companion object {

        // Copied from SpannableStringBuilder
        private val START_MASK = 0xF0
        private val END_MASK = 0x0F
        private val START_SHIFT = 4
        private val PARAGRAPH = 3

        fun isInvalidParagraph(spannable: Spannable, start: Int, end: Int, flags: Int) : Boolean {
            // Copied from SpannableStringBuilder that throws an exception in this case.
            val flagsStart = flags and START_MASK shr START_SHIFT
            if (isInvalidParagraph(spannable, start, flagsStart)) {
                AppLog.w(AppLog.T.EDITOR, "PARAGRAPH span must start at paragraph boundary"
                        + " (" + start + " follows " + spannable.get(start - 1) + ")")
                return true
            }

            val flagsEnd = flags and END_MASK
            if (isInvalidParagraph(spannable, end, flagsEnd)) {
                AppLog.w(AppLog.T.EDITOR, "PARAGRAPH span must end at paragraph boundary"
                        + " (" + end + " follows " + spannable.get(end - 1) + ")")
                return true
            }
            return false
        }

        private fun isInvalidParagraph(spannable: Spannable, index: Int, flag: Int): Boolean {
            return flag == PARAGRAPH && index != 0 && index != spannable.length && spannable.get(index - 1) != '\n'
        }

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
