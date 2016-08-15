package org.wordpress.aztec


import android.app.Activity
import android.text.TextUtils
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
    fun testSimpleStyling() {
        Assert.assertFalse(boldButton.isChecked)
        boldButton.performClick()
        Assert.assertTrue(boldButton.isChecked)


        editText.append("bold")
        Assert.assertEquals("<b>bold</b>", editText.toHtml())
        boldButton.performClick()
        Assert.assertFalse(boldButton.isChecked)

        editText.setText("")
        junit.framework.Assert.assertTrue(TextUtils.isEmpty(editText.text))

        italicButton.performClick()
        Assert.assertTrue(italicButton.isChecked)
        editText.append("italic")
        Assert.assertEquals("<i>italic</i>", editText.toHtml())
        italicButton.performClick()
        Assert.assertFalse(italicButton.isChecked)


        editText.setText("")
        junit.framework.Assert.assertTrue(TextUtils.isEmpty(editText.text))
    }




}