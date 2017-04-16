package org.wordpress.aztec

import android.app.Activity
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.wordpress.aztec.TestUtils.safeAppend
import org.wordpress.aztec.TestUtils.safeLength

/**
 * Testing quote behaviour.
 */
@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(23))
class HeadingTest {

    val defaultHeadingFormat = TextFormat.FORMAT_HEADING_1
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
        editText.fromHtml("<h1 foo=\"bar\">Heading 1</h1>")
        editText.append("inserted")
        Assert.assertEquals("<h1 foo=\"bar\">Heading 1inserted</h1>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun closeHeadingWithNewline() {
        editText.fromHtml("<h1 foo=\"bar\">Heading 1</h1>")
        editText.text.append("\n")
        editText.text.append("\n")
        editText.text.append("not heading")
        Assert.assertEquals("<h1 foo=\"bar\">Heading 1</h1><br>not heading", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun splitHeadingWithNewline() {
        editText.fromHtml("<h1 foo=\"bar\">Heading 1</h1>")
        editText.text.insert(3, "\n")
        Assert.assertEquals("<h1 foo=\"bar\">Hea</h1><h1 foo=\"bar\">ding 1</h1>", editText.toHtml())
        editText.text.insert(6, "\n")
        Assert.assertEquals("<h1 foo=\"bar\">Hea</h1><h1 foo=\"bar\">di</h1><h1 foo=\"bar\">ng 1</h1>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun splitTwoHeadingsWithNewline() {
        //cursor position right before Heading 2
        editText.fromHtml("<h1 foo=\"bar\">Heading 1</h1><h2>Heading 2</h2>")
        val mark = editText.text.indexOf("Heading 2")
        editText.text.insert(mark, "\n")
        Assert.assertEquals("<h1 foo=\"bar\">Heading 1</h1><br><h2>Heading 2</h2>", editText.toHtml())

        //alternative cursor position right after Heading 1
        editText.fromHtml("<h1 foo=\"bar\">Heading 1</h1><h2>Heading 2</h2>")
        editText.text.insert(mark - 1, "\n")
        Assert.assertEquals("<h1 foo=\"bar\">Heading 1</h1><br><h2>Heading 2</h2>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun newlineAtHeadingEndTextEndNoHtmlEffect() {
        editText.fromHtml("<h1 foo=\"bar\">Heading 1</h1>")
        safeAppend(editText, "\n")
        Assert.assertEquals("<h1 foo=\"bar\">Heading 1</h1>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun splitHeadingAndQuoteWithNewline() {
        editText.fromHtml("<h1 foo=\"bar\">Heading 1</h1><blockquote>Quote</blockquote>")
        val mark = editText.text.indexOf("Quote") - 1
        editText.text.insert(mark, "\n")
        Assert.assertEquals("<h1 foo=\"bar\">Heading 1</h1><br><blockquote>Quote</blockquote>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun changeHeadingOfSingleLine() {
        editText.fromHtml("<h1 foo=\"bar\">Heading 1</h1>")
        editText.toggleFormatting(defaultHeadingFormat)
        editText.toggleFormatting(TextFormat.FORMAT_HEADING_2)
        Assert.assertEquals("<h2>Heading 1</h2>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun changeHeadingOfSelectedMultilineText() {
        editText.fromHtml("<h1 foo=\"bar\">Heading 1</h1><h2>Heading 2</h2>")
        editText.setSelection(0, safeLength(editText))
        editText.toggleFormatting(TextFormat.FORMAT_HEADING_2)
        Assert.assertEquals("<h2 foo=\"bar\">Heading 1</h2><h2>Heading 2</h2>", editText.toHtml())
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
        editText.fromHtml("<h1 foo=\"bar\">Quote</h1>")
        editText.toggleFormatting(TextFormat.FORMAT_QUOTE)
        Assert.assertEquals("<h1 foo=\"bar\"><blockquote>Quote</blockquote></h1>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun applyHeadingToQuote() {
        editText.fromHtml("<blockquote>Quote</blockquote>")
        editText.toggleFormatting(TextFormat.FORMAT_HEADING_1)
        Assert.assertEquals("<blockquote><h1>Quote</h1></blockquote>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun applyHeadingToTextSurroundedByLists() {
        editText.fromHtml("<ol><li>Ordered</li></ol>Heading 1<ol><li>Ordered</li></ol>")
        val mark = editText.text.indexOf("Heading 1") + 1
        editText.setSelection(mark)
        editText.toggleFormatting(defaultHeadingFormat)
        Assert.assertEquals("<ol><li>Ordered</li></ol><h1>Heading 1</h1><ol><li>Ordered</li></ol>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun applyHeadingToTextSurroundedByQuotes() {
        editText.fromHtml("<blockquote>Quote</blockquote>Heading 1<blockquote>Quote</blockquote>")
        val mark = editText.text.indexOf("Heading 1") + 1
        editText.setSelection(mark)
        editText.toggleFormatting(defaultHeadingFormat)
        Assert.assertEquals("<blockquote>Quote</blockquote><h1>Heading 1</h1><blockquote>Quote</blockquote>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun applyTextStyleToHeading() {
        editText.fromHtml("<h5>Heading 5</h5><h1 foo=\"bar\">Heading 1</h1><h5>Heading 5</h5>")
        editText.setSelection(editText.text.indexOf("Heading 1"), editText.text.indexOf("Heading 1") + "Heading 1".length)
        editText.toggleFormatting(TextFormat.FORMAT_BOLD)
        Assert.assertEquals("<h5>Heading 5</h5><h1 foo=\"bar\"><b>Heading 1</b></h1><h5>Heading 5</h5>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun applyTextStyleToPartiallySelectedHeading() {
        editText.fromHtml("<h1 foo=\"bar\">Heading 1</h1>")
        editText.setSelection(0, 3)
        editText.toggleFormatting(TextFormat.FORMAT_BOLD)
        Assert.assertEquals("<h1 foo=\"bar\"><b>Hea</b>ding 1</h1>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun moveHeadingUpToUnstyledLine() {
        editText.fromHtml("<b>bold</b><h1 foo=\"bar\">Heading 1</h1>")
        val mark = editText.text.indexOf("Heading 1")
        editText.text.delete(mark - 1, mark)
        Assert.assertEquals("<b>bold</b>Heading 1", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun moveUnstyledLineUpToHeading() {
        editText.fromHtml("<h1 foo=\"bar\">Heading 1</h1>unstyled")
        val mark = editText.text.indexOf("unstyled")
        editText.text.delete(mark - 1, mark)
        Assert.assertEquals("<h1 foo=\"bar\">Heading 1unstyled</h1>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun deleteHeadingChars_Issue287() {
        editText.fromHtml("<h1>he</h1>")

        var l = safeLength(editText)
        editText.text.delete(l - 1, l)

        l = safeLength(editText)
        editText.text.delete(l - 1, l)
        Assert.assertEquals("<h1></h1>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addHeading_issue289() {
        editText.fromHtml("<h1>Heading 1</h1>")

        safeAppend(editText, "\n")

        editText.setSelection(safeLength(editText))
        editText.toggleFormatting(TextFormat.FORMAT_HEADING_2)
        Assert.assertEquals("<h1>Heading 1</h1><h2></h2>", editText.toHtml())

        safeAppend(editText, "Heading 2")
        Assert.assertEquals("<h1>Heading 1</h1><h2>Heading 2</h2>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testNewlinesAboveHeading() {
        editText.fromHtml("<br><h1>heading</h1>")

        Assert.assertEquals("\nheading", editText.text.toString())

        editText.fromHtml(editText.toHtml())

        Assert.assertEquals("\nheading", editText.text.toString())

        editText.text.insert(0, Constants.NEWLINE_STRING)

        Assert.assertEquals("\n\nheading", editText.text.toString())

        Assert.assertEquals("<br><br><h1>heading</h1>", editText.toHtml())
    }
}
