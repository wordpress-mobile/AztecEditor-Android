package org.wordpress.aztec.demo.tests

import androidx.test.espresso.intent.rule.IntentsTestRule
import org.junit.Rule
import org.junit.Test
import org.wordpress.aztec.demo.BaseTest
import org.wordpress.aztec.demo.MainActivity
import org.wordpress.aztec.demo.pages.EditorPage

/**
 * Tests the parsing of the CSS 'color' style attribute.
 * Example:
 * <code>
 *     <u style="color:blue">Blue Underline</u>
 * </code>
 */
class HtmlAttributeStyleColorTests : BaseTest() {

    @Rule
    @JvmField
    val mActivityIntentsTestRule = IntentsTestRule<MainActivity>(MainActivity::class.java)

    /**
     * Tests that text appended to a string in the editor with a color style attribute applied, will
     * produce properly formatted HTML when switched to the source editor.
     */
    @Test
    fun testAppendTextToColoredItem() {
        val htmlStart = "<b style=\"color:blue\">Blue</b>"
        val appendText = " is a beautiful color"
        val htmlVerify = "<b style=\"color:blue\">Blue$appendText</b>"
        val editorPage = EditorPage()

        // insert starter text
        editorPage
                .toggleHtml()
                .insertHTML(htmlStart)
                .toggleHtml()

        // append text
        editorPage
                .setCursorPositionAtEnd()
                .insertText(appendText)

        // verify html
        editorPage
                .toggleHtml()
                .verifyHTML(htmlVerify)
    }

    /**
     * Tests placing a newline in the middle of styled text and verifying the HTML was split
     * and assigned the appropriate style attribute.
     */
    @Test
    fun testInsertNewlineInsideColoredItem() {
        val htmlStart = "<b style=\"color:blue\">Blue</b>"
        val htmlEnd = "<b style=\"color:blue\">Bl</b>\n\n<b style=\"color:blue\">ue</b>"
        val editorPage = EditorPage()

        // insert starter text
        editorPage
                .toggleHtml()
                .insertHTML(htmlStart)
                .toggleHtml()

        // set cursor to middle of text and add newline
        editorPage
                .setCursorPositionAtEnd()
                .moveCursorLeftAsManyTimes(2)
                .insertNewLine()

        // verify html
        editorPage
                .toggleHtml()
                .verifyHTML(htmlEnd)
    }
}
