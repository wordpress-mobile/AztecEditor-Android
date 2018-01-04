package org.wordpress.aztec.watchers

import android.text.Editable
import android.text.Spanned
import android.text.TextWatcher
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import org.wordpress.aztec.spans.FullWidthImageProcessingMarker
import org.wordpress.aztec.spans.IAztecFullWidthImageSpan
import org.wordpress.aztec.util.SpanWrapper
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
            var lines = aztecText.text.getSpans(start, end, IAztecFullWidthImageSpan::class.java)

            // necessary as spans starting at the `start` and ending at the `end` are not included in the list above
            lines += aztecText.text.getSpans(start, start, IAztecFullWidthImageSpan::class.java)
            lines += aztecText.text.getSpans(end, end, IAztecFullWidthImageSpan::class.java)

            lines.distinct().forEach {
                val wrapper = SpanWrapper<IAztecFullWidthImageSpan>(aztecText.text, it)

                // do not process images that were removed
                if (wrapper.start == -1) {
                    return@forEach
                }

                val mustFixSpanStart = wrapper.start > 0 && aztecText.text[wrapper.start - 1] != Constants.NEWLINE
                val mustFixSpanEnd = wrapper.end < aztecText.length() && aztecText.text[wrapper.end] != Constants.NEWLINE

                val marker = FullWidthImageProcessingMarker()
                aztecText.text.setSpan(marker, 0, 0, Spanned.SPAN_MARK_MARK)

                if (mustFixSpanStart) {
                    // if characters added, insert a newline before the line
                    val spanStart = wrapper.start
                    if (count > 0) {
                        insertVisualNewline(spanStart)
                        aztecText.setSelection(spanStart)
                    } else {
                        // if newline deleted, add it back and delete a character before it
                        if (deletedNewline) {
                            aztecText.text.delete(spanStart - 1, spanStart)
                            if (spanStart > 1 && aztecText.text[spanStart - 2] != Constants.NEWLINE) {
                                insertVisualNewline(spanStart - 1)
                            }
                            aztecText.setSelection(spanStart - 1)
                        } else {
                            // just add a newline
                            insertVisualNewline(spanStart)
                            aztecText.setSelection(spanStart)
                        }
                    }
                }

                if (mustFixSpanEnd) {
                    if (count > 0) {
                        // if text added right after a line, add a newline
                        insertVisualNewline(wrapper.end)
                    } else {
                        // if text deleted, remove the line
                        aztecText.text.delete(wrapper.start, wrapper.end)
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
