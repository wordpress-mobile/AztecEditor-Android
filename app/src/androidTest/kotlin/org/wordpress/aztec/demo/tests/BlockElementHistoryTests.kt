package org.wordpress.aztec.demo.tests

import org.junit.Test
import org.wordpress.aztec.demo.BaseHistoryTest
import org.wordpress.aztec.demo.pages.EditorPage

/**
 * Tests History (undo/redo) functionality on block elements:
 * * Headings
 * * Quotes
 * * Ordered/Unordered Lists
 */
class BlockElementHistoryTests : BaseHistoryTest() {

    @Test
    fun testMakeHeadingUndoRedo() {
        // Add heading to editor
        val word1 = "TESTING"
        val headingHtml = "<h1>$word1</h1>"
        val editorPage = EditorPage()

        editorPage
                .insertText(word1)
                .threadSleep(throttleTime)
                .selectAllText()
                .makeHeader(EditorPage.HeadingStyle.ONE)
                .threadSleep(throttleTime)

        // Verify header added
        editorPage
                .toggleHtml()
                .verifyHTML(headingHtml)
                .toggleHtml()

        // Undo make heading and verify
        editorPage
                .undoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(word1)
                .toggleHtml()

        // Redo make heading and verify
        editorPage
                .redoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(headingHtml)
    }

    @Test
    fun testMakeMultipleHeadingDeleteAllUndo() {
        val headingsHtml = "<h1>Heading 1</h1><h2>Heading 2</h2><h3>Heading 3</h3>"
        val editorPage = EditorPage()

        // Add multiple heading styles to editor
        editorPage
                .toggleHtml() // enter the source view
                .insertHTML(headingsHtml)
                .toggleHtml() // enter editor view
                .threadSleep(throttleTime)

        // select all text and delete
        editorPage
                .selectAllAndDelete()
                .threadSleep(throttleTime)

        // undo select all and delete action
        editorPage
                .undoChange()
                .threadSleep(throttleTime)

        // verify
        editorPage
                .toggleHtml()
                .verifyHTML(headingsHtml)
    }

    @Test
    fun testInsertBlockQuoteUndoRedo() {
        val quote = "Chuck Norris counted to infinity, twice"
        val quoteHtml = "<blockquote>$quote</blockquote>"
        val quoteEmptyHtml = "<blockquote></blockquote>"
        val editorPage = EditorPage()

        // Add some block quote text and verify
        editorPage
                .toggleQuote()
                .threadSleep(throttleTime)
                .insertText(quote)
                .toggleHtml()
                .verifyHTML(quoteHtml)
                .toggleHtml()
                .threadSleep(throttleTime)

        // Undo adding of block quote and verify.
        editorPage
                .undoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(quoteEmptyHtml)
                .toggleHtml()

        // Redo adding of block quote and verify
        editorPage
                .redoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(quoteHtml)
                .toggleHtml()
    }

    @Test
    fun testInsertBlockQuoteAddNewLineUndoRedo() {
        val firstLine = "Chuck Norris counted to infinity, twice,"
        val firstLineHtml = "<blockquote>$firstLine</blockquote>"
        val secondLine = "even still, the dragon won!"
        val secondLineHtml = "<blockquote>$firstLine\n\n$secondLine</blockquote>"
        val editorPage = EditorPage()

        // Add some block quote text and verify
        editorPage
                .toggleQuote()
                .threadSleep(throttleTime)
                .insertText(firstLine)
                .threadSleep(throttleTime)

        // Add new line, verify
        editorPage
                .insertNewLine()
                .insertText(secondLine)
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(secondLineHtml)
                .toggleHtml()

        // Undo add new line, verify
        editorPage
                .undoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(firstLineHtml)
                .toggleHtml()

        // Redo add new line, verify
        editorPage
                .redoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(secondLineHtml)
    }

    @Test
    fun testToggleSelectedBlockQuoteUndoRedo() {
        val quote = "Chuck Norris counted to infinity, twice"
        val quoteHtml = "<blockquote>$quote</blockquote>"
        val editorPage = EditorPage()

        // Add some text to the editor
        editorPage
                .insertText(quote)
                .threadSleep(throttleTime)

        // Select text, convert to block quote, verify
        editorPage
                .selectAllText()
                .toggleQuote()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(quoteHtml)
                .toggleHtml()

        // Undo convert to block quote, verify
        editorPage
                .undoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(quote)
                .toggleHtml()

        // Redo convert to block quote, verify
        editorPage
                .redoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(quoteHtml)
    }

    @Test
    fun testInsertListUndoRedo() {
        val item1 = "Item 1"
        val item2 = "Item 2"
        val item3 = "Item 3"
        val htmlFullList = "<ol><li>$item1</li><li>$item2</li><li>$item3</li></ol>"
        val htmlItem2 = "<ol><li>Item 1</li><li>Item 2</li></ol>"
        val htmlItem1 = "<ol><li>Item 1</li></ol>"
        val editorPage = EditorPage()

        // Add some text to the editor, verify
        editorPage
                .makeList(EditorPage.ListStyle.ORDERED)
                .insertText(item1)
                .threadSleep(throttleTime)
                .insertNewLine()
                .threadSleep(throttleTime)
                .insertText(item2)
                .threadSleep(throttleTime)
                .insertNewLine()
                .threadSleep(throttleTime)
                .insertText(item3)
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(htmlFullList)
                .toggleHtml()

        // Undo Item 3, verify.
        editorPage
                .undoChange().undoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(htmlItem2)
                .toggleHtml()

        // Undo Item 2, verify
        editorPage
                .undoChange().undoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(htmlItem1)
                .toggleHtml()

        // Redo Item 2, verify
        editorPage
                .redoChange().redoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(htmlItem2)
                .toggleHtml()

        // Redo Item 3, verify
        editorPage
                .redoChange().redoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(htmlFullList)
    }

    @Test
    fun testListDeleteEmptyItemUndoRedo() {
        val item1 = "Item 1"
        val itemHtml = "<ul><li>$item1</li></ul>"
        val itemNewLineHtml = "<ul><li>$item1</li><li></li></ul>"
        val editorPage = EditorPage()

        // Add list with one item
        editorPage
                .makeList(EditorPage.ListStyle.UNORDERED)
                .insertText(item1)
                .threadSleep(throttleTime)

        // Insert a new line and verify
        editorPage
                .insertNewLine()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(itemNewLineHtml)
                .toggleHtml()

        // Undo insert new line, verify
        editorPage
                .undoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(itemHtml)
                .toggleHtml()

        // Redo insert new line, verify
        editorPage
                .redoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(itemNewLineHtml)
                .toggleHtml()
    }
}