package org.wordpress.aztec

import android.app.Activity
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

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
        activity.setContentView(editText)
    }

    @Test
    @Throws(Exception::class)
    fun mixedInlineAndBlockElementsWithoutExtraSpacing() {
        editText.append("some text")
        editText.append("\n")
        editText.append("quote")
        editText.setSelection(editText.length())
        editText.toggleFormatting(TextFormat.FORMAT_QUOTE)
        Assert.assertEquals("some text<blockquote>quote</blockquote>", editText.toHtml())
        editText.setSelection(editText.length())
        editText.append("\n")
        editText.append("\n")
        editText.append("list")
        editText.setSelection(editText.length())
        editText.toggleFormatting(TextFormat.FORMAT_UNORDERED_LIST)
        editText.append("\n")
        editText.append("\n")
        editText.append("some text")

        Assert.assertEquals("some text<blockquote>quote</blockquote><ul><li>list</li></ul><br>some text", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun mixedInlineAndBlockElementsWithExtraSpacing() {
        editText.append("some text")
        editText.append("\n")
        editText.append("\n")
        editText.append("quote")
        editText.setSelection(editText.length())
        editText.toggleFormatting(TextFormat.FORMAT_QUOTE)
        Assert.assertEquals("some text<br><blockquote>quote</blockquote>", editText.toHtml())
        editText.setSelection(editText.length())
        editText.append("\n")
        editText.append("\n")
        editText.append("\n")
        editText.append("list")
        editText.setSelection(editText.length())
        editText.toggleFormatting(TextFormat.FORMAT_UNORDERED_LIST)
        editText.append("\n")
        editText.append("\n")
        editText.append("\n")
        editText.append("some text")

        Assert.assertEquals("some text<br><blockquote>quote</blockquote><br><ul><li>list</li></ul><br><br>some text", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun checkForDanglingListWithoutItems() {
        editText.toggleFormatting(TextFormat.FORMAT_ORDERED_LIST)
        Assert.assertEquals("<ol><li></li></ol>", editText.toHtml())
        Assert.assertEquals(Constants.ZWJ_STRING, editText.text.toString())

        editText.text.delete(0, 1)
        Assert.assertEquals("", editText.text.toString())
        Assert.assertEquals("", editText.toHtml())
    }
}