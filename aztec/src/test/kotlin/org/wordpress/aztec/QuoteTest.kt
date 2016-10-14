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
 * Testing quote behaviour.
 */
@RunWith(RobolectricGradleTestRunner::class)
@Config(constants = BuildConfig::class)
class QuoteTest() {

    val formattingType = TextFormat.FORMAT_QUOTE
    val quoteTag = "blockquote"
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

    @Test
    @Throws(Exception::class)
    fun styleSingleItem() {
        editText.append("first item")
        editText.toggleFormatting(formattingType)
        Assert.assertEquals("<$quoteTag>first item</$quoteTag>", editText.toHtml())
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

        editText.toggleFormatting(formattingType)
        Assert.assertEquals("<$quoteTag>first item<br>second item<br>third item</$quoteTag>", editText.toHtml())
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

        editText.toggleFormatting(formattingType)
        Assert.assertEquals("<$quoteTag>first item<br>second item</$quoteTag>third item", editText.toHtml())
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

        editText.toggleFormatting(formattingType)
        Assert.assertEquals("first item<$quoteTag>second item</$quoteTag>third item", editText.toHtml())
    }


    //enable styling on empty line and enter text

    @Test
    @Throws(Exception::class)
    fun emptyQuote() {
        editText.toggleFormatting(formattingType)
        Assert.assertEquals("<$quoteTag></$quoteTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleSingleEnteredItem() {
        editText.toggleFormatting(formattingType)
        editText.append("first item")
        Assert.assertEquals("<$quoteTag>first item</$quoteTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleMultipleEnteredItems() {
        editText.toggleFormatting(formattingType)
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")
        Assert.assertEquals("<$quoteTag>first item<br>second item</$quoteTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun closingPopulatedQuote() {
        editText.toggleFormatting(formattingType)
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")

        editText.append("\n")
        editText.append("\n")
        editText.append("not in the quote")
        Assert.assertEquals("<$quoteTag>first item<br>second item</$quoteTag>not in the quote", editText.toHtml().toString())
    }


    @Test
    @Throws(Exception::class)
    fun closingEmptyQuote() {
        editText.toggleFormatting(formattingType)
        editText.append("\n")
        Assert.assertEquals("", editText.toHtml().toString())
    }

    @Test
    @Throws(Exception::class)
    fun extendingQuoteBySplittingItems() {
        editText.toggleFormatting(formattingType)
        editText.append("firstitem")
        editText.text.insert(5, "\n")
        Assert.assertEquals("<$quoteTag>first<br>item</$quoteTag>", editText.toHtml().toString())
    }


    @Test
    @Throws(Exception::class)
    fun quoteSplitWithToolbar() {
        editText.fromHtml("<$quoteTag>first item<br>second item<br>third item</$quoteTag>")
        editText.setSelection(14)
        editText.toggleFormatting(formattingType)

        Assert.assertEquals("<$quoteTag>first item</$quoteTag>second item<$quoteTag>third item</$quoteTag>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun removeQuoteStyling() {
        editText.fromHtml("<$quoteTag>first item</$quoteTag>")
        editText.setSelection(1)
        editText.toggleFormatting(formattingType)

        Assert.assertEquals("first item", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun removeQuoteStylingForPartialSelection() {
        editText.fromHtml("<$quoteTag>first item</$quoteTag>")
        editText.setSelection(2, 4)
        editText.toggleFormatting(formattingType)

        Assert.assertEquals("first item", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun removeQuoteStylingForMultilinePartialSelection() {
        editText.toggleFormatting(formattingType)
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
        editText.append("not in quote")

        editText.setSelection(firstMark, secondMark)
        editText.toggleFormatting(formattingType)

        Assert.assertEquals("<$quoteTag>first item</$quoteTag>second item<br>third item<$quoteTag>fourth item</$quoteTag>not in quote", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun emptyQuoteSurroundedBytItems() {
        editText.toggleFormatting(formattingType)
        editText.append("first item")
        editText.append("\n")
        val firstMark = editText.length()
        editText.append("second item")
        editText.append("\n")
        val secondMart = editText.length()
        editText.append("third item")

        editText.text.delete(firstMark - 1, secondMart - 2)

        Assert.assertEquals("<$quoteTag>first item<br><br>third item</$quoteTag>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun trailingEmptyLine() {
        editText.toggleFormatting(formattingType)
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")
        editText.append("\n")
        editText.append("third item")
        val mark = editText.length()
        editText.append("\n")

        Assert.assertEquals("<$quoteTag>first item<br>second item<br>third item</$quoteTag>", editText.toHtml())
        editText.append("\n")

        Assert.assertEquals("<$quoteTag>first item<br>second item<br>third item</$quoteTag>", editText.toHtml())

        editText.append("not in quote")
        editText.setSelection(mark)
        editText.text.insert(mark, "\n")
        Assert.assertEquals("<$quoteTag>first item<br>second item<br>third item</$quoteTag><br>not in quote", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun openQuoteByAddingNewline() {
        editText.fromHtml("<$quoteTag>first item<br>second item</$quoteTag>not in quote")


        val mark = editText.text.indexOf("second item") + "second item".length

        editText.text.insert(mark, "\n")
        editText.text.insert(mark + 1, "third item")


        Assert.assertEquals("<$quoteTag>first item<br>second item<br>third item</$quoteTag>not in quote", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun openQuoteByAppendingTextToTheEnd() {
        editText.fromHtml("<$quoteTag>first item<br>second item</$quoteTag>not in quote")
        editText.setSelection(editText.length())

        editText.text.insert(editText.text.indexOf("\nnot in quote"), " (appended)")

        Assert.assertEquals("<$quoteTag>first item<br>second item (appended)</$quoteTag>not in quote", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun openQuoteByMovingOutsideTextInsideIt() {
        editText.fromHtml("<$quoteTag>first item<br>second item</$quoteTag>")
        editText.append("not in quote")

        editText.text.delete(editText.text.indexOf("not in quote"), editText.text.indexOf("not in quote"))
        Assert.assertEquals("<$quoteTag>first item<br>second itemnot in quote</$quoteTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun quoteRemainsClosedWhenLastCharacterIsDeleted() {
        editText.fromHtml("<$quoteTag>first item<br>second item</$quoteTag>not in quote")
        editText.setSelection(editText.length())

        val mark = editText.text.indexOf("second item") + "second item".length;

        //delete last character from "second item"
        editText.text.delete(mark - 1, mark)
        Assert.assertEquals("<$quoteTag>first item<br>second ite</$quoteTag>not in quote", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun openingAndReopeningOfQuote() {
        editText.fromHtml("<$quoteTag>first item<br>second item</$quoteTag>")
        editText.setSelection(editText.length())

        editText.append("\n")
        editText.append("third item")
        Assert.assertEquals("<$quoteTag>first item<br>second item<br>third item</$quoteTag>", editText.toHtml())
        editText.append("\n")
        editText.append("\n")
        val mark = editText.length() - 1
        editText.append("not in the quote")
        Assert.assertEquals("<$quoteTag>first item<br>second item<br>third item</$quoteTag>not in the quote", editText.toHtml())
        editText.append("\n")
        editText.append("foo")
        Assert.assertEquals("<$quoteTag>first item<br>second item<br>third item</$quoteTag>not in the quote<br>foo", editText.toHtml())

        //reopen quote
        editText.text.delete(mark, mark + 1)
        Assert.assertEquals("<$quoteTag>first item<br>second item<br>third itemnot in the quote</$quoteTag>foo", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun closeQuote() {
        editText.fromHtml("<$quoteTag>first item<br>second item</$quoteTag>")
        editText.setSelection(editText.length())

        Assert.assertEquals("first item\nsecond item", editText.text.toString())
        editText.append("\n")
        Assert.assertEquals("first item\nsecond item\n\u200B", editText.text.toString())
        val mark = editText.length() - 1

        editText.text.delete(editText.length() - 1, editText.length())
        Assert.assertEquals("first item\nsecond item\n", editText.text.toString())

        editText.append("not in the quote")
        Assert.assertEquals("<$quoteTag>first item<br>second item</$quoteTag>not in the quote", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun handlequoteReopeningAfterLastElementDeletion() {
        editText.fromHtml("<$quoteTag>first item<br>second item<br>third item</$quoteTag>")
        editText.setSelection(editText.length())

        editText.text.delete(editText.text.indexOf("third item", 0), editText.length())

        editText.append("not in the quote")
        Assert.assertEquals("<$quoteTag>first item<br>second item</$quoteTag>not in the quote", editText.toHtml())

        editText.text.insert(editText.text.indexOf("not in the quote") - 1, " addition")
        Assert.assertEquals("<$quoteTag>first item<br>second item addition</$quoteTag>not in the quote", editText.toHtml())

        editText.text.insert(editText.text.indexOf("not in the quote") - 1, "\n")
        editText.text.insert(editText.text.indexOf("not in the quote") - 1, "third item")
        Assert.assertEquals("first item\nsecond item addition\nthird item\nnot in the quote", editText.text.toString())
        Assert.assertEquals("<$quoteTag>first item<br>second item addition<br>third item</$quoteTag>not in the quote", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun additionToClosedQuote() {
        editText.toggleFormatting(formattingType)
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")

        val mark = editText.length()

        editText.append("\n")
        editText.append("\n")
        editText.append("not in the quote")
        Assert.assertEquals("<$quoteTag>first item<br>second item</$quoteTag>not in the quote", editText.toHtml().toString())

        editText.text.insert(mark, " (addition)")

        Assert.assertEquals("<$quoteTag>first item<br>second item (addition)</$quoteTag>not in the quote", editText.toHtml().toString())
    }

    @Test
    @Throws(Exception::class)
    fun addItemToQuoteFromBottom() {
        editText.toggleFormatting(formattingType)
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")
        editText.append("\n")
        editText.append("\n")
        editText.append("third item")
        editText.setSelection(editText.length())

        editText.toggleFormatting(formattingType)

        Assert.assertEquals("<$quoteTag>first item<br>second item<br>third item</$quoteTag>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun addItemToQuoteFromTop() {
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")
        editText.setSelection(editText.length())
        editText.toggleFormatting(formattingType)
        editText.append("\n")
        editText.append("third item")

        editText.setSelection(0)

        editText.toggleFormatting(formattingType)

        Assert.assertEquals("<$quoteTag>first item<br>second item<br>third item</$quoteTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addItemToQuoteFromInside() {
        editText.toggleFormatting(formattingType)
        editText.append("first item")
        editText.append("\n")
        editText.append("\n")
        editText.append("second item")
        editText.append("\n")
        editText.append("third item")
        editText.setSelection(editText.length())
        editText.toggleFormatting(formattingType)

        Assert.assertEquals("<$quoteTag>first item</$quoteTag>second item<$quoteTag>third item</$quoteTag>", editText.toHtml())
        editText.setSelection(15)
        editText.toggleFormatting(formattingType)

        Assert.assertEquals("<$quoteTag>first item<br>second item<br>third item</$quoteTag>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun appendToQuoteFromTopAtFirstLine() {
        editText.toggleFormatting(formattingType)
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")
        editText.setSelection(0)
        editText.text.insert(0, "addition ")

        Assert.assertEquals("<$quoteTag>addition first item<br>second item</$quoteTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun appendToQuoteFromTop() {
        editText.append("not in quote")
        editText.append("\n")
        editText.toggleFormatting(formattingType)
        val mark = editText.length() - 1
        editText.append("first item")
        editText.append("\n")
        editText.append("second item")

        editText.setSelection(mark)
        editText.text.insert(mark, "addition ")

        Assert.assertEquals("not in quote<$quoteTag>addition first item<br>second item</$quoteTag>", editText.toHtml())
    }


    @Test
    @Throws(Exception::class)
    fun deleteFirstItemWithKeyboard() {
        editText.toggleFormatting(formattingType)
        editText.append("first item")
        val firstMark = editText.length()
        editText.append("\n")
        editText.append("second item")
        val secondMark = editText.length()
        editText.append("\n")
        editText.append("third item")
        editText.setSelection(0)

        Assert.assertEquals("first item\nsecond item\nthird item", editText.text.toString())

        editText.text.delete(firstMark + 1, secondMark)

        Assert.assertEquals("first item\n\nthird item", editText.text.toString())

        Assert.assertEquals("<$quoteTag>first item<br><br>third item</$quoteTag>", editText.toHtml())

        editText.text.delete(0, firstMark)

        Assert.assertEquals("<$quoteTag><br>third item</$quoteTag>", editText.toHtml())
    }

}
