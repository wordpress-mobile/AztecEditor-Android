package org.wordpress.aztec.toolbar

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import android.widget.PopupMenu.OnMenuItemClickListener
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.R
import org.wordpress.aztec.AztecTextFormat
import org.wordpress.aztec.plugins.IToolbarButton
import org.wordpress.aztec.source.SourceViewEditText
import java.util.*

class AztecToolbar : FrameLayout, OnMenuItemClickListener {
    private var aztecToolbarListener: IAztecToolbarClickListener? = null
    private var editor: AztecText? = null
    private var headingMenu: PopupMenu? = null
    private var listMenu: PopupMenu? = null
    private var sourceEditor: SourceViewEditText? = null
    private var dialogShortcuts: AlertDialog? = null
    private var isAdvanced: Boolean = false
    private var isExpanded: Boolean = false
    private var isMediaModeEnabled: Boolean = false

    private lateinit var buttonScroll: HorizontalScrollView
    private lateinit var buttonEllipsisCollapse: RippleToggleButton
    private lateinit var buttonEllipsisExpand: RippleToggleButton
    private lateinit var layoutExpandedTranslateInRight: Animation
    private lateinit var layoutExpandedTranslateOutLeft: Animation
    private lateinit var ellipsisSpinLeft: Animation
    private lateinit var ellipsisSpinRight: Animation
    private lateinit var layoutExpanded: LinearLayout

    var toolbarButtonPlugins: ArrayList<IToolbarButton> = ArrayList()

    constructor(context: Context) : super(context) {
        initView(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(attrs)
    }

    fun setToolbarListener(listener: IAztecToolbarClickListener) {
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
                    aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_BOLD, true)
                    findViewById(ToolbarAction.BOLD.buttonId).performClick()
                    return true
                }
            }
            KeyEvent.KEYCODE_D -> {
                if (event.isCtrlPressed) { // Strikethrough = Ctrl + D
                    aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_STRIKETHROUGH, true)
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
                    aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_ITALIC, true)
                    findViewById(ToolbarAction.ITALIC.buttonId).performClick()
                    return true
                }
            }
            KeyEvent.KEYCODE_K -> {
                if (event.isCtrlPressed) { // Link = Ctrl + K
                    aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_LINK, true)
                    findViewById(ToolbarAction.LINK.buttonId).performClick()
                    return true
                }
            }
            KeyEvent.KEYCODE_M -> {
                if (event.isAltPressed && event.isCtrlPressed) { // Media = Alt + Ctrl + M
                    aztecToolbarListener?.onToolbarMediaButtonClicked()
                    findViewById(ToolbarAction.ADD_MEDIA.buttonId).performClick()
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
                    findViewById(ToolbarAction.QUOTE.buttonId).performClick()
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
            else -> {
                toolbarButtonPlugins.forEach {
                    if (it.matchesKeyShortcut(keyCode, event)) {
                        aztecToolbarListener?.onToolbarFormatButtonClicked(it.action.textFormat, true)
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

        when (item?.itemId) {
        // Heading Menu
            R.id.paragraph -> {
                aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_PARAGRAPH, false)
                editor?.toggleFormatting(AztecTextFormat.FORMAT_PARAGRAPH)
                setHeadingMenuSelector(AztecTextFormat.FORMAT_PARAGRAPH)
                return true
            }
            R.id.heading_1 -> {
                aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_HEADING_1, false)
                editor?.toggleFormatting(AztecTextFormat.FORMAT_HEADING_1)
                setHeadingMenuSelector(AztecTextFormat.FORMAT_HEADING_1)
                return true
            }
            R.id.heading_2 -> {
                aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_HEADING_2, false)
                editor?.toggleFormatting(AztecTextFormat.FORMAT_HEADING_2)
                setHeadingMenuSelector(AztecTextFormat.FORMAT_HEADING_2)
                return true
            }
            R.id.heading_3 -> {
                aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_HEADING_3, false)
                editor?.toggleFormatting(AztecTextFormat.FORMAT_HEADING_3)
                setHeadingMenuSelector(AztecTextFormat.FORMAT_HEADING_3)
                return true
            }
            R.id.heading_4 -> {
                aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_HEADING_4, false)
                editor?.toggleFormatting(AztecTextFormat.FORMAT_HEADING_4)
                setHeadingMenuSelector(AztecTextFormat.FORMAT_HEADING_4)
                return true
            }
            R.id.heading_5 -> {
                aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_HEADING_5, false)
                editor?.toggleFormatting(AztecTextFormat.FORMAT_HEADING_5)
                setHeadingMenuSelector(AztecTextFormat.FORMAT_HEADING_5)
                return true
            }
            R.id.heading_6 -> {
                aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_HEADING_6, false)
                editor?.toggleFormatting(AztecTextFormat.FORMAT_HEADING_6)
                setHeadingMenuSelector(AztecTextFormat.FORMAT_HEADING_6)
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
                return true
            }
            R.id.list_unordered -> {
                aztecToolbarListener?.onToolbarFormatButtonClicked(AztecTextFormat.FORMAT_UNORDERED_LIST, false)
                editor?.toggleFormatting(AztecTextFormat.FORMAT_UNORDERED_LIST)
                toggleListMenuSelection(item.itemId, checked)
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
        setAdvancedState()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = SourceViewEditText.SavedState(superState)
        val bundle = Bundle()
        bundle.putBoolean("isSourceVisible", sourceEditor?.visibility == View.VISIBLE)
        bundle.putBoolean("isMediaMode", isMediaModeEnabled)
        bundle.putBoolean("isExpanded", isExpanded)
        savedState.state = bundle
        return savedState
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

    private fun initView(attrs: AttributeSet?) {
        val styles = context.obtainStyledAttributes(attrs, R.styleable.AztecToolbar, 0, R.style.AztecToolbarStyle)
        isAdvanced = styles.getBoolean(R.styleable.AztecToolbar_advanced, false)
        styles.recycle()

        val layout = if (isAdvanced) R.layout.aztec_format_bar_advanced else R.layout.aztec_format_bar_basic
        View.inflate(context, layout, this)
        setAdvancedState()

        for (toolbarAction in ToolbarAction.values()) {
            val button = findViewById(toolbarAction.buttonId)
            button?.setOnClickListener { onToolbarAction(toolbarAction) }

            if (toolbarAction == ToolbarAction.HEADING) {
                setHeadingMenu(findViewById(toolbarAction.buttonId))
            }

            if (toolbarAction == ToolbarAction.LIST) {
                setListMenu(findViewById(toolbarAction.buttonId))
            }
        }
    }

    fun addButton(buttonPlugin: IToolbarButton) {
        val pluginContainer = findViewById(R.id.plugin_buttons) as LinearLayout
        buttonPlugin.inflateButton(pluginContainer)

        toolbarButtonPlugins.add(buttonPlugin)
        
        val button = findViewById(buttonPlugin.action.buttonId)
        button?.setOnClickListener { _ -> buttonPlugin.toggle() }
    }

    fun highlightActionButtons(toolbarActions: ArrayList<IToolbarAction>) {
        ToolbarAction.values().forEach { action ->
            if (toolbarActions.contains(action)) {
                toggleButton(findViewById(action.buttonId), true)
            } else {
                toggleButton(findViewById(action.buttonId), false)
            }
        }
    }

    private fun getSelectedActions(): ArrayList<IToolbarAction> {
        val actions = ArrayList<IToolbarAction>()

        for (action in ToolbarAction.values()) {
            if (action != ToolbarAction.ELLIPSIS_COLLAPSE &&
                    action != ToolbarAction.ELLIPSIS_EXPAND) {
                val view = findViewById(action.buttonId) as ToggleButton
                if (view.isChecked) actions.add(action)
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
    }

    private fun onToolbarAction(action: IToolbarAction) {
        if (!isEditorAttached()) return

        //if nothing is selected just mark the style as active
        if (!editor!!.isTextSelected() && action.actionType == ToolbarActionType.INLINE_STYLE) {
            val actions = getSelectedActions()
            val textFormats = ArrayList<ITextFormat>()

            actions.forEach { if (it.isStylingAction()) textFormats.add(it.textFormat) }

            if (getSelectedHeadingMenuItem() != null) {
                textFormats.add(getSelectedHeadingMenuItem()!!)
            }

            if (getSelectedListMenuItem() != null) {
                textFormats.add(getSelectedListMenuItem()!!)
            }

            aztecToolbarListener?.onToolbarFormatButtonClicked(action.textFormat, false)
            return editor!!.setSelectedStyles(textFormats)
        }

        //if text is selected and action is styling - toggle the style
        if (action.isStylingAction() && action != ToolbarAction.HEADING && action != ToolbarAction.LIST) {
            aztecToolbarListener?.onToolbarFormatButtonClicked(action.textFormat, false)
            return editor!!.toggleFormatting(action.textFormat)
        }

        //other toolbar action
        when (action) {
            ToolbarAction.ADD_MEDIA -> {
                aztecToolbarListener?.onToolbarMediaButtonClicked()
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

    fun toggleEditorMode() {
        if (editor!!.visibility == View.VISIBLE) {
            sourceEditor!!.displayStyledAndFormattedHtml(editor!!.toPlainHtml(true))
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

    fun getHeadingMenu(): PopupMenu? {
        return headingMenu
    }

    fun getListMenu(): PopupMenu? {
        return listMenu
    }

    fun getSelectedHeadingMenuItem(): ITextFormat? {
        if (headingMenu?.menu?.findItem(R.id.paragraph)?.isChecked!!) return AztecTextFormat.FORMAT_PARAGRAPH
        else if (headingMenu?.menu?.findItem(R.id.heading_1)?.isChecked!!) return AztecTextFormat.FORMAT_HEADING_1
        else if (headingMenu?.menu?.findItem(R.id.heading_2)?.isChecked!!) return AztecTextFormat.FORMAT_HEADING_2
        else if (headingMenu?.menu?.findItem(R.id.heading_3)?.isChecked!!) return AztecTextFormat.FORMAT_HEADING_3
        else if (headingMenu?.menu?.findItem(R.id.heading_4)?.isChecked!!) return AztecTextFormat.FORMAT_HEADING_4
        else if (headingMenu?.menu?.findItem(R.id.heading_5)?.isChecked!!) return AztecTextFormat.FORMAT_HEADING_5
        else if (headingMenu?.menu?.findItem(R.id.heading_6)?.isChecked!!) return AztecTextFormat.FORMAT_HEADING_6
//        TODO: Uncomment when Preformat is to be added back as a feature
//        else if (headingMenu?.menu?.findItem(R.id.preformat)?.isChecked!!) return AztecTextFormat.FORMAT_PREFORMAT
        return null
    }

    fun getSelectedListMenuItem(): ITextFormat? {
        if (listMenu?.menu?.findItem(R.id.list_unordered)?.isChecked!!) return AztecTextFormat.FORMAT_UNORDERED_LIST
        else if (listMenu?.menu?.findItem(R.id.list_ordered)?.isChecked!!) return AztecTextFormat.FORMAT_ORDERED_LIST
        return null
    }

    fun setExpanded(expanded: Boolean) {
        isExpanded = expanded
        setAdvancedState()
    }

    private fun animateToolbarCollapse() {
        buttonEllipsisCollapse.startAnimation(ellipsisSpinLeft)
        isExpanded = false
    }

    private fun animateToolbarExpand() {
        buttonEllipsisExpand.startAnimation(ellipsisSpinRight)
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
        if (textFormats.size == 0) {
            // Select paragraph by default.
            headingMenu?.menu?.findItem(R.id.paragraph)?.isChecked = true
            // Use unnumbered heading selector by default.
            setHeadingMenuSelector(AztecTextFormat.FORMAT_PARAGRAPH)
        } else {
            textFormats.forEach {
                when (it) {
                    AztecTextFormat.FORMAT_HEADING_1 -> headingMenu?.menu?.findItem(R.id.heading_1)?.isChecked = true
                    AztecTextFormat.FORMAT_HEADING_2 -> headingMenu?.menu?.findItem(R.id.heading_2)?.isChecked = true
                    AztecTextFormat.FORMAT_HEADING_3 -> headingMenu?.menu?.findItem(R.id.heading_3)?.isChecked = true
                    AztecTextFormat.FORMAT_HEADING_4 -> headingMenu?.menu?.findItem(R.id.heading_4)?.isChecked = true
                    AztecTextFormat.FORMAT_HEADING_5 -> headingMenu?.menu?.findItem(R.id.heading_5)?.isChecked = true
                    AztecTextFormat.FORMAT_HEADING_6 -> headingMenu?.menu?.findItem(R.id.heading_6)?.isChecked = true
//                    TODO: Uncomment when Preformat is to be added back as a feature
//                    AztecTextFormat.FORMAT_PREFORMAT -> headingMenu?.menu?.findItem(R.id.preformat)?.isChecked = true
                    else -> {
                        // Select paragraph by default.
                        headingMenu?.menu?.findItem(R.id.paragraph)?.isChecked = true
                    }
                }

                setHeadingMenuSelector(it)

                return
            }
        }
    }

    private fun selectListMenuItem(textFormats: ArrayList<ITextFormat>) {
        if (textFormats.size == 0) {
            // Select no list by default.
            listMenu?.menu?.findItem(R.id.list_none)?.isChecked = true
            // Use unordered list selector by default.
            setListMenuSelector(AztecTextFormat.FORMAT_UNORDERED_LIST)
        } else {
            textFormats.forEach {
                when (it) {
                    AztecTextFormat.FORMAT_UNORDERED_LIST -> listMenu?.menu?.findItem(R.id.list_unordered)?.isChecked = true
                    AztecTextFormat.FORMAT_ORDERED_LIST -> listMenu?.menu?.findItem(R.id.list_ordered)?.isChecked = true
                    else -> {
                        // Select no list by default.
                        listMenu?.menu?.findItem(R.id.list_none)?.isChecked = true
                    }
                }

                setListMenuSelector(it)

                return
            }
        }
    }

    private fun setAnimations() {
        layoutExpandedTranslateInRight = AnimationUtils.loadAnimation(context, R.anim.translate_in_right)

        layoutExpandedTranslateOutLeft = AnimationUtils.loadAnimation(context, R.anim.translate_out_left)
        layoutExpandedTranslateOutLeft.setAnimationListener(
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
                        buttonEllipsisCollapse.visibility = View.GONE
                        buttonEllipsisExpand.visibility = View.VISIBLE
                    }

                    override fun onAnimationRepeat(animation: Animation) {
                    }

                    override fun onAnimationStart(animation: Animation) {
                        buttonScroll.smoothScrollTo(0, 0)
                        layoutExpanded.startAnimation(layoutExpandedTranslateOutLeft)
                    }
                }
        )

        ellipsisSpinRight = AnimationUtils.loadAnimation(context, R.anim.spin_right_90)
        ellipsisSpinRight.setAnimationListener(
                object : Animation.AnimationListener {
                    override fun onAnimationEnd(animation: Animation) {
                        buttonEllipsisCollapse.visibility = View.VISIBLE
                        buttonEllipsisExpand.visibility = View.GONE
                    }

                    override fun onAnimationRepeat(animation: Animation) {
                    }

                    override fun onAnimationStart(animation: Animation) {
                        layoutExpanded.visibility = View.VISIBLE
                        layoutExpanded.startAnimation(layoutExpandedTranslateInRight)
                    }
                }
        )
    }

    private fun setButtonViews() {
        layoutExpanded = findViewById(R.id.format_bar_button_layout_expanded) as LinearLayout
        buttonScroll = findViewById(R.id.format_bar_button_scroll) as HorizontalScrollView
        buttonEllipsisCollapse = findViewById(R.id.format_bar_button_ellipsis_collapse) as RippleToggleButton
        buttonEllipsisExpand = findViewById(R.id.format_bar_button_ellipsis_expand) as RippleToggleButton
    }

    private fun setHeadingMenu(view: View) {
        headingMenu = PopupMenu(context, view)
        headingMenu?.setOnMenuItemClickListener(this)
        headingMenu?.inflate(R.menu.heading)
    }

    private fun setListMenu(view: View) {
        listMenu = PopupMenu(context, view)
        listMenu?.setOnMenuItemClickListener(this)
        listMenu?.inflate(R.menu.list)
    }

    private fun setListMenuSelector(textFormat: ITextFormat) {
        when (textFormat) {
            AztecTextFormat.FORMAT_UNORDERED_LIST -> findViewById(R.id.format_bar_button_list).setBackgroundResource(R.drawable.format_bar_button_ul_selector)
            AztecTextFormat.FORMAT_ORDERED_LIST -> findViewById(R.id.format_bar_button_list).setBackgroundResource(R.drawable.format_bar_button_ol_selector)
            else -> {
                // Use unordered list selector by default.
                findViewById(R.id.format_bar_button_list).setBackgroundResource(R.drawable.format_bar_button_ul_selector)
            }
        }
    }

    private fun setHeadingMenuSelector(textFormat: ITextFormat) {
        when (textFormat) {
            AztecTextFormat.FORMAT_HEADING_1 -> findViewById(R.id.format_bar_button_heading).setBackgroundResource(R.drawable.format_bar_button_heading_1_selector)
            AztecTextFormat.FORMAT_HEADING_2 -> findViewById(R.id.format_bar_button_heading).setBackgroundResource(R.drawable.format_bar_button_heading_2_selector)
            AztecTextFormat.FORMAT_HEADING_3 -> findViewById(R.id.format_bar_button_heading).setBackgroundResource(R.drawable.format_bar_button_heading_3_selector)
            AztecTextFormat.FORMAT_HEADING_4 -> findViewById(R.id.format_bar_button_heading).setBackgroundResource(R.drawable.format_bar_button_heading_4_selector)
            AztecTextFormat.FORMAT_HEADING_5 -> findViewById(R.id.format_bar_button_heading).setBackgroundResource(R.drawable.format_bar_button_heading_5_selector)
            AztecTextFormat.FORMAT_HEADING_6 -> findViewById(R.id.format_bar_button_heading).setBackgroundResource(R.drawable.format_bar_button_heading_6_selector)
            AztecTextFormat.FORMAT_PARAGRAPH -> findViewById(R.id.format_bar_button_heading).setBackgroundResource(R.drawable.format_bar_button_heading_selector)
            else -> {
                // Use unnumbered heading selector by default.
                findViewById(R.id.format_bar_button_heading).setBackgroundResource(R.drawable.format_bar_button_heading_selector)
            }
        }
    }

    private fun showCollapsedToolbar() {
        layoutExpanded.visibility = View.GONE
        buttonEllipsisCollapse.visibility = View.GONE
        buttonEllipsisExpand.visibility = View.VISIBLE
    }

    private fun showExpandedToolbar() {
        layoutExpanded.visibility = View.VISIBLE
        buttonEllipsisCollapse.visibility = View.VISIBLE
        buttonEllipsisExpand.visibility = View.GONE
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
        if (isChecked) {
            listMenu?.menu?.findItem(listMenuItemId)?.isChecked = true

            when (listMenuItemId) {
                R.id.list_ordered -> setListMenuSelector(AztecTextFormat.FORMAT_ORDERED_LIST)
                R.id.list_unordered -> setListMenuSelector(AztecTextFormat.FORMAT_UNORDERED_LIST)
                else -> setListMenuSelector(AztecTextFormat.FORMAT_UNORDERED_LIST) // Use unordered list selector by default.
            }
        } else {
            listMenu?.menu?.findItem(R.id.list_none)?.isChecked = true

            // Use unordered list selector by default.
            setListMenuSelector(AztecTextFormat.FORMAT_UNORDERED_LIST)
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
        ToolbarAction.values().forEach { action ->
            if (action == ToolbarAction.ADD_MEDIA) {
                toggleButton(findViewById(action.buttonId), isEnabled)
            } else {
                toggleButtonState(findViewById(action.buttonId), !isEnabled)
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