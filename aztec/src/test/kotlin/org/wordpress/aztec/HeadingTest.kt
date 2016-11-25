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

    val defaultHeadingFormat = TextFormat.FORMAT_HEADING_1
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
    fun applyHeadingToSingleLine() {
        editText.append("Heading 1")
        editText.toggleFormatting(defaultHeadingFormat)
        Assert.assertEquals("<h1>Heading 1</h1>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun applyHeadingToPartiallySelectedText() {
        editText.append("Heading 1")
        editText.setSelection(1, editText.length() - 2)
        editText.toggleFormatting(defaultHeadingFormat)
        Assert.assertEquals("<h1>Heading 1</h1>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun applyHeadingToSelectedMultilineText() {
        editText.append("First line")
        editText.append("\n")
        editText.append("Second line")
        editText.setSelection(3, editText.length() - 3)
        editText.toggleFormatting(defaultHeadingFormat)
        Assert.assertEquals("<h1>First line</h1><h1>Second line</h1>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun prependTextToHeading() {
        editText.append("Heading 1")
        editText.toggleFormatting(defaultHeadingFormat)
        editText.text.insert(0, "inserted")
        Assert.assertEquals("<h1>insertedHeading 1</h1>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun appendTextToHeading() {
        editText.append("Heading 1")
        editText.toggleFormatting(defaultHeadingFormat)
        editText.append("inserted")
        Assert.assertEquals("<h1>Heading 1inserted</h1>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun splitHeadingWithNewline() {
        editText.append("Heading 1")
        editText.toggleFormatting(defaultHeadingFormat)
        editText.text.insert(3, "\n")
        Assert.assertEquals("<h1>Hea</h1><h1>ding 1</h1>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun splitTwoHeadingsWithNewline() {
        editText.fromHtml("<h1>Heading 1</h1><h2>Heading 2</h2>")
        val mark = editText.text.indexOf("Heading 2")
        editText.text.insert(mark, "\n")
        Assert.assertEquals("<h1>Heading 1</h1><br><h2>Heading 2</h2>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun changeHeadingOfSingleLine() {
        editText.append("Heading 1")
        editText.toggleFormatting(defaultHeadingFormat)
        editText.toggleFormatting(TextFormat.FORMAT_HEADING_2)
        Assert.assertEquals("<h2>Heading 1</h2>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun changeHeadingOfSelectedMultilineText() {
        editText.fromHtml("<h1>Heading 1</h1><h2>Heading 2</h2>")
        editText.setSelection(0, editText.length())
        editText.toggleFormatting(TextFormat.FORMAT_HEADING_2)
        Assert.assertEquals("<h2>Heading 1</h2><h2>Heading 2</h2>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun applyHeadingToTextInsideList() {
        editText.fromHtml("<ol><li>Item 1</li><li>Item 2</li></ol>")
        editText.setSelection(0)
        editText.toggleFormatting(defaultHeadingFormat)
        Assert.assertEquals("<ol><li><h1>Item 1</h1></li><li>Item 2</li></ol>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun applyHeadingToTextInsideQuote() {
        editText.fromHtml("<blockquote>Quote</blockquote>")
        editText.toggleFormatting(defaultHeadingFormat)
        Assert.assertEquals("<blockquote><h1>Quote</h1></blockquote>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun applyQuoteToHeading() {
        editText.fromHtml("<h1>Quote</h1>")
        editText.toggleFormatting(TextFormat.FORMAT_QUOTE)
        Assert.assertEquals("<blockquote><h1>Quote</h1></blockquote>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun applyHeadingToTxtSurroundedByLists() {
        editText.fromHtml("<ol><li>Ordered</li></ol>Heading 1<ol><li>Ordered</li></ol>")
        val mark = editText.text.indexOf("Heading 1") + 1
        editText.setSelection(mark)
        editText.toggleFormatting(defaultHeadingFormat)
        Assert.assertEquals("<ol><li>Ordered</li></ol><h1>Heading 1</h1><ol><li>Ordered</li></ol>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun applyHeadingToTxtSurroundedByQuotes() {
        editText.fromHtml("<blockquote>Quote</blockquote>Heading 1<blockquote>Quote</blockquote>")
        val mark = editText.text.indexOf("Heading 1") + 1
        editText.setSelection(mark)
        editText.toggleFormatting(defaultHeadingFormat)
        Assert.assertEquals("<blockquote>Quote</blockquote><h1>Heading 1</h1><blockquote>Quote</blockquote>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun applyTextStyleToHeading() {
        editText.append("Heading 1")
        editText.toggleFormatting(defaultHeadingFormat)
        editText.setSelection(0, editText.length())
        editText.toggleFormatting(TextFormat.FORMAT_BOLD)
        Assert.assertEquals("<h1><b>Heading 1</b></h1>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun applyTextStyleToPartiallySelectedHeading() {
        editText.append("Heading 1")
        editText.toggleFormatting(defaultHeadingFormat)
        editText.setSelection(0, 3)
        editText.toggleFormatting(TextFormat.FORMAT_BOLD)
        Assert.assertEquals("<h1><b>Hea</b>ding 1</h1>", editText.toHtml())
    }
}