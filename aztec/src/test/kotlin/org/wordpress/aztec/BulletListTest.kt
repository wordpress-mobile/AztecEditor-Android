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
        editText.append("first item")
        editText.bullet(!editText.contains(AztecText.FORMAT_BULLET))
        Assert.assertEquals("<ul><li>first item</li></ul>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleMultipleSelectedItems() {
        junit.framework.Assert.assertTrue(TextUtils.isEmpty(editText.text))
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")
        editText.append("\n")
        editText.append("third item")
        editText.setSelection(0, editText.length())

        editText.bullet(!editText.contains(AztecText.FORMAT_BULLET))
        Assert.assertEquals("<ul><li>first item</li><li>second item</li><li>third item</li></ul>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun stylePartiallySelectedMultipleItems() {
        junit.framework.Assert.assertTrue(TextUtils.isEmpty(editText.text))
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")
        editText.append("\n")
        editText.append("third item")
        editText.setSelection(4, 15) //we partially selected first and second item

        editText.bullet(!editText.contains(AztecText.FORMAT_BULLET))
        Assert.assertEquals("<ul><li>first item</li><li>second item</li></ul>third item", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleSurroundedItem() {
        junit.framework.Assert.assertTrue(TextUtils.isEmpty(editText.text))
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")
        editText.append("\n")
        editText.append("third item")
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
        editText.append("first item")
        Assert.assertEquals("<ul><li>first item</li></ul>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleMultipleEnteredItems() {
        editText.bullet(!editText.contains(AztecText.FORMAT_BULLET))
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")
        Assert.assertEquals("<ul><li>first item</li><li>second item</li></ul>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun closingPopulatedList() {
        editText.bullet(!editText.contains(AztecText.FORMAT_BULLET))
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")

        editText.append("\n")
        editText.append("\n")
        editText.append("not in the list")
        Assert.assertEquals("<ul><li>first item</li><li>second item</li></ul>not in the list", editText.toHtml().toString())
    }


    @Test
    @Throws(Exception::class)
    fun closingEmptyList() {
        editText.bullet(!editText.contains(AztecText.FORMAT_BULLET))
        editText.append("\n")
        Assert.assertEquals("", editText.toHtml().toString())
    }

    @Test
    @Throws(Exception::class)
    fun extendingListBySplittingItems() {
        editText.bullet(!editText.contains(AztecText.FORMAT_BULLET))
        editText.append("firstitem")
        editText.text.insert(5,"\n")
        Assert.assertEquals("<ul><li>first</li><li>item</li></ul>", editText.toHtml().toString())
    }


    @Test
    @Throws(Exception::class)
    fun bulletListSplitWithToolbar() {
        editText.fromHtml("<ul><li>first item</li><li>second item</li><li>third item</li></ul>")
        editText.setSelection(14)
        editText.bullet(!editText.contains(AztecText.FORMAT_BULLET))

        Assert.assertEquals("<ul><li>first item</li></ul>second item<ul><li>third item</li></ul>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun additionToClosedList() {
        editText.bullet(!editText.contains(AztecText.FORMAT_BULLET))
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")

        val mark = editText.length()

        editText.append("\n")
        editText.append("\n")
        editText.append("not in the list")
        Assert.assertEquals("<ul><li>first item</li><li>second item</li></ul>not in the list", editText.toHtml().toString())

        editText.text.insert(mark," (addition)")

        Assert.assertEquals("<ul><li>first item</li><li>second item (addition)</li></ul>not in the list", editText.toHtml().toString())

    }

    @Test
    @Throws(Exception::class)
    fun removeBulletListStyling() {
        editText.fromHtml("<ul><li>first item</li></ul>")
        editText.setSelection(1)
        editText.bullet(!editText.contains(AztecText.FORMAT_BULLET))

        Assert.assertEquals("first item", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun removeBulletListStylingForPartialSelection() {
        editText.fromHtml("<ul><li>first item</li></ul>")
        editText.setSelection(2,4)
        editText.bullet(!editText.contains(AztecText.FORMAT_BULLET))

        Assert.assertEquals("first item", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun removeBulletListStylingForMultilinePartialSelection() {
        editText.bullet(!editText.contains(AztecText.FORMAT_BULLET))
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")
        val firstMark = editText.length() - 4
        editText.append("\n")
        editText.append("third item")
        editText.append("\n")
        val secondMark = editText.length() - 4
        editText.append("fourth item")
        editText.append("\n")
        editText.append("\n")
        editText.append("not in list")

        editText.setSelection(firstMark,secondMark)
        editText.bullet(!editText.contains(AztecText.FORMAT_BULLET))

        Assert.assertEquals("<ul><li>first item</li></ul>second item<br>third item<ul><li>fourth item</li></ul>not in list", editText.toHtml())
    }


}