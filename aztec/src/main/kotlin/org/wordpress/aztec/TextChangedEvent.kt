package org.wordpress.aztec

import android.text.Editable
import android.text.Spanned


data class TextChangedEvent(val text: CharSequence, val start: Int, val before: Int, val countOfCharacters: Int) {

    val inputStart = start
    val inputEnd = start + countOfCharacters
    val count = countOfCharacters

    fun isAfterZeroWidthJoiner(): Boolean {
        if (start >= 1 && count > 0) {
            val previousCharacter = text[start - 1]
            return previousCharacter == '\u200B'
        }
        return false
    }

    fun isNewline(): Boolean {
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

    fun getSpanToOpen(editableText: Editable): AztecList?{
        if (start >= 1 && count >= 0) {
            if (text.length > start) {
                val char = text[start]

                if(count == 1 && char != '\n') return null

                val spans = editableText.getSpans(start, start, AztecList::class.java)
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


    fun getSpanToClose(editableText: Editable): AztecList?{
        if (start >= 1 && count == 0) {
            if (text[start - 1] == '\n') {
                val spans = editableText.getSpans(start, start, AztecList::class.java)
                if (!spans.isEmpty()) {
                    val flags = editableText.getSpanFlags(spans[0])
                    if ((flags and Spanned.SPAN_EXCLUSIVE_INCLUSIVE) == Spanned.SPAN_EXCLUSIVE_INCLUSIVE) {
                        return spans[0]
                    }
                }
            }
        }

        return null

    }


}

