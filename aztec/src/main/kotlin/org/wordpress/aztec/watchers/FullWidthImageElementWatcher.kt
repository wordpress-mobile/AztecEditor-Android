package org.wordpress.aztec.watchers

import android.text.Editable
import android.text.TextWatcher
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import org.wordpress.aztec.spans.AztecFullWidthImageSpan

class FullWidthImageElementWatcher(val aztecText: AztecText) : TextWatcher {

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
        aztecText.text.insert(position, Constants.NEWLINE_STRING)
    }

    private fun normalizeEditingAroundImageSpans(count: Int, start: Int) {
        if (!aztecText.isTextChangedListenerDisabled()) {
            val end = start + count
            val line = aztecText.text.getSpans(end, end, AztecFullWidthImageSpan::class.java).firstOrNull() ?:
                    aztecText.text.getSpans(start, start, AztecFullWidthImageSpan::class.java).firstOrNull()

            if (line != null) {
                val changedLineBeginning = aztecText.text.getSpanStart(line) == end && end - 1 >= 0 &&
                        aztecText.text[end - 1] != Constants.NEWLINE
                val changedLineEnd = aztecText.text.getSpanEnd(line) == start &&
                        aztecText.text[start] != Constants.NEWLINE

                aztecText.disableTextChangedListener()

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
                        aztecText.text.delete(aztecText.text.getSpanStart(line), start)
                    }
                }

                aztecText.enableTextChangedListener()
            }
        }
    }

    companion object {
        fun install(text: AztecText) {
            text.addTextChangedListener(FullWidthImageElementWatcher(text))
        }
    }
}