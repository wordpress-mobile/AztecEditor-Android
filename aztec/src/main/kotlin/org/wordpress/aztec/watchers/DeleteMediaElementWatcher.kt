package org.wordpress.aztec.watchers

import android.text.Editable
import android.text.TextWatcher
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import org.wordpress.aztec.spans.AztecMediaSpan
import java.lang.ref.WeakReference

class DeleteMediaElementWatcher(aztecText: AztecText) : TextWatcher {

    private val aztecTextRef: WeakReference<AztecText?> = WeakReference(aztecText)

    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
        if (aztecTextRef.get()?.isTextChangedListenerDisabled() ?: true) {
            return
        }
        var deletedMedia = count > 0 && text[start + count - 1] == Constants.IMG_CHAR

        if (deletedMedia) {
            var mediaSpan = aztecTextRef.get()?.text?.getSpans(start, start + count, AztecMediaSpan::class.java)?.firstOrNull()
            mediaSpan?.onMediaDeleted()
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
            text.addTextChangedListener(DeleteMediaElementWatcher(text))
        }
    }
}