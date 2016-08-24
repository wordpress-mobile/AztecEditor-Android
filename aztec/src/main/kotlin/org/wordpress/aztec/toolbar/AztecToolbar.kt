package org.wordpress.aztec.toolbar

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
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


class AztecToolbar : FrameLayout {

    private var editor: AztecText? = null

    private var addLinkDialog: AlertDialog? = null

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        bundle.putBoolean("isUrlDialogVisible", if (addLinkDialog != null) addLinkDialog!!.isShowing else false)
        return bundle
    }


    override fun onRestoreInstanceState(state: Parcelable?) {
        var superState = state

        if (state is Bundle) {
            val dialogIsVisible = state.getBoolean("isUrlDialogVisible")

            superState = state.getParcelable("superState");
            if (dialogIsVisible) {
                showLinkDialog()
            }
        }
        super.onRestoreInstanceState(superState)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (addLinkDialog != null && addLinkDialog!!.isShowing) {
            addLinkDialog!!.dismiss()
        }
    }

    private fun isEditorAttached(): Boolean {
        return editor != null && editor is AztecText
    }

    fun setEditor(editor: AztecText) {
        this.editor = editor
        //highlight toolbar buttons based on what styles are applied to the text beneath cursor
        editor!!.setOnSelectionChangedListener(object : AztecText.OnSelectionChangedListener {
            override fun onSelectionChanged(selStart: Int, selEnd: Int) {
                highlightAppliedStyles(selStart, selEnd)
            }
        })
    }

    private fun initView() {
        View.inflate(context, R.layout.format_bar, this)

        for (toolbarAction in ToolbarAction.values()) {
            val button = findViewById(toolbarAction.buttonId)
            button?.setOnClickListener { onToolbarAction(toolbarAction) }
        }
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

    private fun getSelectedActions(): ArrayList<ToolbarAction> {
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


    private fun highlightAppliedStyles(selStart: Int, selEnd: Int) {
        if (!isEditorAttached()) return

        val newSelStart = if (selStart > 0 && !editor!!.isTextSelected()) selStart - 1 else selStart

        val appliedStyles = editor!!.getAppliedStyles(newSelStart, selEnd)

        highlightActionButtons(ToolbarAction.getToolbarActionsForStyles(appliedStyles))
    }


    private fun onToolbarAction(action: ToolbarAction) {
        if (!isEditorAttached()) return

        //if nothing is selected just mark the style as active
        if (!editor!!.isTextSelected() && action.actionType == ToolbarActionType.INLINE_STYLE) {
            val actions = getSelectedActions()
            val textFormats = ArrayList<TextFormat>()

            actions.forEach { if (it.isStylingAction()) textFormats.add(it.textFormat!!) }
            return editor!!.setSelectedStyles(textFormats)
        }

        //if text is selected and action is styling - toggle the style
        if (action.isStylingAction()) {
            return editor!!.toggleFormatting(action.textFormat!!)
        }

        //other toolbar action
        when (action) {
            ToolbarAction.LINK -> showLinkDialog()
            ToolbarAction.HTML -> editor!!.setText(editor!!.toHtml())
            else -> {
                Toast.makeText(context, "Unsupported action", Toast.LENGTH_SHORT).show()
            }
        }

    }


    private fun showLinkDialog() {
        if (!isEditorAttached()) return

        val urlAndAnchor = editor!!.getSelectedUrlWithAnchor()

        val url = urlAndAnchor.first
        val anchor = urlAndAnchor.second

        val builder = AlertDialog.Builder(context)

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_link, null, false)
        val urlInput = dialogView.findViewById(R.id.linkURL) as EditText
        val anchorInput = dialogView.findViewById(R.id.linkText) as EditText

        urlInput.setText(url)
        anchorInput.setText(anchor)

        builder.setView(dialogView)
        builder.setTitle(R.string.dialog_title)

        builder.setPositiveButton(R.string.dialog_button_ok, DialogInterface.OnClickListener { dialog, which ->
            val linkText = urlInput.text.toString().trim { it <= ' ' }
            val anchorText = anchorInput.text.toString().trim { it <= ' ' }

            editor!!.link(linkText, anchorText)

        })

        if (editor!!.isUrlSelected()) {
            builder.setNeutralButton(R.string.dialog_button_remove_link, DialogInterface.OnClickListener { dialogInterface, i ->
                editor!!.removeLink()
            })
        }

        builder.setNegativeButton(R.string.dialog_button_cancel, DialogInterface.OnClickListener { dialogInterface, i ->
            dialogInterface.dismiss()
        })

        addLinkDialog = builder.create()
        addLinkDialog!!.show()
    }
}
