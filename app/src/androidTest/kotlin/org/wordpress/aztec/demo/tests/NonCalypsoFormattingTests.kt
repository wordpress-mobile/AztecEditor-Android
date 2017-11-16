package org.wordpress.aztec.demo.tests

import android.support.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.wordpress.aztec.demo.BaseTest
import org.wordpress.aztec.demo.NonCalypsoActivity
import org.wordpress.aztec.demo.pages.EditorPage

class NonCalypsoFormattingTests : BaseTest() {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(NonCalypsoActivity::class.java)

    @Test
    fun testRetainParagraphFormatting() {
        val text = "some text"
        val html = "<p>$text</p>"

        EditorPage()
                .toggleHtml()
                .insertHTML(html)
                .toggleHtml()
                .toggleHtml()
                .verifyHTML(html)
    }
}