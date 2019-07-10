package org.wordpress.aztec.demo.tests

import androidx.test.rule.ActivityTestRule
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.demo.BaseTest
import org.wordpress.aztec.demo.MainActivity
import org.wordpress.aztec.demo.pages.EditorPage

class MixedTextFormattingTests : BaseTest() {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    // Test reproducing the issue described in
    // https://github.com/wordpress-mobile/AztecEditor-Android/pull/476#issuecomment-327762497
    @Test
    fun testBoldAndItalicFormatting() {
        val text1 = "so"
        val text2 = "me "
        val text3 = "text "
        val regex = Regex("<strong>$text1</strong><em>$text2</em><(strong|em)><(strong|em)>$text3</(strong|em)></(strong|em)>")

        EditorPage()
                .toggleBold()
                .insertText(text1)
                .toggleBold()
                .toggleItalics()
                .insertText(text2)
                .toggleBold()
                .insertText(text3)
                .toggleHtml()
                .verifyHTML(regex)
    }

    // Test reproducing the issue described in
    // https://github.com/wordpress-mobile/AztecEditor-Android/issues/530
    @Test
    fun testBoldFormattingAndSpaceInsertion() {
        val text1 = "a"
        val text2 = "b"
        val text3 = " "
        val html = "a <strong>b</strong>"

        EditorPage()
                .insertText(text1)
                .toggleBold()
                .focusedInsertText(text2)
                .moveCursorLeftAsManyTimes(1)
                .focusedInsertText(text3)
                .toggleHtml()
                .verifyHTML(html)
    }

    @Test
    fun testRemoveFormattingAndContinueTyping() {
        val text1 = "some"
        val text2 = "more"
        val text3 = "text"
        val html = "$text1<strong>$text2</strong>$text3"

        EditorPage()
                .insertText(text1)
                .toggleBold()
                .insertText(text2)
                .toggleBold()
                .insertText(text3)
                .toggleHtml()
                .verifyHTML(html)
    }

    @Test
    fun testParagraphAndBlockFormatting() {
        val text = "some text"
        val html1 = "<p>$text</p>"
        val html2 = "<blockquote>$text</blockquote>"

        EditorPage()
                .toggleHtml()
                .insertHTML(html1)
                .toggleHtml()
                .toggleQuote()
                .toggleHtml()
                .verifyHTML(html2)
    }

    @Test
    fun testRetainParagraphFormatting() {
        // Paragraph are only retained if they have at least one attribute
        // https://github.com/wordpress-mobile/AztecEditor-Android/pull/483#discussion_r151377857
        val text = "some text"
        val html = "<p a=\"ok\">$text</p>"

        EditorPage()
                .toggleHtml()
                .insertHTML(html)
                .toggleHtml()
                .toggleHtml()
                .verifyHTML(html)
    }

    @Test
    fun testRetainHeadingFormatting() {
        val text = "some text"
        val html = "<h1>$text</h1>"

        EditorPage()
                .toggleHtml()
                .insertHTML(html)
                .toggleHtml()
                .selectAllText()
                .makeHeader(EditorPage.HeadingStyle.ONE)
                .toggleHtml()
                .verifyHTML(html)
    }

    @Test
    fun testSwitchHeadingFormatting() {
        val text = "some text"
        val html = "<h1>$text</h1>"

        EditorPage()
                .toggleHtml()
                .insertHTML(html)
                .toggleHtml()
                .selectAllText()
                .makeHeader(EditorPage.HeadingStyle.DEFAULT)
                .toggleHtml()
                .verifyHTML(text)
    }

    @Test
    fun testTwoHeadings() {
        val text = "some text"
        val html = "<h1>$text</h1><h2>$text</h2>"

        EditorPage()
                .makeHeader(EditorPage.HeadingStyle.ONE)
                .insertText(text)
                .insertText("\n")
                .makeHeader(EditorPage.HeadingStyle.TWO)
                .insertText(text)
                .toggleHtml()
                .verifyHTML(html)
    }

    @Test
    fun testEndHeadingFormatting() {
        val text = "some text"
        val html = "<h1>$text</h1>\n$text"

        EditorPage()
                .makeHeader(EditorPage.HeadingStyle.ONE)
                .insertText(text)
                .insertText("\n")
                .insertText(text)
                .toggleHtml()
                .verifyHTML(html)
    }

    @Test
    fun testEndQuoteFormatting() {
        val text = "some text"
        val html = "<blockquote>$text</blockquote>\n$text"

        EditorPage()
                .toggleQuote()
                .insertText(text)
                .insertText("\n\n")
                .insertText(text)
                .toggleHtml()
                .verifyHTML(html)
    }

    @Ignore("Until this issue is fixed: https://github.com/wordpress-mobile/AztecEditor-Android/issues/676")
    @Test
    fun testRemoveQuoteFormatting() {
        val text = "some text"
        val html = "$text\n<br>\n$text"

        EditorPage()
                .toggleQuote()
                .insertText(text)
                .insertText("\n")
                .insertText(text)
                .toggleQuote()
                .toggleHtml()
                .verifyHTML(html)
    }

    @Ignore("Until this issue is fixed: https://github.com/wordpress-mobile/AztecEditor-Android/issues/676")
    @Test
    fun testQuotedListFormatting() {
        val text = "some text\nsome text\nsome text"
        val html = "<blockquote><ul><li>some text</li><li>some text</li><li>some text</li></ul></blockquote>"

        EditorPage()
                .toggleQuote()
                .makeList(EditorPage.ListStyle.UNORDERED)
                .insertText(text)
                .toggleHtml()
                .verifyHTML(html)
    }

    @Ignore("Until this issue is fixed: https://github.com/wordpress-mobile/AztecEditor-Android/issues/676")
    @Test
    fun testQuotedListRemoveListFormatting() {
        val text = "some text\nsome text\nsome text"
        val html = "<blockquote><ul><li>some text</li><li>some text</li></ul>\nsome text</blockquote>"

        EditorPage()
                .toggleQuote()
                .makeList(EditorPage.ListStyle.UNORDERED)
                .insertText(text)
                .makeList(EditorPage.ListStyle.UNORDERED)
                .toggleHtml()
                .verifyHTML(html)
    }

    @Test
    fun testListwithQuoteFormatting() {
        val text1 = "some text\nsome text\nsome text\n"
        val text2 = "some text"
        val html = "<ul><li>some text</li><li>some text</li><li>some text</li><li><blockquote>some text</blockquote></li></ul>"

        EditorPage()
                .makeList(EditorPage.ListStyle.UNORDERED)
                .insertText(text1)
                .toggleQuote()
                .insertText(text2)
                .toggleHtml()
                .verifyHTML(html)
    }

    /**
     * Currently, this html <b>bold <i>italic</i> bold</b> after being parsed to span and back to html will become
     * <b>bold </b><b><i>italic</i></b><b> bold</b>. This is not a bug, this is how Google originally implemented the parsing inside Html.java.
     * https://github.com/wordpress-mobile/AztecEditor-Android/issues/136
     *
     * In this test we check the new `hasChanges` method to check if the post content has been edited by the user
     */
    @Test
    fun testHasNoChangesWithMixedBoldAndItalicFormatting() {
        val input = "<b>bold <i>italic</i> bold</b>"

        EditorPage()
                .toggleHtml()
                .insertHTML(input)
                .toggleHtml()
                .toggleHtml()
                .hasChanges(AztecText.EditorHasChanges.NO_CHANGES) // Verify that the user had not changed the input
    }

    @Test
    fun testHasChangesWithMixedBoldAndItalicFormatting() {
        val input = "<b>bold <i>italic</i> bold</b>"
        val insertedText = " text added"
        val afterParser = "<b>bold </b><i><b>italic</b></i><b> bold$insertedText</b>"

        EditorPage()
                .toggleHtml()
                .insertHTML(input)
                .toggleHtml()
                .setCursorPositionAtEnd()
                .insertText(insertedText)
                .toggleHtml()
                .hasChanges(AztecText.EditorHasChanges.CHANGES)
                .verifyHTML(afterParser)
    }

    @Test
    fun testHasChangesOnHTMLEditor() {
        val input = "<b>Test</b>"
        val insertedText = " text added"
        val afterParser = "<b>Test</b>$insertedText"

        EditorPage().toggleHtml()
                .insertHTML(input)
                .toggleHtml()
                .toggleHtml() // switch back to HTML editor
                .insertHTML(insertedText)
                .hasChangesHTML(AztecText.EditorHasChanges.CHANGES)
                .verifyHTML(afterParser)
    }

    @Test
    fun testHasChangesOnHTMLEditorTestedFromVisualEditor() {
        val input = "<b>Test</b>"
        val insertedText = " text added"
        val afterParser = "Test$insertedText"

        EditorPage().toggleHtml()
                .insertHTML(input)
                .toggleHtml()
                .toggleHtml() // switch back to HTML editor
                .insertHTML(insertedText)
                .hasChangesHTML(AztecText.EditorHasChanges.CHANGES)
                .toggleHtml() // switch back to Visual editor
                .verify(afterParser)
    }
}
