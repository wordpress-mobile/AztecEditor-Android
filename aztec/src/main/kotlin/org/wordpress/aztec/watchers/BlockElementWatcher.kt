package org.wordpress.aztec.watchers

import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.handlers.TextDeleter

import java.lang.ref.WeakReference

class BlockElementWatcher private constructor(private val textChangeHandler: TextChangeHandler, aztecText: AztecText) : TextWatcher {

    interface TextChangeHandler {
        fun handleTextChanged(text: Spannable, inputStart: Int, count: Int, textDeleter: TextDeleter)
    }

    private val aztecTextRef: WeakReference<AztecText?> = WeakReference(aztecText)

    private val textDeleter = object : TextDeleter {
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
        textChangeHandler.handleTextChanged(
                s as Spannable,
                start,
                count,
                textDeleter)

    }

    override fun afterTextChanged(text: Editable) {}

    companion object {
        fun install(text: AztecText, textChangeHandler: TextChangeHandler) {
            text.addTextChangedListener(BlockElementWatcher(textChangeHandler, text))
        }
    }
}