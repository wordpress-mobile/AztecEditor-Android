package org.wordpress.aztec.toolbar

import android.content.ClipboardManager
import android.content.Context
import android.util.AttributeSet
import android.util.Patterns
import android.view.View
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
