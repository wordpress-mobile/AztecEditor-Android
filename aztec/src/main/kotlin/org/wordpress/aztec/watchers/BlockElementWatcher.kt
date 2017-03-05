package org.wordpress.aztec.watchers

import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import org.wordpress.aztec.AztecText
import java.lang.ref.WeakReference

class BlockElementWatcher private constructor(private val textChangeHandler: TextChangeHandler, aztecText: AztecText) : TextWatcher {

    interface TextChangeHandler {
        fun handleTextChanged(text: Spannable, inputStart: Int, count: Int)
    }

    private val aztecTextRef: WeakReference<AztecText?> = WeakReference(aztecText)

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (aztecTextRef.get()?.isTextChangedListenerDisabled() ?: true) {
            return
        }

        // handle the text change. The potential text deletion will happen in a scheduled Runnable, to run on next frame
        textChangeHandler.handleTextChanged(
                s as Spannable,
                start,
                count)

    }

    override fun afterTextChanged(text: Editable) {}

    companion object {
        fun install(text: AztecText, textChangeHandler: TextChangeHandler) {
            text.addTextChangedListener(BlockElementWatcher(textChangeHandler, text))
        }
    }
}