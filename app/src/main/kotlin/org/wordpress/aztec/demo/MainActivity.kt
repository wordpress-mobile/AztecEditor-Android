package org.wordpress.aztec.demo

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.toolbar.FormatToolbar
import org.wordpress.aztec.toolbar.ToolbarAction
import java.util.*

class MainActivity : Activity(), FormatToolbar.OnToolbarActionListener {

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
    private lateinit var mToolbar: FormatToolbar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        aztec = findViewById(R.id.aztec) as AztecText
        mToolbar = findViewById(R.id.toolbar) as FormatToolbar


        //highlight toolbar buttons based on what styles are applied to the text beneath cursor
        aztec.setOnSelectionChangedListener(object : AztecText.OnSelectionChangedListener {
            override fun onSelectionChanged(selStart: Int, selEnd: Int) {
                val activeToolbarActions = ArrayList<ToolbarAction>()
                if (selStart != selEnd) {
                    mToolbar.highlightActionButtons(activeToolbarActions)
                    return
                }

                var newSelStart = selStart

                if (selStart > 0) {
                    newSelStart = selStart - 1
                }

                for (textFormat: AztecText.TextFormat in AztecText.TextFormat.values()) {
                    if (aztec.contains(textFormat, newSelStart, selEnd)) {
                        val underlyingAction = getToolbarActionForTextFormat(textFormat)

                        if (underlyingAction != null) {
                            activeToolbarActions.add(underlyingAction)
                        }
                    }
                }

                mToolbar.highlightActionButtons(activeToolbarActions)

    }
})

        // ImageGetter coming soon...
//        aztec.fromHtml(EXAMPLE)
        aztec.setSelection(aztec.editableText.length)

        mToolbar.setToolbarActionListener(this)
    }

    override fun onToolbarAction(action: ToolbarAction) {
        //we dont have anything selected
        if (aztec.selectionStart == aztec.selectionEnd) {

            val actions = mToolbar.getSelectedActions()
            val textFormats = ArrayList<AztecText.TextFormat>()

            actions.forEach { action ->
                val textFormat = getTextFormatFromToolbarAction(action)

                if(textFormat != null){
                    textFormats.add(textFormat)
                }
            }

            aztec.setSelectedStyles(textFormats)

        }

        when (action) {
            ToolbarAction.BOLD -> aztec.bold(!aztec.contains(AztecText.TextFormat.FORMAT_BOLD))
            ToolbarAction.ITALIC -> aztec.italic(!aztec.contains(AztecText.TextFormat.FORMAT_ITALIC))
            ToolbarAction.BULLET_LIST -> aztec.bullet(!aztec.contains(AztecText.TextFormat.FORMAT_BULLET))
            ToolbarAction.LINK -> showLinkDialog()
            ToolbarAction.BLOCKQUOTE -> aztec.quote(!aztec.contains(AztecText.TextFormat.FORMAT_QUOTE))
            else -> {
                Toast.makeText(this@MainActivity, "Unsupported action", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getToolbarActionForTextFormat(textFormat: AztecText.TextFormat): ToolbarAction? {
        when (textFormat) {
            AztecText.TextFormat.FORMAT_BOLD -> return ToolbarAction.BOLD
            AztecText.TextFormat.FORMAT_ITALIC -> return ToolbarAction.ITALIC
            AztecText.TextFormat.FORMAT_BULLET -> return ToolbarAction.BULLET_LIST
            AztecText.TextFormat.FORMAT_QUOTE -> return ToolbarAction.BLOCKQUOTE
            AztecText.TextFormat.FORMAT_LINK -> return ToolbarAction.LINK
            else -> return null
        }
    }

    fun getTextFormatFromToolbarAction(textFormat: ToolbarAction): AztecText.TextFormat? {
        when (textFormat) {
            ToolbarAction.BOLD -> return AztecText.TextFormat.FORMAT_BOLD
            ToolbarAction.ITALIC -> return AztecText.TextFormat.FORMAT_ITALIC
            ToolbarAction.BULLET_LIST -> return AztecText.TextFormat.FORMAT_BULLET
            ToolbarAction.BLOCKQUOTE -> return AztecText.TextFormat.FORMAT_QUOTE
            ToolbarAction.LINK -> return AztecText.TextFormat.FORMAT_LINK
            else -> return null
        }
    }

    private fun showLinkDialog() {
        val start = aztec.selectionStart
        val end = aztec.selectionEnd

        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)

        val view = layoutInflater.inflate(R.layout.dialog_link, null, false)
        val editText = view.findViewById(R.id.edit) as EditText
        builder.setView(view)
        builder.setTitle(R.string.dialog_title)

        builder.setPositiveButton(R.string.dialog_button_ok, DialogInterface.OnClickListener { dialog, which ->
            val link = editText.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(link)) {
                return@OnClickListener
            }

            // When AztecText lose focus, use this method
            aztec.link(link, start, end)
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
}
