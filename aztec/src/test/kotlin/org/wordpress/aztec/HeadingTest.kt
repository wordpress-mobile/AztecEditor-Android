package org.wordpress.aztec

import android.app.Activity
import android.view.MenuItem
import android.widget.PopupMenu
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.wordpress.aztec.TestUtils.safeAppend
import org.wordpress.aztec.TestUtils.safeLength
import org.wordpress.aztec.source.SourceViewEditText
import org.wordpress.aztec.toolbar.AztecToolbar

/**
 * Testing heading behaviour.
 */
@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(23))
class HeadingTest() {

    lateinit var editText: AztecText
    lateinit var sourceText: SourceViewEditText
    lateinit var toolbar: AztecToolbar
    lateinit var menuHeading: PopupMenu
    lateinit var menuHeading1: MenuItem
    lateinit var menuHeading2: MenuItem
    lateinit var menuParagraph: MenuItem

    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().visible().get()
        editText = AztecText(activity)
        editText.setCalypsoMode(false)
        sourceText = SourceViewEditText(activity)
        sourceText.setCalypsoMode(false)
        toolbar = AztecToolbar(activity)
        toolbar.setEditor(editText, sourceText, false)
        menuHeading = toolbar.getHeadingMenu() as PopupMenu
        menuHeading1 = menuHeading.menu.getItem(1)
        menuHeading2 = menuHeading.menu.getItem(2)
        menuParagraph = menuHeading.menu.getItem(0)
        activity.setContentView(editText)
    }

    @Test
    @Throws(Exception::class)
    fun applyHeadingToSingleLine() {
        editText.append("Heading 1")
        toolbar.onMenuItemClick(menuHeading1)
        Assert.assertEquals("<h1>Heading 1</h1>", editText.toHtml())
        Assert.assertEquals(TextFormat.FORMAT_HEADING_1, toolbar.getSelectedHeadingMenuItem())
    }

    @Test
    @Throws(Exception::class)
    fun applyHeadingToPartiallySelectedText() {
        editText.append("Heading 1")
        editText.setSelection(1, editText.length() - 2)
        toolbar.onMenuItemClick(menuHeading1)
        Assert.assertEquals("<h1>Heading 1</h1>", editText.toHtml())
        Assert.assertEquals(TextFormat.FORMAT_HEADING_1, toolbar.getSelectedHeadingMenuItem())
    }

    @Test
    @Throws(Exception::class)
    fun applyHeadingToSelectedMultilineText() {
        editText.append("First line")
        editText.append("\n")
        editText.append("Second line")
        editText.setSelection(3, editText.length() - 3)
        toolbar.onMenuItemClick(menuHeading1)
        Assert.assertEquals("<h1>First line</h1><h1>Second line</h1>", editText.toHtml())
        Assert.assertEquals(TextFormat.FORMAT_HEADING_1, toolbar.getSelectedHeadingMenuItem())
    }

    @Test
    @Throws(Exception::class)
    fun prependTextToHeading() {
        editText.append("Heading 1")
        toolbar.onMenuItemClick(menuHeading1)
        editText.text.insert(0, "inserted")
        Assert.assertEquals("<h1>insertedHeading 1</h1>", editText.toHtml())
        Assert.assertEquals(TextFormat.FORMAT_HEADING_1, toolbar.getSelectedHeadingMenuItem())
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
        toolbar.onMenuItemClick(menuHeading1)
        toolbar.onMenuItemClick(menuHeading2)
        Assert.assertEquals("<h2 foo=\"bar\">Heading 1</h2>", editText.toHtml())
        Assert.assertEquals(TextFormat.FORMAT_HEADING_2, toolbar.getSelectedHeadingMenuItem())
    }

    @Test
    @Throws(Exception::class)
    fun changeHeadingOfSelectedMultilineText() {
        editText.fromHtml("<h1 foo=\"bar\">Heading 1</h1><h2>Heading 2</h2>")
        editText.setSelection(0, safeLength(editText))
        toolbar.onMenuItemClick(menuHeading2)
        Assert.assertEquals("<h2 foo=\"bar\">Heading 1</h2><h2>Heading 2</h2>", editText.toHtml())
        Assert.assertEquals(TextFormat.FORMAT_HEADING_2, toolbar.getSelectedHeadingMenuItem())
    }

    @Test
    @Throws(Exception::class)
    fun applyHeadingToTextInsideList() {
        editText.fromHtml("<ol><li>Item 1</li><li>Item 2</li></ol>")
        editText.setSelection(0)
        toolbar.onMenuItemClick(menuHeading1)
        Assert.assertEquals("<ol><li><h1>Item 1</h1></li><li>Item 2</li></ol>", editText.toHtml())
        Assert.assertEquals(TextFormat.FORMAT_HEADING_1, toolbar.getSelectedHeadingMenuItem())
    }

    @Test
    @Throws(Exception::class)
    fun applyHeadingToTextInsideQuote() {
        editText.fromHtml("<blockquote>Quote</blockquote>")
        toolbar.onMenuItemClick(menuHeading1)
        Assert.assertEquals("<blockquote><h1>Quote</h1></blockquote>", editText.toHtml())
        Assert.assertEquals(TextFormat.FORMAT_HEADING_1, toolbar.getSelectedHeadingMenuItem())
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
        toolbar.onMenuItemClick(menuHeading1)
        Assert.assertEquals("<blockquote><h1>Quote</h1></blockquote>", editText.toHtml())
        Assert.assertEquals(TextFormat.FORMAT_HEADING_1, toolbar.getSelectedHeadingMenuItem())
    }

    @Test
    @Throws(Exception::class)
    fun applyHeadingToTextSurroundedByLists() {
        editText.fromHtml("<ol><li>Ordered</li></ol>Heading 1<ol><li>Ordered</li></ol>")
        val mark = editText.text.indexOf("Heading 1") + 1
        editText.setSelection(mark)
        toolbar.onMenuItemClick(menuHeading1)
        Assert.assertEquals("<ol><li>Ordered</li></ol><h1>Heading 1</h1><ol><li>Ordered</li></ol>", editText.toHtml())
        Assert.assertEquals(TextFormat.FORMAT_HEADING_1, toolbar.getSelectedHeadingMenuItem())
    }

    @Test
    @Throws(Exception::class)
    fun applyHeadingToTextSurroundedByQuotes() {
        editText.fromHtml("<blockquote>Quote</blockquote>Heading 1<blockquote>Quote</blockquote>")
        val mark = editText.text.indexOf("Heading 1") + 1
        editText.setSelection(mark)
        toolbar.onMenuItemClick(menuHeading1)
        Assert.assertEquals("<blockquote>Quote</blockquote><h1>Heading 1</h1><blockquote>Quote</blockquote>", editText.toHtml())
        Assert.assertEquals(TextFormat.FORMAT_HEADING_1, toolbar.getSelectedHeadingMenuItem())
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
    fun updateHeadingMenuOnSelectionChange() {
        editText.fromHtml("<h1>Heading 1</h1><h2>Heading 2</h2><h3>Heading 3</h3><b>Bold</b><i>Italic</i>None")
        var cursor: Int

        cursor = editText.text.indexOf("ing 1")
        editText.setSelection(cursor)
        Assert.assertEquals(TextFormat.FORMAT_HEADING_1, toolbar.getSelectedHeadingMenuItem())

        cursor = editText.text.indexOf("ld")
        editText.setSelection(cursor)
        Assert.assertEquals(TextFormat.FORMAT_PARAGRAPH, toolbar.getSelectedHeadingMenuItem())

        cursor = editText.text.indexOf("ing 2")
        editText.setSelection(cursor)
        Assert.assertEquals(TextFormat.FORMAT_HEADING_2, toolbar.getSelectedHeadingMenuItem())

        cursor = editText.text.indexOf("lic")
        editText.setSelection(cursor)
        Assert.assertEquals(TextFormat.FORMAT_PARAGRAPH, toolbar.getSelectedHeadingMenuItem())

        cursor = editText.text.indexOf("ing 3")
        editText.setSelection(cursor)
        Assert.assertEquals(TextFormat.FORMAT_HEADING_3, toolbar.getSelectedHeadingMenuItem())

        cursor = editText.text.indexOf("one")
        editText.setSelection(cursor)
        Assert.assertEquals(TextFormat.FORMAT_PARAGRAPH, toolbar.getSelectedHeadingMenuItem())
    }

    /**
     * https://github.com/wordpress-mobile/AztecEditor-Android/issues/287
     */
    @Test
    @Throws(Exception::class)
    fun deleteAllCharactersFromHeading() {
        editText.fromHtml("<h1>he</h1>")

        var l = safeLength(editText)
        editText.text.delete(l - 1, l)

        l = safeLength(editText)
        editText.text.delete(l - 1, l)
        Assert.assertEquals("<h1></h1>", editText.toHtml())
    }

    /**
     * https://github.com/wordpress-mobile/AztecEditor-Android/issues/289
     */
    @Test
    @Throws(Exception::class)
    fun addCharactersAfterSelectingHeading() {
        editText.fromHtml("<h1>Heading 1</h1>")

        safeAppend(editText, "\n")

        editText.setSelection(safeLength(editText))
        toolbar.onMenuItemClick(menuHeading2)
        Assert.assertEquals("<h1>Heading 1</h1><h2></h2>", editText.toHtml())
        Assert.assertEquals(TextFormat.FORMAT_HEADING_2, toolbar.getSelectedHeadingMenuItem())

        safeAppend(editText, "Heading 2")
        Assert.assertEquals("<h1>Heading 1</h1><h2>Heading 2</h2>", editText.toHtml())
        Assert.assertEquals(TextFormat.FORMAT_HEADING_2, toolbar.getSelectedHeadingMenuItem())
    }

    /**
     * https://github.com/wordpress-mobile/AztecEditor-Android/issues/317
     */
    @Test
    @Throws(Exception::class)
    fun removeHeadingWithParagraph() {
        editText.fromHtml("<h1>Heading 1</h1><h2>Heading 2</h2><h3>Heading 3</h3>")
        val cursor = editText.text.indexOf("ing 2")
        editText.setSelection(cursor)

        toolbar.onMenuItemClick(menuParagraph)
        Assert.assertEquals("<h1>Heading 1</h1>Heading 2<h3>Heading 3</h3>", editText.toHtml())
        Assert.assertEquals(TextFormat.FORMAT_PARAGRAPH, toolbar.getSelectedHeadingMenuItem())

        toolbar.onMenuItemClick(menuHeading2)
        Assert.assertEquals("<h1>Heading 1</h1><h2>Heading 2</h2><h3>Heading 3</h3>", editText.toHtml())
//        TODO: Correct heading menu selection.  This is incorrect.  Heading 2 should be selected.
//        AztecToolbar.highlightAppliedStyles returns Heading 1, Heading 2, and Heading 3 so then
//        AztecToolbar.selectHeaderMenu selects the first format.  See this issue for details.
//        https://github.com/wordpress-mobile/AztecEditor-Android/issues/317
//        Assert.assertEquals(TextFormat.FORMAT_HEADING_2, toolbar.getSelectedHeadingMenuItem())
        Assert.assertEquals(TextFormat.FORMAT_HEADING_1, toolbar.getSelectedHeadingMenuItem())
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