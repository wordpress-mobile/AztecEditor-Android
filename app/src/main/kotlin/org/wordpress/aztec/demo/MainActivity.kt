package org.wordpress.aztec.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.toolbar.FormatToolbar

class MainActivity : AppCompatActivity() {

    companion object {
        private val BOLD = "<b>Bold</b><br><br>"
        private val ITALIC = "<i>Italic</i><br><br>"
        private val UNDERLINE = "<u>Underline</u><br><br>"
        private val STRIKETHROUGH = "<s class=\"test\">Strikethrough</s><br><br>" // <s> or <strike> or <del>
        private val BULLET = "<ul><li>asdfg</li></ul>"
        private val QUOTE = "<blockquote>Quote</blockquote>"
        private val LINK = "<a href=\"https://github.com/wordpress-mobile/WordPress-Aztec-Android\">Link</a><br><br>"
        private val UNKNOWN = "<iframe class=\"classic\">Menu</iframe><br><br>"
        private val COMMENT = "<!--This is a comment--><br><br>"
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
                        "<br><br>"
        private val EXAMPLE = BOLD + ITALIC + UNDERLINE + STRIKETHROUGH + BULLET + QUOTE + LINK + UNKNOWN + COMMENT + HIDDEN
    }

    private lateinit var aztec: AztecText
    private lateinit var mFormattingToolbar: FormatToolbar

    private val TEST = "<ul><li>one</li><li>hello</li><li>two</li></ul>"
//    private val TEST = "<blockquote>one</blockquote>two<blockquote>three</blockquote>"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        aztec = findViewById(R.id.aztec) as AztecText
        mFormattingToolbar = findViewById(R.id.formatting_toolbar) as FormatToolbar
        mFormattingToolbar.setEditor(aztec, "")


//        aztec.fromHtml(TEST)
        aztec.setSelection(aztec.editableText.length)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.undo -> aztec.undo()
            R.id.redo -> aztec.redo()
            else -> {
            }
        }

        return true
    }


}
