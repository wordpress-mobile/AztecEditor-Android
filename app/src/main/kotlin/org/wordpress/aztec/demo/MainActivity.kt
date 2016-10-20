package org.wordpress.aztec.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.source.SourceViewEditText
import org.wordpress.aztec.toolbar.AztecToolbar
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        private val HEADING =
                "<h1>Heading 1</h1><br>" +
                "<h2>Heading 2</h2><br>" +
                "<h3>Heading 3</h3><br>" +
                "<h4>Heading 4</h4><br>" +
                "<h5>Heading 5</h5><br>" +
                "<h6>Heading 6</h6><br>"
        private val BOLD = "<b>Bold</b><br>"
        private val ITALIC = "<i>Italic</i><br>"
        private val UNDERLINE = "<u>Underline</u><br>"
        private val STRIKETHROUGH = "<s class=\"test\">Strikethrough</s><br>" // <s> or <strike> or <del>
        private val ORDERED = "<ol><li>Ordered</li></ol>"
        private val UNORDERED = "<ul><li>Unordered</li></ul>"
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
        private val EXAMPLE = HEADING + BOLD + ITALIC + UNDERLINE + STRIKETHROUGH + ORDERED + UNORDERED + QUOTE + LINK + HIDDEN + COMMENT + COMMENT_MORE + COMMENT_PAGE + UNKNOWN
    }

    private lateinit var aztec: AztecText
    private lateinit var source: SourceViewEditText
    private lateinit var formattingToolbar: AztecToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        aztec = findViewById(R.id.aztec) as AztecText
        source = findViewById(R.id.source) as SourceViewEditText

        formattingToolbar = findViewById(R.id.formatting_toolbar) as AztecToolbar
        formattingToolbar.setEditor(aztec, source)

        // initialize the text & HTML
        aztec.fromHtml(EXAMPLE)
        source.displayStyledAndFormattedHtml(aztec.toHtml())
        aztec.fromHtml(source.getPureHtml())

        source.history = aztec.history
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.undo ->
                if (aztec.visibility == View.VISIBLE) {
                    aztec.undo()
                } else {
                    source.undo()
                }
            R.id.redo ->
                if (aztec.visibility == View.VISIBLE) {
                    aztec.redo()
                } else {
                    source.redo()
                }
            else -> {
            }
        }

        return true
    }
}
