package org.wordpress.aztec

import android.text.SpannableStringBuilder
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for {@link AztecParser}.
 */
@RunWith(RobolectricGradleTestRunner::class)
@Config(constants = BuildConfig::class)
class AztecParserTest {
    companion object {
        private val HTML_BOLD = "<b>Bold</b><br><br>"
        private val HTML_BULLET = "<ul><li>Bullet</li></ul>"
        private val HTML_COMMENT = "<!--Comment--><br><br>"
        private val HTML_ITALIC = "<i>Italic</i><br><br>"
        private val HTML_LINK = "<a href=\"https://github.com/wordpress-mobile/WordPress-Aztec-Android\">Link</a><br><br>"
        private val HTML_QUOTE = "<blockquote>Quote</blockquote>"
        private val HTML_STRIKETHROUGH = "<s class=\"test\">Strikethrough</s><br><br>" // <s> or <strike> or <del>
        private val HTML_UNDERLINE = "<u>Underline</u><br><br>"
        private val HTML_UNKNOWN = "<iframe class=\"classic\">Menu</iframe><br><br>"
        private val SPAN_BOLD = "Bold\n\n"
        private val SPAN_BULLET = "Bullet\n\n"
        private val SPAN_COMMENT = "Comment\n\n"
        private val SPAN_ITALIC = "Italic\n\n"
        private val SPAN_LINK = "Link\n\n"
        private val SPAN_QUOTE = "Quote\n\n"
        private val SPAN_STRIKETHROUGH = "Strikethrough\n\n"
        private val SPAN_UNDERLINE = "Underline\n\n"
        private val SPAN_UNKNOWN = "\uFFFC\n\n"
    }

    /**
     * Parse all text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, {@link AztecParser} is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlAll_isEqual() {
        val input =
                HTML_BOLD +
                HTML_ITALIC +
                HTML_UNDERLINE +
                HTML_STRIKETHROUGH +
                HTML_BULLET +
                HTML_QUOTE +
                HTML_LINK +
                HTML_UNKNOWN +
                HTML_COMMENT
        val builder = SpannableStringBuilder()
        builder.append(AztecParser.fromHtml(input))
        val output = AztecParser.toHtml(builder)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse bold text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, {@link AztecParser} is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlBold_isEqual() {
        val input =
                HTML_BOLD
        val builder = SpannableStringBuilder()
        builder.append(AztecParser.fromHtml(input))
        val output = AztecParser.toHtml(builder)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse bullet text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, {@link AztecParser} is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlBullet_isEqual() {
        val input =
                HTML_BULLET
        val builder = SpannableStringBuilder()
        builder.append(AztecParser.fromHtml(input))
        val output = AztecParser.toHtml(builder)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse comment text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, {@link AztecParser} is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlComment_isEqual() {
        val input =
                HTML_COMMENT
        val builder = SpannableStringBuilder()
        builder.append(AztecParser.fromHtml(input))
        val output = AztecParser.toHtml(builder)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse italic text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, {@link AztecParser} is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlItalic_isEqual() {
        val input =
                HTML_ITALIC
        val builder = SpannableStringBuilder()
        builder.append(AztecParser.fromHtml(input))
        val output = AztecParser.toHtml(builder)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse link text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, {@link AztecParser} is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlLink_isEqual() {
        val input =
                HTML_LINK
        val builder = SpannableStringBuilder()
        builder.append(AztecParser.fromHtml(input))
        val output = AztecParser.toHtml(builder)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse quote text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, {@link AztecParser} is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlQuote_isEqual() {
        val input =
                HTML_QUOTE
        val builder = SpannableStringBuilder()
        builder.append(AztecParser.fromHtml(input))
        val output = AztecParser.toHtml(builder)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse strikethrough text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, {@link AztecParser} is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlStrikethrough_isEqual() {
        val input =
                HTML_STRIKETHROUGH
        val builder = SpannableStringBuilder()
        builder.append(AztecParser.fromHtml(input))
        val output = AztecParser.toHtml(builder)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse underline text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, {@link AztecParser} is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlUnderline_isEqual() {
        val input =
                HTML_UNDERLINE
        val builder = SpannableStringBuilder()
        builder.append(AztecParser.fromHtml(input))
        val output = AztecParser.toHtml(builder)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse unknown text from HTML to span to HTML.  If input and output are equal with
     * the same length and corresponding characters, {@link AztecParser} is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseHtmlToSpanToHtmlUnknown_isEqual() {
        val input =
                HTML_UNKNOWN
        val builder = SpannableStringBuilder()
        builder.append(AztecParser.fromHtml(input))
        val output = AztecParser.toHtml(builder)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse all text from span to HTML to span.  If input and output are equal with
     * the same length and corresponding characters, {@link AztecParser} is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseSpanToHtmlToSpanAll_isEqual() {
        val input = SpannableStringBuilder()
        input.append(
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
        val html = AztecParser.toHtml(input)
        val output = AztecParser.fromHtml(html)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse bold text from span to HTML to span.  If input and output are equal with
     * the same length and corresponding characters, {@link AztecParser} is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseSpanToHtmlToSpanBold_isEqual() {
        val input = SpannableStringBuilder()
        input.append(
                SPAN_BOLD
        )
        val html = AztecParser.toHtml(input)
        val output = AztecParser.fromHtml(html)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse bullet text from span to HTML to span.  If input and output are equal with
     * the same length and corresponding characters, {@link AztecParser} is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseSpanToHtmlToSpanBullet_isEqual() {
        val input = SpannableStringBuilder()
        input.append(
                SPAN_BULLET
        )
        val html = AztecParser.toHtml(input)
        val output = AztecParser.fromHtml(html)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse comment text from span to HTML to span.  If input and output are equal with
     * the same length and corresponding characters, {@link AztecParser} is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseSpanToHtmlToSpanComment_isEqual() {
        val input = SpannableStringBuilder()
        input.append(
                SPAN_COMMENT
        )
        val html = AztecParser.toHtml(input)
        val output = AztecParser.fromHtml(html)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse italic text from span to HTML to span.  If input and output are equal with
     * the same length and corresponding characters, {@link AztecParser} is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseSpanToHtmlToSpanItalic_isEqual() {
        val input = SpannableStringBuilder()
        input.append(
                SPAN_ITALIC
        )
        val html = AztecParser.toHtml(input)
        val output = AztecParser.fromHtml(html)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse link text from span to HTML to span.  If input and output are equal with
     * the same length and corresponding characters, {@link AztecParser} is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseSpanToHtmlToSpanLink_isEqual() {
        val input = SpannableStringBuilder()
        input.append(
                SPAN_LINK
        )
        val html = AztecParser.toHtml(input)
        val output = AztecParser.fromHtml(html)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse quote text from span to HTML to span.  If input and output are equal with
     * the same length and corresponding characters, {@link AztecParser} is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseSpanToHtmlToSpanQuote_isEqual() {
        val input = SpannableStringBuilder()
        input.append(
                SPAN_QUOTE
        )
        val html = AztecParser.toHtml(input)
        val output = AztecParser.fromHtml(html)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse strikethrough text from span to HTML to span.  If input and output are equal with
     * the same length and corresponding characters, {@link AztecParser} is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseSpanToHtmlToSpanStrikethrough_isEqual() {
        val input = SpannableStringBuilder()
        input.append(
                SPAN_STRIKETHROUGH
        )
        val html = AztecParser.toHtml(input)
        val output = AztecParser.fromHtml(html)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse underline text from span to HTML to span.  If input and output are equal with
     * the same length and corresponding characters, {@link AztecParser} is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseSpanToHtmlToSpanUnderline_isEqual() {
        val input = SpannableStringBuilder()
        input.append(
                SPAN_UNDERLINE
        )
        val html = AztecParser.toHtml(input)
        val output = AztecParser.fromHtml(html)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse unknown text from span to HTML to span.  If input and output are equal with
     * the same length and corresponding characters, {@link AztecParser} is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseSpanToHtmlToSpanUnknown_isEqual() {
        val input = SpannableStringBuilder()
        input.append(
                SPAN_UNKNOWN
        )
        val html = AztecParser.toHtml(input)
        val output = AztecParser.fromHtml(html)
        Assert.assertEquals(input, output)
    }
}
