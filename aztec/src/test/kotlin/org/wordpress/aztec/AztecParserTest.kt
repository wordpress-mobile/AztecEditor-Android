@file:Suppress("DEPRECATION")

package org.wordpress.aztec

import android.test.AndroidTestCase
import android.text.SpannableString
import android.text.SpannableStringBuilder
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Tests for [AztecParser].
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = intArrayOf(23))
class AztecParserTest(alignmentRendering: AlignmentRendering) : AndroidTestCase() {
    private var mParser = AztecParser(alignmentRendering)
    private val HTML_BOLD = "<b>Bold</b><br><br>"
    private val HTML_LIST_ORDERED = "<ol><li>Ordered</li></ol>"
    private val HTML_LIST_ORDERED_WITH_EMPTY_ITEM = "<ol><li>Ordered 1</li><li></li><li>Ordered 2</li></ol>"
    private val HTML_LIST_ORDERED_WITH_QUOTE = "<ol><li><blockquote>Ordered Quote</blockquote></li></ol>"
    private val HTML_LIST_ORDERED_WITH_WHITE_SPACE = "<ol><li>Ordered<br></br></li></ol>"
    private val HTML_LIST_UNORDERED = "<ul><li>Unordered</li></ul>"
    private val HTML_LIST_UNORDERED_WITH_EMPTY_ITEM = "<ul><li>Unordered 1</li><li></li><li>Unordered 2</li></ul>"
    private val HTML_LIST_UNORDERED_WITH_QUOTE = "<ul><li><blockquote>Unordered Quote</blockquote></li></ul>"
    private val HTML_LIST_UNORDERED_WITH_WHITE_SPACE = "<ul><li>Unordered<br></br></li></ul>"
    private val HTML_COMMENT = "<!--Comment--><br><br>"
    private val HTML_HEADING_ALL = "<h1>Heading 1</h1><br><br><h2>Heading 2</h2><br><br><h3>Heading 3</h3><br><br><h4>Heading 4</h4><br><br><h5>Heading 5</h5><br><br><h6>Heading 6</h6><br><br>"
    private val HTML_HEADING_ONE = "<h1>Heading 1</h1>"
    private val HTML_ITALIC = "<i>Italic</i><br><br>"
    private val HTML_LINK = "<a href=\"https://github.com/wordpress-mobile/WordPress-Aztec-Android\">Link</a>"
    private val HTML_MORE = "<!--more-->"
    private val HTML_PAGE = "<!--nextpage-->"
    private val HTML_QUOTE = "<blockquote>Quote</blockquote>"
    private val HTML_QUOTE_EMPTY = "<blockquote></blockquote>"
    private val HTML_QUOTE_WITH_LIST_ORDERED = "<blockquote><ol><li>Ordered</li></ol></blockquote>"
    private val HTML_QUOTE_WITH_LIST_UNORDERED = "<blockquote><ul><li>Unordered</li></ul></blockquote>"
    private val HTML_QUOTE_WITH_WHITE_SPACE = "<blockquote>Quote<br><br></br></blockquote>"
    private val HTML_STRIKETHROUGH = "<s>Strikethrough</s>" // <s> or <strike> or <del>
    private val HTML_UNDERLINE = "<u>Underline</u><br><br>"
    private val HTML_UNKNOWN = "<iframe class=\"classic\">Menu</iframe><br><br>"
    private val HTML_COMMENT_INSIDE_UNKNOWN = "<unknown><!--more--></unknown>"
    private val HTML_NESTED_MIXED =
            "<span></span>" +
            "<div class=\"first\">" +
            "<div class=\"second\">" +
            "<div class=\"third\">" +
            "Div<br><span><b>b</b></span><br>Hidden" +
            "</div>" +
            "<div class=\"fourth\"></div>" +
            "<div class=\"fifth\"></div>" +
            "</div>" +
            "<span class=\"second last\"></span>" +
            "<span></span><div><div><div><span></span></div></div></div><div></div>" +
            "</div>" +
            "<br><br>"
    private val HTML_NESTED_EMPTY_END = "1<span></span><div><div><div><span></span>a</div><div></div><div></div></div><span></span></div>"
    private val HTML_NESTED_EMPTY_START = "<span></span><div><div><div><span></span></div><div></div></div><span></span></div>1"
    private val HTML_NESTED_EMPTY = "<span></span><div><div><div><span></span></div></div></div><div></div>"
    private val HTML_NESTED_WITH_TEXT = "<div>1<div>2<div>3<span>4</span>5</div>6</div>7</div>"
    private val HTML_NESTED_INTERLEAVING =
            "<div><div><div><span></span><div></div><span></span></div></div></div><br>" +
            "<div><span>1</span><br><div>2</div>3<span></span><br>4</div><br><br>5<br><br><div></div>"
    private val HTML_NESTED_INLINE = "<u><i><b>Nested</b></i></u>"
    private val HTML_HIDDEN_WITH_NO_TEXT = "<br><br><div></div><br><br>"
    private val HTML_EMOJI = "\uD83D\uDC4D❤" // Thumbsup + heart
    private val HTML_NON_LATIN_TEXT = "测试一个"

    private val SPAN_BOLD = "Bold\n\n"
    private val SPAN_LIST_ORDERED = "Ordered\n\n"
    private val SPAN_LIST_UNORDERED = "Unordered\n\n"
    private val SPAN_COMMENT = "Comment\n\n"
    private val SPAN_HEADING = "Heading 1\n\nHeading 2\n\nHeading 3\n\nHeading 4\n\nHeading 5\n\nHeading 6\n\n"
    private val SPAN_ITALIC = "Italic\n\n"
    private val SPAN_LINK = "Link\n\n"
    private val SPAN_MORE = "more\n\n"
    private val SPAN_PAGE = "page\n\n"
    private val SPAN_QUOTE = "Quote\n\n"
    private val SPAN_STRIKETHROUGH = "Strikethrough\n\n"
    private val SPAN_UNDERLINE = "Underline\n\n"
    private val SPAN_UNKNOWN = "\uFFFC\n\n"

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "Testing parser with AlignmentRendering.{0}")
        fun data(): Collection<Array<AlignmentRendering>> {
            return listOf(
                    arrayOf(AlignmentRendering.SPAN_LEVEL),
                    arrayOf(AlignmentRendering.VIEW_LEVEL)
            )
        }
    }

    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        context = ApplicationProvider.getApplicationContext()
    }

    /**
     * Parse all text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlAll_isEqual() {
        val input =
                HTML_HEADING_ALL +
                HTML_BOLD +
                HTML_ITALIC +
                HTML_UNDERLINE +
                HTML_STRIKETHROUGH +
                HTML_LIST_ORDERED +
                HTML_LIST_ORDERED_WITH_EMPTY_ITEM +
                HTML_LIST_ORDERED_WITH_QUOTE +
                HTML_LIST_UNORDERED +
                HTML_LIST_UNORDERED_WITH_EMPTY_ITEM +
                HTML_LIST_UNORDERED_WITH_QUOTE +
                HTML_QUOTE +
                HTML_LINK +
                HTML_UNKNOWN +
                HTML_QUOTE_WITH_LIST_ORDERED +
                HTML_QUOTE_WITH_LIST_UNORDERED +
                HTML_QUOTE_EMPTY +
                HTML_COMMENT +
                HTML_NESTED_MIXED +
                HTML_NESTED_EMPTY_END +
                HTML_NESTED_EMPTY_START +
                HTML_NESTED_EMPTY +
                HTML_NESTED_WITH_TEXT +
                HTML_NESTED_INTERLEAVING +
                HTML_EMOJI +
                HTML_NON_LATIN_TEXT

        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse bold text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlBold_isEqual() {
        val input = HTML_BOLD
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse unordered list text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlListUnordered_isEqual() {
        val input = HTML_LIST_UNORDERED
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse unordered list with quote from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlListUnorderedWithQuote_isEqual() {
        val input = HTML_LIST_UNORDERED_WITH_QUOTE
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse unordered list with quote surrounded by text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlListUnorderedWithQuoteSurroundedByText_isEqual() {
        val input = "One" + HTML_LIST_UNORDERED_WITH_QUOTE + "Two"
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse unordered list with white space text from HTML to span to HTML.  If input without white space and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlListUnorderedWhiteSpace_isEqual() {
        val input = HTML_LIST_UNORDERED_WITH_WHITE_SPACE
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(HTML_LIST_UNORDERED, output)
    }

    /**
     * Parse ordered list text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlListOrdered_isEqual() {
        val input = HTML_LIST_ORDERED
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse ordered list with quote from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlListOrderedWithQuote_isEqual() {
        val input = HTML_LIST_ORDERED_WITH_QUOTE
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse ordered list with quote surrounded by text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlListOrderedWithQuoteSurroundedByText_isEqual() {
        val input = "One" + HTML_LIST_ORDERED_WITH_QUOTE + "Two"
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse ordered list with white space text from HTML to span to HTML.  If input without white space and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlListOrderedWhiteSpace_isEqual() {
        val input = HTML_LIST_ORDERED_WITH_WHITE_SPACE
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(HTML_LIST_ORDERED, output)
    }

    /**
     * Parse ordered list surrounded text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlListOrderedSurroundedByText_isEqual() {
        val input = "1" + HTML_LIST_ORDERED + "2"
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse ordered list surrounded text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlListOrderedSurroundedByNewlineAndText_isEqual() {
        val input = "1<br>$HTML_LIST_ORDERED<br>2"
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse ordered lists with text between from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlListsWithTextBetween_isEqual() {
        val input = HTML_LIST_ORDERED + "1" + HTML_LIST_ORDERED
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse block elements with preceding newline from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlLinebreakFollowedByBlock_isEqual() {
        var input: String
        var output: String
        var span: SpannableString

        input = "<br>$HTML_HEADING_ONE"
        span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        Assert.assertEquals("\nHeading 1", span.toString())
        output = mParser.toHtml(span)
        Assert.assertEquals(input, output)

        input = "Text<br>$HTML_HEADING_ONE"
        span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        output = mParser.toHtml(span)
        Assert.assertEquals(input, output)

        input = "<br>$HTML_QUOTE"
        span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        Assert.assertEquals("\nQuote", span.toString())
        output = mParser.toHtml(span)
        Assert.assertEquals(input, output)

        input = "Text<br>$HTML_QUOTE"
        span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        output = mParser.toHtml(span)
        Assert.assertEquals(input, output)

        input = "<br>$HTML_LIST_ORDERED"
        span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        Assert.assertEquals("\nOrdered", span.toString())
        output = mParser.toHtml(span)
        Assert.assertEquals(input, output)

        input = "Text<br>$HTML_LIST_ORDERED"
        span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        output = mParser.toHtml(span)
        Assert.assertEquals(input, output)

        input = "<br>$HTML_LIST_UNORDERED"
        span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        Assert.assertEquals("\nUnordered", span.toString())
        output = mParser.toHtml(span)
        Assert.assertEquals(input, output)

        input = "Text<br>$HTML_LIST_UNORDERED"
        span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse comment text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlComment_isEqual() {
        val input = HTML_COMMENT
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse heading text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlHeading_isEqual() {
        val input = HTML_HEADING_ALL
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse italic text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlItalic_isEqual() {
        val input = HTML_ITALIC
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse link text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlLink_isEqual() {
        val input = HTML_LINK
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse more comment text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlMore_isEqual() {
        val input = HTML_MORE
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse page comment text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlPage_isEqual() {
        val input = HTML_PAGE
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse quote text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlQuote_isEqual() {
        val input = HTML_QUOTE
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse empty quote text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlQuoteEmpty_isEqual() {
        val input = HTML_QUOTE_EMPTY
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse quote text with white space from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlQuoteWithWhiteSpace_isEqual() {
        val input = HTML_QUOTE_WITH_WHITE_SPACE
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(HTML_QUOTE, output)
    }

    /**
     * Parse quote with ordered list from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlQuoteWithListOrdered_isEqual() {
        val input = HTML_QUOTE_WITH_LIST_ORDERED
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse quote with unordered list from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlQuoteWithListUnordered_isEqual() {
        val input = HTML_QUOTE_WITH_LIST_UNORDERED
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse strikethrough text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlStrikethrough_isEqual() {
        val input = HTML_STRIKETHROUGH
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse underline text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlUnderline_isEqual() {
        val input = HTML_UNDERLINE
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse unknown text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlUnknown_isEqual() {
        val input = HTML_UNKNOWN
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse nested blocks text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlNestedMixed_isEqual() {
        val input = HTML_NESTED_MIXED
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse nested blocks text from HTML to span to HTML twice with the same spannable string
     * instance.  If input and output are equal with the same length and corresponding characters,
     * [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlNestedMixedTwice_isEqual() {
        val input = HTML_NESTED_MIXED
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        Assert.assertEquals(input, mParser.toHtml(span))
        Assert.assertEquals(input, mParser.toHtml(span))
    }

    /**
     * Parse empty nested blocks text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlNestedEmpty_isEqual() {
        val input = HTML_NESTED_EMPTY
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse empty nested blocks at the end from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlNestedEmptyEnd_isEqual() {
        val input = HTML_NESTED_EMPTY_END
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse empty nested blocks at the beginning from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlNestedEmptyStart_isEqual() {
        val input = HTML_NESTED_EMPTY_START
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse non-empty nested blocks text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlNestedNonEmpty_isEqual() {
        val input = HTML_NESTED_WITH_TEXT
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse interleaving nested blocks text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlNestedInterleaving_isEqual() {
        val input = HTML_NESTED_INTERLEAVING
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse hidden HTML with no text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHiddenHtmlWithNoTextToSpanToHtmlNestedInterleaving_isEqual() {
        val input = HTML_HIDDEN_WITH_NO_TEXT
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun preserveListListUnorderedWithEmptyListItem() {
        val input = HTML_LIST_UNORDERED_WITH_EMPTY_ITEM
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun preserveListOrderedWithEmptyListItem() {
        val input = HTML_LIST_ORDERED_WITH_EMPTY_ITEM
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse all text from span to HTML to span.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseSpanToHtmlToSpanAll_isEqual() {
        val input = SpannableString(
                SPAN_HEADING +
                SPAN_BOLD +
                SPAN_ITALIC +
                SPAN_UNDERLINE +
                SPAN_STRIKETHROUGH +
                SPAN_LIST_UNORDERED +
                SPAN_QUOTE +
                SPAN_LINK +
                SPAN_UNKNOWN +
                SPAN_COMMENT
        )
        val html = mParser.toHtml(input)
        val output = mParser.fromHtml(html, RuntimeEnvironment.application.applicationContext)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse bold text from span to HTML to span.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseSpanToHtmlToSpanBold_isEqual() {
        val input = SpannableString(SPAN_BOLD)
        val html = mParser.toHtml(input)
        val output = mParser.fromHtml(html, RuntimeEnvironment.application.applicationContext)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse ordered list text from span to HTML to span.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseSpanToHtmlToSpanListOrdered_isEqual() {
        val input = SpannableString(SPAN_LIST_ORDERED)
        val html = mParser.toHtml(input)
        val output = mParser.fromHtml(html, RuntimeEnvironment.application.applicationContext)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse unordered list text from span to HTML to span.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseSpanToHtmlToSpanListUnordered_isEqual() {
        val input = SpannableString(SPAN_LIST_UNORDERED)
        val html = mParser.toHtml(input)
        val output = mParser.fromHtml(html, RuntimeEnvironment.application.applicationContext)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse comment text from span to HTML to span.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseSpanToHtmlToSpanComment_isEqual() {
        val input = SpannableString(SPAN_COMMENT)
        val html = mParser.toHtml(input)
        val output = mParser.fromHtml(html, RuntimeEnvironment.application.applicationContext)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse heading text from span to HTML to span.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseSpanToHtmlToSpanHeading_isEqual() {
        val input = SpannableString(SPAN_HEADING)
        val html = mParser.toHtml(input)
        val output = mParser.fromHtml(html, RuntimeEnvironment.application.applicationContext)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse italic text from span to HTML to span.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseSpanToHtmlToSpanItalic_isEqual() {
        val input = SpannableString(SPAN_ITALIC)
        val html = mParser.toHtml(input)
        val output = mParser.fromHtml(html, RuntimeEnvironment.application.applicationContext)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse link text from span to HTML to span.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseSpanToHtmlToSpanLink_isEqual() {
        val input = SpannableString(SPAN_LINK)
        val html = mParser.toHtml(input)
        val output = mParser.fromHtml(html, RuntimeEnvironment.application.applicationContext)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse more comment text from span to HTML to span.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseSpanToHtmlToSpanMore_isEqual() {
        val input = SpannableString(SPAN_MORE)
        val html = mParser.toHtml(input)
        val output = mParser.fromHtml(html, RuntimeEnvironment.application.applicationContext)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse page comment text from span to HTML to span.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseSpanToHtmlToSpanPage_isEqual() {
        val input = SpannableString(SPAN_PAGE)
        val html = mParser.toHtml(input)
        val output = mParser.fromHtml(html, RuntimeEnvironment.application.applicationContext)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse quote text from span to HTML to span.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseSpanToHtmlToSpanQuote_isEqual() {
        val input = SpannableString(SPAN_QUOTE)
        val html = mParser.toHtml(input)
        val output = mParser.fromHtml(html, RuntimeEnvironment.application.applicationContext)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse strikethrough text from span to HTML to span.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseSpanToHtmlToSpanStrikethrough_isEqual() {
        val input = SpannableString(SPAN_STRIKETHROUGH)
        val html = mParser.toHtml(input)
        val output = mParser.fromHtml(html, RuntimeEnvironment.application.applicationContext)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse underline text from span to HTML to span.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseSpanToHtmlToSpanUnderline_isEqual() {
        val input = SpannableString(SPAN_UNDERLINE)
        val html = mParser.toHtml(input)
        val output = mParser.fromHtml(html, RuntimeEnvironment.application.applicationContext)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse unknown text from span to HTML to span.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseSpanToHtmlToSpanUnknown_isEqual() {
        val input = SpannableString(SPAN_UNKNOWN)
        val html = mParser.toHtml(input)
        val output = mParser.fromHtml(html, RuntimeEnvironment.application.applicationContext)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse comment tag nested inside unknown HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlCommentInsideUnknown_isEqual() {
        val input = HTML_COMMENT_INSIDE_UNKNOWN
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse single heading HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlSingleHeading_isEqual() {
        val input = HTML_HEADING_ONE
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse HTML of heading surrounded by text to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlSingleHeadingSurroundedByText_isEqual() {
        val input = "1" + HTML_HEADING_ONE + "1"
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse HTML of heading surrounded by list to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlHeadingSurroundedByList_isEqual() {
        val input = HTML_LIST_ORDERED + HTML_HEADING_ONE + HTML_LIST_ORDERED
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse HTML of heading surrounded by quote to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlHeadingSurroundedByQuote_isEqual() {
        val input = HTML_QUOTE + HTML_HEADING_ONE + HTML_QUOTE
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse HTML of heading surrounded by quote to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlLineBreakBetweenHeadings_isEqual() {
        val input = HTML_HEADING_ONE + "<br>" + HTML_HEADING_ONE
        val span = SpannableStringBuilder(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse HTML of nested inline text style to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlNestedInlineStyles_isEqual() {
        val input = HTML_NESTED_INLINE
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlListOrderedWithTrailingEmptyItem_isEqual() {
        val input = "<ol><li>Ordered item</li><li></li></ol>"
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlListUnorderedWithLinebreak_isEqual() {
        val input = "<ul><li>a</li></ul><br>1"
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlListOrderedWithTrailingEmptyItemAndLinebreak_isEqual() {
        val input = "<ol><li>Ordered item</li><li></li></ol><br>1"
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlListUnorderedFollowedByLinebreak_isEqual() {
        val input = "<ul><li>Ordered item</li><li>b</li></ul><br>1"
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlListUnorderedFollowedByUnknwonHtml_isEqual() {
        val input = HTML_LIST_UNORDERED + HTML_UNKNOWN
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse HTML of header with single character surrounded by other headers to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlSingleCharHeaderSurroundedByHeaders_isEqual() {
        val input = "<h1>Heading 1</h1><h2>2</h2><h3>Heading 3</h3>"
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlOrderedListWithTrailingEmptyItemAnd2Linebreaks_isEqual() {
        val input = "<ol><li>Ordered item</li><li></li></ol><br><br>1"
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlUnorderedListFollowedBy2Linebreaks_isEqual() {
        val input = "<ul><li>Ordered item</li><li>b</li></ul><br><br>1"
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlListWithEmptyItemFollowedByText_isEqual() {
        val input = "<ol><li>Ordered item</li><li></li></ol>1"
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlListWithNonEmptyItemsFollowedByText_isEqual() {
        val input = "<ol><li>Ordered item</li><li>a</li></ol>1"
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlBrAfterHeadings_isEqual() {
        val input = "<h1>h1</h1><br><h2>h2</h2><br><h3>h3</h3><br>"
        val span = SpannableStringBuilder(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlBrAfterHeadings2_isEqual() {
        val input = "<ol><li><ul><li>supernesting</li></ul></li></ol><br>"
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Currently, this html <p>Hello There!<br></p> after being parsed to span and back to html will become
     * <p>Hello There!</p>.
     * This is not a bug, this is how we originally implemented the function that cleans the HTML input
     * in AztecParser->tidy method.
     *
     * Since we're using this editor in Gutenberg Mobile project, where the selection could be sent from
     * the JS side to the native, we needed to take in consideration this behavior of Aztec in GB-mobile,
     * and modify the logic that does set the selection accordingly.
     *
     * This test just checks that the underlying parser is working as expected.
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlBrBeforePara_isNotEqual() {
        val input = "<p>Hello There!<br></p>"
        val expectedOutput = "<p>Hello There!</p>"
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(expectedOutput, output)
    }

    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlBrBeforePara2_isNotEqual() {
        val input = "<p>Hello There!<br><br><br><br></p>"
        val expectedOutput = "<p>Hello There!</p>"
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(expectedOutput, output)
    }

    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlMixedContentInListItem_isEqual() {
        val input = "<ul><li>some text<blockquote>Quote</blockquote>some text</li></ul>"
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlProperVisualNewlineSync_isEqual() {
        val input = "<blockquote>Hello</blockquote><u>Bye</u><blockquote>Hello</blockquote>End"
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    @Ignore("Until this issue is fixed: https://github.com/wordpress-mobile/AztecEditor-Android/issues/434")
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlListInDiv_isEqual() {
        val input = "<div>" + HTML_LIST_UNORDERED + "</div>"
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    @Ignore("Until this issue is fixed: https://github.com/wordpress-mobile/AztecEditor-Android/issues/434")
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlBrBeforeAndAfterDiv_isEqual() {
        val input = "<br><div><br></div>"
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Currently, this html <b>bold <i>italic</i> bold</b> after being parsed to span and back to html will become
     * <b>bold </b><b><i>italic</i></b><b> bold</b>.
     * This is not a bug, this is how Google originally implemented the parsing inside Html.java.
     * https://github.com/wordpress-mobile/AztecEditor-Android/issues/136
     *
     * This test just checks that the underlying parser is working as expected.
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlMixedBoldAndItalic_isNotEqual() {
        val input = "<b>bold <i>italic</i> bold</b>"
        val inputAfterParser = "<b>bold </b><b><i>italic</i></b><b> bold</b>"
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(output, inputAfterParser)
    }

    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlParagraphInsideHiddenSpan_isEqual() {
        val input = "<p>a</p><div><p>b</p></div><p>c</p>"
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlParagraphWithMultipleWhitespace_isNotEqual() {
        val input = "<p>   Hello There!</p>"
        val inputAfterParser = "<p>Hello There!</p>"
        val span = SpannableString(mParser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = mParser.toHtml(span)
        Assert.assertEquals(output, inputAfterParser)
    }
}
