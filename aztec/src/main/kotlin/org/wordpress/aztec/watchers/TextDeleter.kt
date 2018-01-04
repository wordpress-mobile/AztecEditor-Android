package org.wordpress.aztec.watchers

import android.text.Editable
import android.text.Spannable
import android.text.Spanned
import android.text.TextWatcher
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.spans.MarkForDeletion
import java.lang.ref.WeakReference

class TextDeleter private constructor(aztecText: AztecText) : TextWatcher {

    private val aztecTextRef: WeakReference<AztecText?> = WeakReference(aztecText)

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(text: Editable) {
        if (aztecTextRef.get()?.isTextChangedListenerDisabled() ?: true) {
            return
        }

        text.getSpans(0, text.length, MarkForDeletion::class.java).forEach {
            val start = text.getSpanStart(it)
            val end = text.getSpanEnd(it)

            if (start > -1 && end > -1) {
                aztecTextRef.get()?.disableTextChangedListener()
                text.delete(start, end)
                aztecTextRef.get()?.enableTextChangedListener()
            }
        }
    }

    companion object {
        fun install(text: AztecText) {
            text.addTextChangedListener(TextDeleter(text))
        }

        fun mark(spannable: Spannable, start: Int, end: Int) {
            spannable.setSpan(MarkForDeletion(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        fun isMarkedForDeletion(spannable: Spannable, start: Int, end: Int): Boolean {
            return spannable.getSpans(start, end, MarkForDeletion::class.java).any()
        }
    }
}