package org.wordpress.aztec.demo.tests

import android.support.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.wordpress.aztec.Aztec
import org.wordpress.aztec.demo.BaseTest
import org.wordpress.aztec.demo.MainActivity
import org.wordpress.aztec.demo.pages.EditorPage

class BaseAztecTests : BaseTest() {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun testHasNoChanges() {
        EditorPage()
                .hasChanges(Aztec.AztecHasChanges.NO_CHANGES)
                .toggleHtml()
                .hasChanges(Aztec.AztecHasChanges.NO_CHANGES)
                .toggleHtml()
                .toggleHtml()
                .hasChanges(Aztec.AztecHasChanges.NO_CHANGES)
                .toggleHtml()
                .toggleHtml()
                .toggleHtml()
                .hasChanges(Aztec.AztecHasChanges.NO_CHANGES)
    }

    @Test
    fun testHasChanges() {
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
                .hasChanges(Aztec.AztecHasChanges.CHANGES)
                .verifyHTML(afterParser)
                .toggleHtml()
                .selectAllText().copyToClipboard()
    }

    @Test
    fun testHasChangesFromPaste() {
        val word1 = "Testing"
        val editorPage = EditorPage()

        editorPage
                .hasChanges(Aztec.AztecHasChanges.NO_CHANGES)
                .copyStringToClipboard(word1)
                .pasteFromClipboard()
                .hasChanges(Aztec.AztecHasChanges.CHANGES)
                .toggleHtml()
                .verifyHTML(word1)
    }
}
