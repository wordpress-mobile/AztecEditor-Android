package org.wordpress.aztec

import android.text.Spannable
import org.wordpress.aztec.spans.AztecBlockSpan


data class TextChangedEvent(val textBefore: CharSequence = "", val deletedFromBlockEnd: Boolean = false, val blockSpanStart: Int = -1) {

    var text: CharSequence = ""
    var before: Int = 0
    var start: Int = 0
    var countOfCharacters: Int = 0

    var inputEnd = start + countOfCharacters

    var numberOfAddedCharacters = countOfCharacters - before
    var numberOfRemovedCharacters = before - countOfCharacters

    var isAddingCharacters = numberOfAddedCharacters > numberOfRemovedCharacters

    var count = if (isAddingCharacters) numberOfAddedCharacters else Math.abs(numberOfRemovedCharacters)

    var inputStart = if (isAddingCharacters) inputEnd - count else inputEnd + count

    constructor(text: CharSequence, start: Int, before: Int, count: Int) : this() {
        this.text = text
        this.start = start
        this.before = before
        this.countOfCharacters = count

        initialize()
    }

    fun initialize() {
        inputEnd = start + countOfCharacters

        numberOfAddedCharacters = countOfCharacters - before
        numberOfRemovedCharacters = before - countOfCharacters

        isAddingCharacters = numberOfAddedCharacters > numberOfRemovedCharacters

        count = if (isAddingCharacters) numberOfAddedCharacters else Math.abs(numberOfRemovedCharacters)

        inputStart = if (isAddingCharacters) inputEnd - count else inputEnd + count
    }

    fun isAfterZeroWidthJoiner(): Boolean {
        if (text.length > inputStart && inputStart >= 1 && count > 0) {
            val previousCharacter = text[inputStart - 1]
            return previousCharacter == Constants.ZWJ_CHAR
        }
        return false
    }

    fun isNewLine() : Boolean {
        if (isAddingCharacters) {
            val currentCharacter = text[inputStart]
            if (currentCharacter == '\n' ||
                    (inputStart - 1 >= 0 && text[inputStart - 1] == '\n' &&
                            currentCharacter == Constants.ZWJ_CHAR)) {
                return true
            }
        }
        return false
    }

    fun isNewLineButNotAtTheBeginning() : Boolean {
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
}

