package org.wordpress.aztec;

import android.test.AndroidTestCase
import android.test.mock.MockContext
import android.text.SpannableString
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.annotation.Config;
import org.wordpress.aztec.source.Format

@RunWith(RobolectricGradleTestRunner::class)
@Config(constants = BuildConfig::class)
class HtmlFormattingTest() : AndroidTestCase() {

    private var parser = AztecParser()


    private val HTML_LINE_BREAKS = "HI<br><br><br><br><br><br>BYE"

    private val HTML_NESTED =
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

    private val HTML_MIXED =
            "\n\n<span><i>Italic</i></span>\n\n" +
            "<b>Bold</b><br>" +
            "\t<div class=\"first\">" +
            "<a href=\"https://github.com/wordpress-mobile/WordPress-Aztec-Android\">Link</a>" +
            "    \t<div class=\"second\">" +
            "        <div class=\"third\">" +
            "            Div<br><span><b>Span</b></span><br>Hidden" +
            "        </div>" +
            "<iframe class=\"classic\">Menu</iframe><br><br>" +
            "        <div class=\"fourth\"><u>Under</u>line</div>\n\n" +
            "        <div class=\"fifth\"></div>" +
            "   \t\t</div>" +
            "    <span class=\"second last\"></span>" +
            "</div>" +
            "<br>"

    private val HTML_MIXED_NO_WS =
            "<span><i>Italic</i></span>" +
                    "<b>Bold</b><br>" +
                    "<div class=\"first\">" +
                    "<a href=\"https://github.com/wordpress-mobile/WordPress-Aztec-Android\">Link</a>" +
                    "<div class=\"second\">" +
                    "<div class=\"third\">" +
                    "Div<br><span><b>Span</b></span><br>Hidden" +
                    "</div>" +
                    "<iframe class=\"classic\">Menu</iframe><br><br>" +
                    "<div class=\"fourth\"><u>Under</u>line</div>" +
                    "<div class=\"fifth\"></div>" +
                    "</div>" +
                    "<span class=\"second last\"></span>" +
                    "</div>" +
                    "<br>"

    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        context = MockContext()
    }

    /**
     * Test the conversion from HTML to visual mode with nested HTML
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun formatNestedHtml() {
        val input = HTML_NESTED
        val span = SpannableString(parser.fromHtml(input, context))
        val output = Format.clearFormatting(Format.addFormatting(parser.toHtml(span)))
        Assert.assertEquals(input, output)
    }

    /**
     * Test the conversion from HTML to visual mode with multiple line breaks
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun formatLineBreaks() {
        val input = HTML_LINE_BREAKS
        val span = SpannableString(parser.fromHtml(input, context))
        val output = Format.clearFormatting(Format.addFormatting(parser.toHtml(span)))
        Assert.assertEquals(input, output)
    }

    /**
     * Test the conversion from HTML to visual mode with mixed HTML
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun formatMixedHtml() {
        val input = HTML_MIXED
        val span = SpannableString(parser.fromHtml(input, context))
        val output = Format.clearFormatting(Format.addFormatting(parser.toHtml(span)))
        Assert.assertEquals(HTML_MIXED_NO_WS, output)
    }
}