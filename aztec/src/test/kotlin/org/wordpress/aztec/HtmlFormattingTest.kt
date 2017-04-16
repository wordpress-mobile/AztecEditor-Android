package org.wordpress.aztec

import android.test.AndroidTestCase
import android.test.mock.MockContext
import android.text.SpannableString
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.wordpress.aztec.source.Format

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(23))
class HtmlFormattingTest : AndroidTestCase() {

    private var parser = AztecParser()

    private val HTML_LINE_BREAKS = "HI<br><br><br><br><br><br>BYE"
    private val HTML_LINE_BREAKS_FORMATTED = "<p>HI</p><p>BYE</p>"

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
                    "<div><span></span><div><div><span></span></div></div></div><div></div>" +
                    "</div>" +
                    "<br><br>"

    //empty spans and br are removed
    private val HTML_NESTED_FORMATTED =
                    "<div class=\"first\">" +
                    "<div class=\"second\">" +
                    "<div class=\"third\">" +
                    "Div<br><span><b>b</b></span><br>Hidden" +
                    "</div>" +
                    "<div class=\"fourth\"></div>" +
                    "<div class=\"fifth\"></div>" +
                    "</div>" +
                    "<div><div><div></div></div></div><div></div>" +
                    "</div>"

    private val HTML_MIXED_WITH_NEWLINES =
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

    private val HTML_MIXED_FORMATTED =
            "<p><span><i>Italic</i></span><b>Bold</b></p>" +
                    "<div class=\"first\">" +
                    "<p><a href=\"https://github.com/wordpress-mobile/WordPress-Aztec-Android\">Link</a></p>" +
                    "<div class=\"second\">" +
                    "<div class=\"third\">" +
                    "Div<br><span><b>Span</b></span><br>Hidden" +
                    "</div>" +
                    "<p><iframe class=\"classic\">Menu</iframe></p>" +
                    "<div class=\"fourth\"><u>Under</u>line</div>" +
                    "<div class=\"fifth\"></div>" +
                    "</div>" +
                    "</div>"

    private val HTML_MIXED_WITHOUT_NEWLINES =
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

    private val HTML_BLOCK_WITH_NEWLINES = "\n\n<div>Division</div>\n\n"
    private val HTML_BLOCK_WITHOUT_NEWLINES = "<div>Division</div>"

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
        val span = SpannableString(parser.fromHtml(input, null, null, context))
        val output = Format.removeSourceEditorFormatting(Format.addSourceEditorFormatting(parser.toHtml(span),false),false)
        Assert.assertEquals(HTML_NESTED, output)
    }

    /**
     * Test the conversion from HTML to visual mode with nested HTML (Calypso format)
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun formatNestedHtmlCalypso() {
        val input = HTML_NESTED
        val span = SpannableString(parser.fromHtml(input, null, null, context))
        val output = Format.removeSourceEditorFormatting(Format.addSourceEditorFormatting(parser.toHtml(span),true),true)
        Assert.assertEquals(HTML_NESTED_FORMATTED, output)
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
        val span = SpannableString(parser.fromHtml(input, null, null, context))
        val output = Format.removeSourceEditorFormatting(Format.addSourceEditorFormatting(parser.toHtml(span),false),false)
        Assert.assertEquals(HTML_LINE_BREAKS, output)
    }

    /**
     * Test the conversion from HTML to visual mode with multiple line breaks (Calypso format)
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun formatLineBreaksCalypso() {
        val input = HTML_LINE_BREAKS
        val span = SpannableString(parser.fromHtml(input, null, null, context))
        val output = Format.removeSourceEditorFormatting(Format.addSourceEditorFormatting(parser.toHtml(span),true),true)
        Assert.assertEquals(HTML_LINE_BREAKS_FORMATTED, output)
    }

    /**
     * Test the conversion from HTML to visual mode with mixed HTML
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun formatMixedHtml() {
        val input = HTML_MIXED_WITH_NEWLINES
        val span = SpannableString(parser.fromHtml(input, null, null, context))
        val output = Format.removeSourceEditorFormatting(Format.addSourceEditorFormatting(parser.toHtml(span),false),false)
        Assert.assertEquals(HTML_MIXED_WITHOUT_NEWLINES, output)
    }

    /**
     * Test the conversion from HTML to visual mode with mixed HTML  (Calypso format)
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun formatMixedHtmlCalypso() {
        val input = HTML_MIXED_WITH_NEWLINES
        val span = SpannableString(parser.fromHtml(input, null, null, context))
        val output = Format.removeSourceEditorFormatting(Format.addSourceEditorFormatting(parser.toHtml(span),true),true)
        Assert.assertEquals(HTML_MIXED_FORMATTED, output)
    }

    /**
     * Test block conversion from HTML to visual mode with newlines.
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun formatNewlines() {
        val input = HTML_BLOCK_WITH_NEWLINES
        val span = SpannableString(parser.fromHtml(input, null, null, context))
        val output = Format.removeSourceEditorFormatting(Format.addSourceEditorFormatting(parser.toHtml(span),false),false)
        Assert.assertEquals(HTML_BLOCK_WITHOUT_NEWLINES, output)
    }
}