package org.wordpress.aztec.toolbar

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.PopupMenu.OnMenuItemClickListener
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.R
import org.wordpress.aztec.TextFormat
import org.wordpress.aztec.source.SourceViewEditText
import org.wordpress.aztec.spans.AztecCommentSpan
import java.util.*

class AztecToolbar : FrameLayout, OnMenuItemClickListener {
    private var addLinkDialog: AlertDialog? = null
    private var editor: AztecText? = null
    private var headingMenu: PopupMenu? = null
    private var sourceEditor: SourceViewEditText? = null

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

        if (addLinkDialog != null && addLinkDialog!!.isShowing) {
            bundle.putBoolean("isUrlDialogVisible", true)

            val urlInput = addLinkDialog!!.findViewById(R.id.linkURL) as EditText
            val anchorInput = addLinkDialog!!.findViewById(R.id.linkText) as EditText

            bundle.putString("retainedUrl", urlInput.text.toString())
            bundle.putString("retainedAnchor", anchorInput.text.toString())
        }

        return bundle
    }


    override fun onRestoreInstanceState(state: Parcelable?) {
        var superState = state

        if (state is Bundle) {
            val isDialogVisible = state.getBoolean("isUrlDialogVisible")
            superState = state.getParcelable("superState")

            if (isDialogVisible) {
                val retainedUrl = state.getString("retainedUrl", "")
                val retainedAnchor = state.getString("retainedAnchor", "")

                showLinkDialog(retainedUrl, retainedAnchor)
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

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        item?.isChecked = (item?.isChecked == false)

        when (item?.itemId) {
            R.id.paragraph -> {
                editor?.toggleFormatting(TextFormat.FORMAT_PARAGRAPH)
                return true
            }
            R.id.heading_1 -> {
                editor?.toggleFormatting(TextFormat.FORMAT_HEADING_1)
                return true
            }
            R.id.heading_2 -> {
                editor?.toggleFormatting(TextFormat.FORMAT_HEADING_2)
                return true
            }
            R.id.heading_3 -> {
                editor?.toggleFormatting(TextFormat.FORMAT_HEADING_3)
                return true
            }
            R.id.heading_4 -> {
                editor?.toggleFormatting(TextFormat.FORMAT_HEADING_4)
                return true
            }
            R.id.heading_5 -> {
                editor?.toggleFormatting(TextFormat.FORMAT_HEADING_5)
                return true
            }
            R.id.heading_6 -> {
                editor?.toggleFormatting(TextFormat.FORMAT_HEADING_6)
                return true
            }
            else -> return false
        }
    }

    private fun isEditorAttached(): Boolean {
        return editor != null && editor is AztecText
    }

    fun setEditor(editor: AztecText, sourceEditor: SourceViewEditText) {
        this.sourceEditor = sourceEditor
        this.editor = editor
        //highlight toolbar buttons based on what styles are applied to the text beneath cursor
        this.editor!!.setOnSelectionChangedListener(object : AztecText.OnSelectionChangedListener {
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

            if (toolbarAction.equals(ToolbarAction.HEADING)) {
                setHeaderMenu(findViewById(toolbarAction.buttonId))
            }
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

    private fun toggleButtonState(button: View?, enabled: Boolean) {
        if (button != null) {
            button.isEnabled = enabled
        }
    }

    private fun highlightAppliedStyles(selStart: Int, selEnd: Int) {
        if (!isEditorAttached()) return

        val newSelStart = if (selStart > 0 && !editor!!.isTextSelected()) selStart - 1 else selStart

        val appliedStyles = editor!!.getAppliedStyles(newSelStart, selEnd)

        highlightActionButtons(ToolbarAction.getToolbarActionsForStyles(appliedStyles))
        selectHeaderMenu(editor!!.getAppliedHeading(newSelStart, selEnd))
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
            ToolbarAction.HEADING -> headingMenu?.show()
            ToolbarAction.LINK -> showLinkDialog()
            ToolbarAction.MORE -> editor!!.applyComment(AztecCommentSpan.Comment.MORE)
            ToolbarAction.PAGE -> editor!!.applyComment(AztecCommentSpan.Comment.PAGE)
            ToolbarAction.HTML -> {
                if (editor!!.visibility == View.VISIBLE) {
                    sourceEditor!!.displayStyledAndFormattedHtml(editor!!.toHtml())

                    editor!!.visibility = View.GONE
                    sourceEditor!!.visibility = View.VISIBLE

                    toggleHtmlMode(true)
                } else {
                    editor!!.fromHtml(sourceEditor!!.getPureHtml())

                    editor!!.visibility = View.VISIBLE
                    sourceEditor!!.visibility = View.GONE

                    toggleHtmlMode(false)
                }
            }
            else -> {
                Toast.makeText(context, "Unsupported action", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun selectHeaderMenu(textFormat: TextFormat?) {
        when (textFormat) {
            TextFormat.FORMAT_HEADING_1 -> headingMenu?.menu?.getItem(1)?.isChecked = true
            TextFormat.FORMAT_HEADING_2 -> headingMenu?.menu?.getItem(2)?.isChecked = true
            TextFormat.FORMAT_HEADING_3 -> headingMenu?.menu?.getItem(3)?.isChecked = true
            TextFormat.FORMAT_HEADING_4 -> headingMenu?.menu?.getItem(4)?.isChecked = true
            TextFormat.FORMAT_HEADING_5 -> headingMenu?.menu?.getItem(5)?.isChecked = true
            TextFormat.FORMAT_HEADING_6 -> headingMenu?.menu?.getItem(6)?.isChecked = true
            else -> headingMenu?.menu?.getItem(0)?.isChecked = true
        }
    }

    private fun setHeaderMenu(view: View) {
        headingMenu = PopupMenu(context, view)
        headingMenu?.setOnMenuItemClickListener(this)
        headingMenu?.inflate(R.menu.heading)
    }

    private fun toggleHtmlMode(isHtmlMode: Boolean) {
        ToolbarAction.values().forEach { action ->
            if (action == ToolbarAction.HTML) {
                toggleButton(findViewById(action.buttonId), isHtmlMode)
            } else {
                toggleButtonState(findViewById(action.buttonId), !isHtmlMode)
            }
        }
	}

    private fun showLinkDialog(presetUrl: String = "", presetAnchor: String = "") {
        if (!isEditorAttached()) return

        val urlAndAnchor = editor!!.getSelectedUrlWithAnchor()

        val url = if (TextUtils.isEmpty(presetUrl)) urlAndAnchor.first else presetUrl
        val anchor = if (TextUtils.isEmpty(presetAnchor)) urlAndAnchor.second else presetAnchor

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
