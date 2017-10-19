package org.wordpress.aztec.demo.tests

import android.support.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.wordpress.aztec.demo.BaseTest
import org.wordpress.aztec.demo.MainActivity
import org.wordpress.aztec.demo.pages.EditorPage

class ListTests : BaseTest() {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun testMixedList() {

        var text = "sample text\n"
        val expected1 = Regex("<ul>[\\S\\s]+</ul>")
        val expected2 = Regex("<ol>[\\S\\s]+</ol>")

        for (i in 1..4) {
            text += text
        }

        EditorPage()
                .tapTop()
                .insertText(text)
                .selectAllText()
                .makeList(EditorPage.ListStyle.UNORDERED)
                .toggleHtml()
                .verifyHTML(expected1)
                .toggleHtml()
                .tapTop()
                .makeList(EditorPage.ListStyle.ORDERED)
                .toggleHtml()
                .verifyHTML(expected2)
    }

}
