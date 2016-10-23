package org.wordpress.aztec

import android.app.Activity
import android.test.AndroidTestCase
import android.test.mock.MockContext
import android.text.TextUtils
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.annotation.Config
import org.wordpress.aztec.spans.AztecCommentSpan

/**
 * Tests for special comments ([AztecCommentSpan.Comment.MORE] and [AztecCommentSpan.Comment.PAGE])
 */
@RunWith(RobolectricGradleTestRunner::class)
@Config(constants = BuildConfig::class)
class AztecCommentTest() : AndroidTestCase() {
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
        activity.setContentView(editText)
        context = MockContext()
    }

    /**
     * Insert [AztecCommentSpan.Comment.MORE] comment across multiple selected block elements.
     * If comment replaces selected text and block elements remain styled before and after comment,
     * [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreAcrossMultipleBlocks() {
        Assert.assertTrue(TextUtils.isEmpty(editText.text))

        val html = HTML_LIST_ORDERED + HTML_LIST_UNORDERED + HTML_QUOTE
        editText.fromHtml(html)
        editText.setSelection(2, 20) // select between second character of ordered list and second character of quote (includes newline characters)
        editText.applyComment(AztecCommentSpan.Comment.MORE)

        Assert.assertEquals("$HTML_LIST_ORDERED_SELECTED_1$HTML_COMMENT_MORE$HTML_QUOTE_SPLIT_2", editText.toHtml())
    }

    /**
     * Insert [AztecCommentSpan.Comment.MORE] comment following an ordered list.
     * If comment is inserted and ordered list remains styled, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreAfterOrderedList() {
        Assert.assertTrue(TextUtils.isEmpty(editText.text))

        editText.fromHtml(HTML_LIST_ORDERED)
        editText.setSelection(editText.length()) // select after list
        editText.applyComment(AztecCommentSpan.Comment.MORE)

        Assert.assertEquals("$HTML_LIST_ORDERED$HTML_COMMENT_MORE<br>", editText.toHtml())
    }

    /**
     * Insert [AztecCommentSpan.Comment.MORE] comment following a quote.
     * If comment is inserted and quote remains styled, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreAfterQuote() {
        Assert.assertTrue(TextUtils.isEmpty(editText.text))

        editText.fromHtml(HTML_QUOTE)
        editText.setSelection(editText.length()) // select after quote
        editText.applyComment(AztecCommentSpan.Comment.MORE)

        Assert.assertEquals("$HTML_QUOTE$HTML_COMMENT_MORE<br>", editText.toHtml())
    }

    /**
     * Insert [AztecCommentSpan.Comment.MORE] comment following an unordered list.
     * If comment is inserted and unordered list remains styled, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreAfterUnorderedList() {
        Assert.assertTrue(TextUtils.isEmpty(editText.text))

        editText.fromHtml(HTML_LIST_UNORDERED)
        editText.setSelection(editText.length()) // select after list
        editText.applyComment(AztecCommentSpan.Comment.MORE)

        Assert.assertEquals("$HTML_LIST_UNORDERED$HTML_COMMENT_MORE<br>", editText.toHtml())
    }

    /**
     * Insert [AztecCommentSpan.Comment.MORE] comment preceding an ordered list.
     * If comment is inserted and ordered list remains styled, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreBeforeOrderedList() {
        Assert.assertTrue(TextUtils.isEmpty(editText.text))

        editText.fromHtml(HTML_LIST_ORDERED)
        editText.setSelection(0) // select before list
        editText.applyComment(AztecCommentSpan.Comment.MORE)

        Assert.assertEquals("<br>$HTML_COMMENT_MORE$HTML_LIST_ORDERED", editText.toHtml())
    }

    /**
     * Insert [AztecCommentSpan.Comment.MORE] comment preceding a quote.
     * If comment is inserted and quote remains styled, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreBeforeQuote() {
        Assert.assertTrue(TextUtils.isEmpty(editText.text))

        editText.fromHtml(HTML_QUOTE)
        editText.setSelection(0) // select before quote
        editText.applyComment(AztecCommentSpan.Comment.MORE)

        Assert.assertEquals("<br>$HTML_COMMENT_MORE$HTML_QUOTE", editText.toHtml())
    }

    /**
     * Insert [AztecCommentSpan.Comment.MORE] comment preceding an unordered list.
     * If comment is inserted and unordered list remains styled, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreBeforeUnorderedList() {
        Assert.assertTrue(TextUtils.isEmpty(editText.text))

        editText.fromHtml(HTML_LIST_UNORDERED)
        editText.setSelection(0) // select before list
        editText.applyComment(AztecCommentSpan.Comment.MORE)

        Assert.assertEquals("<br>$HTML_COMMENT_MORE$HTML_LIST_UNORDERED", editText.toHtml())
    }

    /**
     * Insert [AztecCommentSpan.Comment.MORE] comment inside of ordered list.
     * If comment is inserted at point of selection and ordered list remains styled before and
     * after comment, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreInsideOrderedList() {
        Assert.assertTrue(TextUtils.isEmpty(editText.text))

        editText.fromHtml(HTML_LIST_ORDERED)
        editText.setSelection(2) // select after second character in list
        editText.applyComment(AztecCommentSpan.Comment.MORE)

        Assert.assertEquals("$HTML_LIST_ORDERED_SPLIT_1$HTML_COMMENT_MORE$HTML_LIST_ORDERED_SPLIT_2", editText.toHtml())
    }

    /**
     * Insert [AztecCommentSpan.Comment.MORE] comment inside of quote.
     * If comment is inserted at point of selection and quote remains styled before and
     * after comment, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreInsideQuote() {
        Assert.assertTrue(TextUtils.isEmpty(editText.text))

        editText.fromHtml(HTML_QUOTE)
        editText.setSelection(2) // select after second character in quote
        editText.applyComment(AztecCommentSpan.Comment.MORE)

        Assert.assertEquals("$HTML_QUOTE_SPLIT_1$HTML_COMMENT_MORE$HTML_QUOTE_SPLIT_2", editText.toHtml())
    }

    /**
     * Insert [AztecCommentSpan.Comment.MORE] comment inside of unordered list.
     * If comment is inserted at point of selection and unordered list remains styled before and
     * after comment, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreInsideUnorderedList() {
        Assert.assertTrue(TextUtils.isEmpty(editText.text))

        editText.fromHtml(HTML_LIST_UNORDERED)
        editText.setSelection(2) // select after second character in list
        editText.applyComment(AztecCommentSpan.Comment.MORE)

        Assert.assertEquals("$HTML_LIST_UNORDERED_SPLIT_1$HTML_COMMENT_MORE$HTML_LIST_UNORDERED_SPLIT_2", editText.toHtml())
    }

    /**
     * Insert [AztecCommentSpan.Comment.MORE] comment inside selected portion of ordered list.
     * If comment replaces selected text and ordered list remains styled before and after comment,
     * [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreInsideSelectedOrderedList() {
        Assert.assertTrue(TextUtils.isEmpty(editText.text))

        editText.fromHtml(HTML_LIST_ORDERED)
        editText.setSelection(2, 4) // select between second and fourth character in list
        editText.applyComment(AztecCommentSpan.Comment.MORE)

        Assert.assertEquals("$HTML_LIST_ORDERED_SELECTED_1$HTML_COMMENT_MORE$HTML_LIST_ORDERED_SELECTED_2", editText.toHtml())
    }

    /**
     * Insert [AztecCommentSpan.Comment.MORE] comment inside selected portion of quote.
     * If comment replaces selected text and quote remains styled before and after comment,
     * [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreInsideSelectedQuote() {
        Assert.assertTrue(TextUtils.isEmpty(editText.text))

        editText.fromHtml(HTML_QUOTE)
        editText.setSelection(2, 4) // select between second and fourth character in quote
        editText.applyComment(AztecCommentSpan.Comment.MORE)

        Assert.assertEquals("$HTML_QUOTE_SELECTED_1$HTML_COMMENT_MORE$HTML_QUOTE_SELECTED_2", editText.toHtml())
    }

    /**
     * Insert [AztecCommentSpan.Comment.MORE] comment inside selected portion of unordered list.
     * If comment replaces selected text and unordered list remains styled before and after comment,
     * [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertMoreInsideSelectedUnorderedList() {
        Assert.assertTrue(TextUtils.isEmpty(editText.text))

        editText.fromHtml(HTML_LIST_UNORDERED)
        editText.setSelection(2, 4) // select between second and fourth character in list
        editText.applyComment(AztecCommentSpan.Comment.MORE)

        Assert.assertEquals("$HTML_LIST_UNORDERED_SELECTED_1$HTML_COMMENT_MORE$HTML_LIST_UNORDERED_SELECTED_2", editText.toHtml())
    }

    /**
     * Insert [AztecCommentSpan.Comment.PAGE] comment across multiple selected block elements.
     * If comment replaces selected text and block elements remain styled before and after comment,
     * [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageAcrossMultipleBlocks() {
        Assert.assertTrue(TextUtils.isEmpty(editText.text))

        val html = HTML_LIST_ORDERED + HTML_LIST_UNORDERED + HTML_QUOTE
        editText.fromHtml(html)
        editText.setSelection(2, 20) // select between second character of ordered list and second character of quote (includes newline characters)
        editText.applyComment(AztecCommentSpan.Comment.PAGE)

        Assert.assertEquals("$HTML_LIST_ORDERED_SELECTED_1$HTML_COMMENT_PAGE$HTML_QUOTE_SPLIT_2", editText.toHtml())
    }

    /**
     * Insert [AztecCommentSpan.Comment.PAGE] comment following an ordered list.
     * If comment is inserted and ordered list remains styled, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageAfterOrderedList() {
        Assert.assertTrue(TextUtils.isEmpty(editText.text))

        editText.fromHtml(HTML_LIST_ORDERED)
        editText.setSelection(editText.length()) // select after list
        editText.applyComment(AztecCommentSpan.Comment.PAGE)

        Assert.assertEquals("$HTML_LIST_ORDERED$HTML_COMMENT_PAGE<br>", editText.toHtml())
    }

    /**
     * Insert [AztecCommentSpan.Comment.PAGE] comment following a quote.
     * If comment is inserted and quote remains styled, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageAfterQuote() {
        Assert.assertTrue(TextUtils.isEmpty(editText.text))

        editText.fromHtml(HTML_QUOTE)
        editText.setSelection(editText.length()) // select after quote
        editText.applyComment(AztecCommentSpan.Comment.PAGE)

        Assert.assertEquals("$HTML_QUOTE$HTML_COMMENT_PAGE<br>", editText.toHtml())
    }

    /**
     * Insert [AztecCommentSpan.Comment.PAGE] comment following an unordered list.
     * If comment is inserted and unordered list remains styled, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageAfterUnorderedList() {
        Assert.assertTrue(TextUtils.isEmpty(editText.text))

        editText.fromHtml(HTML_LIST_UNORDERED)
        editText.setSelection(editText.length()) // select after list
        editText.applyComment(AztecCommentSpan.Comment.PAGE)

        Assert.assertEquals("$HTML_LIST_UNORDERED$HTML_COMMENT_PAGE<br>", editText.toHtml())
    }

    /**
     * Insert [AztecCommentSpan.Comment.PAGE] comment preceding an ordered list.
     * If comment is inserted and ordered list remains styled, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageBeforeOrderedList() {
        Assert.assertTrue(TextUtils.isEmpty(editText.text))

        editText.fromHtml(HTML_LIST_ORDERED)
        editText.setSelection(0) // select before list
        editText.applyComment(AztecCommentSpan.Comment.PAGE)

        Assert.assertEquals("<br>$HTML_COMMENT_PAGE$HTML_LIST_ORDERED", editText.toHtml())
    }

    /**
     * Insert [AztecCommentSpan.Comment.PAGE] comment preceding a quote.
     * If comment is inserted and quote remains styled, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageBeforeQuote() {
        Assert.assertTrue(TextUtils.isEmpty(editText.text))

        editText.fromHtml(HTML_QUOTE)
        editText.setSelection(0) // select before quote
        editText.applyComment(AztecCommentSpan.Comment.PAGE)

        Assert.assertEquals("<br>$HTML_COMMENT_PAGE$HTML_QUOTE", editText.toHtml())
    }

    /**
     * Insert [AztecCommentSpan.Comment.PAGE] comment preceding an unordered list.
     * If comment is inserted and unordered list remains styled, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageBeforeUnorderedList() {
        Assert.assertTrue(TextUtils.isEmpty(editText.text))

        editText.fromHtml(HTML_LIST_UNORDERED)
        editText.setSelection(0) // select before list
        editText.applyComment(AztecCommentSpan.Comment.PAGE)

        Assert.assertEquals("<br>$HTML_COMMENT_PAGE$HTML_LIST_UNORDERED", editText.toHtml())
    }

    /**
     * Insert [AztecCommentSpan.Comment.PAGE] comment inside of ordered list.
     * If comment is inserted at point of selection and ordered list remains styled before and
     * after comment, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageInsideOrderedList() {
        Assert.assertTrue(TextUtils.isEmpty(editText.text))

        editText.fromHtml(HTML_LIST_ORDERED)
        editText.setSelection(2) // select after second character in list
        editText.applyComment(AztecCommentSpan.Comment.PAGE)

        Assert.assertEquals("$HTML_LIST_ORDERED_SPLIT_1$HTML_COMMENT_PAGE$HTML_LIST_ORDERED_SPLIT_2", editText.toHtml())
    }

    /**
     * Insert [AztecCommentSpan.Comment.PAGE] comment inside of quote.
     * If comment is inserted at point of selection and quote remains styled before and
     * after comment, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageInsideQuote() {
        Assert.assertTrue(TextUtils.isEmpty(editText.text))

        editText.fromHtml(HTML_QUOTE)
        editText.setSelection(2) // select after second character in quote
        editText.applyComment(AztecCommentSpan.Comment.PAGE)

        Assert.assertEquals("$HTML_QUOTE_SPLIT_1$HTML_COMMENT_PAGE$HTML_QUOTE_SPLIT_2", editText.toHtml())
    }

    /**
     * Insert [AztecCommentSpan.Comment.PAGE] comment inside of unordered list.
     * If comment is inserted at point of selection and unordered list remains styled before and
     * after comment, [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageInsideUnorderedList() {
        Assert.assertTrue(TextUtils.isEmpty(editText.text))

        editText.fromHtml(HTML_LIST_UNORDERED)
        editText.setSelection(2) // select after second character in list
        editText.applyComment(AztecCommentSpan.Comment.PAGE)

        Assert.assertEquals("$HTML_LIST_UNORDERED_SPLIT_1$HTML_COMMENT_PAGE$HTML_LIST_UNORDERED_SPLIT_2", editText.toHtml())
    }

    /**
     * Insert [AztecCommentSpan.Comment.PAGE] comment inside selected portion of ordered list.
     * If comment replaces selected text and ordered list remains styled before and after comment,
     * [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageInsideSelectedOrderedList() {
        Assert.assertTrue(TextUtils.isEmpty(editText.text))

        editText.fromHtml(HTML_LIST_ORDERED)
        editText.setSelection(2, 4) // select between second and fourth character in list
        editText.applyComment(AztecCommentSpan.Comment.PAGE)

        Assert.assertEquals("$HTML_LIST_ORDERED_SELECTED_1$HTML_COMMENT_PAGE$HTML_LIST_ORDERED_SELECTED_2", editText.toHtml())
    }

    /**
     * Insert [AztecCommentSpan.Comment.PAGE] comment inside selected portion of quote.
     * If comment replaces selected text and quote remains styled before and after comment,
     * [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageInsideSelectedQuote() {
        Assert.assertTrue(TextUtils.isEmpty(editText.text))

        editText.fromHtml(HTML_QUOTE)
        editText.setSelection(2, 4) // select between second and fourth character in quote
        editText.applyComment(AztecCommentSpan.Comment.PAGE)

        Assert.assertEquals("$HTML_QUOTE_SELECTED_1$HTML_COMMENT_PAGE$HTML_QUOTE_SELECTED_2", editText.toHtml())
    }

    /**
     * Insert [AztecCommentSpan.Comment.PAGE] comment inside selected portion of unordered list.
     * If comment replaces selected text and unordered list remains styled before and after comment,
     * [AztecText] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun insertPageInsideSelectedUnorderedList() {
        Assert.assertTrue(TextUtils.isEmpty(editText.text))

        editText.fromHtml(HTML_LIST_UNORDERED)
        editText.setSelection(2, 4) // select between second and fourth character in list
        editText.applyComment(AztecCommentSpan.Comment.PAGE)

        Assert.assertEquals("$HTML_LIST_UNORDERED_SELECTED_1$HTML_COMMENT_PAGE$HTML_LIST_UNORDERED_SELECTED_2", editText.toHtml())
    }
}
