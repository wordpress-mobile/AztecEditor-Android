@file:Suppress("DEPRECATION")

package org.wordpress.aztec

import android.test.AndroidTestCase
import android.text.SpannableString
import android.text.SpannableStringBuilder
import androidx.test.core.app.ApplicationProvider
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.wordpress.aztec.source.Format

@RunWith(RobolectricTestRunner::class)
@Config(sdk = intArrayOf(23))
class CalypsoFormattingTest : AndroidTestCase() {
    private var parser = AztecParser(AlignmentRendering.SPAN_LEVEL)

    private val HTML_LINE_BREAKS = "HI<br><br><br><br><br><br>BYE"
    private val HTML_LINE_BREAKS_FORMATTED = "HI\n\n\n\n\n\nBYE"

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

    private val HTML_NESTED_CALYPSO =
            "<div class=\"first\">\n" +
                    "<div class=\"second\">\n" +
                    "<div class=\"third\">Div\n" +
                    "<span><b>b</b></span>\n" +
                    "Hidden</div>\n" +
                    "<div class=\"fourth\"></div>\n" +
                    "<div class=\"fifth\"></div>\n" +
                    "</div>\n<div>\n<div>\n<div></div>\n" +
                    "</div>\n</div>\n<div></div>\n</div>"

    private val HTML_MIXED_WITH_NEWLINES =
            "\n\n<span><i>Italic</i></span>\n\n<b>Bold</b><br>" +
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

    private val HTML_MIXED_WITH_NEWLINES_CALYPSO =
            "<span><i>Italic</i></span> <b>Bold</b>\n" +
                    "<div class=\"first\">" +
                    "<a href=\"https://github.com/wordpress-mobile/WordPress-Aztec-Android\">Link</a>\n" +
                    "<div class=\"second\">\n" +
                    "<div class=\"third\">Div\n" +
                    "<span><b>Span</b></span>\n" +
                    "Hidden</div>\n" +
                    "<iframe class=\"classic\">Menu</iframe>\n" +
                    "<div class=\"fourth\"><u>Under</u>line</div>\n" +
                    "<div class=\"fifth\"></div>\n</div>\n</div>"

    private val HTML_PARAGRAPHS_WITH_ATTRIBUTES =
            "a\n<p a=\"A\">b</p>\nc"

    private val HTML_PARAGRAPHS_MIXED =
            "a\n<p a=\"A\">b</p>\n<p a=\"A\">b</p>\nc\n\nd\n<p>e</p>"

    private val HTML_PARAGRAPHS_MIXED_CALPYSO =
            "a\n<p a=\"A\">b</p>\n<p a=\"A\">b</p>c\n\nd\n\ne"

    private val HTML_MIXED_REGEX =
            "\n\n<span><i>Italic</i></span>\n\n<b>Bold</b><br>" +
                    "\t<div class=\"\$\$\$first\">" +
                    "<a href=\"https://github.com/wordpress-mobile/Word\\Press-Aztec-Android\">Link</a>" +
                    "    \t<div class=\"sec $8 ond\"></div></div>"

    private val HTML_MIXED_REGEX_CALYPSO =
            "<span><i>Italic</i></span>\n\n<b>Bold</b>\n" +
                    "<div class=\"\$\$\$first\">" +
                    "<a href=\"https://github.com/wordpress-mobile/Word\\Press-Aztec-Android\">Link</a>\n" +
                    "<div class=\"sec $8 ond\"></div>\n" +
                    "</div>"

    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        context = ApplicationProvider.getApplicationContext()
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
        val span = SpannableString(parser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = Format.addSourceEditorFormatting(parser.toHtml(span), true)
        Assert.assertEquals(HTML_NESTED_CALYPSO, output)
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
        val span = SpannableString(parser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = Format.addSourceEditorFormatting(parser.toHtml(span), true)
        Assert.assertEquals(HTML_LINE_BREAKS_FORMATTED, output)
    }

    /**
     * Test the conversion from HTML to visual mode with mixed HTML (Calypso format)
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun formatMixedHtmlCalypso() {
        val input = HTML_MIXED_WITH_NEWLINES
        val span = SpannableString(parser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = Format.addSourceEditorFormatting(parser.toHtml(span), true)
        Assert.assertEquals(HTML_MIXED_WITH_NEWLINES_CALYPSO, output)
    }

    /**
     * Test the conversion of HTML containing special regex characters
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun formatRegexSpecialCharactersCalypso() {
        val input = Format.removeSourceEditorFormatting(HTML_MIXED_REGEX, true)
        val span = SpannableString(parser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = Format.addSourceEditorFormatting(parser.toHtml(span), true)
        Assert.assertEquals(HTML_MIXED_REGEX_CALYPSO, output)
    }

    /**
     * Test the preservation of paragraphs with attributes (Calypso format)
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun paragraphsWithAttributes() {
        val input = HTML_PARAGRAPHS_WITH_ATTRIBUTES
        val span = SpannableString(parser.fromHtml(input, RuntimeEnvironment.application.applicationContext))
        val output = Format.addSourceEditorFormatting(parser.toHtml(span), true)
        Assert.assertEquals(HTML_PARAGRAPHS_WITH_ATTRIBUTES, output)
    }

    /**
     * Test the preservation of paragraphs with attributes and removal of those without (Calypso format)
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun mixedParagraphsWithAttributes() {
        val input = HTML_PARAGRAPHS_MIXED
        val span = SpannableStringBuilder(parser.fromHtml(Format.removeSourceEditorFormatting(input, true),
                RuntimeEnvironment.application.applicationContext))
        val output = Format.addSourceEditorFormatting(parser.toHtml(span), true)
        Assert.assertEquals(HTML_PARAGRAPHS_MIXED_CALPYSO, output)
    }
}
