package org.wordpress.aztec.demo.tests

import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.wordpress.aztec.demo.BaseTest
import org.wordpress.aztec.demo.MainActivity
import org.wordpress.aztec.demo.pages.EditorPage

class BasicTextEditingTests : BaseTest() {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    // Test reproducing the issue described in https://github.com/wordpress-mobile/AztecEditor-Android/issues/711
    @Test
    fun testEditOutChangesSwitch() {
        EditorPage()
                .insertText("text")
                .toggleHtml()
                .toggleHtml()
                .clearText()
                .toggleHtml()
                .verifyHTML("")
    }

}
