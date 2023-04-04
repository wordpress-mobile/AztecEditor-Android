package org.wordpress.aztec

import android.view.inputmethod.InputConnection

/**
 * Wrapper around proprietary Samsung InputConnection. Forwards all the calls to it, except for getExtractedText and
 * some custom logic in commitText
 */
class DeleteOverrideInputConnection(
        inputConnection: InputConnection,
        private val shouldDeleteSurroundingText: (beforeLength: Int, afterLength: Int) -> Boolean
) : InputConnectionWrapper(inputConnection) {
    override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
        return shouldDeleteSurroundingText(beforeLength, afterLength)
                && super.deleteSurroundingText(beforeLength, afterLength)
    }
}
