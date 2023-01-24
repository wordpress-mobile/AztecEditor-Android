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

/**
 * Testing ordered and unordered lists.
 *
 * This test uses ParameterizedRobolectricTestRunner and runs twice - for ol and ul tags.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class ListIndentTest(listTextFormat: ITextFormat, listHtmlTag: String) {

    val listType = listTextFormat
    val listTag = listHtmlTag

    lateinit var editText: AztecText
    lateinit var menuList: PopupMenu
    lateinit var menuListOrdered: MenuItem
    lateinit var menuListUnordered: MenuItem
    lateinit var sourceText: SourceViewEditText
    lateinit var toolbar: AztecToolbar

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "Testing lists with {1} tag")
        fun data(): Collection<Array<Any>> {
            return listOf(
                    arrayOf(AztecTextFormat.FORMAT_ORDERED_LIST, "ol"),
                    arrayOf(AztecTextFormat.FORMAT_UNORDERED_LIST, "ul")
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
        toolbar.setEditor(editText, sourceText)
        menuList = toolbar.getListMenu() as PopupMenu
        menuListOrdered = menuList.menu.getItem(1)
        menuListUnordered = menuList.menu.getItem(0)
        activity.setContentView(editText)
    }

    @Test
    @Throws(Exception::class)
    fun applyListToLastLine() {
        editText.setText("a")

        editText.setSelection(editText.length())
        editText.toggleFormatting(listType)

        Assert.assertEquals("<$listTag><li>a</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun indentLastItemOfAListIfNoOtherItemsIndented() {
        editText.fromHtml("<$listTag><li>Item 1</li><li>Item 2</li></$listTag>")
        editText.setSelection(editText.editableText.indexOf("2"))
        Assert.assertEquals(editText.isIndentAvailable(), true)
        editText.indent()

        Assert.assertEquals("<$listTag><li>Item 1<$listTag style=\"text-align:left;\"><li>Item 2</li></$listTag></li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun outdentLastItemOfAListIfNoOtherItemsIndented() {
        editText.fromHtml("<$listTag><li>Item 1<$listTag style=\"text-align:left;\"><li>Item 2</li></$listTag></li></$listTag>")
        editText.setSelection(editText.editableText.indexOf("2"))
        Assert.assertEquals(editText.isOutdentAvailable(), true)
        editText.outdent()

        Assert.assertEquals("<$listTag><li>Item 1</li><li>Item 2</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun indentMiddleItemOfAListIfNoOtherItemsIndented() {
        editText.fromHtml("<$listTag><li>Item 1</li><li>Item 2</li><li>Item 3</li></$listTag>")
        editText.setSelection(editText.editableText.indexOf("2"))
        Assert.assertEquals(editText.isIndentAvailable(), true)
        editText.indent()

        Assert.assertEquals("<$listTag><li>Item 1<$listTag style=\"text-align:left;\"><li>Item 2</li></$listTag></li><li>Item 3</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun outdentMiddleItemOfAListIfNoOtherItemsIndented() {
        editText.fromHtml("<$listTag><li>Item 1<$listTag style=\"text-align:left;\"><li>Item 2</li></$listTag></li><li>Item 3</li></$listTag>")
        editText.setSelection(editText.editableText.indexOf("2"))
        Assert.assertEquals(editText.isOutdentAvailable(), true)
        editText.outdent()

        Assert.assertEquals("<$listTag><li>Item 1</li><li>Item 2</li><li>Item 3</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun indentMiddleItemOfAListIfFollowingItemIndented() {
        editText.fromHtml("<$listTag><li>Item 1</li><li>Item 2<$listTag style=\"text-align:left;\"><li>Item 3</li></$listTag></li></$listTag>")
        editText.setSelection(editText.editableText.indexOf("2"))
        Assert.assertEquals(editText.isIndentAvailable(), true)
        editText.indent()

        Assert.assertEquals("<$listTag><li>Item 1<$listTag style=\"text-align:left;\"><li>Item 2</li><li>Item 3</li></$listTag></li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun outdentMiddleItemOfAListIfFollowingItemIndented() {
        editText.fromHtml("<$listTag><li>Item 1<$listTag style=\"text-align:left;\"><li>Item 2</li><li>Item 3</li></$listTag></li></$listTag>")
        editText.setSelection(editText.editableText.indexOf("2"))
        Assert.assertEquals(editText.isOutdentAvailable(), true)
        editText.outdent()

        Assert.assertEquals("<$listTag><li>Item 1</li><li>Item 2<$listTag style=\"text-align:left;\"><li>Item 3</li></$listTag></li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun indentLastItemOfAListIfPreviousItemIndented() {
        editText.fromHtml("<$listTag><li>Item 1<$listTag style=\"text-align:left;\"><li>Item 2</li></$listTag></li><li>Item 3</li></$listTag>")
        editText.setSelection(editText.editableText.indexOf("3"))
        Assert.assertEquals(editText.isIndentAvailable(), true)
        editText.indent()

        Assert.assertEquals("<$listTag><li>Item 1<$listTag style=\"text-align:left;\"><li>Item 2</li><li>Item 3</li></$listTag></li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun outdentLastItemOfAListIfPreviousItemIndented() {
        editText.fromHtml("<$listTag><li>Item 1<$listTag style=\"text-align:left;\"><li>Item 2</li><li>Item 3</li></$listTag></li></$listTag>")
        editText.setSelection(editText.editableText.indexOf("3"))
        Assert.assertEquals(editText.isOutdentAvailable(), true)
        editText.outdent()

        Assert.assertEquals("<$listTag><li>Item 1<$listTag style=\"text-align:left;\"><li>Item 2</li></$listTag></li><li>Item 3</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun indentItemInAMiddleOfOtherIndentedItems() {
        editText.fromHtml("<$listTag><li>Item 1<$listTag style=\"text-align:left;\"><li>Item 2</li></$listTag></li><li>Item 3<$listTag style=\"text-align:left;\"><li>Item 4</li></$listTag></li></$listTag>")
        editText.setSelection(editText.editableText.indexOf("3"))
        Assert.assertEquals(editText.isIndentAvailable(), true)
        editText.indent()

        Assert.assertEquals("<$listTag><li>Item 1<$listTag style=\"text-align:left;\"><li>Item 2</li><li>Item 3</li><li>Item 4</li></$listTag></li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun outdentItemInAMiddleOfOtherIndentedItems() {
        editText.fromHtml("<$listTag><li>Item 1<$listTag style=\"text-align:left;\"><li>Item 2</li><li>Item 3</li><li>Item 4</li></$listTag></li></$listTag>")
        editText.setSelection(editText.editableText.indexOf("3"))
        Assert.assertEquals(editText.isOutdentAvailable(), true)
        editText.outdent()

        Assert.assertEquals("<$listTag><li>Item 1<$listTag style=\"text-align:left;\"><li>Item 2</li></$listTag></li><li>Item 3<$listTag style=\"text-align:left;\"><li>Item 4</li></$listTag></li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun indentItemInAMiddleOfOtherNestedIndentedItems() {
        editText.fromHtml("<$listTag><li>Item 1<$listTag style=\"text-align:left;\"><li>Item 2<$listTag style=\"text-align:left;\"><li>Item 3</li></$listTag></li><li>Item 4<$listTag style=\"text-align:left;\"><li>Item 5</li></$listTag></li></$listTag></$listTag></li>")
        editText.setSelection(editText.editableText.indexOf("4"))
        Assert.assertEquals(editText.isIndentAvailable(), true)
        editText.indent()

        Assert.assertEquals("<$listTag><li>Item 1<$listTag style=\"text-align:left;\"><li>Item 2<$listTag style=\"text-align:left;\"><li>Item 3</li><li>Item 4</li><li>Item 5</li></$listTag></li></$listTag></li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun outdentItemInAMiddleOfOtherNestedIndentedItems() {
        editText.fromHtml("<$listTag><li>Item 1<$listTag style=\"text-align:left;\"><li>Item 2<$listTag style=\"text-align:left;\"><li>Item 3</li><li>Item 4</li><li>Item 5</li></$listTag></li></$listTag></li></$listTag>")
        editText.setSelection(editText.editableText.indexOf("4"))
        Assert.assertEquals(editText.isOutdentAvailable(), true)
        editText.outdent()

        Assert.assertEquals("<$listTag><li>Item 1<$listTag style=\"text-align:left;\"><li>Item 2<$listTag style=\"text-align:left;\"><li>Item 3</li></$listTag></li><li>Item 4<$listTag style=\"text-align:left;\"><li>Item 5</li></$listTag></li></$listTag></li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun outdentSplitsListInTheMiddle() {
        editText.fromHtml("<$listTag><li>Item 1</li><li>Item 2</li><li>Item 3</li></$listTag>")
        editText.setSelection(editText.editableText.indexOf("2"))
        Assert.assertEquals(editText.isOutdentAvailable(), true)
        editText.outdent()

        Assert.assertEquals("<$listTag style=\"text-align:left;\"><li>Item 1</li></$listTag>Item 2<$listTag><li>Item 3</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun indentMultipleItems() {
        editText.fromHtml("<$listTag><li>Item 1</li><li>Item 2</li><li>Item 3</li></$listTag>")
        editText.setSelection(editText.editableText.indexOf("2"), editText.editableText.indexOf("3"))
        Assert.assertEquals(editText.isIndentAvailable(), true)
        editText.indent()

        Assert.assertEquals("<$listTag><li>Item 1<$listTag style=\"text-align:left;\"><li>Item 2</li><li>Item 3</li></$listTag></li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun outdentMultipleItems() {
        editText.fromHtml("<$listTag><li>Item 1<$listTag><li>Item 2</li><li>Item 3</li></$listTag></li></$listTag>")
        editText.setSelection(editText.editableText.indexOf("2"), editText.editableText.indexOf("3"))
        Assert.assertEquals(editText.isOutdentAvailable(), true)
        editText.outdent()

        Assert.assertEquals("<$listTag><li>Item 1</li><li>Item 2</li><li>Item 3</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun indentMultipleItemsInALongerList() {
        editText.fromHtml("<$listTag><li>Item 1</li><li>Item 2</li><li>Item 3</li><li>Item 4</li></$listTag>")
        editText.setSelection(editText.editableText.indexOf("2"), editText.editableText.indexOf("3"))
        Assert.assertEquals(editText.isIndentAvailable(), true)
        editText.indent()

        Assert.assertEquals("<$listTag><li>Item 1<$listTag style=\"text-align:left;\"><li>Item 2</li><li>Item 3</li></$listTag></li><li>Item 4</li></$listTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun cannotIndentMultipleItemsOnDifferentLevels() {
        val source = "<$listTag><li>Item 1</li><li>Item 2<$listTag><li>Item 3</li></$listTag></li></$listTag>"
        editText.fromHtml(source)
        editText.setSelection(editText.editableText.indexOf("2"), editText.editableText.indexOf("3"))
        Assert.assertEquals(editText.isIndentAvailable(), false)
        editText.indent()

        Assert.assertEquals(source, editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun cannotOutdentMultipleItemsOnDifferentLevels() {
        val source = "<$listTag><li>Item 1</li><li>Item 2<$listTag><li>Item 3</li></$listTag></li></$listTag>"
        editText.fromHtml(source)
        editText.setSelection(editText.editableText.indexOf("2"), editText.editableText.indexOf("3"))
        Assert.assertEquals(editText.isOutdentAvailable(), false)
        editText.outdent()

        Assert.assertEquals(source, editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun cannotIndentFirstItemOfTheList() {
        val source = "<$listTag><li>Item 1</li><li>Item 2</li></$listTag>"
        editText.fromHtml(source)
        editText.setSelection(editText.editableText.indexOf("1"))
        Assert.assertEquals(editText.isIndentAvailable(), false)
        editText.indent()

        Assert.assertEquals(source, editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun cannotIndentFirstItemOfNestedList() {
        val source = "<$listTag><li>Item 1<$listTag><li>Item 2</li><li>Item 3</li></$listTag></li></$listTag>"
        editText.fromHtml(source)
        editText.setSelection(editText.editableText.indexOf("2"))
        Assert.assertEquals(editText.isIndentAvailable(), false)
        editText.indent()

        Assert.assertEquals(source, editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun cannotIndentTheDeepestItem() {
        val source = "<$listTag><li>Item 1<$listTag><li>Item 2<$listTag><li>Item 3</li><li>Item 4</li></$listTag></li></$listTag></li></$listTag>"
        editText.fromHtml(source)
        editText.setSelection(editText.editableText.indexOf("3"))
        Assert.assertEquals(editText.isIndentAvailable(), false)
        editText.indent()

        Assert.assertEquals(source, editText.toHtml())
    }
}
