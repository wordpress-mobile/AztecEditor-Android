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
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li></$listTag><br>not in the list", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun closingEmptyList() {
        editText.toggleFormatting(listType)
        editText.append("\n")
        Assert.assertEquals("", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun extendingListBySplittingItems() {
        editText.toggleFormatting(listType)
        editText.append("firstitem")
        editText.text.insert(5, "\n")
        Assert.assertEquals("<$listTag><li>first</li><li>item</li></$listTag>", editText.toHtml())
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
    fun splitTwoListsWithNewline() {
        editText.fromHtml("<$listTag><li>List 1</li></$listTag><$listTag><li>List 2</li></$listTag>")
        val mark = editText.text.indexOf("List 2")
        editText.text.insert(mark, "\n")
        Assert.assertEquals("<$listTag><li>List 1</li></$listTag><br><$listTag><li>List 2</li></$listTag>", editText.toHtml())
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

        Assert.assertEquals("<$listTag><li>first item</li></$listTag>second item<br>third item<$listTag><li>fourth item</li></$listTag><br>not in list", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun emptyBulletSurroundedBytItems() {
        editText.toggleFormatting(listType)
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")
        editText.append("\n")
        editText.append("third item")

        val start = editText.text.indexOf("second item")
        val end = start + "second item".length

        editText.text.delete(start, end)

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

        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag><br>", editText.toHtml())

        editText.append("not in list")
        editText.setSelection(mark)
        editText.text.insert(mark, "\n")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li><li></li></$listTag><br>not in list", editText.toHtml())
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
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag><br>not in the list", editText.toHtml())
        editText.append("\n")
        editText.append("foo")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag><br>not in the list<br>foo", editText.toHtml())

        //reopen list
        editText.text.delete(mark - 1, mark + 1)
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third itemnot in the list</li></$listTag>foo", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun closeList() {
        editText.fromHtml("<$listTag><li>first item</li><li>second item</li></$listTag>")
        editText.setSelection(editText.length())

        editText.append("\n")
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

        editText.append("\n")
        editText.append("not in the list")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li></$listTag><br>not in the list", editText.toHtml())

        editText.text.insert(editText.text.indexOf("not in the list") - 2, " addition")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item addition</li></$listTag><br>not in the list", editText.toHtml())

        editText.text.insert(editText.text.indexOf("not in the list") - 2, "\n")
        editText.text.insert(editText.text.indexOf("not in the list") - 2, "third item")
        Assert.assertEquals("<$listTag><li>first item</li><li>second item addition</li><li>third item</li></$listTag><br>not in the list", editText.toHtml())
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
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li></$listTag><br>not in the list", editText.toHtml())

        editText.text.insert(mark, " (addition)")

        Assert.assertEquals("<$listTag><li>first item</li><li>second item (addition)</li></$listTag><br>not in the list", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun toggleListType() {

        val oppositeTextFormat = if (listType == TextFormat.FORMAT_ORDERED_LIST)
            TextFormat.FORMAT_UNORDERED_LIST else TextFormat.FORMAT_ORDERED_LIST

        val oppositeTag = if (listTag == "ol") "ul" else "ol"

        editText.fromHtml("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag>")
        editText.setSelection(editText.length())

        editText.toggleFormatting(oppositeTextFormat)

        Assert.assertEquals("<$oppositeTag><li>first item</li><li>second item</li><li>third item</li></$oppositeTag>", editText.toHtml())

        editText.toggleFormatting(listType)
        Assert.assertEquals("<$listTag><li>first item</li><li>second item</li><li>third item</li></$listTag>", editText.toHtml())
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
        editText.text.delete(editText.length() - 1, editText.length())
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
        editText.text.delete(editText.length() - 1, editText.length())
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

        Assert.assertEquals("<$listTag><li></li><li></li><li>third item</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addMultipleEmptyItemsWithKeyboard() {
        editText.toggleFormatting(listType)
        editText.append("item")
        editText.text.insert(0, "\n")
        Assert.assertEquals("<$listTag><li></li><li>item</li></$listTag>", editText.toHtml())

        editText.text.insert(1, "\n")
        editText.text.insert(2, "\n")
        Assert.assertEquals("<$listTag><li></li><li></li><li></li><li>item</li></$listTag>", editText.toHtml())

        editText.append("\n")
        editText.append("\n")
        Assert.assertEquals("<$listTag><li></li><li></li><li></li><li>item</li></$listTag><br>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun closeListWithEmptyLineBelowIt() {
        editText.fromHtml("<$listTag><li>Ordered</li></$listTag><br>not in list")

        //remove newline after list (put cursor on newline after list and press backspace)
        val mark = editText.text.indexOf("Ordered") + "Ordered".length
        editText.text.delete(mark, mark + 1)
        Assert.assertEquals("<$listTag><li>Ordered</li></$listTag>not in list", editText.toHtml())

        //press enter twice after at the end of the list to add new item and then remove it and close list
        editText.setSelection(mark)
        editText.text.insert(mark, "\n")

        // must add 2 because of the extra ZWJ char
        editText.setSelection(mark + 2)
        editText.text.insert(mark + 2, "\n")
        Assert.assertEquals("<$listTag><li>Ordered</li></$listTag><br><br>not in list", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun closeListWithTrailingEmptyItem() {
        editText.fromHtml("<$listTag><li>Ordered</li><li></li></$listTag>")

        //insert newline after empty list item to remove it and close the list (put cursor on empty list item and pres enter)
        val mark = editText.text.indexOf("Ordered") + "Ordered".length + 1 // must add 2 because of the extra ZWJ char
        editText.setSelection(mark)
        editText.text.insert(editText.selectionEnd, "\n")

        Assert.assertEquals("<$listTag><li>Ordered</li></$listTag><br>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addLinebreaksAfterListWithEmptyItem() {
        editText.fromHtml("<$listTag><li>item</li><li></li></$listTag>after")

        editText.text.insert(editText.text.indexOf("after"), "\n")
        Assert.assertEquals("<$listTag><li>item</li><li></li></$listTag><br>after", editText.toHtml())

        editText.text.insert(editText.text.indexOf("after"), "\n")
        Assert.assertEquals("<$listTag><li>item</li><li></li></$listTag><br><br>after", editText.toHtml())

        editText.text.delete(editText.text.indexOf("after") - 1, editText.text.indexOf("after"))
        Assert.assertEquals("<$listTag><li>item</li><li></li></$listTag><br>after", editText.toHtml())

        editText.text.delete(editText.text.indexOf("after") - 1, editText.text.indexOf("after"))
        Assert.assertEquals("<$listTag><li>item</li><li></li></$listTag>after", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addLinebreaksAfterListWithNonEmptyItem() {
        editText.fromHtml("<$listTag><li>item</li><li>item2</li></$listTag>after")

        editText.text.insert(editText.text.indexOf("after"), "\n")
        Assert.assertEquals("<$listTag><li>item</li><li>item2</li></$listTag><br>after", editText.toHtml())

        editText.text.insert(editText.text.indexOf("after"), "\n")
        Assert.assertEquals("<$listTag><li>item</li><li>item2</li></$listTag><br><br>after", editText.toHtml())

        editText.text.delete(editText.text.indexOf("after") - 1, editText.text.indexOf("after"))
        Assert.assertEquals("<$listTag><li>item</li><li>item2</li></$listTag><br>after", editText.toHtml())

        editText.text.delete(editText.text.indexOf("after") - 1, editText.text.indexOf("after"))
        Assert.assertEquals("<$listTag><li>item</li><li>item2</li></$listTag>after", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun deleteTextFromLastItemAndCheckForZwjChar() {
        editText.fromHtml("<$listTag><li>item</li><li></li><li></li><li>item2</li></$listTag>after")

        Assert.assertTrue(editText.text.indexOf(Constants.ZWJ_CHAR) == -1)

        val mark = editText.text.indexOf("item2")
        editText.text.delete(mark, mark + "item2".length)
        Assert.assertEquals(editText.text[mark], Constants.ZWJ_CHAR)
        Assert.assertEquals("<$listTag><li>item</li><li></li><li></li><li></li></$listTag>after", editText.toHtml())

        editText.text.delete(mark, mark + 1)
        Assert.assertEquals("<$listTag><li>item</li><li></li><li></li></$listTag>after", editText.toHtml())
        Assert.assertEquals(editText.text[mark - 1], Constants.ZWJ_CHAR)
    }


    @Test
    @Throws(Exception::class)
    fun checkIfZwjCharAddedToLastEmptyListItem() {
        editText.fromHtml("before<$listTag><li>item</li><li></li><li></li></$listTag>after")

        val mark = editText.text.indexOf("after") - 2
        Assert.assertEquals(editText.text[mark], Constants.ZWJ_CHAR)

        editText.fromHtml("<$listTag><li>item</li><li></li></$listTag>")

        Assert.assertEquals(editText.text.last(), Constants.ZWJ_CHAR)
    }

    @Test
    @Throws(Exception::class)
    fun addTwoNewlinesAfterList() {
        editText.fromHtml("<$listTag><li>a</li><li>b</li></$listTag>")

        editText.setSelection(editText.length())
        editText.append("\n")
        editText.append("\n")
        Assert.assertEquals("<$listTag><li>a</li><li>b</li></$listTag><br>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun deleteEmptyListItemWithBackspace() {
        editText.fromHtml("<$listTag><li>a</li><li>b</li></$listTag>")

        editText.setSelection(editText.text.length)
        editText.append("\n")
        editText.text.delete(editText.text.length - 1, editText.text.length)
        Assert.assertEquals("<$listTag><li>a</li><li>b</li></$listTag>", editText.toHtml())

        editText.append("\n")
        editText.append("\n")
        editText.text.delete(editText.text.length - 1, editText.text.length)
        editText.append("c")
        Assert.assertEquals("a\nb\nc", editText.text.toString())
        Assert.assertEquals("<$listTag><li>a</li><li>b</li></$listTag>c", editText.toHtml())

        editText.text.delete(editText.text.length - 1, editText.text.length)
        Assert.assertEquals("<$listTag><li>a</li><li>b</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun appendTextToLastItemWithBacskpace() {
        editText.fromHtml("<$listTag><li>a</li><li>b</li></$listTag>c")

        val mark = editText.text.indexOf("c")
        editText.text.delete(mark - 1, mark)
        Assert.assertEquals("<$listTag><li>a</li><li>bc</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun appendTextToEmptyLastItemWithBacskpace() {
        editText.fromHtml("<$listTag><li>a</li><li></li><li></li></$listTag>c")

        var mark = editText.text.indexOf("c")
        editText.text.delete(mark - 1, mark)
        Assert.assertEquals("a\n\nc", editText.text.toString())
        Assert.assertEquals("<$listTag><li>a</li><li></li><li>c</li></$listTag>", editText.toHtml())

        mark = editText.text.indexOf("c")
        editText.text.delete(mark - 1, mark)
        Assert.assertEquals("a\nc", editText.text.toString())
        Assert.assertEquals("<$listTag><li>a</li><li>c</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun deleteSecondEmptyLineAndTestForZwjCharOnFirst() {
        editText.fromHtml("<$listTag><li></li><li></li></$listTag>")

        editText.setSelection(editText.length())
        editText.text.delete(editText.length() - 1, editText.length())

        Assert.assertEquals(Constants.ZWJ_STRING, editText.text.toString())
        Assert.assertEquals("<$listTag><li></li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun deleteLastItemFromList() {
        editText.fromHtml("<$listTag><li>a</li></$listTag>")

        editText.setSelection(editText.length())
        editText.text.delete(editText.length() - 1, editText.length())

        Assert.assertEquals(Constants.ZWJ_STRING, editText.text.toString())
        Assert.assertEquals("<$listTag><li></li></$listTag>", editText.toHtml())

        editText.text.delete(editText.length() - 1, editText.length())
        Assert.assertEquals("", editText.text.toString())
    }

    @Test
    @Throws(Exception::class)
    fun deleteLastItemFromListWithTextAbove() {
        editText.fromHtml("abc<$listTag><li>a</li></$listTag>")

        editText.setSelection(editText.length())
        editText.text.delete(editText.length() - 1, editText.length())

        Assert.assertEquals("abc\n" + Constants.ZWJ_STRING, editText.text.toString())
        Assert.assertEquals("abc<$listTag><li></li></$listTag>", editText.toHtml())

        editText.text.delete(editText.length() - 1, editText.length())
        Assert.assertEquals("abc\n", editText.text.toString())
        Assert.assertEquals("abc<br>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addTwoLinesThenDeleteTheList() {
        editText.fromHtml("<$listTag><li></li></$listTag>")

        editText.append("a")
        editText.append("\n")
        editText.append("b")
        Assert.assertEquals("<$listTag><li>a</li><li>b</li></$listTag>", editText.toHtml())

        editText.text.delete(editText.length() - 1, editText.length())
        Assert.assertEquals("a\n" + Constants.ZWJ_CHAR, editText.text.toString())

        editText.text.delete(editText.length() - 1, editText.length())
        Assert.assertEquals("a", editText.text.toString())

        editText.text.delete(editText.length() - 1, editText.length())
        Assert.assertEquals(Constants.ZWJ_STRING, editText.text.toString())
        Assert.assertEquals("<$listTag><li></li></$listTag>", editText.toHtml())

        editText.text.delete(editText.length() - 1, editText.length())
        Assert.assertEquals("", editText.text.toString())
        Assert.assertEquals("", editText.toHtml())
    }
}
