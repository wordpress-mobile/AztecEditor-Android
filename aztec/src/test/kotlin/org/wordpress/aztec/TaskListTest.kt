package org.wordpress.aztec

import android.app.Activity
import android.view.MenuItem
import android.widget.PopupMenu
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.Robolectric
import org.wordpress.aztec.source.SourceViewEditText
import org.wordpress.aztec.toolbar.AztecToolbar
import org.wordpress.aztec.watchers.EndOfBufferMarkerAdder

/**
 * Testing ordered and unordered lists.
 *
 * This test uses ParameterizedRobolectricTestRunner and runs twice - for ol and ul tags.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class TaskListTest(listTextFormat: ITextFormat, listHtmlTag: String) {

    val listType = listTextFormat
    val listTag = listHtmlTag

    private val listStartTag = "$listTag type=\"task-list\""
    private val checkbox = "<input type=\"checkbox\" class=\"task-list-item-checkbox\" />"

    val otherListType = AztecTextFormat.FORMAT_ORDERED_LIST
    val otherListTag = "ol"

    lateinit var editText: AztecText
    lateinit var menuList: PopupMenu
    lateinit var menuListOrdered: MenuItem
    lateinit var menuListUnordered: MenuItem
    lateinit var menuListTaskList: MenuItem
    lateinit var sourceText: SourceViewEditText
    lateinit var toolbar: AztecToolbar

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "Testing lists with {1} tag")
        fun data(): Collection<Array<Any>> {
            return listOf(
                    arrayOf(AztecTextFormat.FORMAT_TASK_LIST, "ul")
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
        editText.setCalypsoMode(false)
        sourceText = SourceViewEditText(activity)
        sourceText.setCalypsoMode(false)
        toolbar = AztecToolbar(activity)
        toolbar.enableTaskList()
        toolbar.setEditor(editText, sourceText)
        menuList = toolbar.getListMenu() as PopupMenu
        menuListTaskList = menuList.menu.getItem(2)
        menuListOrdered = menuList.menu.getItem(1)
        menuListUnordered = menuList.menu.getItem(0)
        activity.setContentView(editText)
    }

    // enter text and then enable styling

    @Test
    @Throws(Exception::class)
    fun styleSingleItem() {
        TestUtils.safeAppend(editText, "first item")
        editText.toggleFormatting(listType)
        Assert.assertEquals("<$listStartTag><li>${checkbox}first item</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleMultipleSelectedItems() {
        Assert.assertTrue(TestUtils.safeEmpty(editText))
        TestUtils.safeAppend(editText, "first item")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "second item")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "third item")
        editText.setSelection(0, editText.length())

        editText.toggleFormatting(listType)
        Assert.assertEquals("<$listStartTag><li>${checkbox}first item</li><li>${checkbox}second item</li><li>${checkbox}third item</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun stylePartiallySelectedMultipleItems() {
        Assert.assertTrue(TestUtils.safeEmpty(editText))
        TestUtils.safeAppend(editText, "first item")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "second item")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "third item")
        editText.setSelection(4, 15) // we partially selected first and second item

        editText.toggleFormatting(listType)
        Assert.assertEquals("<$listStartTag><li>${checkbox}first item</li><li>${checkbox}second item</li></$listTag>third item", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleSurroundedItem() {
        Assert.assertTrue(TestUtils.safeEmpty(editText))
        TestUtils.safeAppend(editText, "first item")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "second item")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "third item")
        editText.setSelection(14)

        editText.toggleFormatting(listType)
        Assert.assertEquals("first item<$listStartTag><li>${checkbox}second item</li></$listTag>third item", editText.toHtml())
    }

    // enable styling on empty line and enter text

    @Test
    @Throws(Exception::class)
    fun styleSingleEnteredItem() {
        editText.toggleFormatting(listType)
        TestUtils.safeAppend(editText, "first item")
        Assert.assertEquals("<$listStartTag><li>${checkbox}first item</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleMultipleEnteredItems() {
        editText.toggleFormatting(listType)
        TestUtils.safeAppend(editText, "first item")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "second item")
        Assert.assertEquals("<$listStartTag><li>${checkbox}first item</li><li>${checkbox}second item</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun closingPopulatedList() {
        editText.toggleFormatting(listType)
        TestUtils.safeAppend(editText, "first item")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "second item")

        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "not in the list")
        Assert.assertEquals("<$listStartTag><li>${checkbox}first item</li><li>${checkbox}second item</li></$listTag>not in the list", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun closingEmptyList() {
        editText.toggleFormatting(listType)
        TestUtils.safeAppend(editText, "\n")
        Assert.assertEquals("", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun extendingListBySplittingItems() {
        editText.toggleFormatting(listType)
        TestUtils.safeAppend(editText, "firstitem")
        editText.text.insert(5, "\n")
        Assert.assertEquals("<$listStartTag><li>${checkbox}first</li><li>${checkbox}item</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun splitTwoListsWithNewline() {
        editText.fromHtml("<$listStartTag><li>${checkbox}List 1</li></$listTag><$listStartTag><li>${checkbox}List 2</li></$listTag>")
        val mark2 = editText.text.indexOf("List 2")
        editText.text.insert(mark2, "\n")
        Assert.assertEquals("<$listStartTag><li>${checkbox}List 1</li></$listTag><$listStartTag><li>$checkbox</li><li>${checkbox}List 2</li></$listTag>", editText.toHtml())

        val mark1 = editText.text.indexOf("List 1") + "List 1".length
        editText.text.insert(mark1, "\n")
        Assert.assertEquals("<$listStartTag><li>${checkbox}List 1</li><li>$checkbox</li></$listTag><$listStartTag><li>$checkbox</li><li>${checkbox}List 2</li></$listTag>", editText.toHtml())

        editText.text.insert(mark1 + 1, "\n")
        Assert.assertEquals("<$listStartTag><li>${checkbox}List 1</li></$listTag><br><$listStartTag><li>$checkbox</li><li>${checkbox}List 2</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun emptyBulletSurroundedBytItems() {
        editText.toggleFormatting(listType)
        TestUtils.safeAppend(editText, "first item")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "second item")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "third item")

        val start = editText.text.indexOf("second item")
        val end = start + "second item".length

        editText.text.delete(start, end)

        Assert.assertEquals("<$listStartTag><li>${checkbox}first item</li><li>$checkbox</li><li>${checkbox}third item</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun trailingEmptyBulletPoint() {
        editText.toggleFormatting(listType)
        TestUtils.safeAppend(editText, "first item")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "second item")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "third item")
        val mark = editText.length()
        TestUtils.safeAppend(editText, "\n")

        Assert.assertEquals("<$listStartTag><li>${checkbox}first item</li><li>${checkbox}second item</li><li>${checkbox}third item</li><li>$checkbox</li></$listTag>", editText.toHtml())
        TestUtils.safeAppend(editText, "\n")

        Assert.assertEquals("<$listStartTag><li>${checkbox}first item</li><li>${checkbox}second item</li><li>${checkbox}third item</li></$listTag>", editText.toHtml())

        TestUtils.safeAppend(editText, "not in list")
        editText.setSelection(mark)
        editText.text.insert(mark, "\n")
        Assert.assertEquals("<$listStartTag><li>${checkbox}first item</li><li>${checkbox}second item</li><li>${checkbox}third item</li><li>$checkbox</li></$listTag>not in list", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun openListByAddingNewline() {
        editText.fromHtml("<$listStartTag><li>${checkbox}first item</li><li>${checkbox}second item</li></$listTag>not in list")
        editText.setSelection(editText.length())

        editText.text.insert(editText.text.indexOf("not in list") - 1, "\n")
        editText.text.insert(editText.text.indexOf("not in list") - 1, "third item")
        Assert.assertEquals("<$listStartTag><li>${checkbox}first item</li><li>${checkbox}second item</li><li>${checkbox}third item</li></$listTag>not in list", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun openListByAppendingTextToTheEnd() {
        editText.fromHtml("<$listStartTag><li>${checkbox}first item</li><li>${checkbox}second item</li></$listTag>not in list")
        editText.setSelection(editText.length())
        editText.text.insert(editText.text.indexOf("not in list") - 1, " (appended)")
        Assert.assertEquals("<$listStartTag><li>${checkbox}first item</li><li>${checkbox}second item (appended)</li></$listTag>not in list", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun openListByMovingOutsideTextInsideList() {
        editText.fromHtml("<$listStartTag><li>${checkbox}first item</li><li>${checkbox}second item</li></$listTag>not in list")
        editText.setSelection(editText.length())
        editText.text.delete(editText.text.indexOf("not in list") - 1, editText.text.indexOf("not in list"))
        Assert.assertEquals("<$listStartTag><li>${checkbox}first item</li><li>${checkbox}second itemnot in list</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun listRemainsClosedWhenLastCharacterIsDeleted() {
        editText.fromHtml("<$listStartTag><li>first item</li><li>second item</li></$listTag>not in list")
        editText.setSelection(editText.length())

        // delete last character from "second item"
        editText.text.delete(editText.text.indexOf("not in list") - 2, editText.text.indexOf("not in list") - 1)
        Assert.assertEquals("<$listStartTag><li>first item</li><li>second ite</li></$listTag>not in list", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun openingAndReopeningOfList() {
        editText.fromHtml("<$listStartTag><li>${checkbox}first item</li><li>${checkbox}second item</li></$listTag>")
        editText.setSelection(editText.length())

        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "third item")
        Assert.assertEquals("<$listStartTag><li>${checkbox}first item</li><li>${checkbox}second item</li><li>${checkbox}third item</li></$listTag>", editText.toHtml())
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "not in the list")
        val mark = editText.text.indexOf("not in the list")
        Assert.assertEquals("<$listStartTag><li>${checkbox}first item</li><li>${checkbox}second item</li><li>${checkbox}third item</li></$listTag>not in the list", editText.toHtml())
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "foo")
        Assert.assertEquals("<$listStartTag><li>${checkbox}first item</li><li>${checkbox}second item</li><li>${checkbox}third item</li></$listTag>not in the list<br>foo", editText.toHtml())

        // reopen list
        editText.text.delete(mark - 1, mark) // delete the newline before the not-in-the-list text
        Assert.assertEquals("<$listStartTag><li>${checkbox}first item</li><li>${checkbox}second item</li><li>${checkbox}third itemnot in the list</li></$listTag>foo", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun closeList() {
        editText.fromHtml("<$listStartTag><li>first item</li><li>second item</li></$listTag>")
        editText.setSelection(editText.length())

        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "\n")
        val mark = editText.length() - 1

        editText.text.delete(mark, mark + 1)
        TestUtils.safeAppend(editText, "not in the list")
        Assert.assertEquals("<$listStartTag><li>first item</li><li>second item</li></$listTag>not in the list", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun handleListReopeningAfterLastElementDeletion() {
        editText.fromHtml("<$listStartTag><li>${checkbox}first item</li><li>${checkbox}second item</li><li>${checkbox}third item</li></$listTag>")
        editText.setSelection(editText.length())

        editText.text.delete(editText.text.indexOf("third item", 0), editText.length())

        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "not in the list")
        Assert.assertEquals("<$listStartTag><li>${checkbox}first item</li><li>${checkbox}second item</li></$listTag>not in the list", editText.toHtml())

        editText.text.insert(editText.text.indexOf("not in the list") - 1, " addition")
        Assert.assertEquals("<$listStartTag><li>${checkbox}first item</li><li>${checkbox}second item addition</li></$listTag>not in the list", editText.toHtml())

        editText.text.insert(editText.text.indexOf("not in the list") - 1, "\n")
        editText.text.insert(editText.text.indexOf("not in the list") - 1, "third item")
        Assert.assertEquals("<$listStartTag><li>${checkbox}first item</li><li>${checkbox}second item addition</li><li>${checkbox}third item</li></$listTag>not in the list", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun additionToClosedList() {
        editText.toggleFormatting(listType)
        TestUtils.safeAppend(editText, "first item")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "second item")

        val mark = editText.length()

        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "not in the list")
        Assert.assertEquals("<$listStartTag><li>${checkbox}first item</li><li>${checkbox}second item</li></$listTag>not in the list", editText.toHtml())

        editText.text.insert(mark, " (addition)")

        Assert.assertEquals("<$listStartTag><li>${checkbox}first item</li><li>${checkbox}second item (addition)</li></$listTag>not in the list", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun toggleListType() {

        val oppositeTextFormat = if (listType == AztecTextFormat.FORMAT_ORDERED_LIST)
            AztecTextFormat.FORMAT_UNORDERED_LIST else AztecTextFormat.FORMAT_ORDERED_LIST

        val oppositeTag = if (listTag == "ol") "ul" else "ol"

        editText.fromHtml("<$listStartTag><li>first item</li><li>second item</li><li>third item</li></$listTag>")
        editText.setSelection(editText.length())

        editText.toggleFormatting(oppositeTextFormat)

        Assert.assertEquals("<$oppositeTag><li>first item</li><li>second item</li><li>third item</li></$oppositeTag>", editText.toHtml())

        editText.toggleFormatting(listType)
        Assert.assertEquals("<$listStartTag><li>${checkbox}first item</li><li>${checkbox}second item</li><li>${checkbox}third item</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addItemToListFromBottom() {
        editText.toggleFormatting(listType)
        TestUtils.safeAppend(editText, "first item")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "second item")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "\n")
        editText.text.delete(editText.length() - 1, editText.length())
        TestUtils.safeAppend(editText, "third item")
        editText.setSelection(editText.length())

        editText.toggleFormatting(listType)

        Assert.assertEquals("<$listStartTag><li>${checkbox}first item</li><li>${checkbox}second item</li><li>${checkbox}third item</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addItemToListFromTop() {
        TestUtils.safeAppend(editText, "first item")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "second item")
        editText.setSelection(editText.length())
        editText.toggleFormatting(listType)
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "third item")

        editText.setSelection(0)

        editText.toggleFormatting(listType)

        Assert.assertEquals("<$listStartTag><li>${checkbox}first item</li><li>${checkbox}second item</li><li>${checkbox}third item</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addItemToListFromInside() {
        editText.toggleFormatting(listType)
        TestUtils.safeAppend(editText, "first item")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "\n")
        editText.text.delete(editText.length() - 1, editText.length())
        TestUtils.safeAppend(editText, "second item")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "third item")
        editText.setSelection(editText.length())
        editText.toggleFormatting(listType)

        Assert.assertEquals("<$listStartTag><li>${checkbox}first item</li></$listTag>second item<$listStartTag><li>${checkbox}third item</li></$listTag>", editText.toHtml())
        editText.setSelection(15)
        editText.toggleFormatting(listType)

        Assert.assertEquals("<$listStartTag><li>${checkbox}first item</li><li>${checkbox}second item</li><li>${checkbox}third item</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun appendToListFromTopAtFirstLine() {
        editText.toggleFormatting(listType)
        TestUtils.safeAppend(editText, "first item")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "second item")
        editText.setSelection(0)
        editText.text.insert(0, "addition ")

        Assert.assertEquals("<$listStartTag><li>${checkbox}addition first item</li><li>${checkbox}second item</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun appendToListFromTop() {
        TestUtils.safeAppend(editText, "not in list")
        TestUtils.safeAppend(editText, "\n")
        editText.toggleFormatting(listType)
        val mark = editText.length() - 1
        TestUtils.safeAppend(editText, "first item")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "second item")

        editText.setSelection(mark)
        editText.text.insert(mark, "addition ")

        Assert.assertEquals("not in list<$listStartTag><li>${checkbox}addition first item</li><li>${checkbox}second item</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun deleteFirstItemWithKeyboard() {
        editText.toggleFormatting(listType)
        TestUtils.safeAppend(editText, "first item")
        val firstMark = editText.length()
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "second item")
        val secondMark = editText.length()
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "third item")
        editText.setSelection(0)
        editText.text.delete(firstMark + 1, secondMark)

        Assert.assertEquals("<$listStartTag><li>${checkbox}first item</li><li>$checkbox</li><li>${checkbox}third item</li></$listTag>", editText.toHtml())

        editText.text.delete(0, firstMark)

        Assert.assertEquals("<$listStartTag><li>$checkbox</li><li>$checkbox</li><li>${checkbox}third item</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addMultipleEmptyItemsWithKeyboard() {
        editText.toggleFormatting(listType)
        TestUtils.safeAppend(editText, "item")
        editText.text.insert(0, "\n")
        Assert.assertEquals("<$listStartTag><li>$checkbox</li><li>${checkbox}item</li></$listTag>", editText.toHtml())

        editText.text.insert(1, "\n")
        editText.text.insert(2, "\n")
        Assert.assertEquals("<$listStartTag><li>$checkbox</li><li>$checkbox</li><li>$checkbox</li><li>${checkbox}item</li></$listTag>", editText.toHtml())

        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "\n")
        Assert.assertEquals("<$listStartTag><li>$checkbox</li><li>$checkbox</li><li>$checkbox</li><li>${checkbox}item</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun closeListWithEmptyLineBelowIt() {
        editText.fromHtml("<$listStartTag><li>Ordered</li></$listTag><br>not in list")

        // remove newline after list (put cursor on newline after list and press backspace)
        val mark = editText.text.indexOf("Ordered") + "Ordered".length
        editText.text.delete(mark + 1, mark + 2) // +1 to cater for the item's endline
        Assert.assertEquals("<$listStartTag><li>Ordered</li></$listTag>not in list", editText.toHtml())

        // press enter twice after at the end of the list to add new item and then remove it and close list
        editText.setSelection(mark)
        editText.text.insert(mark, "\n")

        editText.setSelection(mark + 1)
        editText.text.insert(mark + 1, "\n")
        Assert.assertEquals("<$listStartTag><li>Ordered</li></$listTag><br>not in list", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun closeListWithTrailingEmptyItem() {
        editText.fromHtml("<$listStartTag><li>Ordered</li><li></li></$listTag>")

        // insert newline after empty list item to remove it and close the list (put cursor on empty list item and pres enter)
        val mark = editText.text.indexOf("Ordered") + "Ordered".length + 1 // must add 1 because of the newline at item end
        editText.setSelection(mark)
        editText.text.insert(editText.selectionEnd, "\n")

        Assert.assertEquals("<$listStartTag><li>Ordered</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addLinebreaksAfterListWithEmptyItem() {
        editText.fromHtml("<$listStartTag><li>item</li><li></li></$listTag>after")

        editText.text.insert(editText.text.indexOf("after"), "\n")
        Assert.assertEquals("<$listStartTag><li>item</li><li></li></$listTag><br>after", editText.toHtml())

        editText.text.insert(editText.text.indexOf("after"), "\n")
        Assert.assertEquals("<$listStartTag><li>item</li><li></li></$listTag><br><br>after", editText.toHtml())

        editText.text.delete(editText.text.indexOf("after") - 1, editText.text.indexOf("after"))
        Assert.assertEquals("<$listStartTag><li>item</li><li></li></$listTag><br>after", editText.toHtml())

        editText.text.delete(editText.text.indexOf("after") - 1, editText.text.indexOf("after"))
        Assert.assertEquals("<$listStartTag><li>item</li><li></li></$listTag>after", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addLinebreaksAfterListWithNonEmptyItem() {
        editText.fromHtml("<$listStartTag><li>item</li><li>item2</li></$listTag>after")

        editText.text.insert(editText.text.indexOf("after"), "\n")
        Assert.assertEquals("<$listStartTag><li>item</li><li>item2</li></$listTag><br>after", editText.toHtml())

        editText.text.insert(editText.text.indexOf("after"), "\n")
        Assert.assertEquals("<$listStartTag><li>item</li><li>item2</li></$listTag><br><br>after", editText.toHtml())

        editText.text.delete(editText.text.indexOf("after") - 1, editText.text.indexOf("after"))
        Assert.assertEquals("<$listStartTag><li>item</li><li>item2</li></$listTag><br>after", editText.toHtml())

        editText.text.delete(editText.text.indexOf("after") - 1, editText.text.indexOf("after"))
        Assert.assertEquals("<$listStartTag><li>item</li><li>item2</li></$listTag>after", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun deleteTextFromLastItem() {
        editText.fromHtml("<$listStartTag><li>item</li><li></li><li></li><li>item2</li></$listTag>after")

        val mark = editText.text.indexOf("item2")
        editText.text.delete(mark, mark + "item2".length)
        Assert.assertEquals("<$listStartTag><li>item</li><li></li><li></li><li></li></$listTag>after", editText.toHtml())

        editText.text.delete(mark - 1, mark)
        Assert.assertEquals("<$listStartTag><li>item</li><li></li><li></li></$listTag>after", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun checkIfEndOfTextMarkerAddedToLastEmptyListItem() {
        editText.fromHtml("before<$listStartTag><li>item</li><li></li><li></li></$listTag>")

        Assert.assertEquals(editText.text.last(), Constants.END_OF_BUFFER_MARKER)

        editText.fromHtml("<$listStartTag><li>item</li><li></li></$listTag>")

        Assert.assertEquals(editText.text.last(), Constants.END_OF_BUFFER_MARKER)
    }

    @Test
    @Throws(Exception::class)
    fun addTwoNewlinesAfterList() {
        editText.fromHtml("<$listStartTag><li>a</li><li>b</li></$listTag>")

        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "\n")
        Assert.assertEquals("<$listStartTag><li>a</li><li>b</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun deleteEmptyListItemWithBackspace() {
        editText.fromHtml("<$listStartTag><li>a</li><li>b</li></$listTag>")

        TestUtils.safeAppend(editText, "\n")
        editText.text.delete(TestUtils.safeLength(editText) - 1, TestUtils.safeLength(editText))
        Assert.assertEquals("<$listStartTag><li>a</li><li>b</li></$listTag>", editText.toHtml())

        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "c")
        Assert.assertEquals("a\nb\nc", editText.text.toString())
        Assert.assertEquals("<$listStartTag><li>a</li><li>b</li></$listTag>c", editText.toHtml())

        editText.text.delete(TestUtils.safeLength(editText) - 1, TestUtils.safeLength(editText))
        Assert.assertEquals("<$listStartTag><li>a</li><li>b</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun appendTextToLastItemWithBacskpace() {
        editText.fromHtml("<$listStartTag><li>a</li><li>b</li></$listTag>c")

        val mark = editText.text.indexOf("c")
        editText.text.delete(mark - 1, mark)
        Assert.assertEquals("<$listStartTag><li>a</li><li>bc</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun appendTextToEmptyLastItemWithBacskpace() {
        editText.fromHtml("<$listStartTag><li>a</li><li></li><li></li></$listTag>c")

        var mark = editText.text.indexOf("c")
        editText.text.delete(mark - 1, mark)
        Assert.assertEquals("a\n\nc", editText.text.toString())
        Assert.assertEquals("<$listStartTag><li>a</li><li></li><li>c</li></$listTag>", editText.toHtml())

        mark = editText.text.indexOf("c")
        editText.text.delete(mark - 1, mark)
        Assert.assertEquals("a\nc", editText.text.toString())
        Assert.assertEquals("<$listStartTag><li>a</li><li>c</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun deleteSecondEmptyLineAndTestForZwjCharOnFirst() {
        editText.fromHtml("<$listStartTag><li></li><li></li></$listTag>")

        editText.text.delete(TestUtils.safeLength(editText) - 1, TestUtils.safeLength(editText))

        Assert.assertEquals(Constants.ZWJ_STRING, editText.text.toString())
        Assert.assertEquals("<$listStartTag><li></li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun deleteLastItemFromList() {
        editText.fromHtml("<$listStartTag><li>a</li></$listTag>")

        editText.setSelection(editText.length())
        editText.text.delete(TestUtils.safeLength(editText) - 1, TestUtils.safeLength(editText))

        Assert.assertEquals(Constants.ZWJ_STRING, editText.text.toString())
        Assert.assertEquals("<$listStartTag><li></li></$listTag>", editText.toHtml())

        TestUtils.backspaceAt(editText, TestUtils.safeLength(editText))
        Assert.assertEquals("", editText.text.toString())
    }

    @Test
    @Throws(Exception::class)
    fun deleteLastItemFromListWithTextAbove() {
        editText.fromHtml("abc<$listStartTag><li>a</li></$listTag>")

        editText.text.delete(TestUtils.safeLength(editText) - 1, TestUtils.safeLength(editText))

        Assert.assertEquals(EndOfBufferMarkerAdder.ensureEndOfTextMarker("abc\n"), editText.text.toString())
        Assert.assertEquals("abc<$listStartTag><li></li></$listTag>", editText.toHtml())

        editText.text.delete(TestUtils.safeLength(editText) - 1, TestUtils.safeLength(editText))
        Assert.assertEquals(EndOfBufferMarkerAdder.ensureEndOfTextMarker("abc"), editText.text.toString())
        Assert.assertEquals("abc", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addTwoLinesThenDeleteTheList() {
        editText.fromHtml("<$listStartTag><li></li></$listTag>")

        TestUtils.safeAppend(editText, "a")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "b")
        Assert.assertEquals("<$listStartTag><li>a</li><li>${checkbox}b</li></$listTag>", editText.toHtml())

        editText.text.delete(TestUtils.safeLength(editText) - 1, TestUtils.safeLength(editText))
        Assert.assertEquals("a\n" + Constants.ZWJ_CHAR, editText.text.toString())

        editText.text.delete(TestUtils.safeLength(editText) - 1, TestUtils.safeLength(editText)) // don't delete the end-of-text marker
        Assert.assertEquals("a", editText.text.toString())

        editText.text.delete(TestUtils.safeLength(editText) - 1, TestUtils.safeLength(editText))
        Assert.assertEquals(Constants.ZWJ_STRING, editText.text.toString())
        Assert.assertEquals("<$listStartTag><li></li></$listTag>", editText.toHtml())

        // Send key event since that's the way AztecText will remove the style when text is empty
        TestUtils.backspaceAt(editText, TestUtils.safeLength(editText))
        Assert.assertEquals("", editText.text.toString())
        Assert.assertEquals("", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun nestListWithSimilarNeighboringList_issue288() {
        val preQuote = "<$listStartTag><li>Unordered1</li><li></li></$listTag>"
        val aftQuote = "<$listStartTag><li>Unordered2</li><li></li></$listTag>"
        editText.fromHtml(preQuote + "<blockquote>Quote</blockquote>" + aftQuote)

        editText.setSelection(editText.text.indexOf("Quote"))

        editText.toggleFormatting(listType)

        Assert.assertEquals("$preQuote<$listStartTag><li>$checkbox<blockquote>Quote</blockquote></li></$listTag>$aftQuote", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun nestedListsHaveVisualNewline() {
        val html = "outpre<blockquote><$listStartTag><li><$listStartTag><li>nested</li></$listTag></li></$listTag></blockquote>outaft"
        editText.fromHtml(html)

        val nestedPosition = editText.text.indexOf("nested")

        // there should be a (visual) newline between the nested list and the parent list item
        Assert.assertEquals(editText.text[nestedPosition - 1], '\n')

        // but not in the html
        Assert.assertEquals(html, editText.toHtml())
    }

    /**
     * Update list menu based on selection.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun listMenuSelection() {
        val html = "<ol><li>Ordered</li></ol>Text<ul><li>Unordered</li></ul>"
        editText.fromHtml(html)

        // Select ordered list.
        editText.setSelection(editText.text.indexOf("Ordered"))
        Assert.assertTrue(menuListOrdered.isChecked)
        Assert.assertFalse(menuListUnordered.isChecked)

        // Select neither ordered nor unordered list.
        editText.setSelection(editText.text.indexOf("Text"))
        Assert.assertFalse(menuListOrdered.isChecked)
        Assert.assertFalse(menuListUnordered.isChecked)

        // Select unordered list.
        editText.setSelection(editText.text.indexOf("Unordered"))
        Assert.assertFalse(menuListOrdered.isChecked)
        Assert.assertTrue(menuListUnordered.isChecked)
    }

    /**
     * Toggle ordered list button and type.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun listOrderedTyping() {
        Assert.assertFalse(menuListOrdered.isChecked)
        toolbar.onMenuItemClick(menuListOrdered)
        Assert.assertTrue(menuListOrdered.isChecked)
        editText.append("ordered")
        Assert.assertEquals("<ol><li>ordered</li></ol>", editText.toHtml())
    }

    /**
     * Toggle unordered list button and type.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun listUnorderedTyping() {
        Assert.assertFalse(menuListUnordered.isChecked)
        toolbar.onMenuItemClick(menuListUnordered)
        Assert.assertTrue(menuListUnordered.isChecked)
        editText.append("unordered")
        Assert.assertEquals("<ul><li>unordered</li></ul>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleFirstEmptyLineAboveEmptyLine() {
        TestUtils.safeAppend(editText, "\n")
        editText.setSelection(0)
        editText.toggleFormatting(listType)

        Assert.assertEquals("<$listStartTag><li>$checkbox</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleEmptyLineInTheMiddleOfEmptyLines() {
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "\n")
        editText.setSelection(3)
        editText.toggleFormatting(listType)

        Assert.assertEquals("<br><br><br><$listStartTag><li>$checkbox</li></$listTag><br><br>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleMultilineSelectionWithLeadingEmptyLIne() {
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "\n")
        editText.setSelection(0, TestUtils.safeLength(editText))
        editText.toggleFormatting(listType)

        Assert.assertEquals("<$listStartTag><li>$checkbox</li><li>$checkbox</li><li>$checkbox</li><li>$checkbox</li></$listTag>", editText.toHtml())

        editText.setSelection(0, TestUtils.safeLength(editText))
        editText.toggleFormatting(listType)

        Assert.assertEquals("<br><br><br>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleEmptyLineSurroundedByText() {
        TestUtils.safeAppend(editText, "1\n")
        TestUtils.safeAppend(editText, "2\n")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "3\n")
        TestUtils.safeAppend(editText, "4")
        editText.setSelection(4)
        editText.toggleFormatting(listType)

        Assert.assertEquals("1<br>2<$listStartTag><li>$checkbox</li></$listTag>3<br>4", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleMultipleLinesWitEmptyLines() {
        TestUtils.safeAppend(editText, "1\n")
        TestUtils.safeAppend(editText, "2\n")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "3\n")
        TestUtils.safeAppend(editText, "4\n")
        TestUtils.safeAppend(editText, "\n")
        editText.setSelection(0, TestUtils.safeLength(editText) - 1)
        editText.toggleFormatting(listType)

        Assert.assertEquals("<$listStartTag><li>${checkbox}1</li><li>${checkbox}2</li><li>$checkbox</li><li>${checkbox}3</li><li>${checkbox}4</li><li>$checkbox</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleEmptyLineAtTheEnd() {
        TestUtils.safeAppend(editText, "1")
        TestUtils.safeAppend(editText, "\n")
        editText.setSelection(TestUtils.safeLength(editText))
        editText.toggleFormatting(listType)

        Assert.assertEquals("1<$listStartTag><li>$checkbox</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun listTypeChangeWithDifferentListsSelected() {
        editText.fromHtml("<$listTag><li>1</li></$listTag><br><$otherListTag><li>2</li></$otherListTag>")

        editText.setSelection(0, editText.length())
        editText.toggleFormatting(listType)

        Assert.assertEquals("<$listStartTag><li>${checkbox}1</li></$listTag><br><$listStartTag><li>${checkbox}2</li></$listTag>", editText.toHtml())
        Assert.assertEquals(menuListOrdered.isChecked, false)
        Assert.assertEquals(menuListUnordered.isChecked, false )
        Assert.assertEquals(menuListTaskList.isChecked, true)

        editText.toggleFormatting(listType)

        Assert.assertEquals("1<br><br>2", editText.toHtml())
        editText.setSelection(0)
        editText.setSelection(0, editText.length())
        Assert.assertFalse(menuListOrdered.isChecked)
        Assert.assertFalse(menuListUnordered.isChecked)
    }

    @Test
    @Throws(Exception::class)
    fun otherListTypeChangeWithDifferentListsSelected() {
        editText.fromHtml("<$listStartTag><li>${checkbox}1</li></$listTag><br><$otherListTag><li>2</li></$otherListTag>")

        editText.setSelection(0, editText.length())
        editText.toggleFormatting(otherListType)

        Assert.assertEquals("<$otherListTag><li>1</li></$otherListTag><br><$otherListTag><li>2</li></$otherListTag>",
                editText.toHtml())
        Assert.assertEquals(menuListOrdered.isChecked, listTag != "ol")
        Assert.assertEquals(menuListUnordered.isChecked, listTag != "ul")

        editText.toggleFormatting(listType)

        Assert.assertEquals("<$listStartTag><li>${checkbox}1</li></$listTag><br><$listStartTag><li>${checkbox}2</li></$listTag>", editText.toHtml())
        Assert.assertEquals(menuListOrdered.isChecked, false)
        Assert.assertEquals(menuListUnordered.isChecked, false)
        Assert.assertEquals(menuListTaskList.isChecked, true)
    }

    @Test
    @Throws(Exception::class)
    fun addQuoteToListItem() {
        editText.fromHtml("<$listStartTag><li>1</li><li>2</li></$listTag>")

        editText.setSelection(0)
        editText.toggleFormatting(AztecTextFormat.FORMAT_QUOTE)

        Assert.assertEquals("<$listStartTag><li><blockquote>1</blockquote></li><li>2</li></$listTag>",
                editText.toHtml())

        editText.setSelection(3)
        editText.toggleFormatting(AztecTextFormat.FORMAT_QUOTE)
        Assert.assertEquals("<$listStartTag><li><blockquote>1</blockquote></li><li><blockquote>2</blockquote></li></$listTag>",
                editText.toHtml())

        editText.setSelection(1)
        editText.toggleFormatting(AztecTextFormat.FORMAT_QUOTE)
        Assert.assertEquals("<$listStartTag><li>1</li><li><blockquote>2</blockquote></li></$listTag>",
                editText.toHtml())

        editText.setSelection(editText.length())
        editText.toggleFormatting(AztecTextFormat.FORMAT_QUOTE)
        Assert.assertEquals("<$listStartTag><li>1</li><li>2</li></$listTag>",
                editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun applyListToLastLine() {
        editText.setText("a")

        editText.setSelection(editText.length())
        editText.toggleFormatting(listType)

        Assert.assertEquals("<$listStartTag><li>${checkbox}a</li></$listTag>", editText.toHtml())
    }
}
