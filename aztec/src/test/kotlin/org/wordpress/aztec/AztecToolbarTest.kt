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
@Config(sdk = intArrayOf(23))
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
    lateinit var alignLeftButton: ToggleButton
    lateinit var alignCenterButton: ToggleButton
    lateinit var alignRightButton: ToggleButton

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

        boldButton = toolbar.findViewById(R.id.format_bar_button_bold)
        italicButton = toolbar.findViewById(R.id.format_bar_button_italic)
        strikeThroughButton = toolbar.findViewById(R.id.format_bar_button_strikethrough)
        underlineButton = toolbar.findViewById(R.id.format_bar_button_underline)
        quoteButton = toolbar.findViewById(R.id.format_bar_button_quote)
        linkButton = toolbar.findViewById(R.id.format_bar_button_link)
        htmlButton = toolbar.findViewById(R.id.format_bar_button_html)
        alignLeftButton = toolbar.findViewById(R.id.format_bar_button_align_left)
        alignCenterButton = toolbar.findViewById(R.id.format_bar_button_align_center)
        alignRightButton = toolbar.findViewById(R.id.format_bar_button_align_right)

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
        Assert.assertFalse(alignLeftButton.isChecked)
        Assert.assertFalse(alignCenterButton.isChecked)
        Assert.assertFalse(alignRightButton.isChecked)

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
        Assert.assertEquals("<strong>bold</strong>", editText.toHtml())

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
        Assert.assertEquals("<strong>bold</strong>", editText.toHtml())

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
        Assert.assertEquals("<em>italic</em>", editText.toHtml())
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
        Assert.assertEquals("<em>italic</em>", editText.toHtml())

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
        Assert.assertEquals("<s>strike</s>", editText.toHtml())

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
        Assert.assertEquals("<s>strike</s>", editText.toHtml())

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
        Assert.assertEquals("<strong>Bo</strong>ld", editText.toHtml())

        // Italic
        italicButton.performClick()
        editText.append("Ita")
        italicButton.performClick()
        editText.append("lic")
        Assert.assertEquals("<strong>Bo</strong>ld<em>Ita</em>lic", editText.toHtml())

        // Strike
        strikeThroughButton.performClick()
        editText.append("Str")
        strikeThroughButton.performClick()
        editText.append("ike")
        Assert.assertEquals("<strong>Bo</strong>ld<em>Ita</em>lic<s>Str</s>ike", editText.toHtml())

        // Underline
        underlineButton.performClick()
        editText.append("Under")
        underlineButton.performClick()
        editText.append("line")
        Assert.assertEquals("<strong>Bo</strong>ld<em>Ita</em>lic<s>Str</s>ike<u>Under</u>line", editText.toHtml())

        // Clear text
        editText.setText("")

        // Bold
        editText.append("Bo")
        boldButton.performClick()
        editText.append("ld")
        boldButton.performClick()
        Assert.assertEquals("Bo<strong>ld</strong>", editText.toHtml())

        // Italic
        editText.append("Ita")
        italicButton.performClick()
        editText.append("lic")
        italicButton.performClick()
        Assert.assertEquals("Bo<strong>ld</strong>Ita<em>lic</em>", editText.toHtml())

        // Strike
        editText.append("Str")
        strikeThroughButton.performClick()
        editText.append("ike")
        strikeThroughButton.performClick()
        Assert.assertEquals("Bo<strong>ld</strong>Ita<em>lic</em>Str<s>ike</s>", editText.toHtml())

        // Underline
        editText.append("Under")
        underlineButton.performClick()
        editText.append("line")
        underlineButton.performClick()
        Assert.assertEquals("Bo<strong>ld</strong>Ita<em>lic</em>Str<s>ike</s>Under<u>line</u>", editText.toHtml())
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
        Assert.assertEquals(" <strong>Bo</strong>ld", editText.toHtml())

        // Space
        editText.append(" ")

        // Italic
        italicButton.performClick()
        editText.append("Ita")
        italicButton.performClick()
        editText.append("lic")
        Assert.assertEquals(" <strong>Bo</strong>ld <em>Ita</em>lic", editText.toHtml())

        // Space
        editText.append(" ")

        // Strike
        strikeThroughButton.performClick()
        editText.append("Str")
        strikeThroughButton.performClick()
        editText.append("ike")
        Assert.assertEquals(" <strong>Bo</strong>ld <em>Ita</em>lic <s>Str</s>ike", editText.toHtml())

        // Space
        editText.append(" ")

        // Underline
        underlineButton.performClick()
        editText.append("Under")
        underlineButton.performClick()
        editText.append("line")
        Assert.assertEquals(" <strong>Bo</strong>ld <em>Ita</em>lic <s>Str</s>ike <u>Under</u>line", editText.toHtml())
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

        Assert.assertEquals("<strong>bold</strong> bolditalic italic strike underline normal", editText.toHtml())

        editText.setSelection(5, 15)

        Assert.assertFalse(boldButton.isChecked)
        boldButton.performClick()
        Assert.assertTrue(boldButton.isChecked)

        Assert.assertFalse(italicButton.isChecked)
        italicButton.performClick()
        Assert.assertTrue(italicButton.isChecked)

        Assert.assertEquals("<strong>bold</strong> <strong><em>bolditalic</em></strong> italic strike underline normal", editText.toHtml())

        editText.setSelection(16, 22)

        Assert.assertFalse(italicButton.isChecked)
        Assert.assertFalse(boldButton.isChecked)

        italicButton.performClick()
        Assert.assertTrue(italicButton.isChecked)

        editText.setSelection(23, 29)

        strikeThroughButton.performClick()
        Assert.assertTrue(strikeThroughButton.isChecked)

        Assert.assertEquals("<strong>bold</strong> <strong><em>bolditalic</em></strong> <em>italic</em> <s>strike</s> underline normal", editText.toHtml())

        editText.setSelection(30, 39)

        underlineButton.performClick()
        Assert.assertTrue(underlineButton.isChecked)

        Assert.assertEquals("<strong>bold</strong> <strong><em>bolditalic</em></strong> <em>italic</em> <s>strike</s> <u>underline</u> normal", editText.toHtml())
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
        Assert.assertEquals("<strong>bold</strong>", editText.toHtml())

        italicButton.performClick()
        Assert.assertTrue(boldButton.isChecked)
        editText.append("bolditalic")
        Assert.assertEquals("<strong>bold</strong><strong><em>bolditalic</em></strong>", editText.toHtml())
        boldButton.performClick()
        Assert.assertFalse(boldButton.isChecked)

        editText.append("italic")
        Assert.assertEquals("<strong>bold</strong><strong><em>bolditalic</em></strong><em>italic</em>", editText.toHtml())
        italicButton.performClick()
        Assert.assertFalse(italicButton.isChecked)

        strikeThroughButton.performClick()
        Assert.assertTrue(strikeThroughButton.isChecked)
        editText.append("strike")
        Assert.assertEquals("<strong>bold</strong><strong><em>bolditalic</em></strong><em>italic</em><s>strike</s>", editText.toHtml())
        strikeThroughButton.performClick()
        Assert.assertFalse(strikeThroughButton.isChecked)

        underlineButton.performClick()
        Assert.assertTrue(underlineButton.isChecked)
        editText.append("underline")
        Assert.assertEquals("<strong>bold</strong><strong><em>bolditalic</em></strong><em>italic</em><s>strike</s><u>underline</u>", editText.toHtml())
        underlineButton.performClick()
        Assert.assertFalse(underlineButton.isChecked)

        editText.append("normal")
        Assert.assertEquals("<strong>bold</strong><strong><em>bolditalic</em></strong><em>italic</em><s>strike</s><u>underline</u>normal", editText.toHtml())
    }

    /**
     * Test toggle state of formatting button as selection moves to differently styled text.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun testSelection() {
        editText.fromHtml("<b>bold</b><b><i>bolditalic</i></b><i>italic</i><s>strike</s><u>underline</u>normal")

        // cursor is at bold text
        editText.setSelection(2)
        Assert.assertTrue(boldButton.isChecked)
        Assert.assertFalse(italicButton.isChecked)
        Assert.assertFalse(strikeThroughButton.isChecked)
        Assert.assertFalse(underlineButton.isChecked)

        // cursor is at bold/italic text
        editText.setSelection(7)
        Assert.assertTrue(boldButton.isChecked)
        Assert.assertTrue(italicButton.isChecked)
        Assert.assertFalse(underlineButton.isChecked)
        Assert.assertFalse(strikeThroughButton.isChecked)

        // bold and bold/italic styles selected
        editText.setSelection(2, 7)
        Assert.assertTrue(boldButton.isChecked)
        Assert.assertFalse(italicButton.isChecked)
        Assert.assertFalse(underlineButton.isChecked)
        Assert.assertFalse(strikeThroughButton.isChecked)

        // cursor is at italic text
        editText.setSelection(15)
        Assert.assertFalse(boldButton.isChecked)
        Assert.assertTrue(italicButton.isChecked)
        Assert.assertFalse(underlineButton.isChecked)
        Assert.assertFalse(strikeThroughButton.isChecked)

        // cursor is at strikethrough text
        editText.setSelection(22)
        Assert.assertFalse(boldButton.isChecked)
        Assert.assertFalse(italicButton.isChecked)
        Assert.assertTrue(strikeThroughButton.isChecked)
        Assert.assertFalse(underlineButton.isChecked)

        // cursor is at underline text
        editText.setSelection(30)
        Assert.assertFalse(boldButton.isChecked)
        Assert.assertFalse(italicButton.isChecked)
        Assert.assertFalse(strikeThroughButton.isChecked)
        Assert.assertTrue(underlineButton.isChecked)

        // cursor is at unstyled text
        editText.setSelection(38)
        Assert.assertFalse(boldButton.isChecked)
        Assert.assertFalse(italicButton.isChecked)
        Assert.assertFalse(strikeThroughButton.isChecked)
        Assert.assertFalse(underlineButton.isChecked)

        // whole text selected
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
        Assert.assertEquals("di", selectedText) // sanity check

        editText.setSelection(3, 5)
        Assert.assertTrue(boldButton.isChecked)
        Assert.assertFalse(italicButton.isChecked)

        italicButton.performClick()
        Assert.assertEquals("<b>bol</b><b><i>ditalic</i></b>", editText.toHtml())
    }

    /**
     * Select part of text with one common style applied to it (bold) and another style (strikethrough)
     * applied to part of it ("ds" from <b>bold</b><b><s>strike</s></b>) and extend partially
     * applied style (strikethrough) to other part of selection.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun extendStyleStrikethroughPartialSelection() {
        editText.fromHtml("<b>bold</b><b><s>strike</s></b>")

        val selectedText = editText.text.substring(3, 5)
        Assert.assertEquals("ds", selectedText) // sanity check

        editText.setSelection(3, 5)
        Assert.assertTrue(boldButton.isChecked)
        Assert.assertFalse(strikeThroughButton.isChecked)

        strikeThroughButton.performClick()
        Assert.assertEquals("<b>bol</b><b><s>dstrike</s></b>", editText.toHtml())
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
        Assert.assertEquals("italic", selectedText) // sanity check

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
        editText.fromHtml("<b>bold</b><b><em>italic</em></b>")

        editText.setSelection(0, editText.length())

        italicButton.performClick()
        Assert.assertEquals("<b><em>bolditalic</em></b>", editText.toHtml())
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
        Assert.assertEquals("<em>bolditalic</em>", editText.toHtml())

        italicButton.performClick()
        Assert.assertEquals("bolditalic", editText.toHtml())

        boldButton.performClick()
        Assert.assertEquals("<strong>bolditalic</strong>", editText.toHtml())
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
        editText.fromHtml("")

        Assert.assertTrue(TestUtils.safeEmpty(editText))

        // noting should be highlighted when we empty edit text
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
        Assert.assertEquals("<div class=\"third\"><strong>Div</strong><br><span>Span</span><br>Hidden</div>",
                editText.toHtml())

        editText.setSelection(4, 8)
        italicButton.performClick()
        Assert.assertEquals("<div class=\"third\"><strong>Div</strong><br><span><em>Span</em></span><br>Hidden</div>",
                editText.toHtml())

        editText.setSelection(9, 15)
        strikeThroughButton.performClick()

        Assert.assertEquals("<div class=\"third\"><strong>Div</strong><br><span><em>Span</em></span><br><s>Hidden</s></div>",
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

    @Test
    @Throws(Exception::class)
    fun quoteSingleSelectionHighlight() {
        // 1\n2\n3\n4
        editText.fromHtml("1<blockquote>2<br>3</blockquote>4")

        editText.setSelection(0)
        Assert.assertFalse(quoteButton.isChecked)

        editText.setSelection(1)
        Assert.assertFalse(quoteButton.isChecked)

        editText.setSelection(2)
        Assert.assertTrue(quoteButton.isChecked)

        editText.setSelection(3)
        Assert.assertTrue(quoteButton.isChecked)

        editText.setSelection(4)
        Assert.assertTrue(quoteButton.isChecked)

        editText.setSelection(5)
        Assert.assertTrue(quoteButton.isChecked)

        editText.setSelection(6)
        Assert.assertFalse(quoteButton.isChecked)

        editText.setSelection(7)
        Assert.assertFalse(quoteButton.isChecked)
    }

    @Test
    @Throws(Exception::class)
    fun quoteMultiSelectionHighlight() {
        // 1\n2\n3\n4
        editText.fromHtml("1<blockquote>2<br>3</blockquote>4")

        // selected 1
        editText.setSelection(0, 1)
        Assert.assertFalse(quoteButton.isChecked)

        // selected 1\n
        editText.setSelection(0, 2)
        Assert.assertFalse(quoteButton.isChecked)

        // selected 1\n2
        editText.setSelection(0, 3)
        Assert.assertTrue(quoteButton.isChecked)

        // selected 1\n2\n
        editText.setSelection(0, 4)
        Assert.assertTrue(quoteButton.isChecked)

        // selected 1\n2\n3\n4
        editText.setSelection(0, 7)
        Assert.assertTrue(quoteButton.isChecked)

        // selected \n
        editText.setSelection(1, 2)
        Assert.assertFalse(quoteButton.isChecked)

        // selected \n2
        editText.setSelection(1, 3)
        Assert.assertTrue(quoteButton.isChecked)

        // selected 2
        editText.setSelection(2, 3)
        Assert.assertTrue(quoteButton.isChecked)

        // selected \n
        editText.setSelection(3, 4)
        Assert.assertTrue(quoteButton.isChecked)

        // selected \n3
        editText.setSelection(3, 5)
        Assert.assertTrue(quoteButton.isChecked)

        // selected 3\n
        editText.setSelection(4, 6)
        Assert.assertTrue(quoteButton.isChecked)

        // selected \n4
        editText.setSelection(5, 7)
        Assert.assertTrue(quoteButton.isChecked)
    }

    @Test
    @Throws(Exception::class)
    fun unformattedTextAlignment() {
        editText.fromHtml("Hello, this is some unformatted text.")

        editText.setSelection(3)
        alignRightButton.performClick()
        Assert.assertEquals("<p style=\"text-align:right;\">Hello, this is some unformatted text.</p>",
                editText.toHtml())

        alignRightButton.performClick()
        Assert.assertEquals("<p>Hello, this is some unformatted text.</p>", editText.toHtml())

        alignLeftButton.performClick()
        Assert.assertEquals("<p style=\"text-align:left;\">Hello, this is some unformatted text.</p>",
                editText.toHtml())

        alignCenterButton.performClick()
        Assert.assertEquals("<p style=\"text-align:center;\">Hello, this is some unformatted text.</p>",
                editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun unformattedSeparateLineAlignment() {
        editText.fromHtml("Hello, this is some unformatted text.<br>Another line<br>Third line")

        editText.setSelection(3)
        alignRightButton.performClick()
        Assert.assertEquals("<p style=\"text-align:right;\">Hello, this is some unformatted text.</p>" +
                "Another line<br>Third line",
                editText.toHtml())

        editText.setSelection(editText.text.indexOf("Third"))
        alignCenterButton.performClick()
        Assert.assertEquals("<p style=\"text-align:right;\">Hello, this is some unformatted text.</p>" +
                "Another line<p style=\"text-align:center;\">Third line</p>",
                editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun inlineStyleAlignment() {
        editText.fromHtml("<b>bold</b><br><i>italic</i><br><u>underline</u>")

        editText.setSelection(editText.text.indexOf("bold"))
        alignRightButton.performClick()
        Assert.assertEquals("<p style=\"text-align:right;\"><b>bold</b></p><i>italic</i><br><u>underline</u>",
                editText.toHtml())

        editText.setSelection(editText.text.indexOf("italic"))
        alignCenterButton.performClick()
        Assert.assertEquals("<p style=\"text-align:right;\"><b>bold</b></p>" +
                "<p style=\"text-align:center;\"><i>italic</i></p><u>underline</u>",
                editText.toHtml())

        editText.setSelection(editText.text.indexOf("underline"))
        alignLeftButton.performClick()
        Assert.assertEquals("<p style=\"text-align:right;\"><b>bold</b></p>" +
                "<p style=\"text-align:center;\"><i>italic</i></p>" +
                "<p style=\"text-align:left;\"><u>underline</u></p>",
                editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun multiselectInlineStyleAlignment() {
        editText.fromHtml("<b>bold</b><br><i>italic</i><br><u>underline</u>")

        editText.setSelection(2, editText.length() - 2)
        alignRightButton.performClick()
        Assert.assertEquals("<p style=\"text-align:right;\"><b>bold</b><br><i>italic</i><br><u>underline</u></p>",
                editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun beginningOfHeadingAlignment() {
        editText.fromHtml("<h1>Heading 1</h1><h2>Heading 2</h2><h3>Heading 3</h3>")

        editText.setSelection(editText.text.indexOf("Heading 2"))
        alignRightButton.performClick()
        Assert.assertEquals("<h1>Heading 1</h1><h2 style=\"text-align:right;\">Heading 2</h2><h3>Heading 3</h3>",
                editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun endOfHeadingAlignment() {
        editText.fromHtml("<h1>Heading 1</h1><h2>Heading 2</h2><h3>Heading 3</h3>")

        editText.setSelection(editText.text.indexOf("2") + 1)
        alignCenterButton.performClick()
        Assert.assertEquals("<h1>Heading 1</h1><h2 style=\"text-align:center;\">Heading 2</h2><h3>Heading 3</h3>",
                editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun multiselectHeadingAlignment() {
        editText.fromHtml("<h1>Heading 1</h1><h2>Heading 2</h2><h3>Heading 3</h3>")

        editText.setSelection(editText.text.indexOf("2"), editText.text.length)
        alignCenterButton.performClick()
        Assert.assertEquals("<h1>Heading 1</h1><h2 style=\"text-align:center;\">Heading 2</h2>" +
                "<h3 style=\"text-align:center;\">Heading 3</h3>",
                editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun onlyListItemsAlignment() {
        editText.fromHtml("<ul><li>item 1</li><li>item 2</li><li>item 3</li></ul>")

        editText.setSelection(0, editText.length())
        alignLeftButton.performClick()
        Assert.assertEquals("<ul><li style=\"text-align:left;\">item 1</li>" +
                "<li style=\"text-align:left;\">item 2</li>" +
                "<li style=\"text-align:left;\">item 3</li></ul>",
                editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun unorderedListAlignment() {
        editText.fromHtml("<ul><li>item 1</li><li>item 2</li><li>item 3</li></ul>")

        editText.setSelection(editText.text.indexOf("1"))
        alignLeftButton.performClick()
        Assert.assertEquals("<ul><li style=\"text-align:left;\">item 1</li>" +
                "<li>item 2</li><li>item 3</li></ul>",
                editText.toHtml())

        editText.setSelection(editText.text.indexOf("1") + 1)
        alignRightButton.performClick()
        Assert.assertEquals("<ul><li style=\"text-align:right;\">item 1</li>" +
                "<li>item 2</li><li>item 3</li></ul>",
                editText.toHtml())

        alignRightButton.performClick()
        Assert.assertEquals("<ul><li>item 1</li><li>item 2</li><li>item 3</li></ul>",
                editText.toHtml())

        editText.setSelection(editText.text.indexOf("1") + 2)
        alignCenterButton.performClick()
        Assert.assertEquals("<ul><li>item 1</li><li style=\"text-align:center;\">item 2</li><li>item 3</li></ul>",
                editText.toHtml())

        editText.setSelection(editText.text.length)
        alignCenterButton.performClick()
        Assert.assertEquals("<ul><li>item 1</li><li style=\"text-align:center;\">item 2</li><li style=\"text-align:center;\">item 3</li></ul>",
                editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun orderedListMultiselectAlignment() {
        editText.fromHtml("<ol><li>item 1</li><li style=\"text-align:center;\">item 2</li></ol>" +
                "<hr /><ol><li>item 3</li><li>item 4</li></ol>")

        editText.setSelection(editText.text.indexOf("2"), editText.text.indexOf("3"))
        alignRightButton.performClick()
        Assert.assertEquals("<ol><li>item 1</li><li style=\"text-align:right;\">item 2</li></ol><hr />" +
                "<ol><li style=\"text-align:right;\">item 3</li><li>item 4</li></ol>",
                editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun blockquoteAlignment() {
        editText.fromHtml("<blockquote>Quote<br>newline</blockquote>")

        editText.setSelection(1)
        alignRightButton.performClick()
        Assert.assertEquals("<blockquote style=\"text-align:right;\">Quote<br>newline</blockquote>",
                editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun preBlockAlignment() {
        editText.fromHtml("<pre>test<br>newline</pre>")

        editText.setSelection(1)
        alignCenterButton.performClick()
        Assert.assertEquals("<pre style=\"text-align:center;\">test<br>newline</pre>",
                editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun codeBlockAlignment() {
        editText.fromHtml("<code>Code<br>newline</code>")

        editText.setSelection(7)
        alignLeftButton.performClick()
        Assert.assertEquals("<code>Code</code>" +
                "<p style=\"text-align:left;\"><code>newline</code></p>",
                editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun commentAlignment() {
        editText.fromHtml("<!-- Comment -->")

        editText.setSelection(3)
        alignRightButton.performClick()
        Assert.assertEquals("<p style=\"text-align:right;\"><!-- Comment --></p>", editText.toHtml())

        alignRightButton.performClick()
        Assert.assertEquals("<p><!-- Comment --></p>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun hiddenElementAlignment() {
        editText.fromHtml("<div>a<div>b<br><span>c</span><br>d</div></div>")

        editText.setSelection(editText.text.indexOf("a"))
        alignRightButton.performClick()
        Assert.assertEquals("<div style=\"text-align:right;\">a<div>b<br><span>c</span><br>d</div></div>",
                editText.toHtml())

        editText.setSelection(editText.text.indexOf("c") + 1)
        alignCenterButton.performClick()
        Assert.assertEquals("<div style=\"text-align:center;\">a" +
                "<div style=\"text-align:center;\">b<br>" +
                "<span style=\"text-align:center;\">c</span><br>d</div></div>",
                editText.toHtml())

        editText.setSelection(editText.text.indexOf("d"))
        alignLeftButton.performClick()
        Assert.assertEquals("<div style=\"text-align:left;\">a" +
                "<div style=\"text-align:left;\">b<br>" +
                "<span style=\"text-align:center;\">c</span><br>d</div></div>",
                editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun rtlTextAlignment() {
        editText.fromHtml("latin<br>بعبثخز")

        editText.setSelection(0)
        alignLeftButton.performClick()
        Assert.assertEquals("<p style=\"text-align:left;\">latin</p>بعبثخز",
                editText.toHtml())

        editText.setSelection(editText.length())
        alignLeftButton.performClick()
        Assert.assertEquals("<p style=\"text-align:left;\">latin</p>" +
                "<p style=\"text-align:left;\">بعبثخز</p>",
                editText.toHtml())

        alignRightButton.performClick()
        Assert.assertEquals("<p style=\"text-align:left;\">latin</p>" +
                "<p style=\"text-align:right;\">بعبثخز</p>",
                editText.toHtml())

        editText.setSelection(0)
        alignRightButton.performClick()
        Assert.assertEquals("<p style=\"text-align:right;\">latin</p>" +
                "<p style=\"text-align:right;\">بعبثخز</p>",
                editText.toHtml())
    }
}
