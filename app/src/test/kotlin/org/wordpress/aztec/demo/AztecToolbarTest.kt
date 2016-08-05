package org.wordpress.aztec.demo

import android.app.Activity
import android.test.AndroidTestCase
import android.test.mock.MockContext
import android.text.TextUtils
import android.widget.ToggleButton
import org.junit.Assert
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

    lateinit var activity: Activity
    lateinit var editText: AztecText

    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        context = MockContext()
        activity = Robolectric.setupActivity(MainActivity::class.java)
        editText = activity.findViewById(R.id.aztec) as AztecText
    }

    @Test
    @Throws(Exception::class)
    fun testSimpleStyling() {

        editText.setText("")
        junit.framework.Assert.assertTrue(TextUtils.isEmpty(editText.text))

        val boldButton = activity.findViewById(R.id.format_bar_button_bold) as ToggleButton
        Assert.assertFalse(boldButton.isChecked)
        boldButton.performClick()
        Assert.assertTrue(boldButton.isChecked)
        editText.append("bold")
        Assert.assertEquals("<b>bold</b>", editText.toHtml())
        boldButton.performClick()
        Assert.assertFalse(boldButton.isChecked)

        editText.setText("")
        junit.framework.Assert.assertTrue(TextUtils.isEmpty(editText.text))

        val italicButton = activity.findViewById(R.id.format_bar_button_italic) as ToggleButton
        italicButton.performClick()
        Assert.assertTrue(italicButton.isChecked)
        editText.append("italic")
        Assert.assertEquals("<i>italic</i>", editText.toHtml())
        italicButton.performClick()
        Assert.assertFalse(italicButton.isChecked)


        editText.setText("")
        junit.framework.Assert.assertTrue(TextUtils.isEmpty(editText.text))

        val bulletPointListBullet = activity.findViewById(R.id.format_bar_button_ul) as ToggleButton
        bulletPointListBullet.performClick()
        Assert.assertTrue(bulletPointListBullet.isChecked)
        editText.append("bullet list item")
        Assert.assertEquals("<ul><li>bullet list item</li></ul>", editText.toHtml())
        bulletPointListBullet.performClick()
        Assert.assertFalse(bulletPointListBullet.isChecked)
        Assert.assertEquals("bullet list item", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun testSimpleBulletPointStyling() {
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


    @Test
    @Throws(Exception::class)
    fun testSelectedMultilineTextBulletpointCreation() {
        editText.setText("")
        junit.framework.Assert.assertTrue(TextUtils.isEmpty(editText.text))

        val bulletPointListBullet = activity.findViewById(R.id.format_bar_button_ul) as ToggleButton

        editText.text.append("first item")
        editText.text.append("\n")
        editText.text.append("second item")
        editText.text.append("\n")
        editText.text.append("third item")

        editText.setSelection(0,editText.text.length)


        Assert.assertFalse(bulletPointListBullet.isChecked)
        bulletPointListBullet.performClick()
        Assert.assertTrue(bulletPointListBullet.isChecked)

        Assert.assertEquals("<ul><li>first item</li><li>second item</li><li>third item</li></ul>", editText.toHtml())

        //now while we still have all of the lines selected lets remove the BulletSpan
        bulletPointListBullet.performClick()
        Assert.assertFalse(bulletPointListBullet.isChecked)
        Assert.assertEquals("first item<br>second item<br>third item", editText.toHtml())

    }


//    @Test
//    @Throws(Exception::class)
//    fun testUlSplit() {
//        junit.framework.Assert.assertTrue(TextUtils.isEmpty(editText.text))
//
//        val bulletPointListBullet = activity.findViewById(R.id.format_bar_button_ul) as ToggleButton
//
//        editText.text.append("first item")
//        editText.text.append("\n")
//        editText.text.append("second item")
//        editText.text.append("\n")
//        editText.text.append("third item")
//
//        //select all
//        editText.setSelection(0,editText.text.length)
//
//        Assert.assertFalse(bulletPointListBullet.isChecked)
//        bulletPointListBullet.performClick()
//        Assert.assertTrue(bulletPointListBullet.isChecked)
//
//        Assert.assertEquals("<ul><li>first item</li><li>second item</li><li>third item</li></ul>", editText.toHtml())
//
//        //put cursor inside "second item"
//        editText.setSelection(14,14)
//
//        bulletPointListBullet.performClick()
//        Assert.assertFalse(bulletPointListBullet.isChecked)
//        Assert.assertEquals("<ul><li>first item</li></ul>second item<ul><li>third item</li></ul>", editText.toHtml())
//    }
}
