package org.wordpress.aztec.demo.tests

import org.junit.Test
import org.wordpress.aztec.History
import org.wordpress.aztec.demo.BaseHistoryTest
import org.wordpress.aztec.demo.pages.EditLinkPage
import org.wordpress.aztec.demo.pages.EditorPage
import org.wordpress.aztec.formatting.LinkFormatter.LinkStyle

/**
 * Tests [History] component (undo/redo) functionality on [LinkStyle] elements.
 */
class LinkHistoryTests : BaseHistoryTest() {

    @Test
    fun testAddLinkViaDialogUndoRedo() {
        val link = "http://wordpress.com"
        val name = "WordPress"
        val html = "<a href=\"$link\">$name</a>"
        val editorPage = EditorPage()

        // Add link and verify
        addLinkAndVerify(editorPage, link, html, name)

        // Undo make link and verify
        editorPage
                .threadSleep(throttleTime)
                .undoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML("")
                .toggleHtml()

        // Redo make link and verify
        editorPage
                .threadSleep(throttleTime)
                .redoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(html)
    }

    @Test
    fun testAddNameAndMakeLinkUndoRedo() {
        val link = "http://wordpress.com"
        val name = "WordPress"
        val html = "<a href=\"$link\">$name</a>"
        val editorPage = EditorPage()

        // Add the text
        editorPage
                .insertText(name)
                .threadSleep(throttleTime)
                .selectAllText()

        // Make link from selected text
        addLinkAndVerify(editorPage, link, html)

        // Undo make link - should just leave the text
        editorPage
                .threadSleep(throttleTime)
                .undoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(name)
                .toggleHtml()

        // Undo add name and verify
        editorPage
                .threadSleep(throttleTime)
                .undoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML("")
                .toggleHtml()

        // Redo both steps and verify
        editorPage
                .threadSleep(throttleTime)
                .redoChange()
                .threadSleep(throttleTime)
                .redoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(html)
                .toggleHtml()
    }

    @Test
    fun testCopyPasteLinkUndoRedo() {
        val link = "http://wordpress.com"
        val name = "WordPress"
        val html = "<a href=\"$link\">$name</a>"
        val html2Links = "$html $html"
        val editorPage = EditorPage()

        // Add link and verify
        addLinkAndVerify(editorPage, link, html, name)

        // Insert a space
        editorPage
                .setCursorPositionAtEnd()
                .insertText(" ")
                .threadSleep(throttleTime)

        // Copy link and paste, verify
        editorPage
                .selectAllText()
                .copyToClipboard()
                .setCursorPositionAtEnd()
                .pasteFromClipboard()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(html2Links)
                .toggleHtml()
                .threadSleep(throttleTime)

        // Undo paste link, verify
        editorPage
                .undoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(html)
                .toggleHtml()
                .threadSleep(throttleTime)

        // Redo paste link, verify
        editorPage
                .redoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(html2Links)
    }

    @Test
    fun testAddLinkAfterTextUndoRedo() {
        val link = "http://wordpress.com"
        val name = "WordPress"
        val html = "Testing <a href=\"$link\">$name</a>"
        val text = "Testing"
        val editorPage = EditorPage()

        // Add text and link, verify
        editorPage
                .insertText("$text ")
                .threadSleep(throttleTime)
                .setCursorPositionAtEnd()
        addLinkAndVerify(editorPage, link, html, name)

        // Undo add link, verify
        editorPage
                .undoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(text)
                .toggleHtml()
                .threadSleep(throttleTime)

        // Redo add link, verify
        editorPage
                .redoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML(html)
    }

    @Test
    fun testAddLinkInHtmlUndoRedo() {
        val link = "http://wordpress.com"
        val name = "WordPress"
        val html = "<a href=\"$link\">$name</a>"
        val editorPage = EditorPage()

        // Add link in source editor, switch to editor.
        editorPage
                .toggleHtml()
                .insertHTML(html)
                .threadSleep(throttleTime)
                .toggleHtml()
                .threadSleep(throttleTime)

        // Undo and verify
        editorPage
                .undoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML("")
                .toggleHtml()
                .threadSleep(throttleTime)

        // Redo and verify
        editorPage
                .redoChange()
                .toggleHtml()
                .verifyHTML(html)
    }

    /**
     * Add a link to the editor and verify success
     *
     * @param [link] The link URL.
     * @param [expected] The string to verify link success.
     * @param [name] The name to assign to the link, or null.
     */
    private fun addLinkAndVerify(editorPage: EditorPage, link: String, expected: String, name: String? = null) {
        editorPage.makeLink()
        EditLinkPage().updateURL(link)
        name?.let {
            EditLinkPage().updateName(it)
        }
        EditLinkPage().ok()

        editorPage
                .toggleHtml()
                .verifyHTML(expected)
                .toggleHtml()
                .threadSleep(throttleTime)
    }
}