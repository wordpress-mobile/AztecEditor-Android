package org.wordpress.aztec

import android.app.Activity
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.wordpress.aztec.TestUtils.safeAppend
import org.wordpress.aztec.TestUtils.safeEmpty
import org.wordpress.aztec.TestUtils.safeLength
import org.wordpress.aztec.watchers.EndOfBufferMarkerAdder

/**
 * Testing quote behaviour.
 */
@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(23))
class QuoteTest {

    val formattingType = AztecTextFormat.FORMAT_QUOTE
    val quoteTag = "blockquote"
    lateinit var editText: AztecText

    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().visible().get()
        editText = AztecText(activity)
        editText.setCalypsoMode(false)
        activity.setContentView(editText)
    }

    @Test
    @Throws(Exception::class)
    fun styleSingleItem() {
        safeAppend(editText, "first item")
        editText.toggleFormatting(formattingType)
        Assert.assertEquals("<$quoteTag>first item</$quoteTag>", editText.toHtml())
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

        editText.toggleFormatting(formattingType)
        Assert.assertEquals("<$quoteTag>first item<br>second item<br>third item</$quoteTag>", editText.toHtml())
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
        editText.setSelection(4, 15) // we partially selected first and second item

        editText.toggleFormatting(formattingType)
        Assert.assertEquals("<$quoteTag>first item<br>second item</$quoteTag>third item", editText.toHtml())
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

        editText.toggleFormatting(formattingType)
        Assert.assertEquals("first item<$quoteTag>second item</$quoteTag>third item", editText.toHtml())
    }

    // enable styling on empty line and enter text

    @Test
    @Throws(Exception::class)
    fun emptyQuote() {
        editText.toggleFormatting(formattingType)
        Assert.assertEquals("<$quoteTag></$quoteTag>", editText.toHtml())

        // remove quote
        editText.toggleFormatting(formattingType)
        Assert.assertEquals(0, safeLength(editText))
        Assert.assertEquals("", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleSingleEnteredItem() {
        editText.toggleFormatting(formattingType)
        safeAppend(editText, "first item")
        Assert.assertEquals("<$quoteTag>first item</$quoteTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleMultipleEnteredItems() {
        editText.toggleFormatting(formattingType)
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        Assert.assertEquals("<$quoteTag>first item<br>second item</$quoteTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun closingPopulatedQuote() {
        editText.toggleFormatting(formattingType)
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")

        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        safeAppend(editText, "not in the quote")
        Assert.assertEquals("<$quoteTag>first item<br>second item</$quoteTag>not in the quote", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun closingEmptyQuote() {
        editText.toggleFormatting(formattingType)
        safeAppend(editText, "\n")
        Assert.assertEquals("", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun extendingQuoteBySplittingItems() {
        editText.toggleFormatting(formattingType)
        editText.append("firstitem")
        editText.text.insert(5, "\n")
        Assert.assertEquals("<$quoteTag>first<br>item</$quoteTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun quoteSplitWithToolbar() {
        editText.fromHtml("<$quoteTag>first item<br>second item<br>third item</$quoteTag>")
        editText.setSelection(14)
        editText.toggleFormatting(formattingType)

        Assert.assertEquals("first item<br>second item<br>third item", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun splitTwoQuotesWithNewline() {
        editText.fromHtml("<blockquote>Quote 1</blockquote><blockquote>Quote 2</blockquote>")
        val mark = editText.text.indexOf("Quote 2") - 1
        editText.text.insert(mark, "\n")
        editText.text.insert(mark + 1, "\n")
        Assert.assertEquals("<blockquote>Quote 1</blockquote><br><blockquote>Quote 2</blockquote>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun newlineAtStartOfQuote() {
        editText.fromHtml("<blockquote>Quote 1</blockquote><blockquote>Quote 2</blockquote>")
        val mark = editText.text.indexOf("Quote 2")
        editText.text.insert(mark, "\n")
        Assert.assertEquals("<blockquote>Quote 1</blockquote><blockquote><br>Quote 2</blockquote>", editText.toHtml())
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
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        val firstMark = safeLength(editText) - 4
        safeAppend(editText, "\n")
        safeAppend(editText, "third item")
        safeAppend(editText, "\n")
        val secondMark = safeLength(editText) - 4
        safeAppend(editText, "fourth item")
        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        safeAppend(editText, "not in quote")

        editText.setSelection(firstMark, secondMark)
        editText.toggleFormatting(formattingType)

        Assert.assertEquals("first item<br>second item<br>third item<br>fourth item<br>not in quote", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun emptyQuoteSurroundedBytItems() {
        editText.toggleFormatting(formattingType)
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        val firstMark = safeLength(editText)
        safeAppend(editText, "second item")
        safeAppend(editText, "\n")
        val secondMark = safeLength(editText)
        safeAppend(editText, "third item")

        editText.text.delete(firstMark, secondMark - 1)

        Assert.assertEquals("<$quoteTag>first item<br><br>third item</$quoteTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun trailingEmptyLine() {
        editText.toggleFormatting(formattingType)
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        safeAppend(editText, "\n")
        safeAppend(editText, "third item")
        safeAppend(editText, "\n")

        Assert.assertEquals("<$quoteTag>first item<br>second item<br>third item</$quoteTag>", editText.toHtml())
        safeAppend(editText, "\n")

        Assert.assertEquals("<$quoteTag>first item<br>second item<br>third item</$quoteTag>", editText.toHtml())

        safeAppend(editText, "not in quote")
        editText.text.insert(editText.text.indexOf("not in quote"), "\n")
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
        editText.setSelection(safeLength(editText))

        editText.text.insert(editText.text.indexOf("\nnot in quote"), " (appended)")

        Assert.assertEquals("<$quoteTag>first item<br>second item (appended)</$quoteTag>not in quote", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun openQuoteByMovingOutsideTextInsideIt() {
        editText.fromHtml("<$quoteTag>first item<br>second item</$quoteTag>")
        safeAppend(editText, "not in quote")

        editText.text.delete(editText.text.indexOf("not in quote"), editText.text.indexOf("not in quote"))
        Assert.assertEquals("<$quoteTag>first item<br>second itemnot in quote</$quoteTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun quoteRemainsClosedWhenLastCharacterIsDeleted() {
        editText.fromHtml("<$quoteTag>first item<br>second item</$quoteTag>not in quote")
        editText.setSelection(safeLength(editText))

        val mark = editText.text.indexOf("second item") + "second item".length

        // delete last character from "second item"
        editText.text.delete(mark - 1, mark)
        Assert.assertEquals("<$quoteTag>first item<br>second ite</$quoteTag>not in quote", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun openingAndReopeningOfQuote() {
        editText.fromHtml("<$quoteTag>first item<br>second item</$quoteTag>")
        editText.setSelection(safeLength(editText))

        safeAppend(editText, "\n")
        safeAppend(editText, "third item")
        Assert.assertEquals("<$quoteTag>first item<br>second item<br>third item</$quoteTag>", editText.toHtml())
        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        val mark = safeLength(editText) - 1
        safeAppend(editText, "not in the quote")
        Assert.assertEquals("<$quoteTag>first item<br>second item<br>third item</$quoteTag>not in the quote", editText.toHtml())
        safeAppend(editText, "\n")
        safeAppend(editText, "foo")
        Assert.assertEquals("<$quoteTag>first item<br>second item<br>third item</$quoteTag>not in the quote<br>foo", editText.toHtml())

        // reopen quote
        editText.text.delete(mark, mark + 1)
        Assert.assertEquals("<$quoteTag>first item<br>second item<br>third itemnot in the quote</$quoteTag>foo", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun closeQuote() {
        editText.fromHtml("<$quoteTag>first item<br>second item</$quoteTag>")
        editText.setSelection(safeLength(editText))

        Assert.assertEquals("first item\nsecond item", editText.text.toString())
        safeAppend(editText, "\n")
        Assert.assertEquals(EndOfBufferMarkerAdder.ensureEndOfTextMarker("first item\nsecond item\n"), editText.text.toString())

        editText.text.delete(editText.length() - 2, editText.length() - 1)
        Assert.assertEquals("first item\nsecond item", editText.text.toString())

        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        safeAppend(editText, "not in the quote")
        Assert.assertEquals("<$quoteTag>first item<br>second item</$quoteTag>not in the quote", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun handleQuoteReopeningAfterLastElementDeletion() {
        editText.fromHtml("<$quoteTag>first item<br>second item<br>third item</$quoteTag>")
        editText.setSelection(safeLength(editText))

        editText.text.delete(editText.text.indexOf("third item", 0), safeLength(editText))
        safeAppend(editText, "\n")

        safeAppend(editText, "not in the quote")
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
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")

        val mark = safeLength(editText)

        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        safeAppend(editText, "not in the quote")
        Assert.assertEquals("<$quoteTag>first item<br>second item</$quoteTag>not in the quote", editText.toHtml())

        editText.text.insert(mark, " (addition)")

        Assert.assertEquals("<$quoteTag>first item<br>second item (addition)</$quoteTag>not in the quote", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addItemToQuoteFromBottom() {
        editText.toggleFormatting(formattingType)
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        safeAppend(editText, "third item")
        editText.setSelection(safeLength(editText))

        editText.toggleFormatting(formattingType)

        Assert.assertEquals("<$quoteTag>first item<br>second item<br>third item</$quoteTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addItemToQuoteFromTop() {
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        editText.setSelection(safeLength(editText))
        editText.toggleFormatting(formattingType)
        safeAppend(editText, "\n")
        safeAppend(editText, "third item")

        editText.setSelection(0)

        editText.toggleFormatting(formattingType)

        Assert.assertEquals("<$quoteTag>first item<br>second item<br>third item</$quoteTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addItemToQuoteFromInside() {
        editText.toggleFormatting(formattingType)
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        safeAppend(editText, "\n")
        safeAppend(editText, "third item")
        editText.setSelection(safeLength(editText))
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
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        editText.setSelection(0)
        editText.text.insert(0, "addition ")

        Assert.assertEquals("<$quoteTag>addition first item<br>second item</$quoteTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun appendToQuoteFromTop() {
        safeAppend(editText, "not in quote")
        safeAppend(editText, "\n")
        editText.toggleFormatting(formattingType)
        val mark = editText.length() - 1
        safeAppend(editText, "first item")
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")

        editText.setSelection(mark)
        editText.text.insert(mark, "addition ")

        Assert.assertEquals("not in quote<$quoteTag>addition first item<br>second item</$quoteTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun deleteFirstItemWithKeyboard() {
        editText.toggleFormatting(formattingType)
        safeAppend(editText, "first item")
        val firstMark = safeLength(editText)
        safeAppend(editText, "\n")
        safeAppend(editText, "second item")
        val secondMark = safeLength(editText)
        safeAppend(editText, "\n")
        safeAppend(editText, "third item")
        editText.setSelection(0)

        Assert.assertEquals("first item\nsecond item\nthird item", editText.text.toString())

        editText.text.delete(firstMark + 1, secondMark)

        Assert.assertEquals("first item\n\nthird item", editText.text.toString())

        Assert.assertEquals("<$quoteTag>first item<br><br>third item</$quoteTag>", editText.toHtml())

        editText.text.delete(0, firstMark)

        Assert.assertEquals("<$quoteTag><br><br>third item</$quoteTag>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleMultilineSelectionWithLeadingEmptyLIne() {
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "\n")
        editText.setSelection(0, TestUtils.safeLength(editText))
        editText.toggleFormatting(formattingType)

        Assert.assertEquals("<$quoteTag></$quoteTag>", editText.toHtml())

        editText.toggleFormatting(formattingType)

        Assert.assertEquals("<br><br><br>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleWithSelectionStartOnLineEnd() {
        TestUtils.safeAppend(editText, "a")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "\n")
        editText.setSelection(1, editText.length())
        editText.toggleFormatting(formattingType)

        Assert.assertEquals("<$quoteTag>a</$quoteTag>", editText.toHtml())

        editText.toggleFormatting(formattingType)

        Assert.assertEquals("a<br><br>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleWithSelectionOnLineEndWithEmptyLineAbove() {
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "1")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "2")
        editText.setSelection(2, editText.length())
        editText.toggleFormatting(formattingType)

        Assert.assertEquals("<br><$quoteTag>1<br>2</$quoteTag>", editText.toHtml())

        editText.toggleFormatting(formattingType)

        Assert.assertEquals("<br>1<br>2", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleWithSelectionAtTheStartOfTheLine() {
        TestUtils.safeAppend(editText, "1")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "2")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "3")
        editText.setSelection(2, editText.length())
        editText.toggleFormatting(formattingType)

        Assert.assertEquals("1<$quoteTag>2<br>3</$quoteTag>", editText.toHtml())

        editText.toggleFormatting(formattingType)

        Assert.assertEquals("1<br>2<br>3", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleEmptyLineWithTrailingEmptyLine() {
        TestUtils.safeAppend(editText, "\n")
        editText.setSelection(0)
        editText.toggleFormatting(formattingType)

        Assert.assertEquals("<$quoteTag></$quoteTag>", editText.toHtml())

        editText.toggleFormatting(formattingType)

        Assert.assertEquals("<br>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleEmptyLineWithTrailingNonEmptyLine() {
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "1")
        editText.setSelection(0)
        editText.toggleFormatting(formattingType)

        Assert.assertEquals("<$quoteTag></$quoteTag>1", editText.toHtml())

        editText.toggleFormatting(formattingType)

        Assert.assertEquals("<br>1", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleEmptyLineSurroundedByNonEmptyLines() {
        TestUtils.safeAppend(editText, "1")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "2")

        editText.setSelection(2)
        editText.toggleFormatting(formattingType)

        Assert.assertEquals("1<$quoteTag></$quoteTag>2", editText.toHtml())

        editText.toggleFormatting(formattingType)

        Assert.assertEquals("1<br><br>2", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun styleMultilineQuoteSurroundedByQuotes() {
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "\n")

        editText.setSelection(1, 2)
        editText.toggleFormatting(formattingType)

        Assert.assertEquals("<br><$quoteTag></$quoteTag>", editText.toHtml())

        editText.toggleFormatting(formattingType)

        Assert.assertEquals("<br><br><br>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testMultiLineSelectionStylingWithSelectionEndOnEOL() {
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "1")
        TestUtils.safeAppend(editText, "\n")
        TestUtils.safeAppend(editText, "\n")

        editText.setSelection(1, 3)
        editText.toggleFormatting(formattingType)

        Assert.assertEquals("<br><$quoteTag>1</$quoteTag>", editText.toHtml())

        editText.toggleFormatting(formattingType)

        Assert.assertEquals("<br>1<br><br>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testMultiCharSelectionStylingWithCursorOnEOL() {
        editText.fromHtml("123<blockquote>4</blockquote>567")

        editText.setSelection(3)
        editText.toggleFormatting(formattingType)

        Assert.assertEquals("<blockquote>123<br>4</blockquote>567", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testSingleCharSelectionStylingWitWithCursorOnEOL() {
        editText.fromHtml("1<blockquote>2</blockquote>3")

        editText.setSelection(1)
        editText.toggleFormatting(formattingType)

        Assert.assertEquals("<blockquote>1<br>2</blockquote>3", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testMultiCharSelectionStylingWitWithCursorOnEOL() {
        editText.fromHtml("123<blockquote>4</blockquote>567")

        editText.setSelection(0, 4)
        editText.toggleFormatting(formattingType)

        Assert.assertEquals("<blockquote>123</blockquote><blockquote>4</blockquote>567", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testMultiCharSelectionStylingWitWithSelectionEndOnEOL() {
        editText.fromHtml("1<blockquote>2</blockquote>3")

        editText.setSelection(0, 1)
        editText.toggleFormatting(formattingType)

        Assert.assertEquals("<blockquote>1</blockquote><blockquote>2</blockquote>3", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testToggleQuoteSurroundedByTextWithSelectionAtTheEndOfIt() {
        // 1\nQuote\n2
        editText.fromHtml("1<blockquote>Quote</blockquote>2")

        // set selection at the end of the Quote
        editText.setSelection(7)
        editText.toggleFormatting(formattingType)

        Assert.assertEquals("1<br>Quote<br>2", editText.toHtml())

        editText.toggleFormatting(formattingType)
        Assert.assertEquals("1<blockquote>Quote</blockquote>2", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testRemoveQuoteFromLastLine() {
        editText.fromHtml("<blockquote>1<br>2</blockquote>")

        editText.setSelection(3)
        editText.toggleFormatting(formattingType)

        Assert.assertEquals("1<br>2", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testRemoveQuoteFromLastNotTheLastLine() {
        editText.fromHtml("<blockquote>1<br>2</blockquote>3")

        editText.setSelection(3)
        editText.toggleFormatting(formattingType)

        Assert.assertEquals("1<br>2<br>3", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun testApplyQuoteToTheSelectedEndOfTheLine() {
        editText.fromHtml("1<blockquote>2</blockquote>3")

        editText.setSelection(1, 2)
        editText.toggleFormatting(formattingType)

        Assert.assertEquals("<blockquote>1</blockquote><blockquote>2</blockquote>3", editText.toHtml())
    }
}
