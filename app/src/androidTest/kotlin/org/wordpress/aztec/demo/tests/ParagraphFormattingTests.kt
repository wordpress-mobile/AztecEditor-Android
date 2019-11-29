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
import org.wordpress.aztec.source.SourceViewEditText

class ParagraphFormattingTests : BaseTest() {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun init() {
        val aztecText = mActivityTestRule.activity.findViewById<AztecText>(R.id.aztec)
        val sourceText = mActivityTestRule.activity.findViewById<SourceViewEditText>(R.id.source)

        aztecText.setCalypsoMode(false)
        sourceText.setCalypsoMode(false)
    }

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
