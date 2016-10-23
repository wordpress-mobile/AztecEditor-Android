package org.wordpress.aztec

import android.app.Activity
import android.widget.ToggleButton
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.annotation.Config
import org.wordpress.aztec.source.SourceViewEditText
import org.wordpress.aztec.toolbar.AztecToolbar

/**
 * Combined test for toolbar and inline styles.
 */
@RunWith(RobolectricGradleTestRunner::class)
@Config(constants = BuildConfig::class)
class AztecToolbarTest {

    lateinit var editText: AztecText
    lateinit var sourceText: SourceViewEditText
    lateinit var toolbar: AztecToolbar

    lateinit var boldButton: ToggleButton
    lateinit var italicButton: ToggleButton
    lateinit var strikeThroughButton: ToggleButton
    lateinit var quoteButton: ToggleButton
    lateinit var bulletListButton: ToggleButton
    lateinit var numberedListButton: ToggleButton
    lateinit var linkButton: ToggleButton
    lateinit var moreButton: ToggleButton
    lateinit var pageButton: ToggleButton
    lateinit var htmlButton: ToggleButton

    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().visible().get()
        editText = AztecText(activity)
        sourceText = SourceViewEditText(activity)

        activity.setContentView(editText)
        toolbar = AztecToolbar(activity)
        toolbar.setEditor(editText, sourceText)

        boldButton = toolbar.findViewById(R.id.format_bar_button_bold) as ToggleButton
        italicButton = toolbar.findViewById(R.id.format_bar_button_italic) as ToggleButton
        strikeThroughButton = toolbar.findViewById(R.id.format_bar_button_strikethrough) as ToggleButton
        quoteButton = toolbar.findViewById(R.id.format_bar_button_quote) as ToggleButton
        bulletListButton = toolbar.findViewById(R.id.format_bar_button_ul) as ToggleButton
        numberedListButton = toolbar.findViewById(R.id.format_bar_button_ol) as ToggleButton
        linkButton = toolbar.findViewById(R.id.format_bar_button_link) as ToggleButton
        moreButton = toolbar.findViewById(R.id.format_bar_button_more) as ToggleButton
        pageButton = toolbar.findViewById(R.id.format_bar_button_more) as ToggleButton
        htmlButton = toolbar.findViewById(R.id.format_bar_button_html) as ToggleButton
    }

    /**
     * Testing initial state of the editor and a toolbar.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun initialState() {
        Assert.assertFalse(boldButton.isChecked)
        Assert.assertFalse(italicButton.isChecked)
        Assert.assertFalse(quoteButton.isChecked)
        Assert.assertFalse(bulletListButton.isChecked)
        Assert.assertFalse(numberedListButton.isChecked)
        Assert.assertFalse(linkButton.isChecked)
        Assert.assertFalse(moreButton.isChecked)
        Assert.assertFalse(pageButton.isChecked)
        Assert.assertFalse(htmlButton.isChecked)

        Assert.assertTrue(editText.isEmpty())
    }

    /**
     * Toggle bold button and type.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testBoldTyping() {
        Assert.assertFalse(boldButton.isChecked)
        boldButton.performClick()
        Assert.assertTrue(boldButton.isChecked)

        editText.append("bold")
        Assert.assertEquals("<b>bold</b>", editText.toHtml())

        boldButton.performClick()
        Assert.assertFalse(boldButton.isChecked)
    }

    /**
     * Select text and toggle bold button.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testBoldToggle() {
        Assert.assertFalse(boldButton.isChecked)

        editText.append("bold")
        editText.setSelection(0, editText.length())
        boldButton.performClick()
        Assert.assertTrue(boldButton.isChecked)
        Assert.assertEquals("<b>bold</b>", editText.toHtml())

        boldButton.performClick()
        Assert.assertFalse(boldButton.isChecked)

        Assert.assertEquals("bold", editText.toHtml())
    }

    /**
     * Toggle italic button and type.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testItalicTyping() {
        Assert.assertFalse(italicButton.isChecked)
        italicButton.performClick()
        Assert.assertTrue(italicButton.isChecked)

        editText.append("italic")
        Assert.assertEquals("<i>italic</i>", editText.toHtml())
        italicButton.performClick()
        Assert.assertFalse(italicButton.isChecked)
    }

    /**
     * Select text and toggle italic button.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testItalicToggle() {
        Assert.assertFalse(italicButton.isChecked)

        editText.append("italic")
        editText.setSelection(0, editText.length())
        italicButton.performClick()
        Assert.assertTrue(italicButton.isChecked)
        Assert.assertEquals("<i>italic</i>", editText.toHtml())

        italicButton.performClick()
        Assert.assertFalse(italicButton.isChecked)

        Assert.assertEquals("italic", editText.toHtml())
    }

    /**
     * Toggle bold Strikethrough and type.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testStrikethroughTyping() {
        Assert.assertFalse(strikeThroughButton.isChecked)
        strikeThroughButton.performClick()
        Assert.assertTrue(strikeThroughButton.isChecked)

        editText.append("strike")
        Assert.assertEquals("<del>strike</del>", editText.toHtml())

        strikeThroughButton.performClick()
        Assert.assertFalse(strikeThroughButton.isChecked)
    }

    /**
     * Select text and toggle Strikethrough button.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testStrikethroughToggle() {
        Assert.assertFalse(strikeThroughButton.isChecked)

        editText.append("strike")
        editText.setSelection(0, editText.length())
        strikeThroughButton.performClick()
        Assert.assertTrue(strikeThroughButton.isChecked)
        Assert.assertEquals("<del>strike</del>", editText.toHtml())

        strikeThroughButton.performClick()
        Assert.assertFalse(strikeThroughButton.isChecked)

        Assert.assertEquals("strike", editText.toHtml())
    }

    /**
     * Select parts of text and apply formatting to it.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testCrossStylesToggle() {

        editText.append("bold bolditalic italic strike normal")
        editText.setSelection(0, 4)

        Assert.assertFalse(boldButton.isChecked)
        boldButton.performClick()
        Assert.assertTrue(boldButton.isChecked)

        Assert.assertEquals("<b>bold</b> bolditalic italic strike normal", editText.toHtml())

        editText.setSelection(5, 15)

        Assert.assertFalse(boldButton.isChecked)
        boldButton.performClick()
        Assert.assertTrue(boldButton.isChecked)

        Assert.assertFalse(italicButton.isChecked)
        italicButton.performClick()
        Assert.assertTrue(italicButton.isChecked)

        Assert.assertEquals("<b>bold</b> <b><i>bolditalic</i></b> italic strike normal", editText.toHtml())

        editText.setSelection(16, 22)

        Assert.assertFalse(italicButton.isChecked)
        Assert.assertFalse(boldButton.isChecked)

        italicButton.performClick()
        Assert.assertTrue(italicButton.isChecked)

        editText.setSelection(23, 29)

        strikeThroughButton.performClick()
        Assert.assertTrue(strikeThroughButton.isChecked)

        Assert.assertEquals("<b>bold</b> <b><i>bolditalic</i></b> <i>italic</i> <del>strike</del> normal", editText.toHtml())
    }

    /**
     * Type while switching text formatting.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testCrossStylesTyping() {
        Assert.assertFalse(boldButton.isChecked)
        boldButton.performClick()
        Assert.assertTrue(boldButton.isChecked)

        editText.append("bold")
        Assert.assertEquals("<b>bold</b>", editText.toHtml())

        italicButton.performClick()
        Assert.assertTrue(boldButton.isChecked)
        editText.append("bolditalic")
        Assert.assertEquals("<b>bold</b><b><i>bolditalic</i></b>", editText.toHtml())

        boldButton.performClick()
        Assert.assertFalse(boldButton.isChecked)
        editText.append("italic")
        Assert.assertEquals("<b>bold</b><b><i>bolditalic</i></b><i>italic</i>", editText.toHtml())

        italicButton.performClick()
        Assert.assertFalse(italicButton.isChecked)

        strikeThroughButton.performClick()
        editText.append("strike")
        Assert.assertEquals("<b>bold</b><b><i>bolditalic</i></b><i>italic</i><del>strike</del>", editText.toHtml())

        strikeThroughButton.performClick()
        Assert.assertFalse(strikeThroughButton.isChecked)

        editText.append("normal")
        Assert.assertEquals("<b>bold</b><b><i>bolditalic</i></b><i>italic</i><del>strike</del>normal", editText.toHtml())
    }

    /**
     * Test toggle state of formatting button as we move selection to differently styled text
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testSelection() {
        editText.fromHtml("<b>bold</b><b><i>bolditalic</i></b><i>italic</i><del>strike</del>normal")

        //cursor is at bold text
        editText.setSelection(2)
        Assert.assertTrue(boldButton.isChecked)


        //cursor is at bold/italic text
        editText.setSelection(7)
        Assert.assertTrue(boldButton.isChecked)
        Assert.assertTrue(italicButton.isChecked)

        //bold text with mixed italic style is selected
        editText.setSelection(2, 7)
        Assert.assertTrue(boldButton.isChecked)
        Assert.assertFalse(italicButton.isChecked)

        //unstyled text selected
        editText.setSelection(22)
        Assert.assertFalse(boldButton.isChecked)
        Assert.assertFalse(italicButton.isChecked)
        Assert.assertTrue(strikeThroughButton.isChecked)

        //unstyled text selected
        editText.setSelection(15)
        Assert.assertFalse(boldButton.isChecked)
        Assert.assertTrue(italicButton.isChecked)

        //whole text selected
        editText.setSelection(0, editText.length() - 1)
        Assert.assertFalse(italicButton.isChecked)
        Assert.assertFalse(boldButton.isChecked)
        Assert.assertFalse(strikeThroughButton.isChecked)
    }

    /**
     * Select whole text with one common style applied to it and another style applied to part of it
     * ("di" from <b>bold</b><b><i>italic</i></b>) and extend partially applied style (italic) to other part of selection
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun extendStyle() {
        editText.fromHtml("<b>bold</b><b><i>italic</i></b>")

        val selectedText = editText.text.substring(3, 5)
        Assert.assertEquals("di", selectedText) //sanity check

        editText.setSelection(3, 5)

        Assert.assertTrue(boldButton.isChecked)
        italicButton.performClick()

        Assert.assertEquals("<b>bol</b><b><i>ditalic</i></b>", editText.toHtml())
    }

    /**
     * Select whole text with one common style applied to it and another style applied to part of it
     * ("ds" from <b>bold</b><b><del>strike</del></b>) and extend partially applied style (strikethrough) to other part of selection
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun extendStyleStrikethrough() {
        editText.fromHtml("<b>bold</b><b><del>strike</del></b>")

        val selectedText = editText.text.substring(3, 5)
        Assert.assertEquals("ds", selectedText) //sanity check

        editText.setSelection(3, 5)

        Assert.assertTrue(boldButton.isChecked)
        strikeThroughButton.performClick()

        Assert.assertEquals("<b>bol</b><b><del>dstrike</del></b>", editText.toHtml())
    }

    /**
     * Select part of text with one common style applied to it and other style applied to part of it
     * ("italic" from <b>bold</b><b><i>italic</i></b>) and remove partially applied style (italic) form it
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun removeStyleFromPartialSelection() {
        editText.fromHtml("<b>bold</b><b><i>italic</i></b>")

        val selectedText = editText.text.substring(4, editText.length())
        Assert.assertEquals("italic", selectedText) //sanity check

        editText.setSelection(4, editText.length())

        italicButton.performClick()

        Assert.assertEquals("<b>bolditalic</b>", editText.toHtml())
    }

    /**
     * Select whole text with one common style applied to it and another style applied to part of it
     * extend partial style (italic) to whole selection
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun extendStyleToWholeSelection() {
        editText.fromHtml("<b>bold</b><b><i>italic</i></b>")

        editText.setSelection(0, editText.length())

        italicButton.performClick()

        Assert.assertEquals("<b><i>bolditalic</i></b>", editText.toHtml())
    }

    /**
     * Select whole text inside editor and remove styles from it while maintaining selection.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun removeStyleFromWholeSelection() {
        editText.fromHtml("<b>bold</b><b><i>italic</i></b>")

        editText.setSelection(0, editText.length())

        boldButton.performClick()

        Assert.assertEquals("bold<i>italic</i>", editText.toHtml())

        italicButton.performClick()
        Assert.assertEquals("<i>bolditalic</i>", editText.toHtml())

        italicButton.performClick()
        Assert.assertEquals("bolditalic", editText.toHtml())
    }

    /**
     * Clear edit text and check that all buttons are not toggled.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun emptySelection() {
        editText.fromHtml("<b>bold</b><b><i>italic</i></b>")
        editText.text.clear()

        Assert.assertTrue(editText.isEmpty())

        //noting should be highlighted when we empty edit text
        Assert.assertFalse(boldButton.isChecked)
        Assert.assertFalse(italicButton.isChecked)
        Assert.assertFalse(quoteButton.isChecked)
        Assert.assertFalse(bulletListButton.isChecked)
        Assert.assertFalse(numberedListButton.isChecked)
        Assert.assertFalse(linkButton.isChecked)
        Assert.assertFalse(htmlButton.isChecked)
    }

    /**
     * Toggle bullet list button and type.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun bulletListTyping() {
        Assert.assertFalse(bulletListButton.isChecked)
        bulletListButton.performClick()
        Assert.assertTrue(bulletListButton.isChecked)

        editText.append("bullet")
        Assert.assertEquals("<ul><li>bullet</li></ul>", editText.toHtml())

        bulletListButton.performClick()
        Assert.assertFalse(boldButton.isChecked)

        Assert.assertEquals("bullet", editText.toHtml())
    }

    /**
     * Test styling inside HiddenHtmlSpan
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun stylingInsideHiddenHtmlSpan() {
        editText.fromHtml("<div class=\"third\">Div<br><span>Span</span><br>Hidden</div>")

        editText.setSelection(0, 3)
        boldButton.performClick()
        Assert.assertEquals("<div class=\"third\"><b>Div</b><br><span>Span</span><br>Hidden</div>",
                editText.toHtml())

        editText.setSelection(4, 8)
        italicButton.performClick()
        Assert.assertEquals("<div class=\"third\"><b>Div</b><br><span><i>Span</i></span><br>Hidden</div>",
                editText.toHtml())

        editText.setSelection(9, 15)
        strikeThroughButton.performClick()

        Assert.assertEquals("<div class=\"third\"><b>Div</b><br><span><i>Span</i></span><br><del>Hidden</del></div>",
                editText.toHtml())

    }

    /**
     * Test the correctness of span-to-HTML conversion after deleting a span from the editor
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun htmlAfterEditingHiddenSpan() {
        editText.fromHtml("<div class=\"third\"><b>Div</b><br><span>Span</span><br><span>Hidden</span></div><div></div>")
        editText.text.delete(4, 8)

        htmlButton.performClick()
        TestUtils.equalsIgnoreWhitespace("<div class=\"third\"><b>Div</b><br><br><span>Hidden</span></div><div></div>", sourceText.text.toString())

        editText.fromHtml("<div class=\"third\"><b>Div</b><br><span>Span</span><br><span>Hidden</span></div><div></div>")
        editText.text.delete(3, 9)

        htmlButton.performClick()
        TestUtils.equalsIgnoreWhitespace("<div class=\"third\"><b>Div</b><span>Hidden</span></div><div></div>", sourceText.text.toString())

        editText.fromHtml("<div class=\"third\"><b>Div</b><br><span>Span</span><br><span>Hidden</span></div><div></div>")
        editText.text.delete(0, editText.length())

        htmlButton.performClick()
        TestUtils.equalsIgnoreWhitespace("", sourceText.text.toString())
    }

    /**
     * Insert comment at selection when More format toolbar button is tapped.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreWithButton() {
        editText.fromHtml("")
        moreButton.performClick()
        TestUtils.equalsIgnoreWhitespace("<!--more-->", sourceText.text.toString())

        // Select location.
        editText.fromHtml("<b>Bold</b>")
        editText.setSelection(3)
        moreButton.performClick()
        TestUtils.equalsIgnoreWhitespace("<b>Bol</b><br><!--more--><br><b>d</b><br>", sourceText.text.toString())

        // Select characters.
        editText.fromHtml("<b>Bold</b>")
        editText.setSelection(2, 3)
        moreButton.performClick()
        TestUtils.equalsIgnoreWhitespace("<b>B</b><br><!--more--><br><b>d</b><br>", sourceText.text.toString())

        // Select characters across lines.
        editText.fromHtml("<b>Bold</b><br><i>Italic</i><br>")
        editText.setSelection(3, 5)
        moreButton.performClick()
        TestUtils.equalsIgnoreWhitespace("<b>Bol</b><br><!--more--><br><i>talic</i><br>", sourceText.text.toString())
    }

    /**
     * Insert comment when <!--more--> is input.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreWithCode() {
        editText.fromHtml("")
        sourceText.append("<!--more-->")
        TestUtils.equalsIgnoreWhitespace("more", editText.text.toString())
    }

    /**
     * Insert comment at selection when Page Break format toolbar button is tapped.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageWithButton() {
        editText.fromHtml("")
        pageButton.performClick()
        TestUtils.equalsIgnoreWhitespace("<!--nextpage-->", sourceText.text.toString())

        // Select location.
        editText.fromHtml("<b>Bold</b>")
        editText.setSelection(3)
        pageButton.performClick()
        TestUtils.equalsIgnoreWhitespace("<b>Bol</b><br><!--nextpage--><br><b>d</b><br>", sourceText.text.toString())

        // Select characters.
        editText.fromHtml("<b>Bold</b>")
        editText.setSelection(2, 3)
        pageButton.performClick()
        TestUtils.equalsIgnoreWhitespace("<b>B</b><br><!--nextpage--><br><b>d</b><br>", sourceText.text.toString())

        // Select characters across lines.
        editText.fromHtml("<b>Bold</b><br><i>Italic</i><br>")
        editText.setSelection(3, 5)
        pageButton.performClick()
        TestUtils.equalsIgnoreWhitespace("<b>Bol</b><br><!--nextpage--><br><i>talic</i><br>", sourceText.text.toString())
    }

    /**
     * Insert comment when <!--nextpage--> is input.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageWithCode() {
        editText.fromHtml("")
        sourceText.append("<!--nextpage-->")
        TestUtils.equalsIgnoreWhitespace("nextpage", editText.text.toString())
    }
}
