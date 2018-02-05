package org.wordpress.aztec.demo.tests

import android.support.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
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
        val regex = Regex("<b>$text1</b><i>$text2</i><[bi]><[bi]>$text3</[bi]></[bi]>")

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
        val html = "a <b>b</b>"

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
        val html = "$text1<b>$text2</b>$text3"

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
}
