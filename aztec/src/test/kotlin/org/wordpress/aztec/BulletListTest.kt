package org.wordpress.aztec

import android.text.TextUtils
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.RuntimeEnvironment
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
        editText = AztecText(RuntimeEnvironment.application)
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

//    @Test
//    @Throws(Exception::class)
//    fun closingList() {
//        Assert.assertTrue(TextUtils.isEmpty(editText.text))
//        editText.bullet(!editText.contains(AztecText.FORMAT_BULLET))
//        editText.text.append("\n")
//        editText.text.append("\n")
//        editText.text.append("not in the list")
//
////        editText.text.append("first item")
////        editText.text.append("\n")
////        editText.text.append("second item")
////        editText.setSelection(0,editText.length())
////        editText.bullet(!editText.contains(AztecText.FORMAT_BULLET))
////        editText.setSelection(editText.length())
////        //double enter to cancel UL input
////        editText.text.append("\n")
////        editText.text.append("\n")
////        editText.text.append("not in the list")
////        Assert.assertEquals("​first item\nsecond item\n\nnot in the list", editText.text.toString())
//        Assert.assertEquals("<ul><li>first item</li><li>second item</li></ul>not in the list", editText.toHtml().toString())
//    }


//    @Test
//    @Throws(Exception::class)
//    fun joinMultipleItemsIntoOneList() {
//        junit.framework.Assert.assertTrue(TextUtils.isEmpty(editText.text))
//        editText.text.append("first item")
//        editText.bullet(!editText.contains(AztecText.FORMAT_BULLET))
//        Assert.assertEquals("<ul><li>first item</li></ul>", editText.toHtml())
//
////        editText.text.append("\n")
////        editText.text.append("second item")
////        Assert.assertEquals("<ul><li>first item</li><li>second item</li></ul>", editText.toHtml())
////
////        //pasting text with newline
////        editText.text.append("\nthird item")
////        Assert.assertEquals("<ul><li>first item</li><li>second item</li><li>third item</li></ul>", editText.toHtml())
////
////        //double enter to cancel UL input
////        editText.text.append("\n")
////        editText.text.append("\n")
////
////        editText.text.append("End")
////        Assert.assertEquals("<ul><li>first item</li><li>second item</li><li>third item</li></ul>End", editText.toHtml())
////
////        editText.text.insert(33," (addition)")
////
////        Assert.assertEquals("<ul><li>first item</li><li>second item</li><li>third item (addition)</li></ul>End", editText.toHtml())
//    }


}