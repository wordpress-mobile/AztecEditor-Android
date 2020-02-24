@file:Suppress("DEPRECATION")

package org.wordpress.aztec.plugins

import android.app.Activity
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import org.wordpress.aztec.plugins.TestUtils.backspaceAt
import org.wordpress.aztec.plugins.TestUtils.safeEmpty
import org.wordpress.aztec.plugins.wpcomments.CommentsTextFormat
import org.wordpress.aztec.plugins.wpcomments.WordPressCommentsPlugin
import org.wordpress.aztec.plugins.wpcomments.spans.WordPressCommentSpan
import org.wordpress.aztec.plugins.wpcomments.toolbar.MoreToolbarButton
import org.wordpress.aztec.plugins.wpcomments.toolbar.PageToolbarButton

/**
 * Tests for special comments ([WordPressCommentSpan.Comment.MORE] and [WordPressCommentSpan.Comment.PAGE])
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = intArrayOf(25))
class WordPressCommentTest {
    lateinit var editText: AztecText

    private val HTML_COMMENT_MORE = "<!--more-->"
    private val HTML_COMMENT_PAGE = "<!--nextpage-->"
    private val HTML_LIST_ORDERED = "<ol><li>Ordered</li></ol>"
    private val HTML_LIST_ORDERED_SELECTED_1 = "<ol><li>Or</li></ol>"
    private val HTML_LIST_ORDERED_SELECTED_2 = "<ol><li>red</li></ol>"
    private val HTML_LIST_ORDERED_SPLIT_1 = "<ol><li>Or</li></ol>"
    private val HTML_LIST_ORDERED_SPLIT_2 = "<ol><li>dered</li></ol>"
    private val HTML_LIST_UNORDERED = "<ul><li>Unordered</li></ul>"
    private val HTML_LIST_UNORDERED_SELECTED_1 = "<ul><li>Un</li></ul>"
    private val HTML_LIST_UNORDERED_SELECTED_2 = "<ul><li>dered</li></ul>"
    private val HTML_LIST_UNORDERED_SPLIT_1 = "<ul><li>Un</li></ul>"
    private val HTML_LIST_UNORDERED_SPLIT_2 = "<ul><li>ordered</li></ul>"
    private val HTML_QUOTE = "<blockquote>Quote</blockquote>"
    private val HTML_QUOTE_SELECTED_1 = "<blockquote>Qu</blockquote>"
    private val HTML_QUOTE_SELECTED_2 = "<blockquote>e</blockquote>"
    private val HTML_QUOTE_SPLIT_1 = "<blockquote>Qu</blockquote>"
    private val HTML_QUOTE_SPLIT_2 = "<blockquote>ote</blockquote>"

    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().visible().get()

        editText = AztecText(activity)
        editText.setCalypsoMode(false)
        activity.setContentView(editText)

        editText.plugins.add(WordPressCommentsPlugin(editText))
        editText.plugins.add(MoreToolbarButton(editText))
        editText.plugins.add(PageToolbarButton(editText))
    }

    /**
     * Insert [WordPressCommentSpan.Comment.MORE] comment across multiple selected block elements.
     * If comment replaces selected text and block elements remain styled before and after comment,
     * [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreAcrossMultipleBlocks() {
        Assert.assertTrue(safeEmpty(editText))

        val html = HTML_LIST_ORDERED + HTML_LIST_UNORDERED + HTML_QUOTE
        editText.fromHtml(html)
        editText.setSelection(2, 20) // select between second character of ordered list and second character of quote (includes newline characters)
        editText.toggleFormatting(CommentsTextFormat.FORMAT_MORE)

        Assert.assertEquals("$HTML_LIST_ORDERED_SELECTED_1$HTML_COMMENT_MORE$HTML_QUOTE_SPLIT_2", editText.toHtml())
    }

    /**
     * Insert [WordPressCommentSpan.Comment.MORE] comment following an ordered list.
     * If comment is inserted and ordered list remains styled, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreAfterOrderedList() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(HTML_LIST_ORDERED)
        editText.setSelection(editText.length()) // select after list
        editText.toggleFormatting(CommentsTextFormat.FORMAT_MORE)

        Assert.assertEquals("$HTML_LIST_ORDERED$HTML_COMMENT_MORE", editText.toHtml())
    }

    /**
     * Insert [WordPressCommentSpan.Comment.MORE] comment following a quote.
     * If comment is inserted and quote remains styled, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreAfterQuote() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(HTML_QUOTE)
        editText.setSelection(editText.length()) // select after quote
        editText.toggleFormatting(CommentsTextFormat.FORMAT_MORE)

        Assert.assertEquals("$HTML_QUOTE$HTML_COMMENT_MORE", editText.toHtml())
    }

    /**
     * Insert [WordPressCommentSpan.Comment.MORE] comment following an unordered list.
     * If comment is inserted and unordered list remains styled, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreAfterUnorderedList() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(HTML_LIST_UNORDERED)
        editText.setSelection(editText.length()) // select after list
        editText.toggleFormatting(CommentsTextFormat.FORMAT_MORE)

        Assert.assertEquals("$HTML_LIST_UNORDERED$HTML_COMMENT_MORE", editText.toHtml())
    }

    /**
     * Insert [WordPressCommentSpan.Comment.MORE] comment preceding an ordered list.
     * If comment is inserted and ordered list remains styled, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreBeforeOrderedList() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(HTML_LIST_ORDERED)
        editText.text.insert(0, "\n")
        backspaceAt(editText, 0)
        editText.toggleFormatting(CommentsTextFormat.FORMAT_MORE)

        Assert.assertEquals("$HTML_COMMENT_MORE$HTML_LIST_ORDERED", editText.toHtml())
    }

    /**
     * Insert [WordPressCommentSpan.Comment.MORE] comment preceding a quote.
     * If comment is inserted and quote remains styled, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreBeforeQuote() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(HTML_QUOTE)
        editText.setSelection(0) // select before quote
        editText.toggleFormatting(CommentsTextFormat.FORMAT_MORE)

        Assert.assertEquals("$HTML_COMMENT_MORE$HTML_QUOTE", editText.toHtml())
    }

    /**
     * Insert [WordPressCommentSpan.Comment.MORE] comment preceding an unordered list.
     * If comment is inserted and unordered list remains styled, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreBeforeUnorderedList() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(HTML_LIST_UNORDERED)
        editText.setSelection(0) // select before list
        editText.toggleFormatting(CommentsTextFormat.FORMAT_MORE)

        Assert.assertEquals("$HTML_COMMENT_MORE$HTML_LIST_UNORDERED", editText.toHtml())
    }

    /**
     * Insert [WordPressCommentSpan.Comment.MORE] comment inside of ordered list.
     * If comment is inserted at point of selection and ordered list remains styled before and
     * after comment, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreInsideOrderedList() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(HTML_LIST_ORDERED)
        editText.setSelection(2) // select after second character in list
        editText.toggleFormatting(CommentsTextFormat.FORMAT_MORE)

        Assert.assertEquals("$HTML_LIST_ORDERED_SPLIT_1$HTML_COMMENT_MORE$HTML_LIST_ORDERED_SPLIT_2", editText.toHtml())
    }

    /**
     * Insert [WordPressCommentSpan.Comment.MORE] comment inside of quote.
     * If comment is inserted at point of selection and quote remains styled before and
     * after comment, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreInsideQuote() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(HTML_QUOTE)
        editText.setSelection(2) // select after second character in quote
        editText.toggleFormatting(CommentsTextFormat.FORMAT_MORE)

        Assert.assertEquals("$HTML_QUOTE_SPLIT_1$HTML_COMMENT_MORE$HTML_QUOTE_SPLIT_2", editText.toHtml())
    }

    /**
     * Insert [WordPressCommentSpan.Comment.MORE] comment inside of unordered list.
     * If comment is inserted at point of selection and unordered list remains styled before and
     * after comment, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreInsideUnorderedList() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(HTML_LIST_UNORDERED)
        editText.setSelection(2) // select after second character in list
        editText.toggleFormatting(CommentsTextFormat.FORMAT_MORE)

        Assert.assertEquals("$HTML_LIST_UNORDERED_SPLIT_1$HTML_COMMENT_MORE$HTML_LIST_UNORDERED_SPLIT_2", editText.toHtml())
    }

    /**
     * Insert [WordPressCommentSpan.Comment.MORE] comment inside selected portion of ordered list.
     * If comment replaces selected text and ordered list remains styled before and after comment,
     * [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreInsideSelectedOrderedList() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(HTML_LIST_ORDERED)
        editText.setSelection(2, 4) // select between second and fourth character in list
        editText.toggleFormatting(CommentsTextFormat.FORMAT_MORE)

        Assert.assertEquals("$HTML_LIST_ORDERED_SELECTED_1$HTML_COMMENT_MORE$HTML_LIST_ORDERED_SELECTED_2", editText.toHtml())
    }

    /**
     * Insert [WordPressCommentSpan.Comment.MORE] comment inside selected portion of quote.
     * If comment replaces selected text and quote remains styled before and after comment,
     * [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreInsideSelectedQuote() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(HTML_QUOTE)
        editText.setSelection(2, 4) // select between second and fourth character in quote
        editText.toggleFormatting(CommentsTextFormat.FORMAT_MORE)

        Assert.assertEquals("$HTML_QUOTE_SELECTED_1$HTML_COMMENT_MORE$HTML_QUOTE_SELECTED_2", editText.toHtml())
    }

    /**
     * Insert [WordPressCommentSpan.Comment.MORE] comment inside selected portion of unordered list.
     * If comment replaces selected text and unordered list remains styled before and after comment,
     * [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreInsideSelectedUnorderedList() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(HTML_LIST_UNORDERED)
        editText.setSelection(2, 4) // select between second and fourth character in list
        editText.toggleFormatting(CommentsTextFormat.FORMAT_MORE)

        Assert.assertEquals("$HTML_LIST_UNORDERED_SELECTED_1$HTML_COMMENT_MORE$HTML_LIST_UNORDERED_SELECTED_2", editText.toHtml())
    }

    /**
     * Insert [WordPressCommentSpan.Comment.PAGE] comment across multiple selected block elements.
     * If comment replaces selected text and block elements remain styled before and after comment,
     * [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageAcrossMultipleBlocks() {
        Assert.assertTrue(safeEmpty(editText))

        val html = HTML_LIST_ORDERED + HTML_LIST_UNORDERED + HTML_QUOTE
        editText.fromHtml(html)
        editText.setSelection(2, 20) // select between second character of ordered list and second character of quote (includes newline characters)
        editText.toggleFormatting(CommentsTextFormat.FORMAT_PAGE)

        Assert.assertEquals("$HTML_LIST_ORDERED_SELECTED_1$HTML_COMMENT_PAGE$HTML_QUOTE_SPLIT_2", editText.toHtml())
    }

    /**
     * Insert [WordPressCommentSpan.Comment.PAGE] comment following an ordered list.
     * If comment is inserted and ordered list remains styled, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageAfterOrderedList() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(HTML_LIST_ORDERED)
        editText.setSelection(editText.length()) // select after list
        editText.toggleFormatting(CommentsTextFormat.FORMAT_PAGE)

        Assert.assertEquals("$HTML_LIST_ORDERED$HTML_COMMENT_PAGE", editText.toHtml())
    }

    /**
     * Insert [WordPressCommentSpan.Comment.PAGE] comment following a quote.
     * If comment is inserted and quote remains styled, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageAfterQuote() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(HTML_QUOTE)
        editText.setSelection(editText.length()) // select after quote
        editText.toggleFormatting(CommentsTextFormat.FORMAT_PAGE)

        Assert.assertEquals("$HTML_QUOTE$HTML_COMMENT_PAGE", editText.toHtml())
    }

    /**
     * Insert [WordPressCommentSpan.Comment.PAGE] comment following an unordered list.
     * If comment is inserted and unordered list remains styled, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageAfterUnorderedList() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(HTML_LIST_UNORDERED)
        editText.setSelection(editText.length()) // select after list
        editText.toggleFormatting(CommentsTextFormat.FORMAT_PAGE)

        Assert.assertEquals("$HTML_LIST_UNORDERED$HTML_COMMENT_PAGE", editText.toHtml())
    }

    /**
     * Insert [WordPressCommentSpan.Comment.PAGE] comment preceding an ordered list.
     * If comment is inserted and ordered list remains styled, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageBeforeOrderedList() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(HTML_LIST_ORDERED)
        editText.setSelection(0) // select before list
        editText.toggleFormatting(CommentsTextFormat.FORMAT_PAGE)

        Assert.assertEquals("$HTML_COMMENT_PAGE$HTML_LIST_ORDERED", editText.toHtml())
    }

    /**
     * Insert [WordPressCommentSpan.Comment.PAGE] comment preceding a quote.
     * If comment is inserted and quote remains styled, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageBeforeQuote() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(HTML_QUOTE)
        editText.setSelection(0) // select before quote
        editText.toggleFormatting(CommentsTextFormat.FORMAT_PAGE)

        Assert.assertEquals("$HTML_COMMENT_PAGE$HTML_QUOTE", editText.toHtml())
    }

    /**
     * Insert [WordPressCommentSpan.Comment.PAGE] comment preceding an unordered list.
     * If comment is inserted and unordered list remains styled, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageBeforeUnorderedList() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(HTML_LIST_UNORDERED)
        editText.setSelection(0) // select before list
        editText.toggleFormatting(CommentsTextFormat.FORMAT_PAGE)

        Assert.assertEquals("$HTML_COMMENT_PAGE$HTML_LIST_UNORDERED", editText.toHtml())
    }

    /**
     * Insert [WordPressCommentSpan.Comment.PAGE] comment inside of ordered list.
     * If comment is inserted at point of selection and ordered list remains styled before and
     * after comment, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageInsideOrderedList() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(HTML_LIST_ORDERED)
        editText.setSelection(2) // select after second character in list
        editText.toggleFormatting(CommentsTextFormat.FORMAT_PAGE)

        Assert.assertEquals("$HTML_LIST_ORDERED_SPLIT_1$HTML_COMMENT_PAGE$HTML_LIST_ORDERED_SPLIT_2", editText.toHtml())
    }

    /**
     * Insert [WordPressCommentSpan.Comment.PAGE] comment inside of quote.
     * If comment is inserted at point of selection and quote remains styled before and
     * after comment, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageInsideQuote() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(HTML_QUOTE)
        editText.setSelection(2) // select after second character in quote
        editText.toggleFormatting(CommentsTextFormat.FORMAT_PAGE)

        Assert.assertEquals("$HTML_QUOTE_SPLIT_1$HTML_COMMENT_PAGE$HTML_QUOTE_SPLIT_2", editText.toHtml())
    }

    /**
     * Insert [WordPressCommentSpan.Comment.PAGE] comment inside of unordered list.
     * If comment is inserted at point of selection and unordered list remains styled before and
     * after comment, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageInsideUnorderedList() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(HTML_LIST_UNORDERED)
        editText.setSelection(2) // select after second character in list
        editText.toggleFormatting(CommentsTextFormat.FORMAT_PAGE)

        Assert.assertEquals("$HTML_LIST_UNORDERED_SPLIT_1$HTML_COMMENT_PAGE$HTML_LIST_UNORDERED_SPLIT_2", editText.toHtml())
    }

    /**
     * Insert [WordPressCommentSpan.Comment.PAGE] comment inside selected portion of ordered list.
     * If comment replaces selected text and ordered list remains styled before and after comment,
     * [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageInsideSelectedOrderedList() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(HTML_LIST_ORDERED)
        editText.setSelection(2, 4) // select between second and fourth character in list
        editText.toggleFormatting(CommentsTextFormat.FORMAT_PAGE)

        Assert.assertEquals("$HTML_LIST_ORDERED_SELECTED_1$HTML_COMMENT_PAGE$HTML_LIST_ORDERED_SELECTED_2", editText.toHtml())
    }

    /**
     * Insert [WordPressCommentSpan.Comment.PAGE] comment inside selected portion of quote.
     * If comment replaces selected text and quote remains styled before and after comment,
     * [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageInsideSelectedQuote() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(HTML_QUOTE)
        editText.setSelection(2, 4) // select between second and fourth character in quote
        editText.toggleFormatting(CommentsTextFormat.FORMAT_PAGE)

        Assert.assertEquals("$HTML_QUOTE_SELECTED_1$HTML_COMMENT_PAGE$HTML_QUOTE_SELECTED_2", editText.toHtml())
    }

    /**
     * Insert [WordPressCommentSpan.Comment.PAGE] comment inside selected portion of unordered list.
     * If comment replaces selected text and unordered list remains styled before and after comment,
     * [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageInsideSelectedUnorderedList() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(HTML_LIST_UNORDERED)
        editText.setSelection(2, 4) // select between second and fourth character in list
        editText.toggleFormatting(CommentsTextFormat.FORMAT_PAGE)

        Assert.assertEquals("$HTML_LIST_UNORDERED_SELECTED_1$HTML_COMMENT_PAGE$HTML_LIST_UNORDERED_SELECTED_2", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun insertTwoPageSpecialThenAddNewlinesInBetweenComments() {
        Assert.assertTrue(safeEmpty(editText))

        editText.toggleFormatting(CommentsTextFormat.FORMAT_PAGE)

        Assert.assertEquals(HTML_COMMENT_PAGE, editText.toHtml())

        val index = editText.length()

        editText.toggleFormatting(CommentsTextFormat.FORMAT_PAGE)

        Assert.assertEquals("$HTML_COMMENT_PAGE$HTML_COMMENT_PAGE", editText.toHtml())

        editText.text.insert(index, Constants.NEWLINE_STRING)

        Assert.assertEquals("$HTML_COMMENT_PAGE<br>$HTML_COMMENT_PAGE", editText.toHtml())

        editText.fromHtml(editText.toHtml())

        Assert.assertEquals("${Constants.MAGIC_CHAR}\n\n${Constants.MAGIC_CHAR}", editText.text.toString())
        Assert.assertEquals("$HTML_COMMENT_PAGE<br>$HTML_COMMENT_PAGE", editText.toHtml())

        editText.text.insert(index, Constants.NEWLINE_STRING)

        Assert.assertEquals("$HTML_COMMENT_PAGE<br><br>$HTML_COMMENT_PAGE", editText.toHtml())
        editText.fromHtml(editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun addNewlinesAroundSpecialComments() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml("a$HTML_COMMENT_PAGE<br>$HTML_COMMENT_PAGE" + "b")

        editText.text.insert(1, Constants.NEWLINE_STRING)
        editText.text.insert(editText.length() - 1, Constants.NEWLINE_STRING)

        Assert.assertEquals("a<br><br>$HTML_COMMENT_PAGE<br>$HTML_COMMENT_PAGE<br>b", editText.toHtml())

        editText.fromHtml(editText.toHtml())

        Assert.assertEquals("a\n\n${Constants.MAGIC_CHAR}\n\n${Constants.MAGIC_CHAR}\n\nb", editText.text.toString())
    }

    @Test
    @Throws(Exception::class)
    fun testInsertionRightNextToSpecialComments() {
        Assert.assertTrue(safeEmpty(editText))

        editText.fromHtml(HTML_COMMENT_MORE)
        editText.setSelection(editText.length())

        editText.text.append("b")

        Assert.assertEquals(HTML_COMMENT_MORE + "b", editText.toHtml())

        editText.text.insert(0, "a")

        Assert.assertEquals("a" + HTML_COMMENT_MORE + "b", editText.toHtml())

        Assert.assertEquals("a\n${Constants.MAGIC_CHAR}\nb", editText.text.toString())
    }
}
