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
    /**
     * Parse text from and to HTML.  If input and output are equal with the same length and
     * corresponding characters, {@link AztecParser} is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseFromAndToHtml_isEqual() {
        val bold = "<b>Bold</b><br><br>"
        val italic = "<i>Italic</i><br><br>"
        val underline = "<u>Underline</u><br><br>"
        val strikethrough = "<s class=\"test\">Strikethrough</s><br><br>" // <s> or <strike> or <del>
        val bullet = "<ul><li>Bullet</li></ul>"
        val quote = "<blockquote>Quote</blockquote>"
        val link = "<a href=\"https://github.com/wordpress-mobile/WordPress-Aztec-Android\">Link</a><br><br>"
        val input = bold + italic + underline + strikethrough + bullet + quote + link

        val builder = SpannableStringBuilder()
        builder.append(AztecParser.fromHtml(input))
        val output = AztecParser.toHtml(builder)
        Assert.assertEquals(input, output)
    }

    /**
     * Parse text to and from HTML.  If input and output are equal with the same length and
     * corresponding characters, {@link AztecParser} is correct.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun parseToAndFromHtml_isEqual() {
        val input = SpannableStringBuilder()
        input.append("Bold\n\nItalic\n\nUnderline\n\n\uFFFC\n\n\uFFFC\n\nQuote\n\nLink\n\n")
        val html = AztecParser.toHtml(input)
        val output = AztecParser.fromHtml(html)
        Assert.assertEquals(input, output)
    }
}
