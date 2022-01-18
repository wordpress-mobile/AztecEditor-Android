package org.wordpress.aztec.toolbar

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.PopupMenu.OnMenuItemClickListener
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import org.wordpress.android.util.AppLog
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.AztecText.EditorHasChanges.NO_CHANGES
import org.wordpress.aztec.AztecTextFormat
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.R
import org.wordpress.aztec.plugins.IMediaToolbarButton
import org.wordpress.aztec.plugins.IToolbarButton
import org.wordpress.aztec.source.SourceViewEditText
import org.wordpress.aztec.util.convertToButtonAccessibilityProperties
import org.wordpress.aztec.util.setBackgroundDrawableRes
import java.util.ArrayList
import java.util.Arrays
import java.util.Locale

/**
 * Aztec toolbar container.
 * Contains both Styling and Media toolbars.
 * Supports RTL layout direction on API 19+
 */
class AztecToolbar : FrameLayout, IAztecToolbar, OnMenuItemClickListener {
    val RETAINED_EDITOR_HTML_PARSED_SHA256_KEY = "RETAINED_EDITOR_HTML_PARSED_SHA256_KEY"
    val RETAINED_SOURCE_HTML_PARSED_SHA256_KEY = "RETAINED_SOURCE_HTML_PARSED_SHA256_KEY"

    private var aztecToolbarListener: IAztecToolbarClickListener? = null
    private var editor: AztecText? = null
    private var headingMenu: PopupMenu? = null
    private var listMenu: PopupMenu? = null
    private var sourceEditor: SourceViewEditText? = null
    private var dialogShortcuts: AlertDialog? = null
    private var isAdvanced: Boolean = false
    private var hasCustomLayout: Boolean = false
    private var isMediaToolbarAvailable: Boolean = false
    private var isExpanded: Boolean = false
    private var isMediaToolbarVisible: Boolean = false
    private var isMediaModeEnabled: Boolean = false

    var editorContentParsedSHA256LastSwitch: ByteArray = ByteArray(0)
    var sourceContentParsedSHA256LastSwitch: ByteArray = ByteArray(0)

    private lateinit var toolbarScrolView: HorizontalScrollView
    private lateinit var buttonEllipsisCollapsed: RippleToggleButton
    private lateinit var buttonEllipsisExpanded: RippleToggleButton
    private lateinit var layoutExpandedTranslateInEnd: Animation
    private lateinit var layoutExpandedTranslateOutStart: Animation

    private lateinit var htmlButton: RippleToggleButton
    private lateinit var buttonMediaCollapsed: RippleToggleButton
    private lateinit var buttonMediaExpanded: RippleToggleButton

    private lateinit var layoutMediaTranslateInEnd: Animation
    private lateinit var layoutMediaTranslateOutStart: Animation
    private lateinit var layoutMediaTranslateOutEnd: Animation
    private lateinit var layoutMediaTranslateInStart: Animation

    private lateinit var ellipsisSpinLeft: Animation
    private lateinit var ellipsisSpinRight: Animation
    private lateinit var mediaButtonSpinLeft: Animation
    private lateinit var mediaButtonSpinRight: Animation
    private lateinit var layoutExpanded: LinearLayout

    private lateinit var mediaToolbar: View
    private lateinit var stylingToolbar: View

    private var toolbarButtonPlugins: ArrayList<IToolbarButton> = ArrayList()

    constructor(context: Context) : super(context) {
        initView(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(attrs)
    }

    override fun setToolbarListener(listener: IAztecToolbarClickListener) {
        aztecToolbarListener = listener
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_1 -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Heading 1 = Alt + Ctrl + 1
                    aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_HEADING_1, true)
                    editor?.toggleFormatting(AztecTextFormat.FORMAT_HEADING_1)
                    return true
                }
            }
            KeyEvent.KEYCODE_2 -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Heading 2 = Alt + Ctrl + 2
                    aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_HEADING_2, true)
                    editor?.toggleFormatting(AztecTextFormat.FORMAT_HEADING_2)
                    return true
                }
            }
            KeyEvent.KEYCODE_3 -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Heading 3 = Alt + Ctrl + 3
                    aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_HEADING_3, true)
                    editor?.toggleFormatting(AztecTextFormat.FORMAT_HEADING_3)
                    return true
                }
            }
            KeyEvent.KEYCODE_4 -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Heading 4 = Alt + Ctrl + 4
                    aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_HEADING_4, true)
                    editor?.toggleFormatting(AztecTextFormat.FORMAT_HEADING_4)
                    return true
                }
            }
            KeyEvent.KEYCODE_5 -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Heading 5 = Alt + Ctrl + 5
                    aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_HEADING_5, true)
                    editor?.toggleFormatting(AztecTextFormat.FORMAT_HEADING_5)
                    return true
                }
            }
            KeyEvent.KEYCODE_6 -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Heading 6 = Alt + Ctrl + 6
                    aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_HEADING_6, true)
                    editor?.toggleFormatting(AztecTextFormat.FORMAT_HEADING_6)
                    return true
                }
            }
            KeyEvent.KEYCODE_7 -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Heading 6 = Alt + Ctrl + 7
                    aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_PARAGRAPH, true)
                    editor?.toggleFormatting(AztecTextFormat.FORMAT_PARAGRAPH)
                    return true
                }
            }
            KeyEvent.KEYCODE_8 -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Preformat = Alt + Ctrl + 8
                    aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_PREFORMAT, true)
                    editor?.toggleFormatting(AztecTextFormat.FORMAT_PREFORMAT)
                    return true
                }
            }
            KeyEvent.KEYCODE_B -> {
                if (event.isCtrlPressed) { // Bold = Ctrl + B
                    aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_STRONG, true)
                    findViewById<ToggleButton>(ToolbarAction.BOLD.buttonId).performClick()
                    return true
                }
            }
            KeyEvent.KEYCODE_D -> {
                if (event.isCtrlPressed) { // Strikethrough = Ctrl + D
                    aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_STRIKETHROUGH, true)
                    findViewById<ToggleButton>(ToolbarAction.STRIKETHROUGH.buttonId).performClick()
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
                    aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_EMPHASIS, true)
                    findViewById<ToggleButton>(ToolbarAction.ITALIC.buttonId).performClick()
                    return true
                }
            }
            KeyEvent.KEYCODE_K -> {
                if (event.isCtrlPressed) { // Link = Ctrl + K
                    aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_LINK, true)
                    findViewById<ToggleButton>(ToolbarAction.LINK.buttonId).performClick()
                    return true
                }
            }
            KeyEvent.KEYCODE_M -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Media = Alt + Ctrl + M
                    if (aztecToolbarListener != null && aztecToolbarListener!!.onToolbarMediaButtonClicked()) {
                        //event is consumed by listener
                    } else {
                        val mediaAction = if (isMediaToolbarVisible) ToolbarAction.ADD_MEDIA_EXPAND else ToolbarAction.ADD_MEDIA_COLLAPSE
                        findViewById<ToggleButton>(mediaAction.buttonId).performClick()
                    }
                    return true
                }
            }
            KeyEvent.KEYCODE_O -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Ordered List = Alt + Ctrl + O
                    aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_ORDERED_LIST, true)
                    editor?.toggleFormatting(AztecTextFormat.FORMAT_ORDERED_LIST)
                    return true
                }
            }
            KeyEvent.KEYCODE_Q -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Quote = Alt + Ctrl + Q
                    aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_QUOTE, true)
                    findViewById<ToggleButton>(ToolbarAction.QUOTE.buttonId).performClick()
                    return true
                }
            }
            KeyEvent.KEYCODE_U -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Unordered List = Alt + Ctrl + U
                    aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_UNORDERED_LIST, true)
                    editor?.toggleFormatting(AztecTextFormat.FORMAT_UNORDERED_LIST)
                    return true
                } else if (event.isCtrlPressed) { // Underline = Ctrl + U
                    aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_UNDERLINE, true)
                    findViewById<ToggleButton>(ToolbarAction.UNDERLINE.buttonId).performClick()
                    return true
                }
            }
            KeyEvent.KEYCODE_X -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Code = Alt + Ctrl + X
//                    TODO: Add Code action.
//                    findViewById<ToggleButton>(ToolbarAction.CODE.buttonId).performClick()
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
            else -> {
                toolbarButtonPlugins.forEach {
                    if (it.matchesKeyShortcut(keyCode, event)) {
                        aztecToolbarListener?.onToolbarFormatButtonClicked(it.action.textFormats.first(), true)
                        it.toggle()
                        return true
                    }
                }
            }
        }

        return false
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        val checked = (item?.isChecked == false)
        item?.isChecked = checked
        val headingButton = findViewById<ToggleButton>(ToolbarAction.HEADING.buttonId)

        when (item?.itemId) {
        // Heading Menu
            R.id.paragraph -> {
                aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_PARAGRAPH, false)
                editor?.toggleFormatting(AztecTextFormat.FORMAT_PARAGRAPH)
                updateHeadingMenuItem(AztecTextFormat.FORMAT_PARAGRAPH, headingButton)
                return true
            }
            R.id.heading_1 -> {
                aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_HEADING_1, false)
                editor?.toggleFormatting(AztecTextFormat.FORMAT_HEADING_1)
                updateHeadingMenuItem(AztecTextFormat.FORMAT_HEADING_1, headingButton)
                return true
            }
            R.id.heading_2 -> {
                aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_HEADING_2, false)
                editor?.toggleFormatting(AztecTextFormat.FORMAT_HEADING_2)
                updateHeadingMenuItem(AztecTextFormat.FORMAT_HEADING_2, headingButton)
                return true
            }
            R.id.heading_3 -> {
                aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_HEADING_3, false)
                editor?.toggleFormatting(AztecTextFormat.FORMAT_HEADING_3)
                updateHeadingMenuItem(AztecTextFormat.FORMAT_HEADING_3, headingButton)
                return true
            }
            R.id.heading_4 -> {
                aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_HEADING_4, false)
                editor?.toggleFormatting(AztecTextFormat.FORMAT_HEADING_4)
                updateHeadingMenuItem(AztecTextFormat.FORMAT_HEADING_4, headingButton)
                return true
            }
            R.id.heading_5 -> {
                aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_HEADING_5, false)
                editor?.toggleFormatting(AztecTextFormat.FORMAT_HEADING_5)
                updateHeadingMenuItem(AztecTextFormat.FORMAT_HEADING_5, headingButton)
                return true
            }
            R.id.heading_6 -> {
                aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_HEADING_6, false)
                editor?.toggleFormatting(AztecTextFormat.FORMAT_HEADING_6)
                updateHeadingMenuItem(AztecTextFormat.FORMAT_HEADING_6, headingButton)
                return true
            }
//            TODO: Uncomment when Preformat is to be added back as a feature
//            R.id.preformat -> {
//                aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_PREFORMAT, false)
//                editor?.toggleFormatting(AztecTextFormat.FORMAT_PREFORMAT)
//                return true
//            }
        // List Menu
            R.id.list_ordered -> {
                aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_ORDERED_LIST, false)
                editor?.toggleFormatting(AztecTextFormat.FORMAT_ORDERED_LIST)
                toggleListMenuSelection(item.itemId, checked)

                editor?.let {
                    highlightAppliedStyles(editor!!.selectionStart, editor!!.selectionEnd)
                }
                return true
            }
            R.id.list_unordered -> {
                aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_UNORDERED_LIST, false)
                editor?.toggleFormatting(AztecTextFormat.FORMAT_UNORDERED_LIST)
                toggleListMenuSelection(item.itemId, checked)

                editor?.let {
                    highlightAppliedStyles(editor!!.selectionStart, editor!!.selectionEnd)
                }
                return true
            }
            else -> return false
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as SourceViewEditText.SavedState
        super.onRestoreInstanceState(savedState.superState)
        val restoredState = savedState.state
        toggleHtmlMode(restoredState.getBoolean("isSourceVisible"))
        enableMediaMode(restoredState.getBoolean("isMediaMode"))
        isExpanded = restoredState.getBoolean("isExpanded")
        isMediaToolbarVisible = restoredState.getBoolean("isMediaToolbarVisible")
        setAdvancedState()
        setupMediaToolbar()
        editorContentParsedSHA256LastSwitch = restoredState.getByteArray(RETAINED_EDITOR_HTML_PARSED_SHA256_KEY)
        sourceContentParsedSHA256LastSwitch = restoredState.getByteArray(RETAINED_SOURCE_HTML_PARSED_SHA256_KEY)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = SourceViewEditText.SavedState(superState)
        val bundle = Bundle()
        bundle.putBoolean("isSourceVisible", sourceEditor?.visibility == View.VISIBLE)
        bundle.putBoolean("isMediaMode", isMediaModeEnabled)
        bundle.putBoolean("isExpanded", isExpanded)
        bundle.putBoolean("isMediaToolbarVisible", isMediaToolbarVisible)
        bundle.putByteArray(RETAINED_EDITOR_HTML_PARSED_SHA256_KEY, editorContentParsedSHA256LastSwitch)
        bundle.putByteArray(RETAINED_SOURCE_HTML_PARSED_SHA256_KEY, sourceContentParsedSHA256LastSwitch)
        savedState.state = bundle
        return savedState
    }

    private fun isEditorAttached(): Boolean {
        return editor != null && editor is AztecText
    }

    override fun setEditor(editor: AztecText, sourceEditor: SourceViewEditText?) {
        this.sourceEditor = sourceEditor
        this.editor = editor

        // highlight toolbar buttons based on what styles are applied to the text beneath cursor
        this.editor!!.setOnSelectionChangedListener(object : AztecText.OnSelectionChangedListener {
            override fun onSelectionChanged(selStart: Int, selEnd: Int) {
                highlightAppliedStyles(selStart, selEnd)
            }
        })

        if (sourceEditor == null) {
            htmlButton.visibility = View.GONE
        } else {
            htmlButton.visibility = View.VISIBLE
        }
    }

    private fun initView(attrs: AttributeSet?) {
        val styles = context.obtainStyledAttributes(attrs, R.styleable.AztecToolbar, 0, R.style.AztecToolbarStyle)
        isAdvanced = styles.getBoolean(R.styleable.AztecToolbar_advanced, false)
        isMediaToolbarAvailable = styles.getBoolean(R.styleable.AztecToolbar_mediaToolbarAvailable, true)

        val toolbarBackgroundColor = styles.getColor(
                R.styleable.AztecToolbar_toolbarBackgroundColor,
                ContextCompat.getColor(context, R.color.format_bar_background)
        )
        val toolbarBorderColor = styles.getColor(R.styleable.AztecToolbar_toolbarBorderColor,
                ContextCompat.getColor(context, R.color.format_bar_divider_horizontal))

        val layout = when {
            styles.hasValue(R.styleable.AztecToolbar_customLayout) -> {
                hasCustomLayout = true
                styles.getResourceId(R.styleable.AztecToolbar_customLayout, 0)
            }
            isAdvanced -> R.layout.aztec_format_bar_advanced
            else -> R.layout.aztec_format_bar_basic
        }

        styles.recycle()
        View.inflate(context, layout, this)

        toolbarScrolView = findViewById(R.id.format_bar_button_scroll)
        htmlButton = findViewById(R.id.format_bar_button_html)
        setBackgroundColor(toolbarBackgroundColor)
        findViewById<View>(R.id.format_bar_horizontal_divider)?.setBackgroundColor(toolbarBorderColor)

        setAdvancedState()
        setupMediaToolbar()
        setupToolbarButtonsForAccessibility()

        for (toolbarAction in ToolbarAction.values()) {
            findViewById<ToggleButton>(toolbarAction.buttonId)?.let {
                it.setOnClickListener { onToolbarAction(toolbarAction) }

                when (toolbarAction) {
                    ToolbarAction.HEADING -> setHeadingMenu(it)
                    ToolbarAction.LIST -> setListMenu(it)
                }
                if (!hasCustomLayout) {
                    it.setBackgroundDrawableRes(toolbarAction.buttonDrawableRes)
                }
            }
        }
    }

    override fun addButton(buttonPlugin: IToolbarButton) {
        val pluginContainer = if (buttonPlugin is IMediaToolbarButton) {
            findViewById(R.id.media_toolbar)
        } else {
            findViewById<LinearLayout>(R.id.plugin_buttons)
        }

        buttonPlugin.inflateButton(pluginContainer)

        toolbarButtonPlugins.add(buttonPlugin)

        val button = findViewById<ToggleButton>(buttonPlugin.action.buttonId)
        button.setOnClickListener { buttonPlugin.toggle() }
        button.setBackgroundDrawableRes(buttonPlugin.action.buttonDrawableRes)

        setupMediaButtonForAccessibility(buttonPlugin)
    }

    private fun setupMediaButtonForAccessibility(buttonPlugin: IToolbarButton) {
        val button = findViewById<ToggleButton>(buttonPlugin.action.buttonId)

        if (buttonPlugin is IMediaToolbarButton) {
            button.convertToButtonAccessibilityProperties()
        }
    }

    private fun setupToolbarButtonsForAccessibility() {
        val targetActions = listOf(ToolbarAction.ADD_MEDIA_EXPAND,
                ToolbarAction.ADD_MEDIA_COLLAPSE,
                ToolbarAction.HORIZONTAL_RULE,
                ToolbarAction.HEADING,
                ToolbarAction.LIST,
                ToolbarAction.LINK
        )

        ToolbarAction.values().forEach { action ->
            if (targetActions.contains(action)) {
                findViewById<ToggleButton>(action.buttonId)?.convertToButtonAccessibilityProperties()
            }
        }
    }

    fun highlightActionButtons(toolbarActions: ArrayList<IToolbarAction>) {
        ToolbarAction.values().forEach { action ->
            if (toolbarActions.contains(action)) {
                toggleButton(findViewById<ToggleButton>(action.buttonId), true)
            } else {
                toggleButton(findViewById<ToggleButton>(action.buttonId), false)
            }
        }
    }

    private fun getSelectedActions(): ArrayList<IToolbarAction> {
        val actions = ArrayList<IToolbarAction>()

        for (action in ToolbarAction.values()) {
            if (action != ToolbarAction.ELLIPSIS_COLLAPSE &&
                    action != ToolbarAction.ELLIPSIS_EXPAND) {
                val view = findViewById<ToggleButton>(action.buttonId)
                if (view?.isChecked == true) actions.add(action)
            }
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
        selectHeadingMenuItem(appliedStyles)
        selectListMenuItem(appliedStyles)
        highlightAlignButtons(appliedStyles)
    }

    private fun highlightAlignButtons(appliedStyles: ArrayList<ITextFormat>) {
        if (!appliedStyles.contains(AztecTextFormat.FORMAT_ALIGN_LEFT)) {
            toggleButton(findViewById(ToolbarAction.ALIGN_LEFT.buttonId), false)
        }
        if (!appliedStyles.contains(AztecTextFormat.FORMAT_ALIGN_CENTER)) {
            toggleButton(findViewById(ToolbarAction.ALIGN_CENTER.buttonId), false)
        }
        if (!appliedStyles.contains(AztecTextFormat.FORMAT_ALIGN_RIGHT)) {
            toggleButton(findViewById(ToolbarAction.ALIGN_RIGHT.buttonId), false)
        }
    }

    private fun onToolbarAction(action: IToolbarAction) {
        if (!isEditorAttached()) return

        // if nothing is selected just mark the style as active
        if (!editor!!.isTextSelected() && action.actionType == ToolbarActionType.INLINE_STYLE) {
            val actions = getSelectedActions()
            val textFormats = ArrayList<ITextFormat>()

            actions.filter { it.isStylingAction() }
                    .forEach { textFormats.add(it.textFormats.first()) }

            if (getSelectedHeadingMenuItem() != null) {
                textFormats.add(getSelectedHeadingMenuItem()!!)
            }

            if (getSelectedListMenuItem() != null) {
                textFormats.add(getSelectedListMenuItem()!!)
            }

            aztecToolbarListener?.onToolbarFormatButtonClicked(action.textFormats.first(), false)
            return editor!!.setSelectedStyles(textFormats)
        }

        // if text is selected and action is styling - toggle the style
        if (action.isStylingAction() && action != ToolbarAction.HEADING && action != ToolbarAction.LIST) {
            aztecToolbarListener?.onToolbarFormatButtonClicked(action.textFormats.first(), false)
            val returnValue = editor!!.toggleFormatting(action.textFormats.first())

            highlightAppliedStyles()

            return returnValue
        }

        // other toolbar action
        when (action) {
            ToolbarAction.ADD_MEDIA_COLLAPSE, ToolbarAction.ADD_MEDIA_EXPAND -> {
                if (aztecToolbarListener != null && aztecToolbarListener!!.onToolbarMediaButtonClicked()) {
                    //event is consumed by listener
                } else {
                    toggleMediaToolbar()
                }
            }
            ToolbarAction.HEADING -> {
                aztecToolbarListener?.onToolbarHeadingButtonClicked()
                headingMenu?.show()
            }
            ToolbarAction.LIST -> {
                aztecToolbarListener?.onToolbarListButtonClicked()
                listMenu?.show()
            }
            ToolbarAction.LINK -> {
                aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_LINK, false)
                editor!!.showLinkDialog()
            }
            ToolbarAction.HTML -> {
                aztecToolbarListener?.onToolbarHtmlButtonClicked()
            }
            ToolbarAction.ELLIPSIS_COLLAPSE -> {
                aztecToolbarListener?.onToolbarCollapseButtonClicked()
                animateToolbarCollapse()
            }
            ToolbarAction.ELLIPSIS_EXPAND -> {
                aztecToolbarListener?.onToolbarExpandButtonClicked()
                animateToolbarExpand()
            }
            else -> {
                Toast.makeText(context, "Unsupported action", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun highlightAppliedStyles() {
        editor?.let {
            highlightAppliedStyles(editor!!.selectionStart, editor!!.selectionEnd)
        }
    }

    private fun syncSourceFromEditor() {
        val editorHtml = editor!!.toPlainHtml(true)
        val sha256 = AztecText.calculateSHA256(editorHtml)

        if (editorContentParsedSHA256LastSwitch.isEmpty()) {
            // initialize the var if it's the first time we're about to use it
            editorContentParsedSHA256LastSwitch = sha256
        }

        if (editor!!.hasChanges() != NO_CHANGES || !Arrays.equals(editorContentParsedSHA256LastSwitch, sha256)) {
            sourceEditor!!.displayStyledAndFormattedHtml(editorHtml)
        }
        editorContentParsedSHA256LastSwitch = sha256
    }

    private fun syncEditorFromSource() {
        // temp var of the source html to load it to the editor if needed
        val sourceHtml = sourceEditor!!.getPureHtml(true)
        val sha256 = AztecText.calculateSHA256(sourceHtml)

        if (sourceContentParsedSHA256LastSwitch.isEmpty()) {
            // initialize the var if it's the first time we're about to use it
            sourceContentParsedSHA256LastSwitch = sha256
        }

        if (sourceEditor!!.hasChanges() != NO_CHANGES || !Arrays.equals(sourceContentParsedSHA256LastSwitch, sha256)) {
            editor!!.fromHtml(sourceHtml)
        }
        sourceContentParsedSHA256LastSwitch = sha256
    }

    override fun toggleEditorMode() {
        // only allow toggling if sourceEditor is present
        if (sourceEditor == null) return

        if (editor!!.visibility == View.VISIBLE) {
            syncSourceFromEditor()
            editor!!.visibility = View.GONE
            sourceEditor!!.visibility = View.VISIBLE

            toggleHtmlMode(true)
        } else {
            syncEditorFromSource()
            editor!!.visibility = View.VISIBLE
            sourceEditor!!.visibility = View.GONE

            toggleHtmlMode(false)
        }
    }

    fun getHeadingMenu(): PopupMenu? {
        return headingMenu
    }

    fun getListMenu(): PopupMenu? {
        return listMenu
    }

    fun getSelectedHeadingMenuItem(): ITextFormat? = when {
        headingMenu?.menu?.findItem(R.id.paragraph)?.isChecked == true -> AztecTextFormat.FORMAT_PARAGRAPH
        headingMenu?.menu?.findItem(R.id.heading_1)?.isChecked == true -> AztecTextFormat.FORMAT_HEADING_1
        headingMenu?.menu?.findItem(R.id.heading_2)?.isChecked == true -> AztecTextFormat.FORMAT_HEADING_2
        headingMenu?.menu?.findItem(R.id.heading_3)?.isChecked == true -> AztecTextFormat.FORMAT_HEADING_3
        headingMenu?.menu?.findItem(R.id.heading_4)?.isChecked == true -> AztecTextFormat.FORMAT_HEADING_4
        headingMenu?.menu?.findItem(R.id.heading_5)?.isChecked == true -> AztecTextFormat.FORMAT_HEADING_5
        headingMenu?.menu?.findItem(R.id.heading_6)?.isChecked == true -> AztecTextFormat.FORMAT_HEADING_6
        //        TODO: Uncomment when Preformat is to be added back as a feature
        //        else if (headingMenu?.menu?.findItem(R.id.preformat)?.isChecked!!) return AztecTextFormat.FORMAT_PREFORMAT
        else -> null
    }

    fun getSelectedListMenuItem(): ITextFormat? {
        if (listMenu?.menu?.findItem(R.id.list_unordered)?.isChecked == true) return AztecTextFormat.FORMAT_UNORDERED_LIST
        else if (listMenu?.menu?.findItem(R.id.list_ordered)?.isChecked == true) return AztecTextFormat.FORMAT_ORDERED_LIST
        return null
    }

    fun setExpanded(expanded: Boolean) {
        isExpanded = expanded
        setAdvancedState()
    }

    private fun animateToolbarCollapse() {
        buttonEllipsisCollapsed.startAnimation(ellipsisSpinLeft)
        isExpanded = false
    }

    private fun animateToolbarExpand() {
        buttonEllipsisExpanded.startAnimation(ellipsisSpinRight)
        isExpanded = true
    }

    private fun setAdvancedState() {
        if (isAdvanced) {
            setButtonViews()
            setAnimations()

            if (isExpanded) {
                showExpandedToolbar()
            } else {
                showCollapsedToolbar()
            }
        }
    }

    private fun selectHeadingMenuItem(textFormats: ArrayList<ITextFormat>) {
        val headingButton = findViewById<ToggleButton>(ToolbarAction.HEADING.buttonId) ?: return
        // Use unnumbered heading selector by default.
        updateHeadingMenuItem(AztecTextFormat.FORMAT_PARAGRAPH, headingButton)
        headingMenu?.menu?.findItem(R.id.paragraph)?.isChecked = true
        foreach@ for (it in textFormats) {
            when (it) {
                AztecTextFormat.FORMAT_HEADING_1 -> headingMenu?.menu?.findItem(R.id.heading_1)?.isChecked = true
                AztecTextFormat.FORMAT_HEADING_2 -> headingMenu?.menu?.findItem(R.id.heading_2)?.isChecked = true
                AztecTextFormat.FORMAT_HEADING_3 -> headingMenu?.menu?.findItem(R.id.heading_3)?.isChecked = true
                AztecTextFormat.FORMAT_HEADING_4 -> headingMenu?.menu?.findItem(R.id.heading_4)?.isChecked = true
                AztecTextFormat.FORMAT_HEADING_5 -> headingMenu?.menu?.findItem(R.id.heading_5)?.isChecked = true
                AztecTextFormat.FORMAT_HEADING_6 -> headingMenu?.menu?.findItem(R.id.heading_6)?.isChecked = true
            //                    TODO: Uncomment when Preformat is to be added back as a feature
            //                    AztecTextFormat.FORMAT_PREFORMAT -> headingMenu?.menu?.findItem(R.id.preformat)?.isChecked = true
                else -> continue@foreach
            }

            updateHeadingMenuItem(it, headingButton)
        }
    }

    private fun selectListMenuItem(textFormats: ArrayList<ITextFormat>) {
        val listButton = findViewById<ToggleButton>(ToolbarAction.LIST.buttonId) ?: return
        updateListMenuItem(AztecTextFormat.FORMAT_NONE, listButton)
        listMenu?.menu?.findItem(R.id.list_none)?.isChecked = true
        foreach@ for (it in textFormats) {
            when (it) {
                AztecTextFormat.FORMAT_UNORDERED_LIST -> listMenu?.menu?.findItem(R.id.list_unordered)?.isChecked = true
                AztecTextFormat.FORMAT_ORDERED_LIST -> listMenu?.menu?.findItem(R.id.list_ordered)?.isChecked = true
                else -> continue@foreach
            }
            updateListMenuItem(it, listButton)

        }
    }

    private fun setAnimations() {
        layoutExpandedTranslateInEnd = AnimationUtils.loadAnimation(context, R.anim.translate_in_end)

        layoutExpandedTranslateOutStart = AnimationUtils.loadAnimation(context, R.anim.translate_out_start)
        layoutExpandedTranslateOutStart.setAnimationListener(
                object : Animation.AnimationListener {
                    override fun onAnimationEnd(animation: Animation) {
                        layoutExpanded.visibility = View.GONE
                    }

                    override fun onAnimationRepeat(animation: Animation) {
                    }

                    override fun onAnimationStart(animation: Animation) {
                    }
                }
        )

        ellipsisSpinLeft = AnimationUtils.loadAnimation(context, R.anim.spin_left_90)
        ellipsisSpinLeft.setAnimationListener(
                object : Animation.AnimationListener {
                    override fun onAnimationEnd(animation: Animation) {
                        buttonEllipsisCollapsed.visibility = View.GONE
                        buttonEllipsisExpanded.visibility = View.VISIBLE
                    }

                    override fun onAnimationRepeat(animation: Animation) {
                    }

                    override fun onAnimationStart(animation: Animation) {
                        scrollToBeginingOfToolbar()
                        layoutExpanded.startAnimation(layoutExpandedTranslateOutStart)
                    }
                }
        )

        ellipsisSpinRight = AnimationUtils.loadAnimation(context, R.anim.spin_right_90)
        ellipsisSpinRight.setAnimationListener(
                object : Animation.AnimationListener {
                    override fun onAnimationEnd(animation: Animation) {
                        buttonEllipsisCollapsed.visibility = View.VISIBLE
                        buttonEllipsisExpanded.visibility = View.GONE
                    }

                    override fun onAnimationRepeat(animation: Animation) {
                    }

                    override fun onAnimationStart(animation: Animation) {
                        layoutExpanded.visibility = View.VISIBLE
                        //in rtl mode the scrollview will scroll to "end" when layoutExpanded becomes visible
                        //keep hard focus on media button to avoid it
                        toolbarScrolView.requestChildFocus(buttonMediaCollapsed, buttonMediaCollapsed)
                        layoutExpanded.startAnimation(layoutExpandedTranslateInEnd)
                    }
                }
        )
    }

    //HorizontalScrollView does not support RTL layout direction on API <= 18, so we will always scroll to the left
    fun scrollToBeginingOfToolbar() {
        if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_LTR
                || Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            toolbarScrolView.fullScroll(View.FOCUS_LEFT)
        } else {
            toolbarScrolView.fullScroll(View.FOCUS_RIGHT)
        }
    }

    private fun setButtonViews() {
        layoutExpanded = findViewById(R.id.format_bar_button_layout_expanded)
        buttonEllipsisCollapsed = findViewById(R.id.format_bar_button_ellipsis_collapsed)
        buttonEllipsisExpanded = findViewById(R.id.format_bar_button_ellipsis_expanded)
    }

    private fun setupMediaToolbar() {
        if (!isMediaToolbarAvailable) return
        val mediaToolbarContainer: LinearLayout = findViewById(R.id.media_button_container)
        mediaToolbarContainer.visibility = if (isMediaToolbarAvailable) View.VISIBLE else View.GONE
        buttonMediaCollapsed = findViewById(R.id.format_bar_button_media_collapsed)

        mediaToolbar = findViewById(R.id.media_toolbar)
        stylingToolbar = findViewById(R.id.styling_toolbar)

        buttonMediaExpanded = findViewById(R.id.format_bar_button_media_expanded)

        if (isMediaToolbarVisible) {
            buttonMediaExpanded.visibility = View.VISIBLE
            buttonMediaCollapsed.visibility = View.GONE
            stylingToolbar.visibility = View.GONE
            mediaToolbar.visibility = View.VISIBLE
        } else {
            buttonMediaExpanded.visibility = View.GONE
            buttonMediaCollapsed.visibility = View.VISIBLE
            stylingToolbar.visibility = View.VISIBLE
            mediaToolbar.visibility = View.GONE
        }

        setupMediaToolbarAnimations()
    }

    private fun setupMediaToolbarAnimations() {
        if (!isMediaToolbarAvailable) return
        layoutMediaTranslateInEnd = AnimationUtils.loadAnimation(context, R.anim.translate_in_end)

        layoutMediaTranslateOutEnd = AnimationUtils.loadAnimation(context, R.anim.translate_out_end)
        layoutMediaTranslateOutEnd.setAnimationListener(
                object : Animation.AnimationListener {
                    override fun onAnimationEnd(animation: Animation) {
                        stylingToolbar.visibility = View.GONE
                    }

                    override fun onAnimationRepeat(animation: Animation) {
                    }

                    override fun onAnimationStart(animation: Animation) {
                    }
                }
        )

        layoutMediaTranslateInStart = AnimationUtils.loadAnimation(context, R.anim.translate_in_start)
        layoutMediaTranslateInStart.setAnimationListener(
                object : Animation.AnimationListener {
                    override fun onAnimationEnd(animation: Animation) {

                    }

                    override fun onAnimationRepeat(animation: Animation) {
                    }

                    override fun onAnimationStart(animation: Animation) {
                        stylingToolbar.visibility = View.VISIBLE
                        //in rtl mode the scrollview will scroll to "end" when stylingToolbar becomes visible
                        //keep hard focus on media button to avoid it
                        toolbarScrolView.requestChildFocus(buttonMediaCollapsed, buttonMediaCollapsed)
                    }
                }
        )

        layoutMediaTranslateOutStart = AnimationUtils.loadAnimation(context, R.anim.translate_out_start)
        layoutMediaTranslateOutStart.setAnimationListener(
                object : Animation.AnimationListener {
                    override fun onAnimationEnd(animation: Animation) {
                        mediaToolbar.visibility = View.GONE
                    }

                    override fun onAnimationRepeat(animation: Animation) {
                    }

                    override fun onAnimationStart(animation: Animation) {
                    }
                }
        )

        mediaButtonSpinRight = AnimationUtils.loadAnimation(context, R.anim.spin_right_45)
        mediaButtonSpinRight.setAnimationListener(
                object : Animation.AnimationListener {
                    override fun onAnimationEnd(animation: Animation) {
                        buttonMediaCollapsed.visibility = View.GONE
                        buttonMediaExpanded.visibility = View.VISIBLE
                        buttonMediaExpanded.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                        buttonMediaExpanded.isChecked = true
                    }

                    override fun onAnimationRepeat(animation: Animation) {
                    }

                    override fun onAnimationStart(animation: Animation) {
                        scrollToBeginingOfToolbar()
                    }
                }
        )

        mediaButtonSpinLeft = AnimationUtils.loadAnimation(context, R.anim.spin_left_45)
        mediaButtonSpinLeft.setAnimationListener(
                object : Animation.AnimationListener {
                    override fun onAnimationEnd(animation: Animation) {
                        buttonMediaCollapsed.visibility = View.VISIBLE
                        buttonMediaExpanded.visibility = View.GONE
                        buttonMediaCollapsed.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                        buttonMediaCollapsed.isChecked = false
                    }

                    override fun onAnimationRepeat(animation: Animation) {
                    }

                    override fun onAnimationStart(animation: Animation) {
                    }
                }
        )
    }

    private fun setHeadingMenu(view: View) {
        headingMenu = PopupMenu(context, view)
        headingMenu?.setOnMenuItemClickListener(this)
        headingMenu?.inflate(R.menu.heading)
        headingMenu?.setOnDismissListener({
            if (getSelectedHeadingMenuItem() == null || getSelectedHeadingMenuItem() == AztecTextFormat.FORMAT_PARAGRAPH) {
                findViewById<ToggleButton>(ToolbarAction.HEADING.buttonId).isChecked = false
            } else {
                findViewById<ToggleButton>(ToolbarAction.HEADING.buttonId).isChecked = true
            }
        })
    }

    private fun setListMenu(view: View) {
        listMenu = PopupMenu(context, view)
        listMenu?.setOnMenuItemClickListener(this)
        listMenu?.inflate(R.menu.list)
        listMenu?.setOnDismissListener({
            if (getSelectedListMenuItem() == null) {
                findViewById<ToggleButton>(ToolbarAction.LIST.buttonId).isChecked = false
            } else {
                findViewById<ToggleButton>(ToolbarAction.LIST.buttonId).isChecked = true
            }
        })
    }

    private fun updateListMenuItem(textFormat: ITextFormat, listButton: ToggleButton) {
        var backgroundRes = R.drawable.format_bar_button_ul_selector
        var contentDescriptionRes = R.string.format_bar_description_list
        var check = true
        when (textFormat) {
            AztecTextFormat.FORMAT_ORDERED_LIST -> {
                backgroundRes = R.drawable.format_bar_button_ol_selector
                contentDescriptionRes = R.string.item_format_list_ordered
            }
            AztecTextFormat.FORMAT_UNORDERED_LIST -> {
                contentDescriptionRes = R.string.item_format_list_unordered
                // keep default background
            }
            AztecTextFormat.FORMAT_NONE -> {
                check = false
                // keep default background and content description
            }
            else -> {
                AppLog.w(AppLog.T.EDITOR, "Unknown list menu item - text format")
                return
            }
        }
        listButton.setBackgroundDrawableRes(backgroundRes)
        listButton.contentDescription = context.getString(contentDescriptionRes)
        listButton.isChecked = check
    }

    private fun updateHeadingMenuItem(textFormat: ITextFormat, headingButton: ToggleButton) {
        var backgroundRes = R.drawable.format_bar_button_heading_selector
        var contentDescriptionRes = R.string.format_bar_description_heading
        var check = true
        when (textFormat) {
            AztecTextFormat.FORMAT_HEADING_1 -> {
                backgroundRes = R.drawable.format_bar_button_heading_1_selector
                contentDescriptionRes = R.string.heading_1
            }
            AztecTextFormat.FORMAT_HEADING_2 -> {
                backgroundRes = R.drawable.format_bar_button_heading_2_selector
                contentDescriptionRes = R.string.heading_2
            }
            AztecTextFormat.FORMAT_HEADING_3 -> {
                backgroundRes = R.drawable.format_bar_button_heading_3_selector
                contentDescriptionRes = R.string.heading_3
            }
            AztecTextFormat.FORMAT_HEADING_4 -> {
                backgroundRes = R.drawable.format_bar_button_heading_4_selector
                contentDescriptionRes = R.string.heading_4
            }
            AztecTextFormat.FORMAT_HEADING_5 -> {
                backgroundRes = R.drawable.format_bar_button_heading_5_selector
                contentDescriptionRes = R.string.heading_5
            }
            AztecTextFormat.FORMAT_HEADING_6 -> {
                backgroundRes = R.drawable.format_bar_button_heading_6_selector
                contentDescriptionRes = R.string.heading_6
            }
            AztecTextFormat.FORMAT_PARAGRAPH -> {
                // keep default background and contentDescription
                check = false
            }
            else -> {
                AppLog.w(AppLog.T.EDITOR, "Unknown heading menu item - text format")
                return
            }
        }
        headingButton.setBackgroundDrawableRes(backgroundRes)
        headingButton.contentDescription = context.getString(contentDescriptionRes)
        headingButton.isChecked = check
    }

    private fun showCollapsedToolbar() {
        layoutExpanded.visibility = View.GONE
        buttonEllipsisCollapsed.visibility = View.GONE
        buttonEllipsisExpanded.visibility = View.VISIBLE
    }

    private fun showExpandedToolbar() {
        layoutExpanded.visibility = View.VISIBLE
        buttonEllipsisCollapsed.visibility = View.VISIBLE
        buttonEllipsisExpanded.visibility = View.GONE
    }

    private fun toggleHtmlMode(isHtmlMode: Boolean) {
        ToolbarAction.values().forEach { action ->
            if (action == ToolbarAction.HTML) {
                toggleButton(findViewById(action.buttonId), isHtmlMode)
            } else {
                toggleButtonState(findViewById(action.buttonId), !isHtmlMode)
            }
        }

        toolbarButtonPlugins.forEach {
            toggleButtonState(findViewById(it.action.buttonId), !isHtmlMode)
        }
    }

    private fun toggleListMenuSelection(listMenuItemId: Int, isChecked: Boolean) {
        val listButton = findViewById<ToggleButton>(ToolbarAction.LIST.buttonId)
        if (isChecked) {
            listMenu?.menu?.findItem(listMenuItemId)?.isChecked = true

            when (listMenuItemId) {
                R.id.list_ordered -> updateListMenuItem(AztecTextFormat.FORMAT_ORDERED_LIST, listButton)
                R.id.list_unordered -> updateListMenuItem(AztecTextFormat.FORMAT_UNORDERED_LIST, listButton)
                else -> {
                    AppLog.w(AppLog.T.EDITOR, "Unknown list menu item")
                    updateListMenuItem(AztecTextFormat.FORMAT_UNORDERED_LIST, listButton) // Use unordered list selector by default.
                }
            }
        } else {
            listMenu?.menu?.findItem(R.id.list_none)?.isChecked = true

            updateListMenuItem(AztecTextFormat.FORMAT_NONE, listButton)
        }
    }

    fun enableFormatButtons(isEnabled: Boolean) {
        ToolbarAction.values().forEach { action ->
            if (action != ToolbarAction.HTML) {
                toggleButtonState(findViewById(action.buttonId), isEnabled)
            }
        }

        toolbarButtonPlugins.forEach {
            toggleButtonState(findViewById(it.action.buttonId), isEnabled)
        }
    }

    fun isMediaModeEnabled(): Boolean {
        return isMediaModeEnabled
    }

    fun enableMediaMode(isEnabled: Boolean) {
        isMediaModeEnabled = isEnabled
        toolbarButtonPlugins.forEach { button -> if (button !is IMediaToolbarButton) button.toolbarStateAboutToChange(this, !isEnabled) }
    }

    @SuppressLint("InflateParams")
    private fun showDialogShortcuts() {
        val layout = LayoutInflater.from(context).inflate(R.layout.dialog_shortcuts, null)
        val builder = AlertDialog.Builder(context)
        builder.setView(layout)
        dialogShortcuts = builder.create()
        dialogShortcuts!!.show()
    }

    fun hideMediaToolbar() {
        if (!isMediaToolbarVisible) return

        buttonMediaExpanded.startAnimation(mediaButtonSpinLeft)
        stylingToolbar.startAnimation(layoutMediaTranslateInStart)
        mediaToolbar.startAnimation(layoutMediaTranslateOutStart)

        isMediaToolbarVisible = false
    }

    fun showMediaToolbar() {
        if (isMediaToolbarVisible) return

        buttonMediaCollapsed.startAnimation(mediaButtonSpinRight)
        stylingToolbar.startAnimation(layoutMediaTranslateOutEnd)

        mediaToolbar.visibility = View.VISIBLE
        mediaToolbar.startAnimation(layoutMediaTranslateInEnd)

        isMediaToolbarVisible = true
    }

    override fun toggleMediaToolbar() {
        if (isMediaToolbarVisible) {
            hideMediaToolbar()
        } else {
            showMediaToolbar()
        }
    }

}
