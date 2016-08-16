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
import org.wordpress.aztec.toolbar.FormatToolbar


/**
 * Tests for [AztecParser].
 */
@RunWith(RobolectricGradleTestRunner::class)
@Config(constants = BuildConfig::class)
class AztecToolbarTest {

    lateinit var activity: Activity
    lateinit var editText: AztecText
    lateinit var toolbar: FormatToolbar

    lateinit var boldButton: ToggleButton
    lateinit var italicButton: ToggleButton
    lateinit var quoteButton: ToggleButton
    lateinit var bulletListButton: ToggleButton
    lateinit var numberedListButton: ToggleButton
    lateinit var linkButton: ToggleButton
    lateinit var htmlButton: ToggleButton


    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().visible().get()
        editText = AztecText(activity)
        activity.setContentView(editText)
        toolbar = FormatToolbar(activity)
        toolbar.setEditor(editText, "")

        boldButton = toolbar.findViewById(R.id.format_bar_button_bold) as ToggleButton
        italicButton = toolbar.findViewById(R.id.format_bar_button_italic) as ToggleButton
        quoteButton = toolbar.findViewById(R.id.format_bar_button_quote) as ToggleButton
        bulletListButton = toolbar.findViewById(R.id.format_bar_button_ul) as ToggleButton
        numberedListButton = toolbar.findViewById(R.id.format_bar_button_ol) as ToggleButton
        linkButton = toolbar.findViewById(R.id.format_bar_button_link) as ToggleButton
        htmlButton = toolbar.findViewById(R.id.format_bar_button_html) as ToggleButton

    }


    @Test
    @Throws(Exception::class)
    fun initialState() {
        Assert.assertFalse(boldButton.isChecked)
        Assert.assertFalse(italicButton.isChecked)
        Assert.assertFalse(quoteButton.isChecked)
        Assert.assertFalse(bulletListButton.isChecked)
        Assert.assertFalse(numberedListButton.isChecked)
        Assert.assertFalse(linkButton.isChecked)
        Assert.assertFalse(htmlButton.isChecked)

        Assert.assertTrue(editText.isEmpty())
    }

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


    @Test
    @Throws(Exception::class)
    fun testCrossStylesToggle() {

        editText.append("bold bolditalic italic normal")
        editText.setSelection(0,4)

        Assert.assertFalse(boldButton.isChecked)
        boldButton.performClick()
        Assert.assertTrue(boldButton.isChecked)

        Assert.assertEquals("<b>bold</b> bolditalic italic normal", editText.toHtml())

        editText.setSelection(5,15)

        Assert.assertFalse(boldButton.isChecked)
        boldButton.performClick()
        Assert.assertTrue(boldButton.isChecked)

        Assert.assertFalse(italicButton.isChecked)
        italicButton.performClick()
        Assert.assertTrue(italicButton.isChecked)

        Assert.assertEquals("<b>bold</b> <b><i>bolditalic</i></b> italic normal", editText.toHtml())

        editText.setSelection(16,22)

        Assert.assertFalse(italicButton.isChecked)
        Assert.assertFalse(boldButton.isChecked)

        italicButton.performClick()
        Assert.assertTrue(italicButton.isChecked)

        Assert.assertEquals("<b>bold</b> <b><i>bolditalic</i></b> <i>italic</i> normal", editText.toHtml())
    }

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
        Assert.assertFalse(boldButton.isChecked)
        editText.append("normal")
        Assert.assertEquals("<b>bold</b><b><i>bolditalic</i></b><i>italic</i>normal", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun testSelection() {
        editText.fromHtml("<b>bold</b><b><i>bolditalic</i></b><i>italic</i>normal")

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
        editText.setSelection(15)
        Assert.assertFalse(boldButton.isChecked)
        Assert.assertTrue(italicButton.isChecked)

        //whole text selected
        editText.setSelection(0, editText.length() - 1)
        Assert.assertFalse(italicButton.isChecked)
        Assert.assertFalse(boldButton.isChecked)

    }

    @Test
    @Throws(Exception::class)
    fun extendStyle() {
        editText.fromHtml("<b>bold</b><b><i>italic</i></b>")

        val selectedText = editText.text.substring(3,5)
        Assert.assertEquals("di",selectedText) //sanity check

        editText.setSelection(3,5)

        Assert.assertTrue(boldButton.isChecked)
        italicButton.performClick()

        Assert.assertEquals("<b>bol</b><b><i>ditalic</i></b>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun removeStyleFromPartialSelection() {
        editText.fromHtml("<b>bold</b><b><i>italic</i></b>")

        val selectedText = editText.text.substring(4,editText.length())
        Assert.assertEquals("italic",selectedText) //sanity check

        editText.setSelection(4,editText.length())

        italicButton.performClick()

        Assert.assertEquals("<b>bolditalic</b>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun extendStyleToWholeSelection() {
        editText.fromHtml("<b>bold</b><b><i>italic</i></b>")

        editText.setSelection(0,editText.length())

        italicButton.performClick()

        Assert.assertEquals("<b><i>bolditalic</i></b>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun removeStyleFromWholeSelection() {
        editText.fromHtml("<b>bold</b><b><i>italic</i></b>")

        editText.setSelection(0,editText.length())

        boldButton.performClick()

        Assert.assertEquals("bold<i>italic</i>", editText.toHtml())

        italicButton.performClick()
        Assert.assertEquals("<i>bolditalic</i>", editText.toHtml())

        italicButton.performClick()
        Assert.assertEquals("bolditalic", editText.toHtml())
    }

}