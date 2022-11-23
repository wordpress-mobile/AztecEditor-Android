package org.wordpress.aztec

import android.app.Activity
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.ToggleButton
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.wordpress.aztec.TestUtils.safeAppend
import org.wordpress.aztec.TestUtils.safeEmpty
import org.wordpress.aztec.TestUtils.safeLength
import org.wordpress.aztec.source.SourceViewEditText
import org.wordpress.aztec.toolbar.AztecToolbar
import org.wordpress.aztec.toolbar.ToolbarAction
import org.wordpress.aztec.toolbar.ToolbarItems

/**
 * Testing preformat behavior.
 */
@RunWith(RobolectricTestRunner::class)
class PreformatTest {
    val tag = "pre"

    lateinit var editText: AztecText
    lateinit var sourceText: SourceViewEditText
    lateinit var toolbar: AztecToolbar
    lateinit var menuHeading: PopupMenu
    lateinit var menuParagraph: MenuItem
    lateinit var buttonPreformat: ToggleButton

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
        toolbar.setToolbarItems(ToolbarItems.BasicLayout(
                ToolbarAction.HEADING,
                ToolbarAction.PREFORMAT,
                ToolbarAction.LIST,
                ToolbarAction.QUOTE,
                ToolbarAction.BOLD,
                ToolbarAction.ITALIC,
                ToolbarAction.LINK,
                ToolbarAction.UNDERLINE,
                ToolbarAction.STRIKETHROUGH,
                ToolbarAction.ALIGN_LEFT,
                ToolbarAction.ALIGN_CENTER,
                ToolbarAction.ALIGN_RIGHT,
                ToolbarAction.HORIZONTAL_RULE,
                ToolbarAction.HTML
        ))
        toolbar.setEditor(editText, sourceText)
        menuHeading = toolbar.getHeadingMenu() as PopupMenu
        menuParagraph = menuHeading.menu.getItem(0)
        buttonPreformat = toolbar.findViewById<ToggleButton>(R.id.format_bar_button_pre)
        activity.setContentView(editText)
    }

    @Test
    @Throws(Exception::class)
    fun closePreformatWithText() {
        buttonPreformat.performClick()
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        safeAppend(editText, "not preformat")
        assertEquals("<$tag>first item<br>second item</$tag>not preformat", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun closePreformatWithoutText() {
        buttonPreformat.performClick()
        safeAppend(editText, "\n")
        assertEquals("", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleSingleItem() {
        safeAppend(editText, "first item")
        buttonPreformat.performClick()
        assertEquals("<$tag>first item</$tag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleMultipleSelectedItems() {
        safeEmpty(editText)
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        safeAppend(editText, "\n")
        safeAppend(editText, "third item")
        editText.setSelection(0, safeLength(editText))
        buttonPreformat.performClick()
        assertEquals("<$tag>first item<br>second item<br>third item</$tag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun stylePartiallySelectedMultipleItems() {
        safeEmpty(editText)
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        safeAppend(editText, "\n")
        safeAppend(editText, "third item")
        editText.setSelection(4, 15) // partially select first and second item
        buttonPreformat.performClick()
        assertEquals("<$tag>first item<br>second item</$tag>third item", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleSurroundedItem() {
        safeEmpty(editText)
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        safeAppend(editText, "\n")
        safeAppend(editText, "third item")
        editText.setSelection(14)
        buttonPreformat.performClick()
        assertEquals("first item<$tag>second item</$tag>third item", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleSingleEnteredItem() {
        buttonPreformat.performClick()
        safeAppend(editText, "first item")
        assertEquals("<$tag>first item</$tag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleMultipleEnteredItems() {
        buttonPreformat.performClick()
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        assertEquals("<$tag>first item<br>second item</$tag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun extendingPreformatBySplittingItems() {
        buttonPreformat.performClick()
        editText.append("firstitem")
        editText.text.insert(5, "\n")
        assertEquals("<$tag>first<br>item</$tag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun splitTwoPreformatsWithNewline() {
        editText.fromHtml("<$tag>Preformat 1</$tag><$tag>Preformat 2</$tag>")
        val mark = editText.text.indexOf("Preformat 2") - 1
        editText.text.insert(mark, "\n")
        assertEquals("<$tag>Preformat 1</$tag><$tag>Preformat 2</$tag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun switchHeadingToPreformat() {
        editText.fromHtml("<h3>First<br>Second<br>Third</h3>")
        editText.setSelection(editText.text.indexOf("ond"))
        buttonPreformat.performClick()
        assertEquals("<$tag>First<br>Second<br>Third</$tag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun switchPreformatToHeading() {
        editText.fromHtml("<$tag>First<br>Second<br>Third</$tag>")
        editText.setSelection(editText.text.indexOf("ond"))
        val menuHeading3 = menuHeading.menu.getItem(3) // Heading 3, i.e. <h3>
        toolbar.onMenuItemClick(menuHeading3)
        val tagHeading = "h3"
        assertEquals("<$tagHeading>First<br>Second<br>Third</$tagHeading>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun newlineAtStartOfPreformat() {
        editText.fromHtml("<$tag>Preformat 1</$tag><$tag>Preformat 2</$tag>")
        val mark = editText.text.indexOf("Preformat 2")
        editText.text.insert(mark, "\n")
        assertEquals("<$tag>Preformat 1</$tag><$tag><br>Preformat 2</$tag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun removeStyleFromEmptyPreformat() {
        buttonPreformat.performClick()
        assertEquals("<$tag></$tag>", editText.toHtml())
        toolbar.onMenuItemClick(menuParagraph)
        assertEquals("", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun removeStyleFromNonEmptyPreformat() {
        editText.fromHtml("<$tag>first item</$tag>")
        editText.setSelection(1)
        toolbar.onMenuItemClick(menuParagraph)
        assertEquals("first item", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun removePreformatStylingForPartialSelection() {
        editText.fromHtml("<$tag>first item</$tag>")
        editText.setSelection(2, 4)
        toolbar.onMenuItemClick(menuParagraph)
        assertEquals("first item", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun emptyPreformatSurroundedBytItems() {
        buttonPreformat.performClick()
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        val firstMark = safeLength(editText)
        safeAppend(editText, "second item")
        safeAppend(editText, "\n")
        val secondMark = safeLength(editText)
        safeAppend(editText, "third item")
        editText.text.delete(firstMark, secondMark - 1)
        assertEquals("<$tag>first item<br><br>third item</$tag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun trailingEmptyLine() {
        buttonPreformat.performClick()
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        safeAppend(editText, "\n")
        safeAppend(editText, "third item")
        safeAppend(editText, "\n")
        assertEquals("<$tag>first item<br>second item<br>third item</$tag>", editText.toHtml())
        safeAppend(editText, "\n")
        assertEquals("<$tag>first item<br>second item<br>third item</$tag>", editText.toHtml())
        safeAppend(editText, "not preformat")
        assertEquals("<$tag>first item<br>second item<br>third item</$tag>not preformat", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun openPreformatByAddingNewline() {
        editText.fromHtml("<$tag>first item<br>second item</$tag>not preformat")
        val mark = editText.text.indexOf("second item") + "second item".length
        editText.text.insert(mark, "\n")
        editText.text.insert(mark + 1, "third item")
        assertEquals("<$tag>first item<br>second item<br>third item</$tag>not preformat", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun openPreformatByAppendingTextToTheEnd() {
        editText.fromHtml("<$tag>first item<br>second item</$tag>not preformat")
        editText.setSelection(safeLength(editText))
        editText.text.insert(editText.text.indexOf("\nnot preformat"), " (appended)")
        assertEquals("<$tag>first item<br>second item (appended)</$tag>not preformat", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun openPreformatByMovingOutsideTextInsideIt() {
        editText.fromHtml("<$tag>first item<br>second item</$tag>")
        safeAppend(editText, "not preformat")
        editText.text.delete(editText.text.indexOf("not preformat"), editText.text.indexOf("not preformat"))
        assertEquals("<$tag>first item<br>second itemnot preformat</$tag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun remainClosedWhenLastCharacterIsDeleted() {
        editText.fromHtml("<$tag>first item<br>second item</$tag>not preformat")
        editText.setSelection(safeLength(editText))
        val mark = editText.text.indexOf("second item") + "second item".length
        // delete last character from "second item"
        editText.text.delete(mark - 1, mark)
        assertEquals("<$tag>first item<br>second ite</$tag>not preformat", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun openingAndReopeningOfPreformat() {
        editText.fromHtml("<$tag>first item<br>second item</$tag>")
        editText.setSelection(safeLength(editText))
        safeAppend(editText, "\n")
        safeAppend(editText, "third item")
        assertEquals("<$tag>first item<br>second item<br>third item</$tag>", editText.toHtml())
        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        val mark = safeLength(editText) - 1
        safeAppend(editText, "not preformat")
        assertEquals("<$tag>first item<br>second item<br>third item</$tag>not preformat", editText.toHtml())
        safeAppend(editText, "\n")
        safeAppend(editText, "foo")
        assertEquals("<$tag>first item<br>second item<br>third item</$tag>not preformat<br>foo", editText.toHtml())
        editText.text.delete(mark, mark + 1)
        assertEquals("<$tag>first item<br>second item<br>third itemnot preformat</$tag>foo", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun handlePreformatReopeningAfterLastElementDeletion() {
        editText.fromHtml("<$tag>first item<br>second item<br>third item</$tag>")
        editText.setSelection(safeLength(editText))
        editText.text.delete(editText.text.indexOf("third item", 0), safeLength(editText))
        safeAppend(editText, "\n")
        safeAppend(editText, "not preformat")
        assertEquals("<$tag>first item<br>second item</$tag>not preformat", editText.toHtml())
        editText.text.insert(editText.text.indexOf("not preformat") - 1, " addition")
        assertEquals("<$tag>first item<br>second item addition</$tag>not preformat", editText.toHtml())
        editText.text.insert(editText.text.indexOf("not preformat") - 1, "\n")
        editText.text.insert(editText.text.indexOf("not preformat") - 1, "third item")
        assertEquals("first item\nsecond item addition\nthird item\nnot preformat", editText.text.toString())
        assertEquals("<$tag>first item<br>second item addition<br>third item</$tag>not preformat", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun additionToClosedPreformat() {
        buttonPreformat.performClick()
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        val mark = safeLength(editText)
        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        safeAppend(editText, "not preformat")
        assertEquals("<$tag>first item<br>second item</$tag>not preformat", editText.toHtml())
        editText.text.insert(mark, " (addition)")
        assertEquals("<$tag>first item<br>second item (addition)</$tag>not preformat", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun appendToPreformatFromTopAtFirstLine() {
        buttonPreformat.performClick()
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        editText.setSelection(0)
        editText.text.insert(0, "addition ")
        assertEquals("<$tag>addition first item<br>second item</$tag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun appendToPreformatFromTop() {
        safeAppend(editText, "not preformat")
        safeAppend(editText, "\n")
        buttonPreformat.performClick()
        val mark = editText.length() - 1
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        editText.setSelection(mark)
        editText.text.insert(mark, "addition ")
        assertEquals("not preformat<$tag>addition first item<br>second item</$tag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun deleteFirstItemWithKeyboard() {
        buttonPreformat.performClick()
        safeAppend(editText, "first item")
        val firstMark = safeLength(editText)
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        val secondMark = safeLength(editText)
        safeAppend(editText, "\n")
        safeAppend(editText, "third item")
        editText.setSelection(0)
        assertEquals("first item\nsecond item\nthird item", editText.text.toString())
        editText.text.delete(firstMark + 1, secondMark)
        assertEquals("first item\n\nthird item", editText.text.toString())
        assertEquals("<$tag>first item<br><br>third item</$tag>", editText.toHtml())
        editText.text.delete(0, firstMark)
        assertEquals("<$tag><br><br>third item</$tag>", editText.toHtml())
    }
}
