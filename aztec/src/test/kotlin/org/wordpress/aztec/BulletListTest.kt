package org.wordpress.aztec

import android.app.Activity
import android.text.TextUtils
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricGradleTestRunner::class)
@Config(constants = BuildConfig::class)
class BulletListTest() {

    lateinit var editText: AztecText

    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().visible().get()
        editText = AztecText(activity)
        activity.setContentView(editText)
    }

    //enter text and then enable styling

    @Test
    @Throws(Exception::class)
    fun styleSingleItem() {
        editText.text.append("first item")
        editText.bullet(!editText.contains(AztecText.FORMAT_BULLET))
        Assert.assertEquals("<ul><li>first item</li></ul>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleMultipleSelectedItems() {
        junit.framework.Assert.assertTrue(TextUtils.isEmpty(editText.text))
        editText.text.append("first item")
        editText.text.append("\n")
        editText.text.append("second item")
        editText.text.append("\n")
        editText.text.append("third item")
        editText.setSelection(0, editText.length())

        editText.bullet(!editText.contains(AztecText.FORMAT_BULLET))
        Assert.assertEquals("<ul><li>first item</li><li>second item</li><li>third item</li></ul>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun stylePartiallySelectedMultipleItems() {
        junit.framework.Assert.assertTrue(TextUtils.isEmpty(editText.text))
        editText.text.append("first item")
        editText.text.append("\n")
        editText.text.append("second item")
        editText.text.append("\n")
        editText.text.append("third item")
        editText.setSelection(4, 15) //we partially selected first and second item

        editText.bullet(!editText.contains(AztecText.FORMAT_BULLET))
        Assert.assertEquals("<ul><li>first item</li><li>second item</li></ul>third item", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleSurroundedItem() {
        junit.framework.Assert.assertTrue(TextUtils.isEmpty(editText.text))
        editText.text.append("first item")
        editText.text.append("\n")
        editText.text.append("second item")
        editText.text.append("\n")
        editText.text.append("third item")
        editText.setSelection(14)

        editText.bullet(!editText.contains(AztecText.FORMAT_BULLET))
        Assert.assertEquals("first item<ul><li>second item</li></ul>third item", editText.toHtml())
    }


    //enable styling on empty line and enter text

    @Test
    @Throws(Exception::class)
    fun emptyList() {
        editText.bullet(!editText.contains(AztecText.FORMAT_BULLET))
        Assert.assertEquals("<ul><li></li></ul>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleSingleEnteredItem() {
        editText.bullet(!editText.contains(AztecText.FORMAT_BULLET))
        editText.text.append("first item")
        Assert.assertEquals("<ul><li>first item</li></ul>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleMultipleEnteredItems() {
        editText.bullet(!editText.contains(AztecText.FORMAT_BULLET))
        editText.text.append("first item")
        editText.text.append("\n")
        editText.text.append("second item")
        Assert.assertEquals("<ul><li>first item</li><li>second item</li></ul>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun closingPopulatedList() {
        editText.bullet(!editText.contains(AztecText.FORMAT_BULLET))
        editText.text.append("first item")
        editText.text.append("\n")
        editText.text.append("second item")

        editText.text.append("\n")
        editText.text.append("\n")
        editText.text.append("not in the list")
        Assert.assertEquals("<ul><li>first item</li><li>second item</li></ul>not in the list", editText.toHtml().toString())
    }


    @Test
    @Throws(Exception::class)
    fun closingEmptyList() {
        editText.bullet(!editText.contains(AztecText.FORMAT_BULLET))
        editText.text.append("\n")
        Assert.assertEquals("", editText.toHtml().toString())
    }

    @Test
    @Throws(Exception::class)
    fun extendingListBySplittingItems() {
        editText.bullet(!editText.contains(AztecText.FORMAT_BULLET))
        editText.text.append("first item")
        editText.text.insert(1,"\n")
        editText.text.insert(5,"\n")
        Assert.assertEquals("<ul><li>f</li><li>irs</li><li>t item</li></ul>", editText.toHtml().toString())
    }


    @Test
    @Throws(Exception::class)
    fun testBulletListSplitWithToolbar() {
        editText.fromHtml("<ul><li>first item</li><li>second item</li><li>third item</li></ul>")
        editText.setSelection(14)
        editText.bullet(!editText.contains(AztecText.FORMAT_BULLET))

        Assert.assertEquals("<ul><li>first item</li></ul>second item<ul><li>third item</li></ul>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun additionToClosedList() {
        editText.bullet(!editText.contains(AztecText.FORMAT_BULLET))
        editText.text.append("first item")
        editText.text.append("\n")
        editText.text.append("second item")

        val mark = editText.length()

        editText.text.append("\n")
        editText.text.append("\n")
        editText.text.append("not in the list")
        Assert.assertEquals("<ul><li>first item</li><li>second item</li></ul>not in the list", editText.toHtml().toString())

        editText.text.insert(mark," (addition)")

        Assert.assertEquals("<ul><li>first item</li><li>second item (addition)</li></ul>not in the list", editText.toHtml().toString())

    }


}