package org.wordpress.aztec.watchers

import android.text.Editable
import android.text.Spanned
import android.text.TextWatcher
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import org.wordpress.aztec.spans.AztecFullWidthImageSpan
import org.wordpress.aztec.spans.FullWidthImageProcessingMarker
import java.lang.ref.WeakReference

class FullWidthImageElementWatcher(aztecText: AztecText) : TextWatcher {

    private val aztecTextRef: WeakReference<AztecText?> = WeakReference(aztecText)

    private var deletedNewline: Boolean = false
    private var changeCount: Int = 0
    private var changeStart: Int = 0

    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
        deletedNewline = count > 0 && text[start + count - 1] == Constants.NEWLINE
    }

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
        changeCount = count
        changeStart = start
    }

    override fun afterTextChanged(text: Editable) {
        normalizeEditingAroundImageSpans(changeCount, changeStart)
    }

    private fun insertVisualNewline(position: Int) {
        aztecTextRef.get()?.text?.insert(position, Constants.NEWLINE_STRING)
    }

    private fun normalizeEditingAroundImageSpans(count: Int, start: Int) {
        val aztecText = aztecTextRef.get()
        if (aztecText != null && !aztecText.isTextChangedListenerDisabled() &&
                aztecText.text.getSpans(0, 0, FullWidthImageProcessingMarker::class.java).isEmpty()) {

            val end = start + count
            var lines = aztecText.text.getSpans(start, start, AztecFullWidthImageSpan::class.java)
            lines += aztecText.text.getSpans(end, end, AztecFullWidthImageSpan::class.java)

            lines.distinct().forEach {
                val changedLineBeginning = aztecText.text.getSpanStart(it) == end && end - 1 >= 0 &&
                        aztecText.text[end - 1] != Constants.NEWLINE
                val changedLineEnd = aztecText.text.getSpanEnd(it) == start && start < aztecText.length() &&
                        aztecText.text[start] != Constants.NEWLINE

                val marker = FullWidthImageProcessingMarker()
                aztecText.text.setSpan(marker, 0, 0, Spanned.SPAN_MARK_MARK)

                if (changedLineBeginning) {
                    // if characters added, insert a newline before the line
                    if (count > 0) {
                        insertVisualNewline(end)
                        aztecText.setSelection(end)
                    } else {
                        // if newline deleted, add it back and delete a character before it
                        if (deletedNewline) {
                            aztecText.text.delete(end - 1, end)
                            insertVisualNewline(end - 1)
                            aztecText.setSelection(end - 1)
                        } else {
                            // just add a newline
                            insertVisualNewline(end)
                            aztecText.setSelection(end)
                        }
                    }
                } else if (changedLineEnd) {
                    if (count > 0) {
                        // if text added right after a line, add a newline
                        insertVisualNewline(start)
                    } else {
                        // if text deleted, remove the line
                        aztecText.text.delete(aztecText.text.getSpanStart(it), start)
                    }
                }

                aztecText.text.removeSpan(marker)
            }
        }
    }

    companion object {
        fun install(text: AztecText) {
            text.addTextChangedListener(FullWidthImageElementWatcher(text))
        }
    }
}