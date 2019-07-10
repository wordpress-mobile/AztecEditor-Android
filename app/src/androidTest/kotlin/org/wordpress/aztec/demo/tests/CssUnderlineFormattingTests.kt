package org.wordpress.aztec.demo.tests

import androidx.test.rule.ActivityTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.demo.BaseTest
import org.wordpress.aztec.demo.MainActivity
import org.wordpress.aztec.demo.R
import org.wordpress.aztec.demo.pages.EditorPage
import org.wordpress.aztec.plugins.CssUnderlinePlugin

class CssUnderlineFormattingTests : BaseTest() {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun init() {
        val aztecText = mActivityTestRule.activity.findViewById<AztecText>(R.id.aztec)
        aztecText.plugins.add(CssUnderlinePlugin())
    }

    @Test
    fun testSimpleCssUnderlineFormatting() {
        val text1 = "some"
        val text2 = "text"
        val html = "$text1<span style=\"text-decoration:underline;\">$text2</span>"

        EditorPage()
                .insertText(text1)
                .toggleUnderline()
                .insertText(text2)
                .toggleHtml()
                .verifyHTML(html)
    }

    @Test
    fun testRegularUnderlineFormattingPreservation() {
        val text1 = "some"
        val text2 = "text"
        val html = "$text1<u>$text2</u>"

        EditorPage()
                .toggleHtml()
                .insertHTML(html)
                .toggleHtml()
                .toggleHtml()
                .verifyHTML(html)
    }

    @Test
    fun testTogglingUnderlineFormatting() {
        val text1 = "some"
        val text2 = "text"
        val html = "$text1<u>$text2</u>"
        val expectedHtml = "$text1<span style=\"text-decoration:underline;\">$text2</span>"

        EditorPage()
                .toggleHtml()
                .insertHTML(html)
                .toggleHtml()
                .selectText(4, 8)
                .toggleUnderline()
                .toggleHtml()
                .verifyHTML("$text1$text2")
                .toggleHtml()
                .selectText(4, 8)
                .toggleUnderline()
                .toggleHtml()
                .verifyHTML(expectedHtml)
    }

    @Test
    fun testSimpleSplittingRegularUnderlineFormatting() {
        val text1 = "some"
        val text2 = "text"
        val html = "$text1<u>$text2</u>"
        val expectedHtml = "$text1<span style=\"text-decoration:underline;\">te</span>\n\n" +
                "<span style=\"text-decoration:underline;\">xt</span>"

        EditorPage()
                .toggleHtml()
                .insertHTML(html)
                .toggleHtml()
                .setCursorPositionAtEnd()
                .moveCursorLeftAsManyTimes(2)
                .insertNewLine()
                .toggleHtml()
                .verifyHTML(expectedHtml)
    }
}
