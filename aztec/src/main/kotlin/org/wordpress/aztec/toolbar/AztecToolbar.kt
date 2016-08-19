package org.wordpress.aztec.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import android.widget.ToggleButton
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.R
import org.wordpress.aztec.TextFormat
import java.util.*


class AztecToolbar : FrameLayout {

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


    private fun isEditorAttached(): Boolean {
        return mEditor != null && mEditor is AztecText
    }

    fun setEditor(editor: AztecText) {
        mEditor = editor
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

        var newSelStart = selStart

        if (selStart > 0 && !mEditor!!.isTextSelected()) {
            newSelStart = selStart - 1
        }

        val appliedStyles = mEditor!!.getAppliedStyles(newSelStart, selEnd)

        mEditor!!.setSelectedStyles(appliedStyles)
        highlightActionButtons(ToolbarAction.getToolbarActionsForStyles(appliedStyles))
    }


    private fun onToolbarAction(action: ToolbarAction) {
        if (!isEditorAttached()) return

        //if nothing is selected just mark the style as active
        if (!mEditor!!.isTextSelected() && action.actionType == ToolbarActionType.INLINE_STYLE) {
            val actions = getSelectedActions()
            val textFormats = ArrayList<TextFormat>()

            actions.forEach { if (it.isStylingAction()) textFormats.add(it.textFormat!!) }
            return mEditor!!.setSelectedStyles(textFormats)
        }

        //if text is selected and action is styling - toggle the style
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
        Toast.makeText(context, "Unsupported action", Toast.LENGTH_SHORT).show()
    }

}
