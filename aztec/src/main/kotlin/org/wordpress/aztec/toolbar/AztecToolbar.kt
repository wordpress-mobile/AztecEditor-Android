package org.wordpress.aztec.toolbar

import android.content.Context
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.PopupMenu.OnMenuItemClickListener
import android.widget.Toast
import android.widget.ToggleButton
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.R
import org.wordpress.aztec.TextFormat
import org.wordpress.aztec.source.SourceViewEditText
import java.util.*

class AztecToolbar : FrameLayout, OnMenuItemClickListener {
    private var aztecToolbarListener: AztecToolbarClickListener? = null
    private var editor: AztecText? = null
    private var headingMenu: PopupMenu? = null
    private var sourceEditor: SourceViewEditText? = null
    private var dialogShortcuts: AlertDialog? = null

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
    }

    fun setToolbarListener(listener: AztecToolbarClickListener) {
        aztecToolbarListener = listener
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_1 -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Heading 1 = Alt + Ctrl + 1
                    editor?.toggleFormatting(TextFormat.FORMAT_HEADING_1)
                    return true
                }
            }
            KeyEvent.KEYCODE_2 -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Heading 2 = Alt + Ctrl + 2
                    editor?.toggleFormatting(TextFormat.FORMAT_HEADING_2)
                    return true
                }
            }
            KeyEvent.KEYCODE_3 -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Heading 3 = Alt + Ctrl + 3
                    editor?.toggleFormatting(TextFormat.FORMAT_HEADING_3)
                    return true
                }
            }
            KeyEvent.KEYCODE_4 -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Heading 4 = Alt + Ctrl + 4
                    editor?.toggleFormatting(TextFormat.FORMAT_HEADING_4)
                    return true
                }
            }
            KeyEvent.KEYCODE_5 -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Heading 5 = Alt + Ctrl + 5
                    editor?.toggleFormatting(TextFormat.FORMAT_HEADING_5)
                    return true
                }
            }
            KeyEvent.KEYCODE_6 -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Heading 6 = Alt + Ctrl + 6
                    editor?.toggleFormatting(TextFormat.FORMAT_HEADING_6)
                    return true
                }
            }
            KeyEvent.KEYCODE_7 -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Heading 6 = Alt + Ctrl + 7
                    editor?.toggleFormatting(TextFormat.FORMAT_PARAGRAPH)
                    return true
                }
            }
            KeyEvent.KEYCODE_8 -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Preformatted = Alt + Ctrl + 8
//                    TODO: Add Preformatted format.
//                    editor?.toggleFormatting(TextFormat.FORMAT_PREFORMATTED)
                    return true
                }
            }
            KeyEvent.KEYCODE_B -> {
                if (event.isCtrlPressed) { // Bold = Ctrl + B
                    findViewById(ToolbarAction.BOLD.buttonId).performClick()
                    return true
                }
            }
            KeyEvent.KEYCODE_D -> {
                if (event.isCtrlPressed) { // Strikethrough = Ctrl + D
                    findViewById(ToolbarAction.STRIKETHROUGH.buttonId).performClick()
                    return true
                }
            }
            KeyEvent.KEYCODE_H -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Shortcuts = Alt + Ctrl + H
                    showDialogShortcuts()
                    return true
                }
            }
            KeyEvent.KEYCODE_I -> {
                if (event.isCtrlPressed) { // Italic = Ctrl + I
                    findViewById(ToolbarAction.ITALIC.buttonId).performClick()
                    return true
                }
            }
            KeyEvent.KEYCODE_K -> {
                if (event.isCtrlPressed) { // Link = Ctrl + K
                    findViewById(ToolbarAction.LINK.buttonId).performClick()
                    return true
                }
            }
            KeyEvent.KEYCODE_M -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Media = Alt + Ctrl + M
                    findViewById(ToolbarAction.ADD_MEDIA.buttonId).performClick()
                    return true
                }
            }
            KeyEvent.KEYCODE_O -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Ordered List = Alt + Ctrl + O
                    findViewById(ToolbarAction.ORDERED_LIST.buttonId).performClick()
                    return true
                }
            }
            KeyEvent.KEYCODE_P -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Page Break = Alt + Ctrl + P
                    findViewById(ToolbarAction.PAGE.buttonId).performClick()
                    return true
                }
            }
            KeyEvent.KEYCODE_Q -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Quote = Alt + Ctrl + Q
                    findViewById(ToolbarAction.QUOTE.buttonId).performClick()
                    return true
                }
            }
            KeyEvent.KEYCODE_T -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Read More = Alt + Ctrl + T
                    findViewById(ToolbarAction.MORE.buttonId).performClick()
                    return true
                }
            }
            KeyEvent.KEYCODE_U -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Unordered List = Alt + Ctrl + U
                    findViewById(ToolbarAction.UNORDERED_LIST.buttonId).performClick()
                    return true
                } else if (event.isCtrlPressed) { // Underline = Ctrl + U
                    findViewById(ToolbarAction.UNDERLINE.buttonId).performClick()
                    return true
                }
            }
            KeyEvent.KEYCODE_X -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Code = Alt + Ctrl + X
//                    TODO: Add Code action.
//                    findViewById(ToolbarAction.CODE.buttonId).performClick()
                    return true
                }
            }
            KeyEvent.KEYCODE_Y -> {
                if (event.isCtrlPressed) { // Redo  = Ctrl + Y
                    editor?.redo()
                    return true
                }
            }
            KeyEvent.KEYCODE_Z -> {
                if (event.isCtrlPressed) { // Undo  = Ctrl + Z
                    editor?.undo()
                    return true
                }
            }
        }

        return false
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
        View.inflate(context, R.layout.aztec_format_bar, this)

        for (toolbarAction in ToolbarAction.values()) {
            val button = findViewById(toolbarAction.buttonId)
            button?.setOnClickListener { onToolbarAction(toolbarAction) }

            if (toolbarAction == ToolbarAction.HEADING) {
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

        val appliedStyles = editor!!.getAppliedStyles(selStart, selEnd)
        highlightActionButtons(ToolbarAction.getToolbarActionsForStyles(appliedStyles))
        selectHeaderMenu(appliedStyles)
    }

    private fun onToolbarAction(action: ToolbarAction) {
        if (!isEditorAttached()) return

        //if nothing is selected just mark the style as active
        if (!editor!!.isTextSelected() && action.actionType == ToolbarActionType.INLINE_STYLE) {
            val actions = getSelectedActions()
            val textFormats = ArrayList<TextFormat>()

            actions.forEach { if (it.isStylingAction()) textFormats.add(it.textFormat!!) }
            if (getSelectedHeading() != null) {
                textFormats.add(getSelectedHeading()!!)
            }
            return editor!!.setSelectedStyles(textFormats)
        }

        //if text is selected and action is styling - toggle the style
        if (action.isStylingAction() && action != ToolbarAction.HEADING) {
            return editor!!.toggleFormatting(action.textFormat!!)
        }

        //other toolbar action
        when (action) {
            ToolbarAction.ADD_MEDIA -> aztecToolbarListener?.onToolbarAddMediaClicked()
            ToolbarAction.HEADING -> headingMenu?.show()
            ToolbarAction.LINK -> editor!!.showLinkDialog()
            ToolbarAction.HTML -> aztecToolbarListener?.onToolbarHtmlModeClicked()
            else -> {
                Toast.makeText(context, "Unsupported action", Toast.LENGTH_SHORT).show()
            }
        }
    }

    public fun toggleEditorMode() {
        if (editor!!.visibility == View.VISIBLE) {
            sourceEditor!!.displayStyledAndFormattedHtml(editor!!.toHtml(true))
            editor!!.visibility = View.GONE
            sourceEditor!!.visibility = View.VISIBLE

            toggleHtmlMode(true)
        } else {
            editor!!.fromHtml(sourceEditor!!.getPureHtml(true))
            editor!!.visibility = View.VISIBLE
            sourceEditor!!.visibility = View.GONE

            toggleHtmlMode(false)
        }
    }

    private fun selectHeaderMenu(textFormats: ArrayList<TextFormat>) {
        headingMenu?.menu?.getItem(0)?.isChecked = true
        textFormats.forEach {
            when (it) {
                TextFormat.FORMAT_HEADING_1 -> headingMenu?.menu?.getItem(1)?.isChecked = true
                TextFormat.FORMAT_HEADING_2 -> headingMenu?.menu?.getItem(2)?.isChecked = true
                TextFormat.FORMAT_HEADING_3 -> headingMenu?.menu?.getItem(3)?.isChecked = true
                TextFormat.FORMAT_HEADING_4 -> headingMenu?.menu?.getItem(4)?.isChecked = true
                TextFormat.FORMAT_HEADING_5 -> headingMenu?.menu?.getItem(5)?.isChecked = true
                TextFormat.FORMAT_HEADING_6 -> headingMenu?.menu?.getItem(6)?.isChecked = true
                else -> {

                }
            }
        }
    }

    private fun setHeaderMenu(view: View) {
        headingMenu = PopupMenu(context, view)
        headingMenu?.setOnMenuItemClickListener(this)
        headingMenu?.inflate(R.menu.heading)
    }

    fun getSelectedHeading(): TextFormat? {
        if (headingMenu?.menu?.getItem(1)?.isChecked!!) return TextFormat.FORMAT_HEADING_1
        else if (headingMenu?.menu?.getItem(2)?.isChecked!!) return TextFormat.FORMAT_HEADING_2
        else if (headingMenu?.menu?.getItem(3)?.isChecked!!) return TextFormat.FORMAT_HEADING_3
        else if (headingMenu?.menu?.getItem(4)?.isChecked!!) return TextFormat.FORMAT_HEADING_4
        else if (headingMenu?.menu?.getItem(5)?.isChecked!!) return TextFormat.FORMAT_HEADING_5
        else if (headingMenu?.menu?.getItem(6)?.isChecked!!) return TextFormat.FORMAT_HEADING_6

        return null
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

    fun enableFormatButtons(isEnabled: Boolean) {
        ToolbarAction.values().forEach { action ->
            if (action != ToolbarAction.HTML) {
                toggleButtonState(findViewById(action.buttonId), isEnabled)
            }
        }
    }

    private fun showDialogShortcuts() {
        val layout = LayoutInflater.from(context).inflate(R.layout.dialog_shortcuts, null)
        val builder = AlertDialog.Builder(context)
        builder.setView(layout)
        dialogShortcuts = builder.create()
        dialogShortcuts!!.show()
    }
}
