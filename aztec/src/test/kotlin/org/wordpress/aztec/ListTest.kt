package org.wordpress.aztec

import android.app.Activity
import android.text.TextUtils
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.Robolectric
import org.robolectric.annotation.Config


/**
 * Testing ordered and unordered lists.
 *
 * This test uses ParameterizedRobolectricTestRunner and runs twice - for ol and ul tags.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(constants = BuildConfig::class, manifest = "src/main/AndroidManifest.xml", sdk = intArrayOf(16))
class ListTest(listTextFormat: TextFormat, listHtmlTag: String) {

    val listType = listTextFormat
    val listTag = listHtmlTag
    lateinit var editText: AztecText

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "Testing lists with {1} tag")
        fun data(): Collection<Array<Any>> {
            return listOf(
                    arrayOf(TextFormat.FORMAT_ORDERED_LIST, "ol"),
                    arrayOf(TextFormat.FORMAT_UNORDERED_LIST, "ul")
            )
        }
    }


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
        Assert.assertEquals("<$listTag><li>first item</li></$listTag>", editText.toHtml())
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
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag>", editText.toHtml())
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
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li></$listTag>third item", editText.toHtml())
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
        Assert.assertEquals("first item<$listTag><li>second item</li></$listTag>third item", editText.toHtml())
    }


    //enable styling on empty line and enter text

    @Test
    @Throws(Exception::class)
    fun emptyList() {
        editText.toggleFormatting(listType)
        Assert.assertEquals("<$listTag><li></li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleSingleEnteredItem() {
        editText.toggleFormatting(listType)
        editText.append("first item")
        Assert.assertEquals("<$listTag><li>first item</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleMultipleEnteredItems() {
        editText.toggleFormatting(listType)
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun closingPopulatedList() {
        editText.toggleFormatting(listType)
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")

        editText.append("\n")
        editText.append("\n")
        editText.append("not in the list")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li></$listTag>not in the list", editText.toHtml().toString())
    }


    @Test
    @Throws(Exception::class)
    fun closingEmptyList() {
        editText.toggleFormatting(listType)
        editText.append("\n")
        Assert.assertEquals("", editText.toHtml().toString())
    }

    @Test
    @Throws(Exception::class)
    fun extendingListBySplittingItems() {
        editText.toggleFormatting(listType)
        editText.append("firstitem")
        editText.text.insert(5, "\n")
        Assert.assertEquals("<$listTag><li>first</li><li>item</li></$listTag>", editText.toHtml().toString())
    }


    @Test
    @Throws(Exception::class)
    fun bulletListSplitWithToolbar() {
        editText.fromHtml("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag>")
        editText.setSelection(14)
        editText.toggleFormatting(listType)

        Assert.assertEquals("<$listTag><li>first item</li></$listTag>second item<$listTag><li>third item</li></$listTag>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun removeBulletListStyling() {
        editText.fromHtml("<$listTag><li>first item</li></$listTag>")
        editText.setSelection(1)
        editText.toggleFormatting(listType)

        Assert.assertEquals("first item", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun removeBulletListStylingForPartialSelection() {
        editText.fromHtml("<$listTag><li>first item</li></$listTag>")
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

        Assert.assertEquals("<$listTag><li>first item</li></$listTag>second item<br>third item<$listTag><li>fourth item</li></$listTag>not in list", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun emptyBulletSurroundedBytItems() {
        editText.toggleFormatting(listType)
        editText.append("first item")
        editText.append("\n")
        val firstMark = editText.length()
        editText.append("second item")
        editText.append("\n")
        val secondMart = editText.length()
        editText.append("third item")

        editText.text.delete(firstMark - 1, secondMart - 2)

        Assert.assertEquals("<$listTag><li>first item</li><li></li><li>third item</li></$listTag>", editText.toHtml())
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

        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li><li></li></$listTag>", editText.toHtml())
        editText.append("\n")

        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag>", editText.toHtml())

        editText.append("not in list")
        editText.setSelection(mark)
        editText.text.insert(mark, "\n")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li><li></li></$listTag>not in list", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun openListByAddingNewline() {
        editText.fromHtml("<$listTag><li>first item</li><li>second item</li></$listTag>not in list")
        editText.setSelection(editText.length())

        editText.text.insert(editText.text.indexOf("not in list") - 1, "\n")
        editText.text.insert(editText.text.indexOf("not in list") - 1, "third item")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag>not in list", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun openListByAppendingTextToTheEnd() {
        editText.fromHtml("<$listTag><li>first item</li><li>second item</li></$listTag>not in list")
        editText.setSelection(editText.length())
        editText.text.insert(editText.text.indexOf("not in list") - 1, " (appended)")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item (appended)</li></$listTag>not in list", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun openListByMovingOutsideTextInsideList() {
        editText.fromHtml("<$listTag><li>first item</li><li>second item</li></$listTag>not in list")
        editText.setSelection(editText.length())
        editText.text.delete(editText.text.indexOf("not in list") - 1, editText.text.indexOf("not in list"))
        Assert.assertEquals("<$listTag><li>first item</li><li>second itemnot in list</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun listRemainsClosedWhenLastCharacterIsDeleted() {
        editText.fromHtml("<$listTag><li>first item</li><li>second item</li></$listTag>not in list")
        editText.setSelection(editText.length())

        //delete last character from "second item"
        editText.text.delete(editText.text.indexOf("not in list") - 2, editText.text.indexOf("not in list") - 1)
        Assert.assertEquals("<$listTag><li>first item</li><li>second ite</li></$listTag>not in list", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun openingAndReopeningOfList() {
        editText.fromHtml("<$listTag><li>first item</li><li>second item</li></$listTag>")
        editText.setSelection(editText.length())

        editText.append("\n")
        editText.append("third item")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag>", editText.toHtml())
        editText.append("\n")
        editText.append("\n")
        val mark = editText.length() - 1
        editText.append("not in the list")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag>not in the list", editText.toHtml())
        editText.append("\n")
        editText.append("foo")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag>not in the list<br>foo", editText.toHtml())

        //reopen list
        editText.text.delete(mark, mark + 1)
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third itemnot in the list</li></$listTag>foo", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun closeList() {
        editText.fromHtml("<$listTag><li>first item</li><li>second item</li></$listTag>")
        editText.setSelection(editText.length())

        editText.append("\n")
        val mark = editText.length() - 1

        editText.text.delete(mark, mark + 1)
        editText.append("not in the list")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li></$listTag>not in the list", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun handleListReopeningAfterLastElementDeletion() {
        editText.fromHtml("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag>")
        editText.setSelection(editText.length())

        editText.text.delete(editText.text.indexOf("third item", 0), editText.length())

        editText.append("not in the list")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li></$listTag>not in the list", editText.toHtml())

        editText.text.insert(editText.text.indexOf("not in the list") - 1, " addition")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item addition</li></$listTag>not in the list", editText.toHtml())

        editText.text.insert(editText.text.indexOf("not in the list") - 1, "\n")
        editText.text.insert(editText.text.indexOf("not in the list") - 1, "third item")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item addition</li><li>third item</li></$listTag>not in the list", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun additionToClosedList() {
        editText.toggleFormatting(listType)
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")

        val mark = editText.length()

        editText.append("\n")
        editText.append("\n")
        editText.append("not in the list")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li></$listTag>not in the list", editText.toHtml().toString())

        editText.text.insert(mark, " (addition)")

        Assert.assertEquals("<$listTag><li>first item</li><li>second item (addition)</li></$listTag>not in the list", editText.toHtml().toString())
    }

    @Test
    @Throws(Exception::class)
    fun toggleListType() {

        val oppositeTextFormat = if (listType == TextFormat.FORMAT_ORDERED_LIST)
            TextFormat.FORMAT_UNORDERED_LIST else TextFormat.FORMAT_ORDERED_LIST

        val oppositeTag = if (listTag.equals("ol")) "ul" else "ol"

        editText.fromHtml("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag>")
        editText.setSelection(editText.length())

        editText.toggleFormatting(oppositeTextFormat)

        Assert.assertEquals("<$oppositeTag><li>first item</li><li>second item</li><li>third item</li></$oppositeTag>", editText.toHtml().toString())

        editText.toggleFormatting(listType)
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag>", editText.toHtml().toString())
    }


    @Test
    @Throws(Exception::class)
    fun addItemToListFromBottom() {
        editText.toggleFormatting(listType)
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")
        editText.append("\n")
        editText.append("\n")
        editText.append("third item")
        editText.setSelection(editText.length())

        editText.toggleFormatting(listType)

        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun addItemToListFromTop() {
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")
        editText.setSelection(editText.length())
        editText.toggleFormatting(listType)
        editText.append("\n")
        editText.append("third item")

        editText.setSelection(0)

        editText.toggleFormatting(listType)

        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag>", editText.toHtml())
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

        Assert.assertEquals("<$listTag><li>first item</li></$listTag>second item<$listTag><li>third item</li></$listTag>", editText.toHtml())
        editText.setSelection(15)
        editText.toggleFormatting(listType)

        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun appendToListFromTopAtFirstLine() {
        editText.toggleFormatting(listType)
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")
        editText.setSelection(0)
        editText.text.insert(0,"addition ")

        Assert.assertEquals("<$listTag><li>addition first item</li><li>second item</li></$listTag>", editText.toHtml())
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

        Assert.assertEquals("not in list<$listTag><li>addition first item</li><li>second item</li></$listTag>", editText.toHtml())
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

        Assert.assertEquals("<$listTag><li>first item</li><li></li><li>third item</li></$listTag>", editText.toHtml())

        editText.text.delete(0,firstMark)

        Assert.assertEquals("<$listTag><li></li><li>third item</li></$listTag>", editText.toHtml())
    }
}
