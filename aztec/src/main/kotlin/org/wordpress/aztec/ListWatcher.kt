package org.wordpress.aztec

import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher

import java.lang.ref.WeakReference

class ListWatcher private constructor(private val listHandler: ListHandler, aztecText: AztecText) : TextWatcher {

    private val aztecTextRef: WeakReference<AztecText?> = WeakReference(aztecText)

    private val textDeleter = object : ListHandler.TextDeleter {
        override fun delete(start: Int, end: Int) {
            aztecTextRef.get()?.post {
                aztecTextRef.get()?.text?.delete(start, end)
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (aztecTextRef.get()?.isTextChangedListenerDisabled() ?: true) {
            return
        }

        // handle the text change. The potential text deletion will happen in a scheduled Runnable, to run on next frame
        listHandler.handleTextChangeForLists(
                s as Spannable,
                start,
                count,
                textDeleter)

    }

    override fun afterTextChanged(text: Editable) {}

    companion object {
        fun install(text: AztecText) {
            text.addTextChangedListener(ListWatcher(ListHandler(text.text), text))
        }
    }
}