package org.wordpress.aztec.demo.tests

import org.junit.Test
import org.wordpress.aztec.History
import org.wordpress.aztec.demo.BaseHistoryTest
import org.wordpress.aztec.demo.pages.EditorPage

/**
 * Various tests for testing the [History] component for formatting styles in the editor:
 * * Bold
 * * Italic
 * * Underline
 * * Strikethrough
 * * Code
 */
class FormattingHistoryTests : BaseHistoryTest() {

    @Test
    fun testAddBoldUndoRedo() {
        val word = "Testing"
        val html = "<strong>$word</strong>"
        val editorPage = EditorPage()

        // Add bold text, verify
        editorPage
                .toggleBold()
                .threadSleep(throttleTime) // Guarantee the <b> tags are in their own history stack
                .insertText(word)
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(html)
                .toggleHtml()

        // Undo add bold text, verify
        editorPage
                .undoChange()
                .threadSleep(throttleTime)
                .undoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML("")
                .toggleHtml()

        //  Redo add bold text, verify
        editorPage
                .redoChange()
                .threadSleep(throttleTime)
                .redoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(html)
    }

    @Test
    fun testAddBoldBetweenTwoWordsUndoRedo() {
        val word1 = "Testing"
        val word2 = " Bolder"
        val word3 = " History"
        val htmlSecond = "$word1<strong>$word2</strong>"
        val htmlFinal = "$word1<strong>$word2</strong>$word3"
        val editorPage = EditorPage()

        // Add first word - regular
        editorPage
                .insertText(word1)
                .threadSleep(throttleTime)

        // Add second word - bold
        editorPage
                .toggleBold()
                .threadSleep(throttleTime)
                .insertText(word2)
                .threadSleep(throttleTime)

        // Verify
        editorPage
                .toggleHtml()
                .verifyHTMLNoStripping(htmlSecond)
                .toggleHtml()
                .toggleBold()
                .threadSleep(throttleTime)

        // Add third word - regular
        editorPage
                .insertText(word3)
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTMLNoStripping(htmlFinal)
                .toggleHtml()

        // Undo add third and second word
        editorPage
                .undoChange()
                .threadSleep(throttleTime)
                .undoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTMLNoStripping(word1)
                .toggleHtml()

        // Redo add 2nd bolded word, verify
        editorPage
                .redoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTMLNoStripping(htmlSecond)
    }

    @Test
    fun testSelectToMakeBoldUndoRedo() {
        val text = "There's no crying in baseball!"
        val html = "<strong>$text</strong>"
        val editorPage = EditorPage()

        // Insert text snippet
        editorPage
                .insertText(text)
                .threadSleep(throttleTime)

        // Select snippet1 and toggle bold, verify
        editorPage
                .selectAllText()
                .toggleBold()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTMLNoStripping(html)
                .toggleHtml()

        // Undo make snippet1 bold
        editorPage
                .tapTop()
                .undoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTMLNoStripping(text)
                .toggleHtml()

        // Redo make snippet1 bold
        editorPage
                .tapTop()
                .redoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTMLNoStripping(html)
    }

    @Test
    fun testCopyPasteBoldUndoRedo() {
        val html = "<b>Bold</b> <i>Italic</i> <u>Underline</u> <s class=\"test\">Strikethrough</s>"
        val verifyHtml = "<b>Bold</b> <b>Bold</b><i>Italic</i> <u>Underline</u> <s class=\"test\">Strikethrough</s>"
        val editorPage = EditorPage()

        // Insert html text snippet
        editorPage
                .toggleHtml()
                .insertHTML(html)
                .toggleHtml()

        // Copy the bold text and paste to end of string
        editorPage
                .tapTop()
                .copyRangeToClipboard(0, 4)
                .tapTop()
                .pasteRangeFromClipboard(5, 5)
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(verifyHtml)
                .toggleHtml()
                .threadSleep(throttleTime)

        // Undo paste and verify
        editorPage
                .undoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(html)
                .toggleHtml()

        // Redo and verify
        editorPage
                .redoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(verifyHtml)
    }

    @Test
    fun testAddItalicAndUnderlineUndoRedo() {
        val text = "There's no crying in baseball!"
        val htmlRegex = Regex("<em><u>$text</u></em>|<u><em>$text</em></u>")
        val editorPage = EditorPage()

        // Insert text snippet
        editorPage
                .insertText(text)
                .threadSleep(throttleTime)

        // Select snippet1 and toggle italic, then underline
        editorPage
                .selectAllText()
                .toggleItalics()
                .threadSleep(throttleTime)
                .toggleUnderline()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(htmlRegex)
                .toggleHtml()

        // Undo formatting on snippet1, verify
        editorPage
                .tapTop()
                .undoChange()
                .threadSleep(throttleTime)
                .undoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTMLNoStripping(text)
                .toggleHtml()

        // Redo formatting on snippet1, verify
        editorPage
                .tapTop()
                .redoChange()
                .threadSleep(throttleTime)
                .redoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(htmlRegex)
    }

    @Test
    fun testMakeStrikethroughUndoRedo() {
        val snippet1 = "There's no crying in"
        val snippet2 = " baseball!"
        val html = "$snippet1<del>$snippet2</del>"
        val editorPage = EditorPage()

        // Insert first snippet
        editorPage
                .insertText(snippet1)
                .threadSleep(throttleTime)

        // Toggle strikethrough, add second snippet and verify
        editorPage
                .toggleStrikethrough()
                .threadSleep(throttleTime)
                .focusedInsertText(snippet2)
                .toggleStrikethrough()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTMLNoStripping(html)
                .toggleHtml()

        // Undo adding stikethrough snippet
        editorPage
                .undoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTMLNoStripping(snippet1)
                .toggleHtml()

        // Redo adding strikethrough snippet
        editorPage
                .redoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTMLNoStripping(html)
    }

    @Test
    fun testAddCodeStyleHtmlUndoRedo() {
        val html1 = "<code>"
        val html2 = "testing code"
        val html3 = "</code>"
        val html = "$html1$html2$html3"
        val editorPage = EditorPage()

        // Insert html in chunks to guarantee history stack layout
        editorPage
                .toggleHtml()
                .insertHTML(html1)
                .threadSleep(throttleTime)
                .insertHTML(html2)
                .threadSleep(throttleTime)
                .insertHTML(html3)
                .toggleHtml()
                .threadSleep(throttleTime)

        // Verify html
        editorPage
                .toggleHtml()
                .verifyHTML(html)
                .toggleHtml()

        // Undo changes, verify
        editorPage
                .undoChange().undoChange().undoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML("")
                .toggleHtml()

        // Redo changes, verify
        editorPage
                .redoChange().redoChange().redoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(html)
    }
}
