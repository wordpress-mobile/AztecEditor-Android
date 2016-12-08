package org.wordpress.aztec


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
            return previousCharacter == Constants.ZWJ_CHAR
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

}

