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


/**
 * Testing ordered and unordered lists.
 *
 * This test uses ParameterizedRobolectricTestRunner and runs twice - for ol and ul tags.
 */
@RunWith(RobolectricGradleTestRunner::class)
@Config(constants = BuildConfig::class)
class QuoteTest() {

    val listType = TextFormat.FORMAT_QUOTE
    val listTag = "blockquote"
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
        editText.toggleFormatting(listType)
        Assert.assertEquals("<$listTag>first item</$listTag>", editText.toHtml())
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

        editText.toggleFormatting(listType)
        Assert.assertEquals("<$listTag>first item<br>second item<br>third item</$listTag>", editText.toHtml())
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

        editText.toggleFormatting(listType)
        Assert.assertEquals("<$listTag>first item<br>second item</$listTag>third item", editText.toHtml())
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

        editText.toggleFormatting(listType)
        Assert.assertEquals("first item<$listTag>second item</$listTag>third item", editText.toHtml())
    }


    //enable styling on empty line and enter text

    @Test
    @Throws(Exception::class)
    fun emptyQuote() {
        editText.toggleFormatting(listType)
        Assert.assertEquals("<$listTag></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleSingleEnteredItem() {
        editText.toggleFormatting(listType)
        editText.append("first item")
        Assert.assertEquals("<$listTag>first item</$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleMultipleEnteredItems() {
        editText.toggleFormatting(listType)
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")
        Assert.assertEquals("<$listTag>first item<br>second item</$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun closingPopulatedQuote() {
        editText.toggleFormatting(listType)
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")

        editText.append("\n")
        editText.append("\n")
        editText.append("not in the list")
        Assert.assertEquals("<$listTag>first item<br>second item</$listTag>not in the list", editText.toHtml().toString())
    }


    @Test
    @Throws(Exception::class)
    fun closingEmptyQuote() {
        editText.toggleFormatting(listType)
        editText.append("\n")
        Assert.assertEquals("", editText.toHtml().toString())
    }

    @Test
    @Throws(Exception::class)
    fun extendingQuoteBySplittingItems() {
        editText.toggleFormatting(listType)
        editText.append("firstitem")
        editText.text.insert(5, "\n")
        Assert.assertEquals("<$listTag>first<br>item</$listTag>", editText.toHtml().toString())
    }


    @Test
    @Throws(Exception::class)
    fun bulletListSplitWithToolbar() {
        editText.fromHtml("<$listTag>first item<br>second item<br>third item</$listTag>")
        editText.setSelection(14)
        editText.toggleFormatting(listType)

        Assert.assertEquals("<$listTag>first item</$listTag>second item<$listTag>third item</$listTag>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun removeQuoteStyling() {
        editText.fromHtml("<$listTag>first item</$listTag>")
        editText.setSelection(1)
        editText.toggleFormatting(listType)

        Assert.assertEquals("first item", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun removeQuoteStylingForPartialSelection() {
        editText.fromHtml("<$listTag>first item</$listTag>")
        editText.setSelection(2, 4)
        editText.toggleFormatting(listType)

        Assert.assertEquals("first item", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun removeBulletListStylingForMultilinePartialSelection() {
        editText.toggleFormatting(listType)
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

        editText.setSelection(firstMark, secondMark)
        editText.toggleFormatting(listType)

        Assert.assertEquals("<$listTag>first item</$listTag>second item<br>third item<$listTag>fourth item</$listTag>not in list", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun emptyQuoteSurroundedBytItems() {
        editText.toggleFormatting(listType)
        editText.append("first item")
        editText.append("\n")
        val firstMark = editText.length()
        editText.append("second item")
        editText.append("\n")
        val secondMart = editText.length()
        editText.append("third item")

        editText.text.delete(firstMark - 1, secondMart - 2)

        Assert.assertEquals("<$listTag>first item<br><br>third item</$listTag>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun trailingEmptyBulletPoint() {
        editText.toggleFormatting(listType)
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")
        editText.append("\n")
        editText.append("third item")
        val mark = editText.length()
        editText.append("\n")

        Assert.assertEquals("<$listTag>first item<br>second item<br>third item<br></$listTag>", editText.toHtml())
        editText.append("\n")

        Assert.assertEquals("<$listTag>first item<br>second item<br>third item</$listTag>", editText.toHtml())

        editText.append("not in list")
        editText.setSelection(mark)
        editText.text.insert(mark, "\n")
        Assert.assertEquals("<$listTag>first item<br>second item<br>third item<br></$listTag>not in list", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun openQuoteByAddingNewline() {
        editText.fromHtml("<$listTag>first item<br>second item</$listTag>not in list")

        editText.text.insert(editText.text.indexOf("\nnot in list")-1, "\nthird item")

        Assert.assertEquals("<$listTag>first item<br>second item<br>third item</$listTag>not in list", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun openListByAppendingTextToTheEnd() {
        editText.fromHtml("<$listTag>first item<br>second item</$listTag>not in list")
        editText.setSelection(editText.length())
        editText.text.insert(editText.text.indexOf("not in list") - 1, " (appended)")
        Assert.assertEquals("<$listTag>first item<br>second item (appended)</$listTag>not in list", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun openQuoteByMovingOutsideTextInsideIt() {
        editText.fromHtml("<$listTag>first item<br>second item</$listTag>")
        editText.append("not in list")

        editText.text.delete(editText.text.indexOf("not in list"), editText.text.indexOf("not in list"))
        Assert.assertEquals("<$listTag>first item<br>second itemnot in list</$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun listRemainsClosedWhenLastCharacterIsDeleted() {
        editText.fromHtml("<$listTag>first item<br>second item</$listTag>not in list")
        editText.setSelection(editText.length())

        //delete last character from "second item"
        editText.text.delete(editText.text.indexOf("not in list") - 2, editText.text.indexOf("not in list") - 1)
        Assert.assertEquals("<$listTag>first item<br>second ite</$listTag>not in list", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun openingAndReopeningOfQuote() {
        editText.fromHtml("<$listTag>first item<br>second item</$listTag>")
        editText.setSelection(editText.length())

        editText.append("\n")
        editText.append("third item")
        Assert.assertEquals("<$listTag>first item<br>second item<br>third item</$listTag>", editText.toHtml())
        editText.append("\n")
        editText.append("\n")
        val mark = editText.length() - 1
        editText.append("not in the list")
        Assert.assertEquals("<$listTag>first item<br>second item<br>third item</$listTag>not in the list", editText.toHtml())
        editText.append("\n")
        editText.append("foo")
        Assert.assertEquals("<$listTag>first item<br>second item<br>third item</$listTag>not in the list<br>foo", editText.toHtml())

        //reopen list
        editText.text.delete(mark, mark + 1)
        Assert.assertEquals("<$listTag>first item<br>second item<br>third itemnot in the list</$listTag>foo", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun closeList() {
        editText.fromHtml("<$listTag>first item<br>second item</$listTag>")
        editText.setSelection(editText.length())

        editText.append("\n")
        val mark = editText.length() - 1

        editText.text.delete(mark, mark + 1)
        editText.append("not in the list")
        Assert.assertEquals("<$listTag>first item<br>second item</$listTag>not in the list", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun handleListReopeningAfterLastElementDeletion() {
        editText.fromHtml("<$listTag>first item<br>second item<br>third item</$listTag>")
        editText.setSelection(editText.length())

        editText.text.delete(editText.text.indexOf("third item", 0), editText.length())

        editText.append("not in the list")
        Assert.assertEquals("<$listTag>first item<br>second item</$listTag>not in the list", editText.toHtml())

        editText.text.insert(editText.text.indexOf("not in the list") - 1, " addition")
        Assert.assertEquals("<$listTag>first item<br>second item addition</$listTag>not in the list", editText.toHtml())

        editText.text.insert(editText.text.indexOf("not in the list") - 1, "\n")
        editText.text.insert(editText.text.indexOf("not in the list") - 1, "third item")
        Assert.assertEquals("<$listTag>first item<br>second item addition<br>third item</$listTag>not in the list", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun additionToClosedQuote() {
        editText.toggleFormatting(listType)
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")

        val mark = editText.length()

        editText.append("\n")
        editText.append("\n")
        editText.append("not in the list")
        Assert.assertEquals("<$listTag>first item<br>second item</$listTag>not in the list", editText.toHtml().toString())

        editText.text.insert(mark, " (addition)")

        Assert.assertEquals("<$listTag>first item<br>second item (addition)</$listTag>not in the list", editText.toHtml().toString())
    }

    @Test
    @Throws(Exception::class)
    fun addItemToQuoteFromBottom() {
        editText.toggleFormatting(listType)
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")
        editText.append("\n")
        editText.append("\n")
        editText.append("third item")
        editText.setSelection(editText.length())

        editText.toggleFormatting(listType)

        Assert.assertEquals("<$listTag>first item<br>second item<br>third item</$listTag>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun addItemToQuoteFromTop() {
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")
        editText.setSelection(editText.length())
        editText.toggleFormatting(listType)
        editText.append("\n")
        editText.append("third item")

        editText.setSelection(0)

        editText.toggleFormatting(listType)

        Assert.assertEquals("<$listTag>first item<br>second item<br>third item</$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addItemToListFromInside() {
        editText.toggleFormatting(listType)
        editText.append("first item")
        editText.append("\n")
        editText.append("\n")
        editText.append("second item")
        editText.append("\n")
        editText.append("third item")
        editText.setSelection(editText.length())
        editText.toggleFormatting(listType)

        Assert.assertEquals("<$listTag>first item</$listTag>second item<$listTag>third item</$listTag>", editText.toHtml())
        editText.setSelection(15)
        editText.toggleFormatting(listType)

        Assert.assertEquals("<$listTag>first item<br>second item<br>third item</$listTag>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun appendToQuoteFromTopAtFirstLine() {
        editText.toggleFormatting(listType)
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")
        editText.setSelection(0)
        editText.text.insert(0,"addition ")

        Assert.assertEquals("<$listTag>addition first item<br>second item</$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun appendToListFromTop() {
        editText.append("not in list")
        editText.append("\n")
        editText.toggleFormatting(listType)
        val mark = editText.length() - 1
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")

        editText.setSelection(mark)
        editText.text.insert(mark,"addition ")

        Assert.assertEquals("not in list<$listTag>addition first item<br>second item</$listTag>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun deleteFirstItemWithKeyboard() {
        editText.toggleFormatting(listType)
        editText.append("first item")
        val firstMark = editText.length()
        editText.append("\n")
        editText.append("second item")
        val secondMark = editText.length()
        editText.append("\n")
        editText.append("third item")
        editText.setSelection(0)
        editText.text.delete(firstMark+1,secondMark)

        Assert.assertEquals("<$listTag>first item<br><br>third item</$listTag>", editText.toHtml())

        editText.text.delete(0,firstMark)

        Assert.assertEquals("<$listTag><br>third item</$listTag>", editText.toHtml())
    }
}
