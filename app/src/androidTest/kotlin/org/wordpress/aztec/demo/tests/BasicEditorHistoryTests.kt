package org.wordpress.aztec.demo.tests

import org.junit.Test
import org.wordpress.aztec.History
import org.wordpress.aztec.demo.BaseHistoryTest
import org.wordpress.aztec.demo.pages.EditorPage

/**
 * Various tests for testing the [History] component for basic editing:
 * * Add/Delete Text
 * * Copy/Paste Text
 */
class BasicEditorHistoryTests : BaseHistoryTest() {

    @Test
    fun testAddWordsUndoRedo() {
        val word1 = "Testing"
        val word2 = " new"
        val word3 = " history"
        val word4 = " timer."
        val editorPage = EditorPage()

        // Add words to the editor
        editorPage
                .insertText(word1)
                .threadSleep(throttleTime)
                .insertText(word2)
                .threadSleep(throttleTime)
                .insertText(word3)
                .threadSleep(throttleTime)
                .insertText(word4)
                .threadSleep(throttleTime)

        // Undo each change and verify.
        editorPage
                .undoChange()
                .verify(word1 + word2 + word3)
                .undoChange()
                .verify(word1 + word2)
                .undoChange()
                .verify(word1)
                .undoChange()
                .verify("")
                .threadSleep(throttleTime)

        // Redo each change and verify.
        editorPage
                .redoChange()
                .verify(word1)
                .redoChange()
                .verify(word1 + word2)
                .redoChange()
                .verify(word1 + word2 + word3)
                .redoChange()
                .verify(word1 + word2 + word3 + word4)
    }

    @Test
    fun testCopyPasteTextUndoRedo() {
        val word1 = "Testing"
        val editorPage = EditorPage()

        // Add text to the editor
        editorPage
                .insertText(word1)
                .threadSleep(throttleTime)

        // Select text and copy to clipboard, then set the
        // cursor position to the end of the current text buffer.
        editorPage
                .selectAllText()
                .copyToClipboard()
                .setCursorPositionAtEnd()
                .threadSleep(throttleTime)

        // Paste text from clipboard
        editorPage
                .pasteFromClipboard()
                .threadSleep(throttleTime)

        // verify text pasted
        editorPage
                .verify(word1 + word1)

        // Undo paste and verify
        editorPage
                .undoChange()
                .threadSleep(throttleTime)
                .verify(word1)

        // Redo paste and verify
        editorPage
                .redoChange()
                .verify(word1 + word1)
    }

    @Test
    fun testAddHtmlWordsUndoRedo() {
        // Add words to the editor
        val word1 = "Testing"
        val word2 = " new"
        val word3 = " history"
        val word4 = " timer."
        val editorPage = EditorPage()

        // Insert words. Wait between each word to ensure history
        // is properly recorded.
        editorPage
                .toggleHtml()
                .insertHTML(word1)
                .threadSleep(throttleTime)
                .insertHTML(word2)
                .threadSleep(throttleTime)
                .insertHTML(word3)
                .threadSleep(throttleTime)
                .insertHTML(word4)
                .threadSleep(throttleTime)

        // Undo each change and verify.
        editorPage
                .undoChange()
                .verifyHTML(word1 + word2 + word3)
                .undoChange()
                .verifyHTML(word1 + word2)
                .undoChange()
                .verifyHTML(word1)
                .undoChange()
                .verifyHTML("")
                .threadSleep(throttleTime)

        // Redo each change and verify.
        editorPage
                .redoChange()
                .verifyHTML(word1)
                .redoChange()
                .verifyHTML(word1 + word2)
                .redoChange()
                .verifyHTML(word1 + word2 + word3)
                .redoChange()
                .verifyHTML(word1 + word2 + word3 + word4)
    }
}