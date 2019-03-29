package org.wordpress.aztec.watchers

import android.text.Editable
import android.text.TextWatcher
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.spans.AztecMediaSpan
import java.lang.ref.WeakReference

class DeleteMediaElementWatcherPreAPI25(aztecText: AztecText) : TextWatcher {
    private val aztecTextRef: WeakReference<AztecText?> = WeakReference(aztecText)

    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
        if (aztecTextRef.get()?.isTextChangedListenerDisabled() ?: true) {
            return
        }

        if (aztecTextRef.get()?.isMediaDeletedListenerDisabled() ?: true) {
            return
        }

        if (count > 0) {
            aztecTextRef.get()?.text?.getSpans(start, start + count, AztecMediaSpan::class.java)
                    ?.forEach {
                        it.onMediaDeleted()
                    }
        }
    }

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
        // no op
    }

    override fun afterTextChanged(text: Editable) {
        // no op
    }

    companion object {
        fun install(text: AztecText) {
            text.addTextChangedListener(DeleteMediaElementWatcherPreAPI25(text))
        }
    }
}