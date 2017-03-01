package org.wordpress.aztec

import android.text.Spannable

import java.lang.ref.WeakReference

class SpanWrapper<out T>(spannable: Spannable?, span: T) {
    private val mSpannable: WeakReference<Spannable?> = WeakReference(spannable)
    private val mSpan: WeakReference<T> = WeakReference(span)

    val span: T
        get() = mSpan.get()

    fun remove() {
        mSpannable.get()?.removeSpan(span)
    }

    var start: Int
        get() { return mSpannable.get()?.getSpanStart(mSpan.get()) ?: -1 }
        set(start) { mSpannable.get()?.setSpan(mSpan.get(), start, end, flags) }

    var end: Int
        get() { return mSpannable.get()?.getSpanEnd(mSpan.get()) ?: -1 }
        set(end) { mSpannable.get()?.setSpan(mSpan.get(), start, end, flags) }

    var flags: Int
        get() { return mSpannable.get()?.getSpanFlags(mSpan.get()) ?: -1 }
        set(flags) { mSpannable.get()?.setSpan(mSpan.get(), start, end, flags) }

    companion object {
        fun <T> getSpans(spannable: Spannable, start: Int, end: Int, type: Class<T>): List<SpanWrapper<T>> {
            return getSpans(spannable, spannable.getSpans(start, end, type))
        }

        fun <T> getSpans(spannable: Spannable, spanObjects: Array<T>): List<SpanWrapper<T>> {
            return spanObjects.map { it -> SpanWrapper(spannable, it) }
        }
    }
}
