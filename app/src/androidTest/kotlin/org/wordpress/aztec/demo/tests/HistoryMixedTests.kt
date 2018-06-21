package org.wordpress.aztec.demo.tests

import org.junit.Ignore
import org.junit.Test
import org.wordpress.aztec.demo.BaseHistoryTest
import org.wordpress.aztec.demo.pages.EditorPage

class HistoryMixedTests : BaseHistoryTest() {

    private val HTML =
            "[caption align=\"alignright\"]<img src=\"https://examplebloge.files.wordpress.com/2017/02/3def4804-d9b5-11e6-88e6-d7d8864392e0.png\">Caption[/caption]\n" +
            "\n" +
            "<h1>Heading 1</h1>\n" +
            "<h2>Heading 2</h2>\n" +
            "<h3>Heading 3</h3>\n" +
            "<h4>Heading 4</h4>\n" +
            "<h5>Heading 5</h5>\n" +
            "<h6>Heading 6</h6>\n" +
            "<b>Bold</b>\n" +
            "<i>Italic</i>\n" +
            "<u>Underline</u>\n" +
            "<s class=\"test\">Strikethrough</s>\n" +
            "<ol>\n" +
            "\t<li>Ordered</li>\n" +
            "\t<li></li>\n" +
            "</ol>\n" +
            "\n" +
            "<hr />\n" +
            "\n" +
            "<ul>\n" +
            "\t<li>Unordered</li>\n" +
            "\t<li></li>\n" +
            "</ul>\n" +
            "<blockquote>Quote</blockquote>\n" +
            "<pre>when (person) {\n" +
            "    MOCTEZUMA -&gt; {\n" +
            "        print (\"friend\")\n" +
            "    }\n" +
            "    CORTES -&gt; {\n" +
            "        print (\"foe\")\n" +
            "    }\n" +
            "}</pre><a href=\"https://github.com/wordpress-mobile/WordPress-Aztec-Android\">Link</a>\n" +
            "<div class=\"first\">\n" +
            "<div class=\"second\">\n" +
            "<div class=\"third\">Div\n" +
            "<span><b>Span</b></span>\n" +
            "Hidden</div>\n" +
            "<div class=\"fourth\"></div>\n" +
            "<div class=\"fifth\"></div>\n" +
            "</div>\n" +
            "</div>\n" +
            "<!--Comment-->\n" +
            "\n" +
            "<!--more-->\n" +
            "\n" +
            "<!--nextpage-->\n" +
            "\n" +
            "<code>if (value == 5) printf(value)</code>\n" +
            "<iframe class=\"classic\">Menu</iframe>\n" +
            "\uD83D\uDC4D测试一个\n" +
            "\n" +
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. [video src=\"https://examplebloge.files.wordpress.com/2017/06/d7d88643-88e6-d9b5-11e6-92e03def4804.mp4\"][audio src=\"https://upload.wikimedia.org/wikipedia/commons/9/94/H-Moll.ogg\"]"

    @Ignore("Until this issue is fixed: https://github.com/wordpress-mobile/AztecEditor-Android/issues/676")
    @Test
    fun testSelectAllDeleteUndoRedo() {
        // Add demo html text to editor
        EditorPage()
                .toggleHtml()
                .replaceHTML(HTML)
                .toggleHtml()
                .threadSleep(throttleTime)

        // Select all text and delete, verify
        EditorPage()
                .tapTop()
                .clearText()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML("")
                .toggleHtml()

        // Undo select all and delete, verify
        EditorPage()
                .undoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTMLNoStripping(HTML)
                .toggleHtml()

        // Redo select all and delete, verify
        EditorPage()
                .redoChange()
                .threadSleep(throttleTime)
                .toggleHtml()
                .verifyHTML("")
    }
}