package org.wordpress.aztec

import android.app.Activity
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.ToggleButton
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
class HeadingTest {
    lateinit var editText: AztecText
    lateinit var sourceText: SourceViewEditText
    lateinit var toolbar: AztecToolbar
    lateinit var buttonQuote: ToggleButton
    lateinit var menuHeading: PopupMenu
    lateinit var menuHeading1: MenuItem
    lateinit var menuHeading2: MenuItem
    lateinit var menuParagraph: MenuItem
//    TODO: Uncomment when Preformat is to be added back as a feature
//    lateinit var menuPreformat: MenuItem

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
        toolbar.setEditor(editText, sourceText)
        buttonQuote = toolbar.findViewById<ToggleButton>(R.id.format_bar_button_quote)
        menuHeading = toolbar.getHeadingMenu() as PopupMenu
        menuHeading1 = menuHeading.menu.getItem(1)
        menuHeading2 = menuHeading.menu.getItem(2)
        menuParagraph = menuHeading.menu.getItem(0)
//        TODO: Uncomment when Preformat is to be added back as a feature
//        menuPreformat = menuHeading.menu.getItem(7)
        activity.setContentView(editText)
    }

    @Test
    @Throws(Exception::class)
    fun applyHeadingToSingleLine() {
        editText.append("Heading 1")
        toolbar.onMenuItemClick(menuHeading1)
        Assert.assertEquals("<h1>Heading 1</h1>", editText.toHtml())
        Assert.assertEquals(AztecTextFormat.FORMAT_HEADING_1, toolbar.getSelectedHeadingMenuItem())
    }

//    TODO: Uncomment when Preformat is to be added back as a feature
//    @Test
//    @Throws(Exception::class)
//    fun applyPreformatToSingleLine() {
//        safeAppend(editText, "Preformat")
//        toolbar.onMenuItemClick(menuPreformat)
//        Assert.assertEquals("<pre>Preformat</pre>", editText.toHtml())
//        Assert.assertEquals(AztecTextFormat.FORMAT_PREFORMAT, toolbar.getSelectedHeadingMenuItem())
//    }

    @Test
    @Throws(Exception::class)
    fun applyHeadingToPartiallySelectedText() {
        editText.append("Heading 1")
        editText.setSelection(1, editText.length() - 2)
        toolbar.onMenuItemClick(menuHeading1)
        Assert.assertEquals("<h1>Heading 1</h1>", editText.toHtml())
        Assert.assertEquals(AztecTextFormat.FORMAT_HEADING_1, toolbar.getSelectedHeadingMenuItem())
    }

//    TODO: Uncomment when Preformat is to be added back as a feature
//    @Test
//    @Throws(Exception::class)
//    fun applyPreformatToPartiallySelectedText() {
//        safeAppend(editText, "Preformat")
//        editText.setSelection(1, editText.length() - 2)
//        toolbar.onMenuItemClick(menuPreformat)
//        Assert.assertEquals("<pre>Preformat</pre>", editText.toHtml())
//        Assert.assertEquals(AztecTextFormat.FORMAT_PREFORMAT, toolbar.getSelectedHeadingMenuItem())
//    }

    @Test
    @Throws(Exception::class)
    fun applyHeadingToSelectedMultilineText() {
        editText.append("First line")
        editText.append("\n")
        editText.append("Second line")
        editText.setSelection(3, editText.length() - 3)
        toolbar.onMenuItemClick(menuHeading1)
        Assert.assertEquals("<h1>First line</h1><h1>Second line</h1>", editText.toHtml())
        Assert.assertEquals(AztecTextFormat.FORMAT_HEADING_1, toolbar.getSelectedHeadingMenuItem())
    }

//    TODO: Uncomment when Preformat is to be added back as a feature
//    @Test
//    @Throws(Exception::class)
//    fun applyPreformatToSelectedMultilineText() {
//        safeAppend(editText, "First line")
//        safeAppend(editText, "\n")
//        safeAppend(editText, "Second line")
//        editText.setSelection(3, editText.length() - 3)
//        toolbar.onMenuItemClick(menuPreformat)
//        Assert.assertEquals("<pre>First line<br>Second line</pre>", editText.toHtml())
//        Assert.assertEquals(AztecTextFormat.FORMAT_PREFORMAT, toolbar.getSelectedHeadingMenuItem())
//    }

    @Test
    @Throws(Exception::class)
    fun prependTextToHeading() {
        editText.append("Heading 1")
        toolbar.onMenuItemClick(menuHeading1)
        editText.text.insert(0, "inserted")
        Assert.assertEquals("<h1>insertedHeading 1</h1>", editText.toHtml())
        Assert.assertEquals(AztecTextFormat.FORMAT_HEADING_1, toolbar.getSelectedHeadingMenuItem())
    }

//    TODO: Uncomment when Preformat is to be added back as a feature
//    @Test
//    @Throws(Exception::class)
//    fun prependTextToPreformat() {
//        safeAppend(editText, "Preformat")
//        toolbar.onMenuItemClick(menuPreformat)
//        editText.text.insert(0, "inserted")
//        Assert.assertEquals("<pre>insertedPreformat</pre>", editText.toHtml())
//        Assert.assertEquals(AztecTextFormat.FORMAT_PREFORMAT, toolbar.getSelectedHeadingMenuItem())
//    }

    @Test
    @Throws(Exception::class)
    fun appendTextToHeading() {
        editText.fromHtml("<h1 foo=\"bar\">Heading 1</h1>")
        editText.append("inserted")
        Assert.assertEquals("<h1 foo=\"bar\">Heading 1inserted</h1>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun appendTextToPreformat() {
        editText.fromHtml("<pre foo=\"bar\">Preformat</pre>")
        safeAppend(editText, "inserted")
        Assert.assertEquals("<pre foo=\"bar\">Preformatinserted</pre>", editText.toHtml())
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
    fun closePreformatWithNewline() {
        editText.fromHtml("<pre foo=\"bar\">Preformat</pre>")
        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        safeAppend(editText, "not preformat")
        Assert.assertEquals("<pre foo=\"bar\">Preformat</pre>not preformat", editText.toHtml())
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
    fun splitMultilineHeadingWithParagraph() {
        editText.fromHtml("<h2 foo=\"bar\">First<br>Second<br>Third</h2>")
        editText.setSelection(editText.text.indexOf("ond"))
        toolbar.onMenuItemClick(menuParagraph)
        Assert.assertEquals("<h2 foo=\"bar\">First</h2>Second<h2 foo=\"bar\">Third</h2>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun splitPreformatWithNewline() {
        editText.fromHtml("<pre foo=\"bar\">Preformat</pre>")
        editText.text.insert(3, "\n")
        Assert.assertEquals("<pre foo=\"bar\">Pre<br>format</pre>", editText.toHtml())
        editText.text.insert(6, "\n")
        Assert.assertEquals("<pre foo=\"bar\">Pre<br>fo<br>rmat</pre>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun splitTwoHeadingsWithNewline() {
        // cursor position right before Heading 2
        editText.fromHtml("<h1 foo=\"bar\">Heading 1</h1><h2>Heading 2</h2>")
        val mark = editText.text.indexOf("Heading 2")
        editText.text.insert(mark, "\n")
        Assert.assertEquals("<h1 foo=\"bar\">Heading 1</h1><br><h2>Heading 2</h2>", editText.toHtml())

        // alternative cursor position right after Heading 1
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
        Assert.assertEquals(AztecTextFormat.FORMAT_HEADING_2, toolbar.getSelectedHeadingMenuItem())
    }

//    TODO: Uncomment when Preformat is to be added back as a feature
//    @Test
//    @Throws(Exception::class)
//    fun changeHeadingToPreformatOfSingleLine() {
//        editText.fromHtml("<h1 foo=\"bar\">Text</h1>")
//        toolbar.onMenuItemClick(menuHeading1)
//        toolbar.onMenuItemClick(menuPreformat)
//        Assert.assertEquals("<pre foo=\"bar\">Text</pre>", editText.toHtml())
//        Assert.assertEquals(AztecTextFormat.FORMAT_PREFORMAT, toolbar.getSelectedHeadingMenuItem())
//    }

    @Test
    @Throws(Exception::class)
    fun changeHeadingOfSelectedMultilineText() {
        editText.fromHtml("<h1 foo=\"bar\">Heading 1</h1><h2>Heading 2</h2>")
        editText.setSelection(0, safeLength(editText))
        toolbar.onMenuItemClick(menuHeading2)
        Assert.assertEquals("<h2 foo=\"bar\">Heading 1</h2><h2>Heading 2</h2>", editText.toHtml())
        Assert.assertEquals(AztecTextFormat.FORMAT_HEADING_2, toolbar.getSelectedHeadingMenuItem())
    }

//    TODO: Uncomment when Preformat is to be added back as a feature
//    @Test
//    @Throws(Exception::class)
//    fun changeHeadingToParagraphToPreformatOfSelectedMultilineText() {
//        editText.fromHtml("<h1 foo=\"bar\">Heading 1</h1><pre>Preformat</pre>")
//        editText.setSelection(0, safeLength(editText))
//        toolbar.onMenuItemClick(menuParagraph)
//        Assert.assertEquals("Heading 1<br>Preformat", editText.toHtml())
//        Assert.assertEquals(AztecTextFormat.FORMAT_PARAGRAPH, toolbar.getSelectedHeadingMenuItem())
//        toolbar.onMenuItemClick(menuPreformat)
//        Assert.assertEquals("<pre>Heading 1<br>Preformat</pre>", editText.toHtml())
//        Assert.assertEquals(AztecTextFormat.FORMAT_PREFORMAT, toolbar.getSelectedHeadingMenuItem())
//    }

    @Test
    @Throws(Exception::class)
    fun applyHeadingToTextInsideList() {
        editText.fromHtml("<ol><li>Item 1</li><li>Item 2</li></ol>")
        editText.setSelection(0)
        toolbar.onMenuItemClick(menuHeading1)
        Assert.assertEquals("<ol><li><h1>Item 1</h1></li><li>Item 2</li></ol>", editText.toHtml())
        Assert.assertEquals(AztecTextFormat.FORMAT_HEADING_1, toolbar.getSelectedHeadingMenuItem())
    }

    @Test
    @Throws(Exception::class)
    fun applyHeadingToTextInsideQuote() {
        editText.fromHtml("<blockquote>Quote</blockquote>")
        toolbar.onMenuItemClick(menuHeading1)
        Assert.assertEquals("<blockquote><h1>Quote</h1></blockquote>", editText.toHtml())
        Assert.assertEquals(AztecTextFormat.FORMAT_HEADING_1, toolbar.getSelectedHeadingMenuItem())
    }

//    TODO: Uncomment when Preformat is to be added back as a feature
//    @Test
//    @Throws(Exception::class)
//    fun applyPreformatToTextInsideQuote() {
//        editText.fromHtml("<blockquote>Quote</blockquote>")
//        toolbar.onMenuItemClick(menuPreformat)
//        Assert.assertEquals("<blockquote><pre>Quote</pre></blockquote>", editText.toHtml())
//        Assert.assertEquals(AztecTextFormat.FORMAT_PARAGRAPH, toolbar.getSelectedHeadingMenuItem())
//    }

    @Test
    @Throws(Exception::class)
    fun applyQuoteToHeading() {
        editText.fromHtml("<h1 foo=\"bar\">Quote</h1>")
        editText.toggleFormatting(AztecTextFormat.FORMAT_QUOTE)
        Assert.assertEquals("<blockquote><h1 foo=\"bar\">Quote</h1></blockquote>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun applyQuoteToPreformat() {
        editText.fromHtml("<pre>Quote</pre>")
        buttonQuote.performClick()
        Assert.assertEquals("<blockquote><pre>Quote</pre></blockquote>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun applyHeadingToQuote() {
        editText.fromHtml("<blockquote>Quote</blockquote>")
        toolbar.onMenuItemClick(menuHeading1)
        Assert.assertEquals("<blockquote><h1>Quote</h1></blockquote>", editText.toHtml())
        Assert.assertEquals(AztecTextFormat.FORMAT_HEADING_1, toolbar.getSelectedHeadingMenuItem())
    }

//    TODO: Uncomment when Preformat is to be added back as a feature
//    @Test
//    @Throws(Exception::class)
//    fun applyPreformatToQuote() {
//        editText.fromHtml("<blockquote>Quote</blockquote>")
//        toolbar.onMenuItemClick(menuPreformat)
//        Assert.assertEquals("<blockquote><pre>Quote</pre></blockquote>", editText.toHtml())
//        Assert.assertEquals(AztecTextFormat.FORMAT_PARAGRAPH, toolbar.getSelectedHeadingMenuItem())
//    }

    @Test
    @Throws(Exception::class)
    fun applyHeadingToTextSurroundedByLists() {
        editText.fromHtml("<ol><li>Ordered</li></ol>Heading 1<ol><li>Ordered</li></ol>")
        val mark = editText.text.indexOf("Heading 1") + 1
        editText.setSelection(mark)
        toolbar.onMenuItemClick(menuHeading1)
        Assert.assertEquals("<ol><li>Ordered</li></ol><h1>Heading 1</h1><ol><li>Ordered</li></ol>", editText.toHtml())
        Assert.assertEquals(AztecTextFormat.FORMAT_HEADING_1, toolbar.getSelectedHeadingMenuItem())
    }

//    TODO: Uncomment when Preformat is to be added back as a feature
//    @Test
//    @Throws(Exception::class)
//    fun applyPreformatToTextSurroundedByLists() {
//        editText.fromHtml("<ol><li>Ordered</li></ol>Preformat<ol><li>Ordered</li></ol>")
//        val mark = editText.text.indexOf("format")
//        editText.setSelection(mark)
//        toolbar.onMenuItemClick(menuPreformat)
//        Assert.assertEquals("<ol><li>Ordered</li></ol><pre>Preformat</pre><ol><li>Ordered</li></ol>", editText.toHtml())
////        TODO: Correct heading menu selection.  This is incorrect.  Preformat should be selected.
////        https://github.com/wordpress-mobile/AztecEditor-Android/issues/317
////        Assert.assertEquals(AztecTextFormat.FORMAT_PREFORMAT, toolbar.getSelectedHeadingMenuItem())
//        Assert.assertEquals(AztecTextFormat.FORMAT_PARAGRAPH, toolbar.getSelectedHeadingMenuItem())
//    }

    @Test
    @Throws(Exception::class)
    fun applyHeadingToTextSurroundedByQuotes() {
        editText.fromHtml("<blockquote>Quote</blockquote>Heading 1<blockquote>Quote</blockquote>")
        val mark = editText.text.indexOf("Heading 1") + 1
        editText.setSelection(mark)
        toolbar.onMenuItemClick(menuHeading1)
        Assert.assertEquals("<blockquote>Quote</blockquote><h1>Heading 1</h1><blockquote>Quote</blockquote>", editText.toHtml())
        Assert.assertEquals(AztecTextFormat.FORMAT_HEADING_1, toolbar.getSelectedHeadingMenuItem())
    }

//    TODO: Uncomment when Preformat is to be added back as a feature
//    @Test
//    @Throws(Exception::class)
//    fun applyPreformatToTextSurroundedByQuotes() {
//        editText.fromHtml("<blockquote>Quote</blockquote>Preformat<blockquote>Quote</blockquote>")
//        val mark = editText.text.indexOf("format")
//        editText.setSelection(mark)
//        toolbar.onMenuItemClick(menuPreformat)
//        Assert.assertEquals("<blockquote>Quote</blockquote><pre>Preformat</pre><blockquote>Quote</blockquote>", editText.toHtml())
//        Assert.assertEquals(AztecTextFormat.FORMAT_PREFORMAT, toolbar.getSelectedHeadingMenuItem())
//    }

    @Test
    @Throws(Exception::class)
    fun applyTextStyleToHeading() {
        editText.fromHtml("<h5>Heading 5</h5><h1 foo=\"bar\">Heading 1</h1><h5>Heading 5</h5>")
        editText.setSelection(editText.text.indexOf("Heading 1"), editText.text.indexOf("Heading 1") + "Heading 1".length)
        editText.toggleFormatting(AztecTextFormat.FORMAT_BOLD)
        Assert.assertEquals("<h5>Heading 5</h5><h1 foo=\"bar\"><b>Heading 1</b></h1><h5>Heading 5</h5>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun applyTextStyleToPreformat() {
        editText.fromHtml("<h5>Heading 5</h5><pre foo=\"bar\">Preformat</pre><h5>Heading 5</h5>")
        editText.setSelection(editText.text.indexOf("Preformat"), editText.text.indexOf("Preformat") + "Preformat".length)
        editText.toggleFormatting(AztecTextFormat.FORMAT_BOLD)
        Assert.assertEquals("<h5>Heading 5</h5><pre foo=\"bar\"><b>Preformat</b></pre><h5>Heading 5</h5>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun applyTextStyleToPartiallySelectedHeading() {
        editText.fromHtml("<h1 foo=\"bar\">Heading 1</h1>")
        editText.setSelection(0, 3)
        editText.toggleFormatting(AztecTextFormat.FORMAT_BOLD)
        Assert.assertEquals("<h1 foo=\"bar\"><b>Hea</b>ding 1</h1>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun applyTextStyleToPartiallySelectedPreformat() {
        editText.fromHtml("<pre foo=\"bar\">Preformat</pre>")
        editText.setSelection(0, 3)
        editText.toggleFormatting(AztecTextFormat.FORMAT_BOLD)
        Assert.assertEquals("<pre foo=\"bar\"><b>Pre</b>format</pre>", editText.toHtml())
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
    fun movePreformatUpToUnstyledLine() {
        editText.fromHtml("<b>Bold</b><pre foo=\"bar\">Preformat</pre>")
        val mark = editText.text.indexOf("Preformat")
        editText.text.delete(mark - 1, mark)
        Assert.assertEquals("<b>Bold</b>Preformat", editText.toHtml())
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
    fun moveUnstyledLineUpToPreformat() {
        editText.fromHtml("<pre>Preformat</pre>unstyled")
        val mark = editText.text.indexOf("unstyled")
        editText.text.delete(mark - 1, mark)
        Assert.assertEquals("<pre>Preformatunstyled</pre>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun updateHeadingMenuOnSelectionChange() {
        editText.fromHtml("<h1>Heading 1</h1><h2>Heading 2</h2><h3>Heading 3</h3><pre>Preformat</pre><b>Bold</b><i>Italic</i>None")
        var cursor: Int

        cursor = editText.text.indexOf("ing 1")
        editText.setSelection(cursor)
        Assert.assertEquals(AztecTextFormat.FORMAT_HEADING_1, toolbar.getSelectedHeadingMenuItem())

        cursor = editText.text.indexOf("ld")
        editText.setSelection(cursor)
        Assert.assertEquals(AztecTextFormat.FORMAT_PARAGRAPH, toolbar.getSelectedHeadingMenuItem())

        cursor = editText.text.indexOf("ing 2")
        editText.setSelection(cursor)
        Assert.assertEquals(AztecTextFormat.FORMAT_HEADING_2, toolbar.getSelectedHeadingMenuItem())

        cursor = editText.text.indexOf("lic")
        editText.setSelection(cursor)
        Assert.assertEquals(AztecTextFormat.FORMAT_PARAGRAPH, toolbar.getSelectedHeadingMenuItem())

        cursor = editText.text.indexOf("ing 3")
        editText.setSelection(cursor)
        Assert.assertEquals(AztecTextFormat.FORMAT_HEADING_3, toolbar.getSelectedHeadingMenuItem())

        cursor = editText.text.indexOf("one")
        editText.setSelection(cursor)
        Assert.assertEquals(AztecTextFormat.FORMAT_PARAGRAPH, toolbar.getSelectedHeadingMenuItem())

//        TODO: Uncomment when Preformat is to be added back as a feature
//        cursor = editText.text.indexOf("format")
//        editText.setSelection(cursor)
//        Assert.assertEquals(AztecTextFormat.FORMAT_PREFORMAT, toolbar.getSelectedHeadingMenuItem())
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
        Assert.assertEquals(AztecTextFormat.FORMAT_HEADING_2, toolbar.getSelectedHeadingMenuItem())

        safeAppend(editText, "Heading 2")
        Assert.assertEquals("<h1>Heading 1</h1><h2>Heading 2</h2>", editText.toHtml())
        Assert.assertEquals(AztecTextFormat.FORMAT_HEADING_2, toolbar.getSelectedHeadingMenuItem())
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
        Assert.assertEquals(AztecTextFormat.FORMAT_PARAGRAPH, toolbar.getSelectedHeadingMenuItem())

        toolbar.onMenuItemClick(menuHeading2)
        Assert.assertEquals("<h1>Heading 1</h1><h2>Heading 2</h2><h3>Heading 3</h3>", editText.toHtml())
        Assert.assertEquals(AztecTextFormat.FORMAT_HEADING_2, toolbar.getSelectedHeadingMenuItem())
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

    @Test
    @Throws(Exception::class)
    fun testAddingNewlinesBeforeHeading() {
        editText.fromHtml("<h1>H</h1>")

        Assert.assertEquals("H", editText.text.toString())

        editText.text.insert(0, Constants.NEWLINE_STRING)

        Assert.assertEquals("\nH", editText.text.toString())

        Assert.assertEquals("<br><h1>H</h1>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testDeletingNewlineBeforeHeading() {
        editText.fromHtml("<br><h1>H</h1>")

        editText.text.delete(0, 1)

        Assert.assertEquals("H", editText.text.toString())

        Assert.assertEquals("<h1>H</h1>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testDeletingNewlineBeforeHeadingWithInlineElementAbove() {
        editText.fromHtml("<b>bold</b><h1>H</h1>")

        editText.text.delete(3, 5)

        Assert.assertEquals("bolH", editText.text.toString())

        Assert.assertEquals("<b>bol</b>H", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testDeletingNewlineBeforeHeadingWithInlineElementAndEmptyLineAbove() {
        editText.fromHtml("<b>bold</b><br><br><h1>H</h1>")

        editText.text.delete(5, 6)

        Assert.assertEquals("bold\nH", editText.text.toString())

        Assert.assertEquals("<b>bold</b><h1>H</h1>", editText.toHtml())
    }
}
