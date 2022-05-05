package org.wordpress.aztec

import android.app.Activity
import android.view.MenuItem
import android.widget.PopupMenu
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.wordpress.aztec.source.SourceViewEditText
import org.wordpress.aztec.toolbar.AztecToolbar

/**
 * Testing ordered and unordered lists.
 *
 * This test uses ParameterizedRobolectricTestRunner and runs twice - for ol and ul tags.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23])
class ListToggleTest {

    lateinit var editText: AztecText
    lateinit var menuList: PopupMenu
    lateinit var menuListOrdered: MenuItem
    lateinit var menuListUnordered: MenuItem
    lateinit var sourceText: SourceViewEditText
    lateinit var toolbar: AztecToolbar

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
        toolbar.setEditor(editText, sourceText)
        menuList = toolbar.getListMenu() as PopupMenu
        menuListOrdered = menuList.menu.getItem(1)
        menuListUnordered = menuList.menu.getItem(0)
        activity.setContentView(editText)
    }

    @Test
    @Throws(Exception::class)
    fun togglesFormattingOfSimpleListFromUnorderedToOrdered() {
        editText.fromHtml("<ul><li>Item 1</li><li>Item 2</li></ul>")
        editText.setSelection(editText.editableText.indexOf("2"))
        editText.toggleFormatting(AztecTextFormat.FORMAT_ORDERED_LIST)

        Assert.assertEquals("<ol><li>Item 1</li><li>Item 2</li></ol>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun togglesFormattingOfSimpleListFromTaskToUnorderedList() {
        editText.fromHtml("<ul type=\"task-list\"><li><input type=\"checkbox\" class=\"task-list-item-checkbox\" />Item 1</li><li><input type=\"checkbox\" class=\"task-list-item-checkbox\" />Item 2</li></ul>")
        editText.setSelection(editText.editableText.indexOf("2"))
        editText.toggleFormatting(AztecTextFormat.FORMAT_UNORDERED_LIST)

        Assert.assertEquals("<ul><li>Item 1</li><li>Item 2</li></ul>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun togglesFormattingOfSimpleListFromOrderedListToTaskList() {
        editText.fromHtml("<ol><li>Item 1</li><li>Item 2</li></ol>")
        editText.setSelection(editText.editableText.indexOf("2"))
        editText.toggleFormatting(AztecTextFormat.FORMAT_TASK_LIST)

        Assert.assertEquals("<ul type=\"task-list\"><li><input type=\"checkbox\" class=\"task-list-item-checkbox\" />Item 1</li><li><input type=\"checkbox\" class=\"task-list-item-checkbox\" />Item 2</li></ul>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun togglesFormattingOfNestedListFromUnorderedToOrdered() {
        editText.fromHtml("<ul><li>Item 1<ul><li>Item 2</li></ul></li></ul>")
        editText.setSelection(editText.editableText.indexOf("Item 2"))
        editText.toggleFormatting(AztecTextFormat.FORMAT_ORDERED_LIST)

        Assert.assertEquals("<ul><li>Item 1<ol><li>Item 2</li></ol></li></ul>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun togglesFormattingOfNestedLists() {
        editText.fromHtml("<ul><li>Item 1<ul><li>Item 2</li></ul></li></ul>")
        editText.setSelection(editText.editableText.indexOf("1"), editText.editableText.indexOf("2"))
        editText.toggleFormatting(AztecTextFormat.FORMAT_ORDERED_LIST)

        Assert.assertEquals("<ol><li>Item 1<ol><li>Item 2</li></ol></li></ol>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun togglesFormattingOfParentList() {
        editText.fromHtml("<ul><li>Item 1<ul><li>Item 2</li></ul></li></ul>")
        editText.setSelection(editText.editableText.indexOf("1"))
        editText.toggleFormatting(AztecTextFormat.FORMAT_ORDERED_LIST)

        Assert.assertEquals("<ol><li>Item 1<ul><li>Item 2</li></ul></li></ol>", editText.toHtml())
    }
}
