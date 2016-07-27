package org.wordpress.aztec.demo

import android.app.AlertDialog
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.text.style.URLSpan
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.TextFormat
import org.wordpress.aztec.toolbar.FormatToolbar
import org.wordpress.aztec.toolbar.ToolbarAction
import org.wordpress.aztec.toolbar.ToolbarActionType
import java.util.*

class MainActivity : AppCompatActivity(), FormatToolbar.OnToolbarActionListener {

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
        private val EXAMPLE = BOLD + ITALIC + UNDERLINE + STRIKETHROUGH + BULLET + QUOTE + LINK + UNKNOWN + COMMENT
    }

    private lateinit var aztec: AztecText
    private lateinit var mFormattingToolbar: FormatToolbar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        aztec = findViewById(R.id.aztec) as AztecText
        mFormattingToolbar = findViewById(R.id.formatting_toolbar) as FormatToolbar


        //highlight toolbar buttons based on what styles are applied to the text beneath cursor
        aztec.setOnSelectionChangedListener(object : AztecText.OnSelectionChangedListener {
            override fun onSelectionChanged(selStart: Int, selEnd: Int) {
                highlightAppliedStyles(selStart,selEnd)
            }
        })

        // ImageGetter coming soon...
//        aztec.fromHtml(EXAMPLE)
        aztec.setSelection(aztec.editableText.length)

        mFormattingToolbar.setToolbarActionListener(this)
    }


    fun highlightAppliedStyles(selStart: Int, selEnd: Int){
        var newSelStart = selStart

        if (selStart > 0 && !aztec.isTextSelected()) {
            newSelStart = selStart - 1
        }

        val appliedStyles = aztec.getAppliedStyles(newSelStart, selEnd)

        //remember the last active style after emptying EditText manually
        if (!aztec.isEmpty()) {
            aztec.setSelectedStyles(appliedStyles)
            mFormattingToolbar.highlightActionButtons(ToolbarAction.getToolbarActionsForStyles(appliedStyles))
        }
    }

    override fun onToolbarAction(action: ToolbarAction) {
        //if noting is selected just activate style
        if (!aztec.isTextSelected() && action.actionType == ToolbarActionType.INLINE_STYLE) {
            val actions = mFormattingToolbar.getSelectedActions()
            val textFormats = ArrayList<TextFormat>()

            actions.forEach { if (it.isStylingAction()) textFormats.add(it.textFormat!!) }
            return aztec.setSelectedStyles(textFormats)
        }

        //if text is selected and action is styling toggle it's style
        if (action.isStylingAction()) {
            return aztec.toggleFormatting(action.textFormat!!)
        }

        //other toolbar action
        when (action) {
            ToolbarAction.LINK -> showLinkDialog()
            ToolbarAction.HTML -> aztec.setText(aztec.toHtml())
            else -> {
                Toast.makeText(this@MainActivity, "Unsupported action", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun showLinkDialog() {
        val start = aztec.selectionStart
        val end = aztec.selectionEnd

        val urlSpans = aztec.text.getSpans(start, end, URLSpan::class.java)


        val url: String?
        var anchor: String?

        var modifyingExistingLink = false

        var spanStart: Int = 0
        var spanEnd: Int = 0

        if (!urlSpans.isEmpty()) {
            val urlSpan = urlSpans[0]

            spanStart = aztec.text.getSpanStart(urlSpan)
            spanEnd = aztec.text.getSpanEnd(urlSpan)

            anchor = aztec.text.substring(spanStart, spanEnd)
            url = urlSpan.url

            if(anchor.equals(url)){
                anchor = ""
            }

            modifyingExistingLink = true

        } else {

            val clipboardUrl = getUrlFromClipboard(this)

            if(TextUtils.isEmpty(clipboardUrl)){
                url = ""
            }else{
                url = clipboardUrl
            }



            if (start == end) {
                anchor = ""
            } else {
                anchor = aztec.text.substring(start, end)
            }
        }


        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)

        val view = layoutInflater.inflate(R.layout.dialog_link, null, false)
        val urlInput = view.findViewById(R.id.linkURL) as EditText
        val anchorInput = view.findViewById(R.id.linkText) as EditText

        urlInput.setText(url)
        anchorInput.setText(anchor)

        builder.setView(view)
        builder.setTitle(R.string.dialog_title)

        builder.setPositiveButton(R.string.dialog_button_ok, DialogInterface.OnClickListener { dialog, which ->
            val link = urlInput.text.toString().trim { it <= ' ' }
            val anchorText = anchorInput.text.toString().trim { it <= ' ' }

            if (TextUtils.isEmpty(link)) {
                return@OnClickListener
            }

            if (modifyingExistingLink) {
                aztec.editLink(link, anchorText, spanStart, spanEnd)
            } else {
                aztec.addLink(link, anchorText, start, end)
            }

        })
        builder.create().show()
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


    /**
     * Checks the Clipboard for text that matches the [Patterns.WEB_URL] pattern.

     * @return the URL text in the clipboard, if it exists; otherwise null
     */
    fun getUrlFromClipboard(context: Context?): String? {
        if (context == null) return null
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val data = clipboard?.primaryClip
        if (data == null || data.itemCount <= 0) return null
        val clipText = data.getItemAt(0).text.toString()
        return if (Patterns.WEB_URL.matcher(clipText).matches()) clipText else null
    }
}
