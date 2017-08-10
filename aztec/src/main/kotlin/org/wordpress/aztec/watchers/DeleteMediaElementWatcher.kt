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

        var deletedMedia = count > 0  && containsMediaChars(text, start, count)

        if (deletedMedia) {
            var mediaSpanList = findAllMediaSpansWithinRange(start, count)
            mediaSpanList?.forEach {
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

    fun containsMediaChars(text: CharSequence, start: Int, count: Int): Boolean {
        for (i in 0..(count-1)) {
            if (text[start + i] == Constants.IMG_CHAR) {
                return true
            }
        }
        return false
    }

    fun findAllMediaSpansWithinRange(start: Int, count: Int): Array<AztecMediaSpan>? {
        return aztecTextRef.get()?.text?.getSpans(start, start + count, AztecMediaSpan::class.java)
    }

    companion object {
        fun install(text: AztecText) {
            text.addTextChangedListener(DeleteMediaElementWatcher(text))
        }
    }
}