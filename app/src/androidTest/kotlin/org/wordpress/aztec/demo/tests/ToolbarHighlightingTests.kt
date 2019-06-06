package org.wordpress.aztec.demo.tests

import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.wordpress.aztec.demo.BaseTest
import org.wordpress.aztec.demo.MainActivity
import org.wordpress.aztec.demo.pages.EditorPage

class ToolbarHighlightingTests : BaseTest() {

    @Rule
    @JvmField
    val mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    // test behavior of highlighted style at 0 index of editor with 1 line of text (EOB marker at the 1 line)
    @Test
    fun testLeadingStyleHighlightInEmptyEditor() {
        val text = "some text"

        EditorPage()
                .toggleBold()
                .insertText(text)
                .toggleItalics()
                .checkBold(isChecked())
                .checkItalics(isChecked())
                .delete(text.length)
                .checkBold(isChecked())
                .checkItalics(isNotChecked())
                .delete(1)
                .checkBold(isNotChecked())
                .checkItalics(isNotChecked())
    }

    // test behavior of highlighted style at 0 index of editor with > 1 lines of text (no EOB marker at the 1 line)
    @Test
    fun testLeadingStyleHighlightInNotEmptyEditor() {
        val text = "some text"

        EditorPage()
                .toggleBold()
                .insertText(text)
                .toggleItalics()
                .insertText("\n")
                .checkBold(isNotChecked())
                .checkItalics(isNotChecked())
                .insertText(text)
                .delete(text.length * 2 + 1)
                .checkBold(isChecked())
                .checkItalics(isNotChecked())
                .delete(1)
                .checkBold(isNotChecked())
                .checkItalics(isNotChecked())
    }

    // make sure that inline style is not sticking to end of buffer marker
    @Test
    fun testInlineIsDeselectedNearEndOfBufferMarker() {
        val text1 = "some"
        val text2 = "text"

        EditorPage()
                .toggleBold()
                .insertText(text1)
                .checkBold(isChecked())
                .toggleBold()
                .checkBold(isNotChecked())
                .insertText(text2)
                .checkBold(isNotChecked())
                .toggleHtml()
                .verifyHTML("<strong>$text1</strong>$text2")
    }

    // make sure that selected toolbar style in empty editor remains when soft keyboard is displayed
    @Test
    fun testStyleHighlightPersistenceInEmptyEditorOnWindowFocusChange() {
        val text = "some text"

        EditorPage()
                .closeKeyboard()
                .toggleBold()
                .tapTop()
                .checkBold(isChecked())
                .closeKeyboard()
                .checkBold(isChecked())
                .insertText(text)
                .toggleHtml()
                .verifyHTML("<strong>$text</strong>")
    }

    @Test
    fun testHasBoldAndStrongFormatting() {
        val input = "<b>bold</b> normal strong"
        val html = "<b>bold</b> normal <strong>strong</strong>"

        EditorPage()
                .toggleHtml()
                .insertHTML(input)
                .toggleHtml()
                .selectText(12, 18)
                .toggleBold()
                .toggleHtml()
                .verifyHTML(html)
    }

    @Test
    fun testExpandBoldAndSetToStrongFormatting() {
        val input = "<b>bold</b> normal strong"
        val html = "<strong>bold normal strong</strong>"

        EditorPage()
                .toggleHtml()
                .insertHTML(input)
                .toggleHtml()
                .selectAllText()
                .toggleBold()
                .toggleHtml()
                .verifyHTML(html)
    }
}
