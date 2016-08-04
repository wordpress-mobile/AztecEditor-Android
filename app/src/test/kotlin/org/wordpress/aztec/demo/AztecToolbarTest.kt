package org.wordpress.aztec.demo

import android.test.AndroidTestCase
import android.test.mock.MockContext
import android.text.TextUtils
import android.widget.ToggleButton
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.annotation.Config
import org.wordpress.aztec.AztecParser
import org.wordpress.aztec.AztecText

/**
 * Tests for [AztecParser].
 */
@RunWith(RobolectricGradleTestRunner::class)
@Config(constants = CustomBuildConfig::class)
class AztecToolbarTest : AndroidTestCase() {
    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        context = MockContext()
    }

//    @Test
//    @Throws(Exception::class)
//    fun testSingleStyles() {
//        val activity = Robolectric.setupActivity(MainActivity::class.java)
//
//        val editText = activity.findViewById(R.id.aztec) as AztecText
//
//        junit.framework.Assert.assertTrue(TextUtils.isEmpty(editText.text))
//
//        val boldButton = activity.findViewById(R.id.format_bar_button_bold) as ToggleButton
//        Assert.assertFalse(boldButton.isChecked)
//        boldButton.performClick()
//        Assert.assertTrue(boldButton.isChecked)
//        editText.append("bold")
//        Assert.assertEquals("<b>bold</b>", editText.toHtml())
//        boldButton.performClick()
//        Assert.assertFalse(boldButton.isChecked)
//
//        val italicButton = activity.findViewById(R.id.format_bar_button_italic) as ToggleButton
//        italicButton.performClick()
//        Assert.assertTrue(italicButton.isChecked)
//        editText.setText("italic")
//        Assert.assertEquals("<i>italic</i>", editText.toHtml())
//        italicButton.performClick()
//        Assert.assertFalse(italicButton.isChecked)
//
//
//
//        editText.setText("bold")
//        Assert.assertEquals("bold", editText.toHtml())
//        editText.setSelection(0,editText.text.length)
//        boldButton.performClick()
//        Assert.assertTrue(boldButton.isChecked)
//        Assert.assertEquals("<b>bold</b>", editText.toHtml())
//        boldButton.performClick()
//        Assert.assertFalse(boldButton.isChecked)
//
//
//        editText.setText("italic")
//        Assert.assertEquals("italic", editText.toHtml())
//        editText.setSelection(0,editText.text.length)
//        italicButton.performClick()
//        Assert.assertTrue(italicButton.isChecked)
//        Assert.assertEquals("<i>italic</i>", editText.toHtml())
//        italicButton.performClick()
//        Assert.assertFalse(italicButton.isChecked)
//
//    }


    @Test
    @Throws(Exception::class)
    fun testNormalBulletPointBehaviour() {
        val activity = Robolectric.setupActivity(MainActivity::class.java)
        val editText = activity.findViewById(R.id.aztec) as AztecText
        editText.setText("")
        junit.framework.Assert.assertTrue(TextUtils.isEmpty(editText.text))

        val bulletPointListBullet = activity.findViewById(R.id.format_bar_button_ul) as ToggleButton

        Assert.assertFalse(bulletPointListBullet.isChecked)
        bulletPointListBullet.performClick()
        Assert.assertTrue(bulletPointListBullet.isChecked)
        editText.text.append("first item")

        Assert.assertEquals("<ul><li>first item</li></ul>", editText.toHtml())
        editText.text.append("\n")
        editText.text.append("second item")
        Assert.assertEquals("<ul><li>first item</li><li>second item</li></ul>", editText.toHtml())

        //pasting text with newline
        editText.text.append("\nthird item")
        Assert.assertEquals("<ul><li>first item</li><li>second item</li><li>third item</li></ul>", editText.toHtml())

        //double enter to cancel UL input
        editText.text.append("\n")
        editText.text.append("\n")

        editText.text.append("End")
        Assert.assertEquals("<ul><li>first item</li><li>second item</li><li>third item</li></ul>End", editText.toHtml())

        editText.text.insert(33," (addition)")

        Assert.assertEquals("<ul><li>first item</li><li>second item</li><li>third item (addition)</li></ul>End", editText.toHtml())


    }
}
