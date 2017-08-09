package org.wordpress.aztec.watchers

import android.text.Editable
import android.text.TextWatcher
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import org.wordpress.aztec.spans.AztecMediaSpan
import java.lang.ref.WeakReference

class DeleteMediaElementWatcher(aztecText: AztecText) : TextWatcher {

    private val aztecTextRef: WeakReference<AztecText?> = WeakReference(aztecText)

    private var deletedMedia: Boolean = false
    private var mediaSpan: AztecMediaSpan? = null

    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
        deletedMedia = count > 0 && text[start + count - 1] == Constants.IMG_CHAR

        if (deletedMedia) {
            val aztecText = aztecTextRef.get()
            mediaSpan = aztecText?.text?.getSpans(start, start + count, AztecMediaSpan::class.java)?.firstOrNull()
        }
    }

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
        // no op
    }

    override fun afterTextChanged(text: Editable) {
        if (aztecTextRef.get()?.isTextChangedListenerDisabled() ?: true) {
            return
        }

        if (deletedMedia) {
            deletedMedia = false
            mediaSpan?.onMediaDeleted()
        }
    }

    companion object {
        fun install(text: AztecText) {
            text.addTextChangedListener(DeleteMediaElementWatcher(text))
        }
    }
}