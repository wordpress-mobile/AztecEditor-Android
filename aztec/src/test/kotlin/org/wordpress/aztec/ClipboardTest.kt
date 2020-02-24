package org.wordpress.aztec

import android.annotation.SuppressLint
import android.app.Activity
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.wordpress.aztec.source.Format

@RunWith(RobolectricTestRunner::class)
@Config(sdk = intArrayOf(23))
class ClipboardTest {
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
    private val STRIKETHROUGH = "<s class=\"test\">Strikethrough</s>" // <s> or <strike> or <del>
    private val ORDERED = "<ol><li>Ordered</li><li></li></ol>"
    private val LINE = "<hr />"
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
    private val PREFORMAT = "<pre>if (this) then that;</pre>"
    private val CODE = "<code>if (value == 5) printf(value)</code><br>"
    private val IMG = "<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\" />"
    private val EMOJI = "\uD83D\uDC4D"
    private val HTML_NON_LATIN_TEXT = "测试一个"
    private val LONG_TEXT = "<br><br>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
    private val LONG_TEXT_EXPECTED = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."

    private val EVERYTHING =
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
                    PREFORMAT +
                    LINK +
                    HIDDEN +
                    COMMENT +
                    COMMENT_MORE +
                    COMMENT_PAGE +
                    CODE +
                    UNKNOWN +
                    EMOJI +
                    HTML_NON_LATIN_TEXT +
                    LONG_TEXT

    private val EVERYTHING_EXPECTED =
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
                    PREFORMAT +
                    LINK +
                    Format.removeSourceEditorFormatting(HIDDEN) +
                    COMMENT +
                    COMMENT_MORE +
                    COMMENT_PAGE +
                    CODE +
                    UNKNOWN +
                    EMOJI +
                    HTML_NON_LATIN_TEXT +
                    LONG_TEXT

    lateinit var editText: AztecText

    /**
     * Initialize variables.
     */
    @Before
    fun init() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().visible().get()
        editText = AztecText(activity)
        editText.setCalypsoMode(false)
        activity.setContentView(editText)
    }

    @Test
    @Throws(Exception::class)
    fun copyAndPasteSameInlineStyle() {
        editText.fromHtml("<b>Bold</b>")

        editText.setSelection(0, editText.length())
        TestUtils.copyToClipboard(editText)

        editText.setSelection(editText.length())
        TestUtils.pasteFromClipboard(editText)

        Assert.assertEquals("<b>BoldBold</b>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun copyAndPasteDifferentInlineStyle() {
        editText.fromHtml("<b>Bold</b><i>Italic</i>")

        editText.setSelection(0, editText.length())
        TestUtils.copyToClipboard(editText)

        editText.setSelection(editText.length())
        TestUtils.pasteFromClipboard(editText)

        Assert.assertEquals("<b>Bold</b><i>Italic</i><b>Bold</b><i>Italic</i>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun copyAndPasteNestedListItem() {
        editText.fromHtml("<ul><li>aaa</li><li>bbb<ul><li>ccc</li></ul></li></ul>")

        // select "ccc"
        editText.setSelection(8, 11)
        TestUtils.copyToClipboard(editText)

        editText.setSelection(editText.length())
        TestUtils.pasteFromClipboard(editText)

        Assert.assertEquals("<ul><li>aaa</li><li>bbb<ul><li>ccc<ul><li>ccc</li></ul></li></ul></li></ul>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun copyAndPasteMultipleListLevelsWithInlineStyle() {
        editText.fromHtml("<ul><li>aaa</li><li>bb<b>b</b><ul><li>ccc</li></ul></li></ul>")

        // select text starting with "b<b>..."
        editText.setSelection(5, 10)
        TestUtils.copyToClipboard(editText)

        editText.setSelection(editText.length())
        TestUtils.pasteFromClipboard(editText)

        Assert.assertEquals("<ul><li>aaa</li><li>bb<b>b</b><ul><li>ccc<ul><li>b<b>b</b><ul><li>cc</li></ul></li></ul></li></ul></li></ul>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun copyUpperListItemAndReplaceEntireText() {
        editText.fromHtml("<ul><li>aaa</li><li>bb<b>b</b><ul><li>ccc</li></ul></li></ul>")

        // select text with "b<b>b</b>"
        editText.setSelection(5, 7)
        TestUtils.copyToClipboard(editText)

        editText.setSelection(0, editText.length())
        TestUtils.pasteFromClipboard(editText)

        Assert.assertEquals("<ul><li>b<b>b</b></li></ul>", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun copyBlockquoteAndReplaceInlineStyleText() {
        editText.fromHtml("<blockquote>Hello</blockquote><u>Bye</u>End")

        // select text with "Hello"
        editText.setSelection(0, 5)
        TestUtils.copyToClipboard(editText)

        // Select "Bye"
        editText.setSelection(6, 9)
        TestUtils.pasteFromClipboard(editText)

        Assert.assertEquals("<blockquote>Hello</blockquote><blockquote>Hello</blockquote>End", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun copyBlockquoteAndPartiallyReplaceInlineStyleText() {
        editText.fromHtml("<blockquote>Hello</blockquote><u>Bye</u>End")

        // select text with "Hello"
        editText.setSelection(0, 5)
        TestUtils.copyToClipboard(editText)

        // Select "Bye"
        editText.setSelection(7, 9)
        TestUtils.pasteFromClipboard(editText)

        Assert.assertEquals("<blockquote>Hello</blockquote><u>B</u><blockquote>Hello</blockquote>End", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun copyAndPasteHeadings() {
        editText.fromHtml("<h1>H1</h1><h2>H2</h2><u>Bye</u>End")

        // select half of first and half of second heading
        editText.setSelection(1, 4)
        TestUtils.copyToClipboard(editText)

        editText.setSelection(editText.length())
        TestUtils.pasteFromClipboard(editText)

        Assert.assertEquals("<h1>H1</h1><h2>H2</h2><u>Bye</u>End<h1>1</h1><h2>H</h2>", editText.toHtml())
    }

    @SuppressLint("SetTextI18n")
    @Test
    @Throws(Exception::class)
    fun copyAndPasteCalypsoParagraphs() {
        editText.setCalypsoMode(true)
        editText.fromHtml("<p>aaa</p><p>bbb</p><p>ccc</p>")

        editText.setSelection(0, editText.length())
        TestUtils.copyToClipboard(editText)

        editText.setSelection(editText.length())
        TestUtils.pasteFromClipboard(editText)

        Assert.assertEquals("aaa\n\nbbb\n\nccc\n\naaa\n\nbbb\n\nccc", editText.toHtml())

        editText.setCalypsoMode(false)
    }

    @Test
    @Throws(Exception::class)
    fun copyAndPasteEverything() {
        editText.fromHtml(EVERYTHING)

        editText.setSelection(0, editText.length())
        TestUtils.copyToClipboard(editText)

        editText.setSelection(editText.length())
        TestUtils.pasteFromClipboard(editText)

        Assert.assertEquals(EVERYTHING_EXPECTED + EVERYTHING_EXPECTED, editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun copyAndReplaceEverything() {
        editText.fromHtml(EVERYTHING)

        editText.setSelection(0, editText.length())
        TestUtils.copyToClipboard(editText)

        editText.setSelection(0, editText.length())
        TestUtils.pasteFromClipboard(editText)

        Assert.assertEquals(EVERYTHING_EXPECTED, editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun copyAndReplacePlainText() {
        editText.fromHtml(LONG_TEXT)

        editText.setSelection(0, editText.text.indexOf(' ', 2))
        TestUtils.copyToClipboard(editText)

        editText.setSelection(0, editText.text.indexOf(' ', 2))
        TestUtils.pasteFromClipboard(editText)

        Assert.assertEquals(LONG_TEXT, editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun copyAndReplacePlainTextCalypsoMode() {
        editText.setCalypsoMode(true)
        editText.fromHtml(LONG_TEXT)

        editText.setSelection(0, editText.text.indexOf(' ', 2))
        TestUtils.copyToClipboard(editText)

        editText.setSelection(0, editText.text.indexOf(' ', 2))
        TestUtils.pasteFromClipboard(editText)

        Assert.assertEquals(LONG_TEXT_EXPECTED, editText.toHtml())

        editText.setCalypsoMode(false)
    }

    @Test
    @Throws(Exception::class)
    fun copyAndPasteAsPlainTextSameInlineStyle() {
        editText.fromHtml("<b>Bold</b>")

        editText.setSelection(0, editText.length())
        TestUtils.copyToClipboard(editText)

        editText.setSelection(editText.length())
        TestUtils.pasteFromClipboardAsPlainText(editText)

        Assert.assertEquals("<b>Bold</b>Bold", editText.toHtml())
    }

    @Test
    @Throws(Exception::class)
    fun copyAndReplaceAsPlainText() {
        editText.fromHtml(LONG_TEXT)

        editText.setSelection(0, editText.length())
        TestUtils.copyToClipboard(editText)

        editText.setSelection(0, editText.length())
        TestUtils.pasteFromClipboardAsPlainText(editText)

        Assert.assertEquals(LONG_TEXT_EXPECTED, editText.toHtml())
    }
}
