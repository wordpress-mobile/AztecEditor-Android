package org.wordpress.aztec.watchers

import android.text.Editable
import android.text.Spannable
import android.text.Spanned
import android.text.TextWatcher
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.spans.AztecNestable
import java.lang.ref.WeakReference
import java.util.*

class BlockElementWatcher(aztecText: AztecText) : TextWatcher {
    val handlers = ArrayList<TextChangeHandler>()

    interface TextChangeHandler {
        fun handleTextChanged(text: Spannable, inputStart: Int, count: Int, nestingLevel: Int)
    }

    private val aztecTextRef: WeakReference<AztecText?> = WeakReference(aztecText)

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (aztecTextRef.get()?.isTextChangedListenerDisabled() ?: true) {
            return
        }

        val nestingLevelAtEditPoint = AztecNestable.getNestingLevelAt(s as Spanned, start, start + count)

        // handle the text change. The potential text deletion will happen in a scheduled Runnable, to run on next frame
        handlers.forEach { textChangeHandler ->
            textChangeHandler.handleTextChanged(
                    s as Spannable,
                    start,
                    count,
                    nestingLevelAtEditPoint)
        }
    }

    override fun afterTextChanged(text: Editable) {}

    fun add(textChangeHandler: TextChangeHandler) : BlockElementWatcher {
        handlers.add(textChangeHandler)
        return this
    }

    fun install(text: AztecText) : BlockElementWatcher {
        text.addTextChangedListener(this)
        return this
    }
}