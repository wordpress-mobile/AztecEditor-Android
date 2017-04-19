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
class CalypsoFormattingTest : AndroidTestCase() {

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

    private val HEADING =
            "<h1>Heading 1</h1>" +
                    "<h2>Heading 2</h2>" +
                    "<h3>Heading 3</h3>" +
                    "<h4>Heading 4</h4>" +
                    "<h5>Heading 5</h5>" +
                    "<h6>Heading 6</h6>"
    private val BOLD = "<b>Bold</b><br>"
    private val ITALIC = "<i>Italic</i><br>"
    private val UNDERLINE = "<u>Underline</u><br>"
    private val STRIKETHROUGH = "<s class=\"test\">Strikethrough</s><br>" // <s> or <strike> or <del>
    private val ORDERED = "<ol><li>Ordered</li><li></li></ol>"
    private val LINE = "<hr>"
    private val UNORDERED = "<ul><li>Unordered</li><li></li></ul>"
    private val QUOTE = "<blockquote>Quote</blockquote>"
    private val LINK = "<a href=\"https://github.com/wordpress-mobile/WordPress-Aztec-Android\">Link</a><br>"
    private val UNKNOWN = "<iframe class=\"classic\">Menu</iframe><br>"
    private val COMMENT = "<!--Comment--><br>"
    private val COMMENT_MORE = "<!--more--><br>"
    private val COMMENT_PAGE = "<!--nextpage--><br>"
    private val HIDDEN =
            "<span></span>" +
                    "<div class=\"first\">" +
                    "    <div class=\"second\">" +
                    "        <div class=\"third\">" +
                    "            Div<br><span><b>Span</b></span><br>Hidden" +
                    "        </div>" +
                    "        <div class=\"fourth\"></div>" +
                    "        <div class=\"fifth\"></div>" +
                    "    </div>" +
                    "    <span class=\"second last\"></span>" +
                    "</div>" +
                    "<br>"
    private val CODE = "<code>if (value == 5) printf(value)</code><br>"
    private val IMG = "<img src=\"https://cloud.githubusercontent.com/assets/3827611/21950131/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" />"
    private val EMOJI = "aaa&#x1F44D;&#x2764;ccc"

    private val EXAMPLE =
            IMG +
                    HEADING +
                    BOLD +
                    ITALIC +
                    UNDERLINE +
                    STRIKETHROUGH +
                    ORDERED +
                    LINE +
                    UNORDERED +
                    QUOTE +
                    LINK +
                    HIDDEN +
                    COMMENT +
                    COMMENT_MORE +
                    COMMENT_PAGE +
                    CODE +
                    UNKNOWN +
                    EMOJI


//    private val FORMATTED_COMPLEX_HTML =

    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        context = MockContext()
    }


//    /**
//     * Test the conversion from HTML to visual mode with nested HTML (Calypso format)
//     *
//     * @throws Exception
//     */
//    @Test
//    @Throws(Exception::class)
//    fun formatComplexHtml() {
//        val input = EXAMPLE
//        val span = SpannableString(parser.fromHtml(input, null, null, context))
//        val output = Format.removeSourceEditorFormatting(Format.addSourceEditorFormatting(parser.toHtml(span), true), true)
//        Assert.assertEquals(HTML_NESTED_FORMATTED, output)
//    }

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
        val output = Format.removeSourceEditorFormatting(Format.addSourceEditorFormatting(parser.toHtml(span), true), true)
        Assert.assertEquals(HTML_NESTED_FORMATTED, output)
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
        val output = Format.removeSourceEditorFormatting(Format.addSourceEditorFormatting(parser.toHtml(span), true), true)
        Assert.assertEquals(HTML_LINE_BREAKS_FORMATTED, output)
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
        val output = Format.removeSourceEditorFormatting(Format.addSourceEditorFormatting(parser.toHtml(span), true), true)
        Assert.assertEquals(HTML_MIXED_FORMATTED, output)
    }

}