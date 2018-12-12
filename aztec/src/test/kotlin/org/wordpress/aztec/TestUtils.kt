package org.wordpress.aztec

import android.view.KeyEvent
import android.widget.EditText
import org.wordpress.aztec.watchers.EndOfBufferMarkerAdder

/**
 * Test utilities to be used with unit tests.
 */
object TestUtils {
    /**
     * Compare two strings ignoring case and whitespace.  Two strings are considered equal if they
     * are of the same length and corresponding characters while ignoring case and whitespace.
     *
     * @param first The first string to compare.
     * @param second The second string to compare.
     * @return True if first equals second ignoring case and whitespace.  False otherwise.
     */
    fun equalsIgnoreCaseAndWhitespace(first: String, second: String): Boolean {
        return first.replace("\\s+".toRegex(), "").equals(second.replace("\\s+".toRegex(), ""), ignoreCase = true)
    }

    /**
     * Compare two string ignoring whitespace.  Two strings are considered equal if they are of the
     * same length and corresponding characters while ignoring whitespace.
     *
     * @param first The first string to compare.
     * @param second The second string to compare.
     * @return True if first equals second ignoring whitespace.  False otherwise.
     */
    fun equalsIgnoreWhitespace(first: String, second: String): Boolean {
        return first.replace("\\s+".toRegex(), "") == second.replace("\\s+".toRegex(), "")
    }

    /**
     * Issue a Backspace key event
     *
     * @param text The EditText to issue the key event to
     * @param position The position to set the cursor prior to issuing the backspace
     */
    fun backspaceAt(text: EditText, position: Int) {
        text.setSelection(position)

        // Send key event since that's the way AztecText will remove the style when text is empty
        text.dispatchKeyEvent(KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL, 0))
        text.dispatchKeyEvent(KeyEvent(0, 0, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL, 0))
    }

    /**
     * Issue a copy-to-clipboard key event
     *
     * @param text The EditText to issue the key event to
     */
    fun copyToClipboard(text: AztecText) {
        text.copy(text.text, text.selectionStart, text.selectionEnd)
    }

    /**
     * Issue a copy to clipboard key event
     *
     * @param text The EditText to issue the key event to
     */
    fun pasteFromClipboard(text: AztecText) {
        text.paste(text.text, text.selectionStart, text.selectionEnd)
    }

    /**
     * Issue a copy as plain text to clipboard key event
     *
     * @param text The EditText to issue the key event to
     */
    fun pasteFromClipboardAsPlainText(text: AztecText) {
        text.paste(text.text, text.selectionStart, text.selectionEnd, true)
    }

    /**
     * Helper for calculating the EditText's length *without* counting the the end-of-text marker char if present
     *
     * @param text The EditText to check for length sans the end-of-text marker char if present
     * @return The length of text sans the end-of-text marker char if present
     */
    fun safeLength(text: EditText): Int {
        return EndOfBufferMarkerAdder.safeLength(text)
    }

    /**
     * Appends to the end of the text, taking into account the end-of-text marker if present and adding the text
     * just before that.
     *
     * @param editText The EditText to append the text to
     * @param string The string to append into the EditText
     */
    fun safeAppend(editText: EditText, string: String) {
        editText.text.insert(EndOfBufferMarkerAdder.safeLength(editText), string)
    }

    /**
     * Checks if the buffer is "empty", taking into account the end-of-text marker if present. For example, if the only
     * text in the buffer is the the end-of-marker, then the buffer is considered empty
     *
     * @param editText The EditText to check for emptiness
     * @return True if the EditText is empty even if it holds the end-of-text marker char, false otherwise.
     */
    fun safeEmpty(editText: EditText): Boolean {
        return editText.text.toString() == EndOfBufferMarkerAdder.ensureEndOfTextMarker(editText.text.toString())
    }
}
