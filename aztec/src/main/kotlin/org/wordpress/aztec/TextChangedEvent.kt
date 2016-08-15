package org.wordpress.aztec

import android.text.Editable
import android.text.Spanned
import android.text.style.BulletSpan


data class TextChangedEvent(val text: CharSequence, val start: Int, val before: Int, val count: Int) {

    val inputStart = start
    val inputEnd = start + count

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

    fun getSpanToOpen(editableText: Editable): BulletSpan?{
        if (start >= 1 && count > 0) {
            if (text.length > start) {

                val spans = editableText.getSpans(start, start, BulletSpan::class.java)
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


}

