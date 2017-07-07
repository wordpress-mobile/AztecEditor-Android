package org.wordpress.aztec

import android.app.Activity
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

import org.wordpress.aztec.TestUtils.backspaceAt
import org.wordpress.aztec.TestUtils.safeAppend
import org.wordpress.aztec.TestUtils.safeEmpty
import org.wordpress.aztec.TestUtils.safeLength

/**
 * Testing interactions of multiple block elements
 */
@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(23))
class BlockElementsTest {

    lateinit var editText: AztecText

    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().visible().get()
        editText = AztecText(activity)
        editText.setCalypsoMode(false)
        activity.setContentView(editText)
    }

    @Test
    @Throws(Exception::class)
    fun mixedInlineAndBlockElementsWithoutExtraSpacing() {
        safeAppend(editText, "some text")
        safeAppend(editText, "\n")
        safeAppend(editText, "quote")
        editText.setSelection(safeLength(editText))
        editText.toggleFormatting(TextFormat.FORMAT_QUOTE)
        Assert.assertEquals("some text<blockquote>quote</blockquote>", editText.toHtml())
        editText.setSelection(safeLength(editText))
        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        safeAppend(editText, "list")
        editText.setSelection(safeLength(editText))
        editText.toggleFormatting(TextFormat.FORMAT_UNORDERED_LIST)
        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        safeAppend(editText, "some text")

        Assert.assertEquals("some text<blockquote>quote</blockquote><ul><li>list</li></ul>some text", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun mixedInlineAndBlockElementsWithExtraSpacing() {
        safeAppend(editText, "some text")
        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        safeAppend(editText, "quote")
        editText.setSelection(safeLength(editText))
        editText.toggleFormatting(TextFormat.FORMAT_QUOTE)
        Assert.assertEquals("some text<br><br><blockquote>quote</blockquote>", editText.toHtml())
        editText.setSelection(safeLength(editText))
        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        safeAppend(editText, "list")
        editText.setSelection(safeLength(editText))
        editText.toggleFormatting(TextFormat.FORMAT_UNORDERED_LIST)
        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        safeAppend(editText, "some text")

        Assert.assertEquals("some text<br><br><blockquote>quote</blockquote><br><ul><li>list</li></ul><br>some text", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun checkForDanglingListWithoutItems() {
        editText.toggleFormatting(TextFormat.FORMAT_ORDERED_LIST)
        Assert.assertEquals("<ol><li></li></ol>", editText.toHtml())
        Assert.assertTrue(safeEmpty(editText))

        backspaceAt(editText, 0)
        Assert.assertTrue(safeEmpty(editText))
        Assert.assertEquals("", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testCollapsingEmptyQuoteAboveNewline() {
        safeAppend(editText, "\n")
        editText.setSelection(0)
        editText.toggleFormatting(TextFormat.FORMAT_QUOTE)
        editText.text.insert(0,"\n")

        Assert.assertEquals("<br>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testCollapsingEmptyListAboveNewline() {
        safeAppend(editText, "\n")
        editText.setSelection(0)
        editText.toggleFormatting(TextFormat.FORMAT_ORDERED_LIST)
        editText.text.insert(0,"\n")

        Assert.assertEquals("<br>", editText.toHtml())
    }
}