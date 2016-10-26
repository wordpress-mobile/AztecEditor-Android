package org.wordpress.aztec

import android.test.AndroidTestCase
import android.test.mock.MockContext
import android.text.SpannableString
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Tests for [AztecParser].
 */
@RunWith(RobolectricGradleTestRunner::class)
@Config(constants = BuildConfig::class)
class AztecParserTest : AndroidTestCase() {
    private var mParser = AztecParser()
    private val HTML_BOLD = "<b>Bold</b><br><br>"
    private val HTML_BULLET = "<ul><li>Bullet</li></ul>"
    private val HTML_BULLET_WITH_WHITE_SPACE = "<ul><li>Bullet<br></br></li></ul>"
    private val HTML_BULLET_WITH_QUOTE = "<ul><li><blockquote>Quote</blockquote></li></ul>"
    private val HTML_ORDERED_LIST = "<ol><li>Ordered item</li></ol>"
    private val HTML_ORDERED_LIST_WITH_WHITE_SPACE = "<ol><li>Ordered item<br></br></li></ol>"
    private val HTML_ORDERED_LIST_WITH_QUOTE = "<ol><li><blockquote>Quote</blockquote></li></ol>"
    private val HTML_COMMENT = "<!--Comment--><br><br>"
    private val HTML_HEADER = "<h1>Heading 1</h1><br><br><h2>Heading 2</h2><br><br><h3>Heading 3</h3><br><br><h4>Heading 4</h4><br><br><h5>Heading 5</h5><br><br><h6>Heading 6</h6><br><br>"
    private val HTML_ITALIC = "<i>Italic</i><br><br>"
    private val HTML_LINK = "<a href=\"https://github.com/wordpress-mobile/WordPress-Aztec-Android\">Link</a>"
    private val HTML_MORE = "<!--more-->"
    private val HTML_PAGE = "<!--nextpage-->"
    private val HTML_QUOTE = "<blockquote>Quote</blockquote>"
    private val HTML_QUOTE_WITH_WHITE_SPACE = "<blockquote>Quote<br><br></br></blockquote>"
    private val HTML_QUOTE_WITH_BULLETS = "<blockquote><ul><li>Bullet</li></ul></blockquote>"
    private val HTML_STRIKETHROUGH = "<s>Strikethrough</s>" // <s> or <strike> or <del>
    private val HTML_UNDERLINE = "<u>Underline</u><br><br>"
    private val HTML_UNKNOWN = "<iframe class=\"classic\">Menu</iframe><br><br>"
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

    private val SPAN_BOLD = "Bold\n\n"
    private val SPAN_BULLET = "Bullet\n\n"
    private val SPAN_COMMENT = "Comment\n\n"
    private val SPAN_HEADER = "Heading 1\n\nHeading 2\n\nHeading 3\n\nHeading 4\n\nHeading 5\n\nHeading 6\n\n"
    private val SPAN_ITALIC = "Italic\n\n"
    private val SPAN_LINK = "Link\n\n"
    private val SPAN_MORE = "more\n\n"
    private val SPAN_PAGE = "page\n\n"
    private val SPAN_QUOTE = "Quote\n\n"
    private val SPAN_STRIKETHROUGH = "Strikethrough\n\n"
    private val SPAN_UNDERLINE = "Underline\n\n"
    private val SPAN_UNKNOWN = "\uFFFC\n\n"

    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        context = MockContext()
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
                HTML_HEADER +
                HTML_BOLD +
                HTML_ITALIC +
                HTML_UNDERLINE +
                HTML_STRIKETHROUGH +
                HTML_BULLET +
                HTML_QUOTE +
                HTML_LINK +
                HTML_BULLET_WITH_QUOTE +
                HTML_UNKNOWN +
                HTML_QUOTE_WITH_BULLETS +
                HTML_COMMENT +
                HTML_NESTED_MIXED +
                HTML_NESTED_EMPTY_END +
                HTML_NESTED_EMPTY_START +
                HTML_NESTED_EMPTY +
                HTML_NESTED_WITH_TEXT +
                HTML_NESTED_INTERLEAVING

        val span = SpannableString(mParser.fromHtml(input, context))
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
        val input =
                HTML_BOLD
        val span = SpannableString(mParser.fromHtml(input, context))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse bullet text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlBullet_isEqual() {
        val input =
                HTML_BULLET
        val span = SpannableString(mParser.fromHtml(input, context))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse bullets with quotes from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlBulletsWithQuotes_isEqual() {
        val input =
                HTML_BULLET_WITH_QUOTE
        val span = SpannableString(mParser.fromHtml(input, context))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse bullet with white space text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlBulletWhiteSpace_isEqual() {
        val input =
                HTML_BULLET_WITH_WHITE_SPACE
        val span = SpannableString(mParser.fromHtml(input, context))
        val output = mParser.toHtml(span)
        Assert.assertEquals(HTML_BULLET, output)
    }

    /**
     * Parse ordered list text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlOrderedList_isEqual() {
        val input =
                HTML_ORDERED_LIST
        val span = SpannableString(mParser.fromHtml(input, context))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse ordered lists with quotes from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlOrderedListsWithQuotes_isEqual() {
        val input =
                HTML_ORDERED_LIST_WITH_QUOTE
        val span = SpannableString(mParser.fromHtml(input, context))
        val output = mParser.toHtml(span)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse ordered list with white space text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlOrderedListtWhiteSpace_isEqual() {
        val input =
                HTML_ORDERED_LIST_WITH_WHITE_SPACE
        val span = SpannableString(mParser.fromHtml(input, context))
        val output = mParser.toHtml(span)
        Assert.assertEquals(HTML_ORDERED_LIST, output)
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
        val input =
                HTML_COMMENT
        val span = SpannableString(mParser.fromHtml(input, context))
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
        val input =
                HTML_HEADER
        val span = SpannableString(mParser.fromHtml(input, context))
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
        val input =
                HTML_ITALIC
        val span = SpannableString(mParser.fromHtml(input, context))
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
        val input =
                HTML_LINK
        val span = SpannableString(mParser.fromHtml(input, context))
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
        val input =
                HTML_MORE
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
        val input =
                HTML_PAGE
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
        val input =
                HTML_QUOTE
        val span = SpannableString(mParser.fromHtml(input, context))
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
        val input =
                HTML_QUOTE_WITH_WHITE_SPACE
        val span = SpannableString(mParser.fromHtml(input, context))
        val output = mParser.toHtml(span)
        Assert.assertEquals(HTML_QUOTE, output)
    }

    /**
     * Parse quote with bullets from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlQuoteWithBullets_isEqual() {
        val input =
                HTML_QUOTE_WITH_BULLETS
        val span = SpannableString(mParser.fromHtml(input, context))
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
        val input =
                HTML_STRIKETHROUGH
        val span = SpannableString(mParser.fromHtml(input, context))
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
        val span = SpannableString(mParser.fromHtml(input, context))
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
        val input =
                HTML_UNKNOWN
        val span = SpannableString(mParser.fromHtml(input, context))
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
        val input =
                HTML_NESTED_MIXED
        val span = SpannableString(mParser.fromHtml(input, context))
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
        val input =
                HTML_NESTED_MIXED
        val span = SpannableString(mParser.fromHtml(input, context))
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
        val input =
                HTML_NESTED_EMPTY
        val span = SpannableString(mParser.fromHtml(input, context))
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
        val input =
                HTML_NESTED_EMPTY_END
        val span = SpannableString(mParser.fromHtml(input, context))
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
        val input =
                HTML_NESTED_EMPTY_START
        val span = SpannableString(mParser.fromHtml(input, context))
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
        val input =
                HTML_NESTED_WITH_TEXT
        val span = SpannableString(mParser.fromHtml(input, context))
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
        val input =
                HTML_NESTED_INTERLEAVING
        val span = SpannableString(mParser.fromHtml(input, context))
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
                SPAN_HEADER +
                SPAN_BOLD +
                SPAN_ITALIC +
                SPAN_UNDERLINE +
                SPAN_STRIKETHROUGH +
                SPAN_BULLET +
                SPAN_QUOTE +
                SPAN_LINK +
                SPAN_UNKNOWN +
                SPAN_COMMENT
        )
        val html = mParser.toHtml(input)
        val output = mParser.fromHtml(html, context)
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
        val input = SpannableString(
                SPAN_BOLD
        )
        val html = mParser.toHtml(input)
        val output = mParser.fromHtml(html, context)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse bullet text from span to HTML to span.  If input and output are equal with
     * the same length and corresponding characters, [AztecParser] is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseSpanToHtmlToSpanBullet_isEqual() {
        val input = SpannableString(
                SPAN_BULLET
        )
        val html = mParser.toHtml(input)
        val output = mParser.fromHtml(html, context)
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
        val input = SpannableString(
                SPAN_COMMENT
        )
        val html = mParser.toHtml(input)
        val output = mParser.fromHtml(html, context)
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
        val input = SpannableString(
                SPAN_HEADER
        )
        val html = mParser.toHtml(input)
        val output = mParser.fromHtml(html, context)
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
        val input = SpannableString(
                SPAN_ITALIC
        )
        val html = mParser.toHtml(input)
        val output = mParser.fromHtml(html, context)
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
        val input = SpannableString(
                SPAN_LINK
        )
        val html = mParser.toHtml(input)
        val output = mParser.fromHtml(html, context)
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
        val input = SpannableString(
                SPAN_MORE
        )
        val html = mParser.toHtml(input)
        val output = mParser.fromHtml(html, context)
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
        val input = SpannableString(
                SPAN_PAGE
        )
        val html = mParser.toHtml(input)
        val output = mParser.fromHtml(html, context)
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
        val input = SpannableString(
                SPAN_QUOTE
        )
        val html = mParser.toHtml(input)
        val output = mParser.fromHtml(html, context)
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
        val input = SpannableString(
                SPAN_STRIKETHROUGH
        )
        val html = mParser.toHtml(input)
        val output = mParser.fromHtml(html, context)
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
        val input = SpannableString(
                SPAN_UNDERLINE
        )
        val html = mParser.toHtml(input)
        val output = mParser.fromHtml(html, context)
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
        val input = SpannableString(
                SPAN_UNKNOWN
        )
        val html = mParser.toHtml(input)
        val output = mParser.fromHtml(html, context)
        Assert.assertEquals(input, output)
    }
}
