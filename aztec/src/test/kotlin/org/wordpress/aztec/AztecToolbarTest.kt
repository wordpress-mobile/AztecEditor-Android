package org.wordpress.aztec

import android.app.Activity
import android.widget.ToggleButton
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.wordpress.aztec.source.SourceViewEditText
import org.wordpress.aztec.toolbar.AztecToolbar

/**
 * Combined test for toolbar and inline styles.
 */
@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(23))
class AztecToolbarTest {

    lateinit var editText: AztecText
    lateinit var sourceText: SourceViewEditText
    lateinit var toolbar: AztecToolbar

    lateinit var boldButton: ToggleButton
    lateinit var italicButton: ToggleButton
    lateinit var strikeThroughButton: ToggleButton
    lateinit var underlineButton: ToggleButton
    lateinit var quoteButton: ToggleButton
    lateinit var linkButton: ToggleButton
    lateinit var htmlButton: ToggleButton

    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().visible().get()
        editText = AztecText(activity)
        editText.setCalypsoMode(false)
        sourceText = SourceViewEditText(activity)

        activity.setContentView(editText)
        toolbar = AztecToolbar(activity)
        toolbar.setEditor(editText, sourceText)

        boldButton = toolbar.findViewById(R.id.format_bar_button_bold) as ToggleButton
        italicButton = toolbar.findViewById(R.id.format_bar_button_italic) as ToggleButton
        strikeThroughButton = toolbar.findViewById(R.id.format_bar_button_strikethrough) as ToggleButton
        underlineButton = toolbar.findViewById(R.id.format_bar_button_underline) as ToggleButton
        quoteButton = toolbar.findViewById(R.id.format_bar_button_quote) as ToggleButton
        linkButton = toolbar.findViewById(R.id.format_bar_button_link) as ToggleButton
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
        Assert.assertFalse(strikeThroughButton.isChecked)
        Assert.assertFalse(underlineButton.isChecked)
        Assert.assertFalse(quoteButton.isChecked)
        Assert.assertFalse(linkButton.isChecked)
        Assert.assertFalse(htmlButton.isChecked)

        Assert.assertTrue(TestUtils.safeEmpty(editText))
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
     * Toggle strikethrough button and type.
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
     * Select text and toggle strikethrough button.
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
     * Toggle underline button and type.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testUnderlineTyping() {
        Assert.assertFalse(underlineButton.isChecked)
        underlineButton.performClick()
        Assert.assertTrue(underlineButton.isChecked)

        editText.append("underline")
        Assert.assertEquals("<u>underline</u>", editText.toHtml())

        underlineButton.performClick()
        Assert.assertFalse(underlineButton.isChecked)
    }

    /**
     * Select text and toggle underline button.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testUnderlineToggle() {
        Assert.assertFalse(underlineButton.isChecked)

        editText.append("underline")
        editText.setSelection(0, editText.length())
        underlineButton.performClick()
        Assert.assertTrue(underlineButton.isChecked)
        Assert.assertEquals("<u>underline</u>", editText.toHtml())

        underlineButton.performClick()
        Assert.assertFalse(underlineButton.isChecked)

        Assert.assertEquals("underline", editText.toHtml())
    }

    /**
     * Test inline style when applying and removing styles while typing.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testInlineStyleWhileTyping() {
        // Clear text
        editText.setText("")

        // Bold
        boldButton.performClick()
        editText.append("Bo")
        boldButton.performClick()
        editText.append("ld")
        Assert.assertEquals("<b>Bo</b>ld", editText.toHtml())

        // Italic
        italicButton.performClick()
        editText.append("Ita")
        italicButton.performClick()
        editText.append("lic")
        Assert.assertEquals("<b>Bo</b>ld<i>Ita</i>lic", editText.toHtml())

        // Strike
        strikeThroughButton.performClick()
        editText.append("Str")
        strikeThroughButton.performClick()
        editText.append("ike")
        Assert.assertEquals("<b>Bo</b>ld<i>Ita</i>lic<del>Str</del>ike", editText.toHtml())

        // Underline
        underlineButton.performClick()
        editText.append("Under")
        underlineButton.performClick()
        editText.append("line")
        Assert.assertEquals("<b>Bo</b>ld<i>Ita</i>lic<del>Str</del>ike<u>Under</u>line", editText.toHtml())

        // Clear text
        editText.setText("")

        // Bold
        editText.append("Bo")
        boldButton.performClick()
        editText.append("ld")
        boldButton.performClick()
        Assert.assertEquals("Bo<b>ld</b>", editText.toHtml())

        // Italic
        editText.append("Ita")
        italicButton.performClick()
        editText.append("lic")
        italicButton.performClick()
        Assert.assertEquals("Bo<b>ld</b>Ita<i>lic</i>", editText.toHtml())

        // Strike
        editText.append("Str")
        strikeThroughButton.performClick()
        editText.append("ike")
        strikeThroughButton.performClick()
        Assert.assertEquals("Bo<b>ld</b>Ita<i>lic</i>Str<del>ike</del>", editText.toHtml())

        // Underline
        editText.append("Under")
        underlineButton.performClick()
        editText.append("line")
        underlineButton.performClick()
        Assert.assertEquals("Bo<b>ld</b>Ita<i>lic</i>Str<del>ike</del>Under<u>line</u>", editText.toHtml())
    }

    /**
     * Test inline style when applying and removing styles while typing with spaces.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testInlineStyleWhileTypingWithSpaces() {
        // Space
        editText.setText(" ")

        // Bold
        boldButton.performClick()
        editText.append("Bo")
        boldButton.performClick()
        editText.append("ld")
        Assert.assertEquals(" <b>Bo</b>ld", editText.toHtml())

        // Space
        editText.append(" ")

        // Italic
        italicButton.performClick()
        editText.append("Ita")
        italicButton.performClick()
        editText.append("lic")
        Assert.assertEquals(" <b>Bo</b>ld <i>Ita</i>lic", editText.toHtml())

        // Space
        editText.append(" ")

        // Strike
        strikeThroughButton.performClick()
        editText.append("Str")
        strikeThroughButton.performClick()
        editText.append("ike")
        Assert.assertEquals(" <b>Bo</b>ld <i>Ita</i>lic <del>Str</del>ike", editText.toHtml())

        // Space
        editText.append(" ")

        // Underline
        underlineButton.performClick()
        editText.append("Under")
        underlineButton.performClick()
        editText.append("line")
        Assert.assertEquals(" <b>Bo</b>ld <i>Ita</i>lic <del>Str</del>ike <u>Under</u>line", editText.toHtml())
    }

    /**
     * Select parts of text and apply formatting to it.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testCrossStylesToggle() {

        editText.append("bold bolditalic italic strike underline normal")
        editText.setSelection(0, 4)

        Assert.assertFalse(boldButton.isChecked)
        boldButton.performClick()
        Assert.assertTrue(boldButton.isChecked)

        Assert.assertEquals("<b>bold</b> bolditalic italic strike underline normal", editText.toHtml())

        editText.setSelection(5, 15)

        Assert.assertFalse(boldButton.isChecked)
        boldButton.performClick()
        Assert.assertTrue(boldButton.isChecked)

        Assert.assertFalse(italicButton.isChecked)
        italicButton.performClick()
        Assert.assertTrue(italicButton.isChecked)

        Assert.assertEquals("<b>bold</b> <b><i>bolditalic</i></b> italic strike underline normal", editText.toHtml())

        editText.setSelection(16, 22)

        Assert.assertFalse(italicButton.isChecked)
        Assert.assertFalse(boldButton.isChecked)

        italicButton.performClick()
        Assert.assertTrue(italicButton.isChecked)

        editText.setSelection(23, 29)

        strikeThroughButton.performClick()
        Assert.assertTrue(strikeThroughButton.isChecked)

        Assert.assertEquals("<b>bold</b> <b><i>bolditalic</i></b> <i>italic</i> <del>strike</del> underline normal", editText.toHtml())

        editText.setSelection(30, 39)

        underlineButton.performClick()
        Assert.assertTrue(underlineButton.isChecked)

        Assert.assertEquals("<b>bold</b> <b><i>bolditalic</i></b> <i>italic</i> <del>strike</del> <u>underline</u> normal", editText.toHtml())
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
        Assert.assertTrue(strikeThroughButton.isChecked)
        editText.append("strike")
        Assert.assertEquals("<b>bold</b><b><i>bolditalic</i></b><i>italic</i><del>strike</del>", editText.toHtml())
        strikeThroughButton.performClick()
        Assert.assertFalse(strikeThroughButton.isChecked)

        underlineButton.performClick()
        Assert.assertTrue(underlineButton.isChecked)
        editText.append("underline")
        Assert.assertEquals("<b>bold</b><b><i>bolditalic</i></b><i>italic</i><del>strike</del><u>underline</u>", editText.toHtml())
        underlineButton.performClick()
        Assert.assertFalse(underlineButton.isChecked)

        editText.append("normal")
        Assert.assertEquals("<b>bold</b><b><i>bolditalic</i></b><i>italic</i><del>strike</del><u>underline</u>normal", editText.toHtml())
    }

    /**
     * Test toggle state of formatting button as selection moves to differently styled text.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testSelection() {
        editText.fromHtml("<b>bold</b><b><i>bolditalic</i></b><i>italic</i><del>strike</del><u>underline</u>normal")

        //cursor is at bold text
        editText.setSelection(2)
        Assert.assertTrue(boldButton.isChecked)
        Assert.assertFalse(italicButton.isChecked)
        Assert.assertFalse(strikeThroughButton.isChecked)
        Assert.assertFalse(underlineButton.isChecked)

        //cursor is at bold/italic text
        editText.setSelection(7)
        Assert.assertTrue(boldButton.isChecked)
        Assert.assertTrue(italicButton.isChecked)
        Assert.assertFalse(underlineButton.isChecked)
        Assert.assertFalse(strikeThroughButton.isChecked)

        //bold and bold/italic styles selected
        editText.setSelection(2, 7)
        Assert.assertTrue(boldButton.isChecked)
        Assert.assertFalse(italicButton.isChecked)
        Assert.assertFalse(underlineButton.isChecked)
        Assert.assertFalse(strikeThroughButton.isChecked)

        //cursor is at italic text
        editText.setSelection(15)
        Assert.assertFalse(boldButton.isChecked)
        Assert.assertTrue(italicButton.isChecked)
        Assert.assertFalse(underlineButton.isChecked)
        Assert.assertFalse(strikeThroughButton.isChecked)

        //cursor is at strikethrough text
        editText.setSelection(22)
        Assert.assertFalse(boldButton.isChecked)
        Assert.assertFalse(italicButton.isChecked)
        Assert.assertTrue(strikeThroughButton.isChecked)
        Assert.assertFalse(underlineButton.isChecked)

        //cursor is at underline text
        editText.setSelection(30)
        Assert.assertFalse(boldButton.isChecked)
        Assert.assertFalse(italicButton.isChecked)
        Assert.assertFalse(strikeThroughButton.isChecked)
        Assert.assertTrue(underlineButton.isChecked)

        //cursor is at unstyled text
        editText.setSelection(38)
        Assert.assertFalse(boldButton.isChecked)
        Assert.assertFalse(italicButton.isChecked)
        Assert.assertFalse(strikeThroughButton.isChecked)
        Assert.assertFalse(underlineButton.isChecked)

        //whole text selected
        editText.setSelection(0, editText.length() - 1)
        Assert.assertFalse(boldButton.isChecked)
        Assert.assertFalse(italicButton.isChecked)
        Assert.assertFalse(strikeThroughButton.isChecked)
        Assert.assertFalse(underlineButton.isChecked)
    }

    /**
     * Select part of text with one common style (bold) applied to it and another style (italic)
     * applied to part of it ("di" from <b>bold</b><b><i>italic</i></b>) and extend partially
     * applied style (italic) to other part of selection.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun extendStyleItalicPartialSelection() {
        editText.fromHtml("<b>bold</b><b><i>italic</i></b>")

        val selectedText = editText.text.substring(3, 5)
        Assert.assertEquals("di", selectedText) //sanity check

        editText.setSelection(3, 5)
        Assert.assertTrue(boldButton.isChecked)
        Assert.assertFalse(italicButton.isChecked)

        italicButton.performClick()
        Assert.assertEquals("<b>bol</b><b><i>ditalic</i></b>", editText.toHtml())
    }

    /**
     * Select part of text with one common style applied to it (bold) and another style (strikethrough)
     * applied to part of it ("ds" from <b>bold</b><b><del>strike</del></b>) and extend partially
     * applied style (strikethrough) to other part of selection.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun extendStyleStrikethroughPartialSelection() {
        editText.fromHtml("<b>bold</b><b><del>strike</del></b>")

        val selectedText = editText.text.substring(3, 5)
        Assert.assertEquals("ds", selectedText) //sanity check

        editText.setSelection(3, 5)
        Assert.assertTrue(boldButton.isChecked)
        Assert.assertFalse(strikeThroughButton.isChecked)

        strikeThroughButton.performClick()
        Assert.assertEquals("<b>bol</b><b><del>dstrike</del></b>", editText.toHtml())
    }

    /**
     * Select part of text with one common style applied (bold) to it and other style (italic)
     * applied to part of it ("italic" from <b>bold</b><b><i>italic</i></b>) and extend partially
     * applied style (italic) to other part of selection.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun extendStyleFromPartialSelection() {
        editText.fromHtml("<b>bold</b><b><i>italic</i></b>")

        val selectedText = editText.text.substring(4, editText.length())
        Assert.assertEquals("italic", selectedText) //sanity check

        editText.setSelection(4, editText.length())

        italicButton.performClick()

        Assert.assertEquals("<b>bolditalic</b>", editText.toHtml())
    }

    /**
     * Select whole text with one common style (bold) applied to it and another style (italic)
     * applied to part of it and extend partial style (italic) to whole selection.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun extendStyleFromWholeSelection() {
        editText.fromHtml("<b>bold</b><b><i>italic</i></b>")

        editText.setSelection(0, editText.length())

        italicButton.performClick()
        Assert.assertEquals("<b><i>bolditalic</i></b>", editText.toHtml())
    }

    /**
     * Select whole text inside editor and remove/add styles while maintaining selection.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun removeAndApplyStyleFromWholeSelection() {
        editText.fromHtml("<b>bold</b><b><i>italic</i></b>")

        editText.setSelection(0, editText.length())

        boldButton.performClick()
        Assert.assertEquals("bold<i>italic</i>", editText.toHtml())

        italicButton.performClick()
        Assert.assertEquals("<i>bolditalic</i>", editText.toHtml())

        italicButton.performClick()
        Assert.assertEquals("bolditalic", editText.toHtml())

        boldButton.performClick()
        Assert.assertEquals("<b>bolditalic</b>", editText.toHtml())
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
        editText.setText("")

        Assert.assertTrue(TestUtils.safeEmpty(editText))

        //noting should be highlighted when we empty edit text
        Assert.assertFalse(boldButton.isChecked)
        Assert.assertFalse(italicButton.isChecked)
        Assert.assertFalse(strikeThroughButton.isChecked)
        Assert.assertFalse(underlineButton.isChecked)
        Assert.assertFalse(quoteButton.isChecked)
        Assert.assertFalse(linkButton.isChecked)
        Assert.assertFalse(htmlButton.isChecked)
    }

    /**
     * Test styling inside HiddenHtmlSpan.
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
     * Test the correctness of span-to-HTML conversion after deleting a span from the editor.
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
}
