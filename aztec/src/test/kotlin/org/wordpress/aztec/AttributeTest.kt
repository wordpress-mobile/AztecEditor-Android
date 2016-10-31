package org.wordpress.aztec

import android.app.Activity
import android.text.SpannableString
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.annotation.Config
import org.wordpress.aztec.source.Format

/**
 * Testing attribute preservation for supported HTML elements
 */
@RunWith(RobolectricGradleTestRunner::class)
@Config(constants = BuildConfig::class)
class AttributeTest {

    companion object {
        private val HEADING =
                "<h1 a=\"A\">Heading 1</h1><br>" +
                "<h2 b=\"B\">Heading 2</h2><br>" +
                "<h3 c=\"C\">Heading 3</h3><br>" +
                "<h4 d=\"D\">Heading 4</h4><br>" +
                "<h5 e=\"E\">Heading 5</h5><br>" +
                "<h6 f=\"F\">Heading 6</h6>"
        private val BOLD = "<b h=\"H\">Bold</b>"
        private val BOLD_NO_ATTRS = "<b>Bold</b>"
        private val ITALIC = "<i i=\"I\">Italic</i>"
        private val UNDERLINE = "<u j=\"J\">Underline</u>"
        private val NESTED = "<i a=\"A\"><b><u class=\"klass\">Nested</u></b><i>"
        private val NESTED_REVERSED = "<u class=\"klass\"><b><i a=\"A\">Nested</i></b></u>"
        private val STRIKETHROUGH = "<s class=\"test\">Strikethrough</s>" // <s> or <strike> or <del>
        private val ORDERED = "<ol l=\"L\"><li>Ordered</li></ol>"
        private val UNORDERED = "<ul m=\"M\"><li>Unordered</li></ul>"
        private val QUOTE = "<blockquote n=\"N\">Quote</blockquote>"
        private val LINK = "<a o=\"O\" href=\"https://github.com/wordpress-mobile/WordPress-Aztec-Android\">Link</a>"
        private val UNKNOWN = "<iframe class=\"classic\" p=\"P\">Menu</iframe>"
        private val COMMENT = "<!--Comment--><br>"
        private val COMMENT_MORE = "<!--more--><br>"
        private val COMMENT_PAGE = "<!--nextpage--><br>"
        private val LIST = "<ol><li a=\"1\">Ordered</li></ol>"
        private val SUB = "<sub i=\"I\">Sub</sub>"
        private val SUP = "<sup i=\"I\">Sup</sup>"
        private val FONT = "<font i=\"I\">Font</font>"
        private val TT = "<tt t=\"T\">Monospace</tt>"
        private val BIG = "<big b=\"B\">Big</big>"
        private val SMALL = "<small s=\"S\">Small</small>"
        private val P = "<p p=\"P\">Paragraph</p>" + BOLD_NO_ATTRS
        private val MIXED = HEADING + BOLD + ITALIC + UNDERLINE + STRIKETHROUGH + ORDERED +
                UNORDERED + QUOTE + LINK + COMMENT + COMMENT_MORE + COMMENT_PAGE +
                UNKNOWN + LIST + SUB + SUP + FONT + TT + BIG + SMALL + P
    }

    lateinit var editText: AztecText

    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().visible().get()
        editText = AztecText(activity)
        activity.setContentView(editText)
    }

    @Test
    @Throws(Exception::class)
    fun headingAttributes() {
        val input = HEADING
        editText.fromHtml(input)
        val output = editText.toHtml()
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun italicAttributes() {
        val input = ITALIC
        editText.fromHtml(input)
        val output = editText.toHtml()
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun boldAttributes() {
        val input = BOLD
        editText.fromHtml(input)
        val output = editText.toHtml()
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun boldWithoutAttributes() {
        val input = BOLD_NO_ATTRS
        editText.fromHtml(input)
        val output = editText.toHtml()
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun nestedAttributes() {
        val input = NESTED
        val expected = NESTED_REVERSED
        editText.fromHtml(input)
        val output = editText.toHtml()
        Assert.assertEquals(expected, output)
    }

    @Test
    @Throws(Exception::class)
    fun underlineAttributes() {
        val input = UNDERLINE
        editText.fromHtml(input)
        val output = editText.toHtml()
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun strikethroughAttributes() {
        val input = STRIKETHROUGH
        editText.fromHtml(input)
        val output = editText.toHtml()
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun orderedAttributes() {
        val input = ORDERED
        editText.fromHtml(input)
        val output = editText.toHtml()
        Assert.assertEquals(input, output)
    }
    @Test
    @Throws(Exception::class)
    fun unorderedAttributes() {
        val input = UNORDERED
        editText.fromHtml(input)
        val output = editText.toHtml()
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun quoteAttributes() {
        val input = QUOTE
        editText.fromHtml(input)
        val output = editText.toHtml()
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun linkAttributes() {
        val input = LINK
        editText.fromHtml(input)
        val output = editText.toHtml()
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun unknownAttributes() {
        val input = UNKNOWN
        editText.fromHtml(input)
        val output = editText.toHtml()
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun listAttributes() {
        val input = LIST
        editText.fromHtml(input)
        val output = editText.toHtml()
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun subAttributes() {
        val input = SUB
        editText.fromHtml(input)
        val output = editText.toHtml()
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun supAttributes() {
        val input = SUP
        editText.fromHtml(input)
        val output = editText.toHtml()
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun fontAttributes() {
        val input = FONT
        editText.fromHtml(input)
        val output = editText.toHtml()
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun typefaceAttributes() {
        val input = TT
        editText.fromHtml(input)
        val output = editText.toHtml()
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun bigAttributes() {
        val input = BIG
        editText.fromHtml(input)
        val output = editText.toHtml()
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun smallAttributes() {
        val input = SMALL
        editText.fromHtml(input)
        val output = editText.toHtml()
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun paragraphAttributes() {
        val input = P
        editText.fromHtml(input)
        val output = editText.toHtml()
        Assert.assertEquals(input, output)
    }

    @Test
    @Throws(Exception::class)
    fun mixedAttributes() {
        val input = MIXED + NESTED
        val expected = MIXED + NESTED_REVERSED
        editText.fromHtml(input)
        val output = editText.toHtml()
        Assert.assertEquals(expected, output)
    }

    @Test
    @Throws(Exception::class)
    fun mixedInlineAndBlockElementsWithoutExtraSpacing() {
        editText.append("some text")
        editText.append("\n")
        editText.append("quote")
        editText.setSelection(editText.length())
        editText.toggleFormatting(TextFormat.FORMAT_QUOTE)
        Assert.assertEquals("some text<blockquote>quote</blockquote>", editText.toHtml())
        editText.setSelection(editText.length())
        editText.append("\n")
        editText.append("\n")
        editText.append("list")
        editText.setSelection(editText.length())
        editText.toggleFormatting(TextFormat.FORMAT_UNORDERED_LIST)
        editText.append("\n")
        editText.append("\n")
        editText.append("some text")

        Assert.assertEquals("some text<blockquote>quote</blockquote><ul><li>list</li></ul>some text", editText.toHtml())
    }
}