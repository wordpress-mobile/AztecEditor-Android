package org.wordpress.aztec

import android.text.Editable
import android.text.Spanned
import org.wordpress.aztec.spans.AztecListSpan


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


    //TODO: make this to also work with bullet span in the future
    fun getListSpanToOpen(editableText: Editable): AztecListSpan? {
        if (start >= 1 && count >= 0) {
            if (text.length > start) {
                val previousCharacter = if (isAddingCharacters) text[inputStart - 1] else text[inputEnd - 1]
                if (previousCharacter == '\n') return null

                val deletingLastCharacter = !isAddingCharacters && text.length == inputEnd
                if (deletingLastCharacter) return null

                if (!isAddingCharacters && text.length > inputEnd) {
                    val lastCharacter = text[inputEnd]
                    if (lastCharacter == '\n') return null
                }

                val spans = editableText.getSpans(start, start, AztecListSpan::class.java)
                if (!spans.isEmpty()) {
                    val flags = editableText.getSpanFlags(spans[0])
                    if ((flags and Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) == Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) {
                        return spans[0]
                    }
                }
            }
        }

        return null

    }

    //TODO: make this to also work with bullet span in the future
    fun getListSpanToClose(editableText: Editable): AztecListSpan? {
        if (start >= 1 && count == 0) {
            if (text[start - 1] != '\n') return null
            val spans = editableText.getSpans(start, start, AztecListSpan::class.java)
            if (!spans.isEmpty()) {
                val flags = editableText.getSpanFlags(spans[0])
                if ((flags and Spanned.SPAN_EXCLUSIVE_INCLUSIVE) == Spanned.SPAN_EXCLUSIVE_INCLUSIVE) {
                    return spans[0]
                }
            }

        }

        return null

    }


}

