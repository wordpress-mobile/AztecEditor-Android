//package org.wordpress.aztec
//
//import android.app.Activity
//import android.text.TextUtils
//import android.widget.EditText
//import org.junit.Assert
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.robolectric.Robolectric
//import org.robolectric.RobolectricGradleTestRunner
//import org.robolectric.annotation.Config
//import java.util.*
//
//
///**
// * Testing quote behaviour.
// */
//@RunWith(RobolectricGradleTestRunner::class)
//@Config(constants = BuildConfig::class)
//class CodeTest() {
//
//    val formattingType = TextFormat.FORMAT_CODE
//    val codeTag = "code"
//    lateinit var editText: AztecText
//
//    /**
//     * Initialize variables.
//     */
//    @Before
//    fun init() {
//        val activity = Robolectric.buildActivity(Activity::class.java).create().visible().get()
//        editText = AztecText(activity)
//        activity.setContentView(editText)
//    }
//
//    fun setStyles(editText: AztecText) {
//        val styles = ArrayList<TextFormat>()
//        styles.add(formattingType)
//        editText.setSelectedStyles(styles)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun styleSingleItem() {
//        editText.append("println(\"hello world\");")
//        setStyles(editText)
//        Assert.assertEquals("<$codeTag>println(\"hello world\");</$codeTag>", editText.toHtml())
//    }
//
//
//    @Test
//    @Throws(Exception::class)
//    fun styleMultipleSelectedItems() {
//        junit.framework.Assert.assertTrue(TextUtils.isEmpty(editText.text))
//        editText.append("first item")
//        editText.append("\n")
//        editText.append("second item")
//        editText.append("\n")
//        editText.append("third item")
//        editText.setSelection(0, editText.length())
//
//        setStyles(editText)
//        Assert.assertEquals("<$codeTag>first item<br>second item<br>third item</$codeTag>", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun stylePartiallySelectedMultipleItems() {
//        junit.framework.Assert.assertTrue(TextUtils.isEmpty(editText.text))
//        editText.append("first item")
//        editText.append("\n")
//        editText.append("second item")
//        editText.append("\n")
//        editText.append("third item")
//        editText.setSelection(4, 15) //we partially selected first and second item
//
//        setStyles(editText)
//        Assert.assertEquals("<$codeTag>first item<br>second item</$codeTag>third item", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun styleSurroundedItem() {
//        junit.framework.Assert.assertTrue(TextUtils.isEmpty(editText.text))
//        editText.append("first item")
//        editText.append("\n")
//        editText.append("second item")
//        editText.append("\n")
//        editText.append("third item")
//        editText.setSelection(14)
//
//        setStyles(editText)
//        Assert.assertEquals("first item<$codeTag>second item</$codeTag>third item", editText.toHtml())
//    }
//
//
//    //enable styling on empty line and enter text
//
//    @Test
//    @Throws(Exception::class)
//    fun emptyQuote() {
//        editText.toggleFormatting(formattingType)
////        setStyles(editText)
//        Assert.assertEquals("<$codeTag></$codeTag>", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun styleSingleEnteredItem() {
//        setStyles(editText)
//        editText.append("first item")
//        Assert.assertEquals("<$codeTag>first item</$codeTag>", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun styleMultipleEnteredItems() {
//        setStyles(editText)
//        editText.append("first item")
//        editText.append("\n")
//        editText.append("second item")
//        Assert.assertEquals("<$codeTag>first item<br>second item</$codeTag>", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun closingPopulatedQuote1() {
//        val styles = ArrayList<TextFormat>()
//        styles.add(TextFormat.FORMAT_STRIKETHROUGH)
//        editText.setSelectedStyles(styles)
//        editText.append("first item")
//        Assert.assertEquals("<s>first item</s>", editText.toHtml().toString())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun closingPopulatedCode() {
//        val styles = ArrayList<TextFormat>()
//        styles.add(formattingType)
//        editText.setSelectedStyles(styles)
//        editText.append("first item")
//
//        editText.append("\n")
//        editText.append("\n")
//        editText.append("not in the quote")
//        Assert.assertEquals("<$codeTag>first item</$codeTag>not in the quote", editText.toHtml().toString())
//    }
//
//
//    @Test
//    @Throws(Exception::class)
//    fun closingEmptyQuote() {
//        setStyles(editText)
//        editText.append("\n")
//        Assert.assertEquals("", editText.toHtml().toString())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun extendingQuoteBySplittingItems() {
//        setStyles(editText)
//        editText.append("firstitem")
//        editText.text.insert(5, "\n")
//        Assert.assertEquals("<$codeTag>first<br>item</$codeTag>", editText.toHtml().toString())
//    }
//
//
//    @Test
//    @Throws(Exception::class)
//    fun quoteSplitWithToolbar() {
//        editText.fromHtml("<$codeTag>first item<br>second item<br>third item</$codeTag>")
//        editText.setSelection(14)
//        setStyles(editText)
//
//        Assert.assertEquals("<$codeTag>first item</$codeTag>second item<$codeTag>third item</$codeTag>", editText.toHtml())
//    }
//
//
//    @Test
//    @Throws(Exception::class)
//    fun removeQuoteStyling() {
//        editText.fromHtml("<$codeTag>first item</$codeTag>")
//        editText.setSelection(1)
//        setStyles(editText)
//
//        Assert.assertEquals("first item", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun removeQuoteStylingForPartialSelection() {
//        editText.fromHtml("<$codeTag>first item</$codeTag>")
//        editText.setSelection(2, 4)
//        setStyles(editText)
//
//        Assert.assertEquals("first item", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun removeQuoteStylingForMultilinePartialSelection() {
//        setStyles(editText)
//        editText.append("first item")
//        editText.append("\n")
//        editText.append("second item")
//        val firstMark = editText.length() - 4
//        editText.append("\n")
//        editText.append("third item")
//        editText.append("\n")
//        val secondMark = editText.length() - 4
//        editText.append("fourth item")
//        editText.append("\n")
//        editText.append("\n")
//        editText.append("not in quote")
//
//        editText.setSelection(firstMark, secondMark)
//        editText.setSelectedStyles(ArrayList());
//
//        Assert.assertEquals("<$codeTag>first item</$codeTag>second item<br>third item<$codeTag>fourth item</$codeTag>not in quote", editText.toHtml())
//    }
//
//
//    @Test
//    @Throws(Exception::class)
//    fun emptyQuoteSurroundedBytItems() {
//        setStyles(editText)
//        editText.append("first item")
//        editText.append("\n")
//        val firstMark = editText.length()
//        editText.append("second item")
//        editText.append("\n")
//        val secondMart = editText.length()
//        editText.append("third item")
//
//        editText.text.delete(firstMark - 1, secondMart - 2)
//
//        Assert.assertEquals("<$codeTag>first item<br><br>third item</$codeTag>", editText.toHtml())
//    }
//
//
//    @Test
//    @Throws(Exception::class)
//    fun trailingEmptyLine() {
//        setStyles(editText)
//        editText.append("first item")
//        editText.append("\n")
//        editText.append("second item")
//        editText.append("\n")
//        editText.append("third item")
//        val mark = editText.length()
//        editText.append("\n")
//
//        Assert.assertEquals("<$codeTag>first item<br>second item<br>third item</$codeTag>", editText.toHtml())
//        editText.append("\n")
//
//        Assert.assertEquals("<$codeTag>first item<br>second item<br>third item</$codeTag>", editText.toHtml())
//
//        editText.append("not in quote")
//        editText.setSelection(mark)
//        editText.text.insert(mark, "\n")
//        Assert.assertEquals("<$codeTag>first item<br>second item<br>third item</$codeTag><br>not in quote", editText.toHtml())
//    }
//
//
//    @Test
//    @Throws(Exception::class)
//    fun openQuoteByAddingNewline() {
//        editText.fromHtml("<$codeTag>first item<br>second item</$codeTag>not in quote")
//
//
//        val mark = editText.text.indexOf("second item") + "second item".length
//
//        editText.text.insert(mark, "\n")
//        editText.text.insert(mark + 1, "third item")
//
//
//        Assert.assertEquals("<$codeTag>first item<br>second item<br>third item</$codeTag>not in quote", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun openQuoteByAppendingTextToTheEnd() {
//        editText.fromHtml("<$codeTag>first item<br>second item</$codeTag>not in quote")
//        editText.setSelection(editText.length())
//
//        editText.text.insert(editText.text.indexOf("\nnot in quote"), " (appended)")
//
//        Assert.assertEquals("<$codeTag>first item<br>second item (appended)</$codeTag>not in quote", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun openQuoteByMovingOutsideTextInsideIt() {
//        editText.fromHtml("<$codeTag>first item<br>second item</$codeTag>")
//        editText.append("not in quote")
//
//        editText.text.delete(editText.text.indexOf("not in quote"), editText.text.indexOf("not in quote"))
//        Assert.assertEquals("<$codeTag>first item<br>second itemnot in quote</$codeTag>", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun quoteRemainsClosedWhenLastCharacterIsDeleted() {
//        editText.fromHtml("<$codeTag>first item<br>second item</$codeTag>not in quote")
//        editText.setSelection(editText.length())
//
//        val mark = editText.text.indexOf("second item") + "second item".length;
//
//        //delete last character from "second item"
//        editText.text.delete(mark - 1, mark)
//        Assert.assertEquals("<$codeTag>first item<br>second ite</$codeTag>not in quote", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun openingAndReopeningOfQuote() {
//        editText.fromHtml("<$codeTag>first item<br>second item</$codeTag>")
//        editText.setSelection(editText.length())
//
//        editText.append("\n")
//        editText.append("third item")
//        Assert.assertEquals("<$codeTag>first item<br>second item<br>third item</$codeTag>", editText.toHtml())
//        editText.append("\n")
//        editText.append("\n")
//        val mark = editText.length() - 1
//        editText.append("not in the quote")
//        Assert.assertEquals("<$codeTag>first item<br>second item<br>third item</$codeTag>not in the quote", editText.toHtml())
//        editText.append("\n")
//        editText.append("foo")
//        Assert.assertEquals("<$codeTag>first item<br>second item<br>third item</$codeTag>not in the quote<br>foo", editText.toHtml())
//
//        //reopen quote
//        editText.text.delete(mark, mark + 1)
//        Assert.assertEquals("<$codeTag>first item<br>second item<br>third itemnot in the quote</$codeTag>foo", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun closeCode() {
//        editText.fromHtml("<$codeTag>first item</$codeTag>")
//        editText.setSelection(editText.length())
//
//        Assert.assertEquals("first item", editText.text.toString())
//        editText.append("\n")
//        Assert.assertEquals("first item\n\u200B", editText.text.toString())
//
//        editText.text.delete(editText.length() - 1, editText.length())
//        Assert.assertEquals("first item\n", editText.text.toString())
//
//        editText.append("not in the quote")
//        Assert.assertEquals("<$codeTag>first item</$codeTag>not in the quote", editText.toHtml())
//    }
//
//
//    @Test
//    @Throws(Exception::class)
//    fun handlequoteReopeningAfterLastElementDeletion() {
//        editText.fromHtml("<$codeTag>first item<br>second item<br>third item</$codeTag>")
//        editText.setSelection(editText.length())
//
//        editText.text.delete(editText.text.indexOf("third item", 0), editText.length())
//
//        editText.append("not in the quote")
//        Assert.assertEquals("<$codeTag>first item<br>second item</$codeTag>not in the quote", editText.toHtml())
//
//        editText.text.insert(editText.text.indexOf("not in the quote") - 1, " addition")
//        Assert.assertEquals("<$codeTag>first item<br>second item addition</$codeTag>not in the quote", editText.toHtml())
//
//        editText.text.insert(editText.text.indexOf("not in the quote") - 1, "\n")
//        editText.text.insert(editText.text.indexOf("not in the quote") - 1, "third item")
//        Assert.assertEquals("first item\nsecond item addition\nthird item\nnot in the quote", editText.text.toString())
//        Assert.assertEquals("<$codeTag>first item<br>second item addition<br>third item</$codeTag>not in the quote", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun additionToClosedQuote() {
//        setStyles(editText)
//        editText.append("first item")
//        editText.append("\n")
//        editText.append("second item")
//
//        val mark = editText.length()
//
//        editText.append("\n")
//        editText.append("\n")
//        editText.append("not in the quote")
//        Assert.assertEquals("<$codeTag>first item<br>second item</$codeTag>not in the quote", editText.toHtml().toString())
//
//        editText.text.insert(mark, " (addition)")
//
//        Assert.assertEquals("<$codeTag>first item<br>second item (addition)</$codeTag>not in the quote", editText.toHtml().toString())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun addItemToQuoteFromBottom() {
//        setStyles(editText)
//        editText.append("first item")
//        editText.append("\n")
//        editText.append("second item")
//        editText.append("\n")
//        editText.append("\n")
//        editText.append("third item")
//        editText.setSelection(editText.length())
//
//        setStyles(editText)
//
//        Assert.assertEquals("<$codeTag>first item<br>second item<br>third item</$codeTag>", editText.toHtml())
//    }
//
//
//    @Test
//    @Throws(Exception::class)
//    fun addItemToQuoteFromTop() {
//        editText.append("first item")
//        editText.append("\n")
//        editText.append("second item")
//        editText.setSelection(editText.length())
//        editText.toggleFormatting(formattingType)
//        editText.append("\n")
//        editText.append("third item")
//
//        editText.setSelection(0)
//
//        editText.toggleFormatting(formattingType)
//
//        Assert.assertEquals("<$codeTag>first item<br>second item<br>third item</$codeTag>", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun addItemToQuoteFromInside() {
//        setStyles(editText)
//        editText.append("first item")
//        editText.append("\n")
//        editText.append("\n")
//        editText.append("second item")
//        editText.append("\n")
//        editText.append("third item")
//        editText.setSelection(editText.length())
//        editText.toggleFormatting(formattingType)
//
//        Assert.assertEquals("<$codeTag>first item</$codeTag>second item<$codeTag>third item</$codeTag>", editText.toHtml())
//        editText.setSelection(15)
//        editText.toggleFormatting(formattingType)
//
//        Assert.assertEquals("<$codeTag>first item<br>second item<br>third item</$codeTag>", editText.toHtml())
//    }
//
//
//    @Test
//    @Throws(Exception::class)
//    fun appendToQuoteFromTopAtFirstLine() {
//        setStyles(editText)
//        editText.append("first item")
//        editText.append("\n")
//        editText.append("second item")
//        editText.setSelection(0)
//        editText.text.insert(0, "addition ")
//
//        Assert.assertEquals("<$codeTag>addition first item<br>second item</$codeTag>", editText.toHtml())
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun appendToQuoteFromTop() {
//        editText.append("not in quote")
//        editText.append("\n")
//        setStyles(editText)
//        val mark = editText.length() - 1
//        editText.append("first item")
//        editText.append("\n")
//        editText.append("second item")
//
//        editText.setSelection(mark)
//        editText.text.insert(mark, "addition ")
//
//        Assert.assertEquals("not in quote<$codeTag>addition first item<br>second item</$codeTag>", editText.toHtml())
//    }
//
//
//    @Test
//    @Throws(Exception::class)
//    fun deleteFirstItemWithKeyboard() {
//        setStyles(editText)
//        editText.append("first item")
//        val firstMark = editText.length()
//        editText.append("\n")
//        editText.append("second item")
//        val secondMark = editText.length()
//        editText.append("\n")
//        editText.append("third item")
//        editText.setSelection(0)
//
//        Assert.assertEquals("first item\nsecond item\nthird item", editText.text.toString())
//
//        editText.text.delete(firstMark + 1, secondMark)
//
//        Assert.assertEquals("first item\n\nthird item", editText.text.toString())
//
//        Assert.assertEquals("<$codeTag>first item<br><br>third item</$codeTag>", editText.toHtml())
//
//        editText.text.delete(0, firstMark)
//
//        Assert.assertEquals("<$codeTag><br><br>third item</$codeTag>", editText.toHtml())
//    }
//
//}
