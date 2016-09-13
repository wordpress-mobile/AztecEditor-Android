package org.wordpress.aztec

import android.text.Editable
import android.text.Spanned
import org.wordpress.aztec.spans.AztecBlockSpan


data class TextChangedEvent(val text: CharSequence, val start: Int, val before: Int, val countOfCharacters: Int) {

    val inputStart = start
    val inputEnd = start + countOfCharacters
    val count = countOfCharacters

    val numberOfAddedCharacters = countOfCharacters - before
    val numberOfRemovedCharacters = before - countOfCharacters

    val isAddingCharacters = numberOfAddedCharacters > numberOfRemovedCharacters

    fun isAfterZeroWidthJoiner(): Boolean {
        if (start >= 1 && count > 0) {
            val previousCharacter = text[start - 1]
            return previousCharacter == '\u200B'
        }
        return false
    }

    fun isNewLine(): Boolean {
        if (!isAddingCharacters) return false

        if (start >= 1 && count == 1) {
            val currentCharacter = text[start]
            //special case for newline at the end of EditText
            if (text.length == start + 1 && currentCharacter == '\n') {
                return true
            } else if (text.length > start + 1 && currentCharacter == '\n') {
                return true
            }
        }

        return false
    }

    fun getBlockSpanToOpen(editableText: Editable): AztecBlockSpan? {
        if (count >= 0) {
            if (text.length > start) {
                val spans = editableText.getSpans(start, start, AztecBlockSpan::class.java)
                if (!spans.isEmpty()) {
                    val previousCharacter = if (isAddingCharacters) text[inputStart - 1] else text[inputEnd - 1]
                    if (previousCharacter == '\n') return null

                    val deletingLastCharacter = !isAddingCharacters && text.length == inputEnd
                    if (deletingLastCharacter) return null

                    if (!isAddingCharacters && text.length > inputEnd) {
                        val lastCharacter = text[inputEnd]
                        if (lastCharacter == '\n') return null
                    }


                    val flags = editableText.getSpanFlags(spans[0])
                    if ((flags and Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) == Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) {
                        return spans[0]
                    }
                } else {
                    val spansAfterInput = editableText.getSpans(inputEnd, inputEnd, AztecBlockSpan::class.java)
                    if (!spansAfterInput.isEmpty()) {
                        val flags = editableText.getSpanFlags(spansAfterInput[0])
                        if (((flags and Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) == Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) ||
                                (flags and Spanned.SPAN_EXCLUSIVE_INCLUSIVE) == Spanned.SPAN_EXCLUSIVE_INCLUSIVE) {
                            return spansAfterInput[0]
                        }
                    }
                }
            }
        }

        return null

    }

    fun getBlockSpanToClose(editableText: Editable): AztecBlockSpan? {
        if (start > 0 && count == 0) {
            if (text[start - 1] != '\n') return null

            val spans = editableText.getSpans(start, start, AztecBlockSpan::class.java)
            if (!spans.isEmpty()) {

                val spanStart = editableText.getSpanStart(spans[0])
                val spanEnd = editableText.getSpanEnd(spans[0])

                if (start == spanStart) {
                    return spans[0]
                } else if (start == spanEnd) {
                    val flags = editableText.getSpanFlags(spans[0])
                    if ((flags and Spanned.SPAN_EXCLUSIVE_INCLUSIVE) == Spanned.SPAN_EXCLUSIVE_INCLUSIVE) {
                        return spans[0]
                    }
                }
            }

        } else if (start == 0 && count == 0 && text.length > 0) {
            val spansAfterInput = editableText.getSpans(start + 1, start + 1, AztecBlockSpan::class.java)
            if (!spansAfterInput.isEmpty()) {
                return spansAfterInput[0]
            }
        }

        return null

    }


}

