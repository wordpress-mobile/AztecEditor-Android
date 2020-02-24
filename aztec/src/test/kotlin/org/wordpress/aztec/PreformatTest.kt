package org.wordpress.aztec

//TODO: Uncomment when Preformat is to be added back as a feature
///**
// * Testing preformat behavior.
// */
//@RunWith(RobolectricTestRunner::class)
//@Config(sdk = intArrayOf(23))
//class PreformatTest {
//    val tag = "pre"
//
//    lateinit var editText: AztecText
//    lateinit var sourceText: SourceViewEditText
//    lateinit var toolbar: AztecToolbar
//    lateinit var menuHeading: PopupMenu
//    lateinit var menuParagraph: MenuItem
//    lateinit var menuPreformat: MenuItem
//
//    /**
//     * Initialize variables.
//     */
//    @Before
//    fun init() {
//        val activity = Robolectric.buildActivity(Activity::class.java).create().visible().get()
//        editText = AztecText(activity)
//        sourceText = SourceViewEditText(activity)
//        toolbar = AztecToolbar(activity)
//        toolbar.setEditor(editText, sourceText)
//        menuHeading = toolbar.getHeadingMenu() as PopupMenu
//        menuParagraph = menuHeading.menu.getItem(0)
//        menuPreformat = menuHeading.menu.getItem(7)
//        activity.setContentView(editText)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun closePreformatWithText() {
//        toolbar.onMenuItemClick(menuPreformat)
//        safeAppend(editText, "first item")
//        safeAppend(editText, "\n")
//        safeAppend(editText, "second item")
//        safeAppend(editText, "\n")
//        safeAppend(editText, "\n")
//        safeAppend(editText, "not preformat")
//        Assert.assertEquals("<$tag>first item\nsecond item</$tag>not preformat", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun closePreformatWithoutText() {
//        toolbar.onMenuItemClick(menuPreformat)
//        safeAppend(editText, "\n")
//        Assert.assertEquals("", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun styleSingleItem() {
//        safeAppend(editText, "first item")
//        toolbar.onMenuItemClick(menuPreformat)
//        Assert.assertEquals("<$tag>first item</$tag>", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun styleMultipleSelectedItems() {
//        safeEmpty(editText)
//        safeAppend(editText, "first item")
//        safeAppend(editText, "\n")
//        safeAppend(editText, "second item")
//        safeAppend(editText, "\n")
//        safeAppend(editText, "third item")
//        editText.setSelection(0, safeLength(editText))
//        toolbar.onMenuItemClick(menuPreformat)
//        Assert.assertEquals("<$tag>first item\n\nsecond item\n\nthird item</$tag>", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun stylePartiallySelectedMultipleItems() {
//        safeEmpty(editText)
//        safeAppend(editText, "first item")
//        safeAppend(editText, "\n")
//        safeAppend(editText, "second item")
//        safeAppend(editText, "\n")
//        safeAppend(editText, "third item")
//        editText.setSelection(4, 15) // partially select first and second item
//        toolbar.onMenuItemClick(menuPreformat)
//        Assert.assertEquals("<$tag>first item\n\nsecond item</$tag>\n\nthird item", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun styleSurroundedItem() {
//        safeEmpty(editText)
//        safeAppend(editText, "first item")
//        safeAppend(editText, "\n")
//        safeAppend(editText, "second item")
//        safeAppend(editText, "\n")
//        safeAppend(editText, "third item")
//        editText.setSelection(14)
//        toolbar.onMenuItemClick(menuPreformat)
//        Assert.assertEquals("first item\n<$tag>second item</$tag>\n\nthird item", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun styleSingleEnteredItem() {
//        toolbar.onMenuItemClick(menuPreformat)
//        safeAppend(editText, "first item")
//        Assert.assertEquals("<$tag>first item</$tag>", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun styleMultipleEnteredItems() {
//        toolbar.onMenuItemClick(menuPreformat)
//        safeAppend(editText, "first item")
//        safeAppend(editText, "\n")
//        safeAppend(editText, "second item")
//        Assert.assertEquals("<$tag>first item\nsecond item</$tag>", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun extendingPreformatBySplittingItems() {
//        toolbar.onMenuItemClick(menuPreformat)
//        editText.append("firstitem")
//        editText.text.insert(5, "\n")
//        Assert.assertEquals("<$tag>first\nitem</$tag>", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun splitTwoPreformatsWithNewline() {
//        editText.fromHtml("<$tag>Preformat 1</$tag><$tag>Preformat 2</$tag>")
//        val mark = editText.text.indexOf("Preformat 2") - 1
//        editText.text.insert(mark, "\n")
//        Assert.assertEquals("<$tag>Preformat 1</$tag><$tag>Preformat 2</$tag>", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun switchHeadingToPreformat() {
//        editText.fromHtml("<h3>First<br>Second<br>Third</h3>")
//        editText.setSelection(editText.text.indexOf("ond"))
//        toolbar.onMenuItemClick(menuPreformat)
//        Assert.assertEquals("<$tag>First\nSecond\nThird</$tag>", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun switchPreformatToHeading() {
//        editText.fromHtml("<$tag>First<br>Second<br>Third</$tag>")
//        editText.setSelection(editText.text.indexOf("ond"))
//        val menuHeading3 = menuHeading.menu.getItem(3) // Heading 3, i.e. <h3>
//        toolbar.onMenuItemClick(menuHeading3)
//        val tagHeading = "h3"
//        Assert.assertEquals("<$tagHeading>First\nSecond\nThird</$tagHeading>", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun newlineAtStartOfPreformat() {
//        editText.fromHtml("<$tag>Preformat 1</$tag><$tag>Preformat 2</$tag>")
//        val mark = editText.text.indexOf("Preformat 2")
//        editText.text.insert(mark, "\n")
//        Assert.assertEquals("<$tag>Preformat 1</$tag><$tag>\nPreformat 2</$tag>", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun removeStyleFromEmptyPreformat() {
//        toolbar.onMenuItemClick(menuPreformat)
//        Assert.assertEquals("<$tag></$tag>", editText.toHtml())
//        toolbar.onMenuItemClick(menuParagraph)
//        Assert.assertEquals("", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun removeStyleFromNonEmptyPreformat() {
//        editText.fromHtml("<$tag>first item</$tag>")
//        editText.setSelection(1)
//        toolbar.onMenuItemClick(menuParagraph)
//        Assert.assertEquals("first item", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun removePreformatStylingForPartialSelection() {
//        editText.fromHtml("<$tag>first item</$tag>")
//        editText.setSelection(2, 4)
//        toolbar.onMenuItemClick(menuParagraph)
//        Assert.assertEquals("first item", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun emptyPreformatSurroundedBytItems() {
//        toolbar.onMenuItemClick(menuPreformat)
//        safeAppend(editText, "first item")
//        safeAppend(editText, "\n")
//        val firstMark = safeLength(editText)
//        safeAppend(editText, "second item")
//        safeAppend(editText, "\n")
//        val secondMark = safeLength(editText)
//        safeAppend(editText, "third item")
//        editText.text.delete(firstMark, secondMark - 1)
//        Assert.assertEquals("<$tag>first item\n\nthird item</$tag>", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun trailingEmptyLine() {
//        toolbar.onMenuItemClick(menuPreformat)
//        safeAppend(editText, "first item")
//        safeAppend(editText, "\n")
//        safeAppend(editText, "second item")
//        safeAppend(editText, "\n")
//        safeAppend(editText, "third item")
//        safeAppend(editText, "\n")
//        Assert.assertEquals("<$tag>first item\nsecond item\nthird item</$tag>", editText.toHtml())
//        safeAppend(editText, "\n")
//        Assert.assertEquals("<$tag>first item\nsecond item\nthird item</$tag>", editText.toHtml())
//        safeAppend(editText, "not preformat")
//        Assert.assertEquals("<$tag>first item\nsecond item\nthird item</$tag>not preformat", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun openPreformatByAddingNewline() {
//        editText.fromHtml("<$tag>first item<br>second item</$tag>not preformat")
//        val mark = editText.text.indexOf("second item") + "second item".length
//        editText.text.insert(mark, "\n")
//        editText.text.insert(mark + 1, "third item")
//        Assert.assertEquals("<$tag>first item\nsecond item\nthird item</$tag>not preformat", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun openPreformatByAppendingTextToTheEnd() {
//        editText.fromHtml("<$tag>first item<br>second item</$tag>not preformat")
//        editText.setSelection(safeLength(editText))
//        editText.text.insert(editText.text.indexOf("\nnot preformat"), " (appended)")
//        Assert.assertEquals("<$tag>first item\nsecond item (appended)</$tag>not preformat", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun openPreformatByMovingOutsideTextInsideIt() {
//        editText.fromHtml("<$tag>first item<br>second item</$tag>")
//        safeAppend(editText, "not preformat")
//        editText.text.delete(editText.text.indexOf("not preformat"), editText.text.indexOf("not preformat"))
//        Assert.assertEquals("<$tag>first item\nsecond itemnot preformat</$tag>", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun remainClosedWhenLastCharacterIsDeleted() {
//        editText.fromHtml("<$tag>first item<br>second item</$tag>not preformat")
//        editText.setSelection(safeLength(editText))
//        val mark = editText.text.indexOf("second item") + "second item".length
//        // delete last character from "second item"
//        editText.text.delete(mark - 1, mark)
//        Assert.assertEquals("<$tag>first item\nsecond ite</$tag>not preformat", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun openingAndReopeningOfPreformat() {
//        editText.fromHtml("<$tag>first item<br>second item</$tag>")
//        editText.setSelection(safeLength(editText))
//        safeAppend(editText, "\n")
//        safeAppend(editText, "third item")
//        Assert.assertEquals("<$tag>first item\nsecond item\nthird item</$tag>", editText.toHtml())
//        safeAppend(editText, "\n")
//        safeAppend(editText, "\n")
//        val mark = safeLength(editText) - 1
//        safeAppend(editText, "not preformat")
//        Assert.assertEquals("<$tag>first item\nsecond item\nthird item</$tag>not preformat", editText.toHtml())
//        safeAppend(editText, "\n")
//        safeAppend(editText, "foo")
//        Assert.assertEquals("<$tag>first item\nsecond item\nthird item</$tag>not preformat\n\nfoo", editText.toHtml())
//        editText.text.delete(mark, mark + 1)
//        Assert.assertEquals("<$tag>first item\nsecond item\nthird itemnot preformat</$tag>\n\nfoo", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun handlePreformatReopeningAfterLastElementDeletion() {
//        editText.fromHtml("<$tag>first item<br>second item<br>third item</$tag>")
//        editText.setSelection(safeLength(editText))
//        editText.text.delete(editText.text.indexOf("third item", 0), safeLength(editText))
//        safeAppend(editText, "\n")
//        safeAppend(editText, "not preformat")
//        Assert.assertEquals("<$tag>first item\nsecond item</$tag>not preformat", editText.toHtml())
//        editText.text.insert(editText.text.indexOf("not preformat") - 1, " addition")
//        Assert.assertEquals("<$tag>first item\nsecond item addition</$tag>not preformat", editText.toHtml())
//        editText.text.insert(editText.text.indexOf("not preformat") - 1, "\n")
//        editText.text.insert(editText.text.indexOf("not preformat") - 1, "third item")
//        Assert.assertEquals("first item\nsecond item addition\nthird item\nnot preformat", editText.text.toString())
//        Assert.assertEquals("<$tag>first item\nsecond item addition\nthird item</$tag>not preformat", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun additionToClosedPreformat() {
//        toolbar.onMenuItemClick(menuPreformat)
//        safeAppend(editText, "first item")
//        safeAppend(editText, "\n")
//        safeAppend(editText, "second item")
//        val mark = safeLength(editText)
//        safeAppend(editText, "\n")
//        safeAppend(editText, "\n")
//        safeAppend(editText, "not preformat")
//        Assert.assertEquals("<$tag>first item\nsecond item</$tag>not preformat", editText.toHtml())
//        editText.text.insert(mark, " (addition)")
//        Assert.assertEquals("<$tag>first item\nsecond item (addition)</$tag>not preformat", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun appendToPreformatFromTopAtFirstLine() {
//        toolbar.onMenuItemClick(menuPreformat)
//        safeAppend(editText, "first item")
//        safeAppend(editText, "\n")
//        safeAppend(editText, "second item")
//        editText.setSelection(0)
//        editText.text.insert(0, "addition ")
//        Assert.assertEquals("<$tag>addition first item\nsecond item</$tag>", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun appendToPreformatFromTop() {
//        safeAppend(editText, "not preformat")
//        safeAppend(editText, "\n")
//        toolbar.onMenuItemClick(menuPreformat)
//        val mark = editText.length() - 1
//        safeAppend(editText, "first item")
//        safeAppend(editText, "\n")
//        safeAppend(editText, "second item")
//        editText.setSelection(mark)
//        editText.text.insert(mark, "addition ")
//        Assert.assertEquals("not preformat\n<$tag>addition first item\nsecond item</$tag>", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun deleteFirstItemWithKeyboard() {
//        toolbar.onMenuItemClick(menuPreformat)
//        safeAppend(editText, "first item")
//        val firstMark = safeLength(editText)
//        safeAppend(editText, "\n")
//        safeAppend(editText, "second item")
//        val secondMark = safeLength(editText)
//        safeAppend(editText, "\n")
//        safeAppend(editText, "third item")
//        editText.setSelection(0)
//        Assert.assertEquals("first item\nsecond item\nthird item", editText.text.toString())
//        editText.text.delete(firstMark + 1, secondMark)
//        Assert.assertEquals("first item\n\nthird item", editText.text.toString())
//        Assert.assertEquals("<$tag>first item\n\nthird item</$tag>", editText.toHtml())
//        editText.text.delete(0, firstMark)
//        Assert.assertEquals("<$tag>\n\nthird item</$tag>", editText.toHtml())
//    }
//}
