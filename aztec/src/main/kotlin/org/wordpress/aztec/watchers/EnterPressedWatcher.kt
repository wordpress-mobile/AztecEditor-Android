
package org.wordpress.aztec.watchers

import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import java.lang.ref.WeakReference

class EnterPressedWatcher(aztecText: AztecText) : TextWatcher {

    private val aztecTextRef: WeakReference<AztecText?> = WeakReference(aztecText)
    private var textBefore : SpannableStringBuilder? = null
    private var start: Int = -1

    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
        val aztecText = aztecTextRef.get()
        if (aztecText?.getAztecKeyListener() != null && !aztecText.isTextChangedListenerDisabled()) {
            // we need to make a copy to preserve the contents as they were before the change
            textBefore = SpannableStringBuilder(text)
            this.start = start
        }
    }

    override fun afterTextChanged(text: Editable) {
        val aztecText = aztecTextRef.get()
        val aztecKeyLister = aztecText?.getAztecKeyListener()
        if (aztecText != null && !aztecText.isTextChangedListenerDisabled() && aztecKeyLister != null) {
            val newTextCopy = SpannableStringBuilder(text)
            // if new text length is longer than original text by 1
            if (textBefore?.length == newTextCopy.length - 1) {
                // now check that the inserted character is actually a NEWLINE
                if (newTextCopy[this.start] == Constants.NEWLINE && aztecKeyLister.onEnterKey()) {
                    text.replace(start, start + 1, "")
                }
            }
        }
    }

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
    }

    companion object {
        fun install(editText: AztecText) {
            editText.addTextChangedListener(EnterPressedWatcher(editText))
        }
    }
}
