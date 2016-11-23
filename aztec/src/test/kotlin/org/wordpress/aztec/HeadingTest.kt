package org.wordpress.aztec

import android.app.Activity
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.annotation.Config


/**
 * Testing quote behaviour.
 */
@RunWith(RobolectricGradleTestRunner::class)
@Config(constants = BuildConfig::class)
class HeadingTest() {

    val formattingType = TextFormat.FORMAT_HEADING_1
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
    fun styleSingleItem() {
        editText.append("Heading 1")
        editText.toggleFormatting(formattingType)
        Assert.assertEquals("<h1>Heading 1</h1>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleSingleItemWithPartialSelection() {
        editText.append("Heading 1")
        editText.setSelection(1, editText.length() - 2)
        editText.toggleFormatting(formattingType)
        Assert.assertEquals("<h1>Heading 1</h1>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleSelectedMultilineText() {
        editText.append("First line")
        editText.append("\n")
        editText.append("Second line")
        editText.setSelection(3, editText.length() - 3)
        editText.toggleFormatting(formattingType)
        Assert.assertEquals("<h1>First line</h1><h1>Second line</h1>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun prependTextToStyledItem() {
        editText.append("Heading 1")
        editText.toggleFormatting(formattingType)
        editText.text.insert(0,"inserted")
        Assert.assertEquals("<h1>insertedHeading 1</h1>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun appendTextToStyledItem() {
        editText.append("Heading 1")
        editText.toggleFormatting(formattingType)
        editText.append("inserted")
        Assert.assertEquals("<h1>Heading 1inserted</h1>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun splitStyleWithNewline() {
        editText.append("Heading 1")
        editText.toggleFormatting(formattingType)
        editText.text.insert(3, "\n")
        Assert.assertEquals("<h1>Hea</h1><h1>ding 1</h1>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun reStyleSingleItem() {
        editText.append("Heading 1")
        editText.toggleFormatting(formattingType)
        editText.toggleFormatting(TextFormat.FORMAT_HEADING_2)
        Assert.assertEquals("<h2>Heading 1</h2>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun reStyleSelectedMultilineText() {
        editText.fromHtml("<h1>Heading 1</h1><h2>Heading 2</h2>")
        editText.setSelection(0,editText.length())
        editText.toggleFormatting(TextFormat.FORMAT_HEADING_2)
        Assert.assertEquals("<h2>Heading 1</h2><h2>Heading 2</h2>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleTextInsideList() {
        editText.fromHtml("<ol><li>Item 1</li><li>Item 2</li></ol>")
        editText.setSelection(0)
        editText.toggleFormatting(formattingType)
    }

    @Test
    @Throws(Exception::class)
    fun styleTextInsideQuote() {
        editText.fromHtml("<blockquote>Quote</blockquote>")
        editText.toggleFormatting(formattingType)
        editText.fromHtml("<blockquote><h1>Quote</h1></blockquote>")
    }

    @Test
    @Throws(Exception::class)
    fun styleTextSurroundedByLists() {
        editText.fromHtml("<ol><li>Ordered</li></ol>Heading 1<ol><li>Ordered</li></ol>")
        val mark = editText.text.indexOf("Heading 1") + 1
        editText.setSelection(mark)
        editText.toggleFormatting(formattingType)
        Assert.assertEquals("<ol><li>Ordered</li></ol><h1>Heading 1</h1><ol><li>Ordered</li></ol>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleTextSurroundedByQuotes() {
        editText.fromHtml("<blockquote>Quote</blockquote>Heading 1<blockquote>Quote</blockquote>")
        val mark = editText.text.indexOf("Heading 1") + 1
        editText.setSelection(mark)
        editText.toggleFormatting(formattingType)
        Assert.assertEquals("<blockquote>Quote</blockquote><h1>Heading 1</h1><blockquote>Quote</blockquote>", editText.toHtml())
    }
}