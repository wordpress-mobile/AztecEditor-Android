package org.wordpress.aztec.toolbar

import android.app.AlertDialog
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.text.TextUtils
import android.text.style.URLSpan
import android.util.AttributeSet
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import android.widget.ToggleButton
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.R
import org.wordpress.aztec.TextFormat
import java.util.*


class FormatToolbar : FrameLayout {

    private var mEditor: AztecText? = null

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
    }


    fun isEditorAttached(): Boolean {
        return mEditor != null
    }

    fun setEditor(editor: AztecText, initialText: String) {
        mEditor = editor
        mEditor!!.setText(initialText)
        //highlight toolbar buttons based on what styles are applied to the text beneath cursor
        mEditor!!.setOnSelectionChangedListener(object : AztecText.OnSelectionChangedListener {
            override fun onSelectionChanged(selStart: Int, selEnd: Int) {
                highlightAppliedStyles(selStart, selEnd)
            }
        })
    }

    private fun initView() {
        View.inflate(context, R.layout.format_bar, this)

        for (toolbarAction in ToolbarAction.values()) {
            val button = findViewById(toolbarAction.buttonId)
            button?.setOnClickListener { sendToolbarEvent(toolbarAction) }
        }

    }

    private fun sendToolbarEvent(toolbarAction: ToolbarAction) {
        onToolbarAction(toolbarAction)
    }



    fun highlightActionButtons(toolbarActions: ArrayList<ToolbarAction>) {
        ToolbarAction.values().forEach { action ->
            if (toolbarActions.contains(action)) {
                toggleButton(findViewById(action.buttonId), true)
            } else {
                toggleButton(findViewById(action.buttonId), false)
            }
        }
    }

    fun getSelectedActions(): ArrayList<ToolbarAction> {
        val actions = ArrayList<ToolbarAction>()

        for (action in ToolbarAction.values()) {
            val view = findViewById(action.buttonId) as ToggleButton
            if (view.isChecked) actions.add(action)
        }

        return actions
    }


    private fun toggleButton(button: View?, checked: Boolean) {
        if (button != null && button is ToggleButton) {
            button.isChecked = checked
        }
    }


    fun highlightAppliedStyles(selStart: Int, selEnd: Int) {
        if (!isEditorAttached()) return

        var newSelStart = selStart

        if (selStart > 0 && !mEditor!!.isTextSelected()) {
            newSelStart = selStart - 1
        }

        val appliedStyles = mEditor!!.getAppliedStyles(newSelStart, selEnd)

        //remember the last active style after emptying EditText manually
        if (!mEditor!!.isEmpty()) {
            mEditor!!.setSelectedStyles(appliedStyles)
            highlightActionButtons(ToolbarAction.getToolbarActionsForStyles(appliedStyles))
        }
    }


    fun onToolbarAction(action: ToolbarAction) {
        if (!isEditorAttached()) return
        //if noting is selected just activate style
        if (!mEditor!!.isTextSelected() && action.actionType == ToolbarActionType.INLINE_STYLE) {
            val actions = getSelectedActions()
            val textFormats = ArrayList<TextFormat>()

            actions.forEach { if (it.isStylingAction()) textFormats.add(it.textFormat!!) }
            return mEditor!!.setSelectedStyles(textFormats)
        }

        //if text is selected and action is styling toggle it's style
        if (action.isStylingAction()) {
            return mEditor!!.toggleFormatting(action.textFormat!!)
        }

        //other toolbar action
        when (action) {
            ToolbarAction.LINK -> showLinkDialog()
            ToolbarAction.HTML -> mEditor!!.setText(mEditor!!.toHtml())
            else -> {
                Toast.makeText(context, "Unsupported action", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun showLinkDialog() {
        if (!isEditorAttached()) return

        var start = mEditor!!.selectionStart
        var end = mEditor!!.selectionEnd

        val urlSpans = mEditor!!.text.getSpans(start, end, URLSpan::class.java)


        val url: String?
        var anchor: String?

        var modifyingExistingLink = false

        var spanStart: Int = 0
        var spanEnd: Int = 0

        if (!urlSpans.isEmpty()) {
            val urlSpan = urlSpans[0]

            spanStart = mEditor!!.text.getSpanStart(urlSpan)
            spanEnd = mEditor!!.text.getSpanEnd(urlSpan)

            if (start < spanStart || end > spanEnd) {
                //looks like some text that is not part of the url was included in selection
                anchor = mEditor!!.text.substring(start, end)
                url = ""

            } else {
                anchor = mEditor!!.text.substring(spanStart, spanEnd)
                url = urlSpan.url
                start = spanStart
                end = spanEnd
            }

            if (anchor.equals(url)) {
                anchor = ""
            }


            modifyingExistingLink = true

        } else {

            val clipboardUrl = getUrlFromClipboard(context)

            if (TextUtils.isEmpty(clipboardUrl)) {
                url = ""
            } else {
                url = clipboardUrl
            }



            if (start == end) {
                anchor = ""
            } else {
                anchor = mEditor!!.text.substring(start, end)
            }
        }


        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_link, null, false)
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
                mEditor!!.editLink(link, anchorText, start, end)
            } else {
                mEditor!!.addLink(link, anchorText, start, end)
            }

        })
        builder.create().show()
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
