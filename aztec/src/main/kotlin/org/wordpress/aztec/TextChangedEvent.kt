package org.wordpress.aztec

import android.text.Editable
import android.text.Spanned
import org.wordpress.aztec.spans.AztecBlockSpan
import java.util.*


data class TextChangedEvent(val text: CharSequence, val start: Int, val before: Int, val countOfCharacters: Int) {


    val inputEnd = start + countOfCharacters


    val numberOfAddedCharacters = countOfCharacters - before
    val numberOfRemovedCharacters = before - countOfCharacters

    val isAddingCharacters = numberOfAddedCharacters > numberOfRemovedCharacters

    val count = if (isAddingCharacters) numberOfAddedCharacters else Math.abs(numberOfRemovedCharacters)

    val inputStart = if (isAddingCharacters) inputEnd - count else inputEnd + count

    fun isAfterZeroWidthJoiner(): Boolean {
        if (text.length > inputStart && inputStart >= 1 && count > 0) {
            val previousCharacter = text[inputStart - 1]
            return previousCharacter == '\u200B'
        }
        return false
    }

    fun isNewLine(): Boolean {
        if (!isAddingCharacters) return false

        if (inputStart >= 1 && count == 1) {
            val currentCharacter = text[inputStart]
            //special case for newline at the end of EditText
            if (text.length == inputStart + 1 && currentCharacter == '\n') {
                return true
            } else if (text.length > inputStart + 1 && currentCharacter == '\n') {
                return true
            }
        }

        return false
    }

    fun getBlockSpanToOpen(editableText: Editable): ArrayList<AztecBlockSpan> {

        val spansToClose = ArrayList<AztecBlockSpan>()
        if (count >= 0) {
            if (text.length > inputStart)  {
                val spans = editableText.getSpans(inputStart, inputStart, AztecBlockSpan::class.java)

                spans.forEach {
                    val previousCharacter = if (isAddingCharacters) text[inputStart - 1] else text[inputEnd]
                    if (previousCharacter == '\n') return@forEach

                    val deletingLastCharacter = !isAddingCharacters && text.length == inputEnd
                    if (deletingLastCharacter) return@forEach

                    if (!isAddingCharacters && text.length > inputEnd) {
                        val lastCharacter = text[inputEnd]
                        if (lastCharacter == '\n') return@forEach
                    }


                    val flags = editableText.getSpanFlags(spans[0])
                    if ((flags and Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) == Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) {
                        spansToClose.add(it)
                    }
                }

                if (spans.isEmpty()) {
                    val spansAfterInput = editableText.getSpans(inputEnd, inputEnd, AztecBlockSpan::class.java)
                    spansAfterInput.forEach {
                        val flags = editableText.getSpanFlags(spansAfterInput[0])
                        if (((flags and Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) == Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) ||
                                (flags and Spanned.SPAN_EXCLUSIVE_INCLUSIVE) == Spanned.SPAN_EXCLUSIVE_INCLUSIVE) {
                            spansToClose.add(it)
                        }
                    }
                }
            }
        }

        return spansToClose

    }

    fun getBlockSpansToClose(editableText: Editable): ArrayList<AztecBlockSpan> {
        val spansToClose = ArrayList<AztecBlockSpan>()

        val startIndex = if (isAddingCharacters) inputStart else inputEnd
        if (startIndex > 0 && count == 1) {
            if (text[startIndex - 1] != '\n') return spansToClose

            val spans = editableText.getSpans(startIndex, startIndex, AztecBlockSpan::class.java)
            spans.forEach {
                val spanStart = editableText.getSpanStart(spans[0])
                val spanEnd = editableText.getSpanEnd(spans[0])

                if (startIndex == spanStart) {
                    spansToClose.add(it)
                } else if (startIndex == spanEnd) {
                    val flags = editableText.getSpanFlags(spans[0])
                    if ((flags and Spanned.SPAN_EXCLUSIVE_INCLUSIVE) == Spanned.SPAN_EXCLUSIVE_INCLUSIVE) {
                        spansToClose.add(it)
                    }
                }

            }


        } else if (startIndex == 0 && count == 1 && text.length > 0) {
            val spansAfterInput = editableText.getSpans(startIndex + 1, startIndex + 1, AztecBlockSpan::class.java)
            spansAfterInput.forEach {
                spansToClose.add(it)
            }
        }

        return spansToClose

    }


}

