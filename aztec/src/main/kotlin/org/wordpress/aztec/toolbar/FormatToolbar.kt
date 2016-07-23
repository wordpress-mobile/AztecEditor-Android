package org.wordpress.aztec.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ToggleButton
import org.wordpress.aztec.R
import java.util.*


class FormatToolbar : FrameLayout {

    private var mToolbarActionListener: OnToolbarActionListener? = null

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
    }

    private fun initView() {
        View.inflate(context, R.layout.format_bar, this)

        for (toolbarAction in ToolbarAction.values()) {
            val button = findViewById(toolbarAction.buttonId)
            button?.setOnClickListener { sendToolbarEvent(toolbarAction) }
        }
    }

    private fun sendToolbarEvent(toolbarAction: ToolbarAction) {
        mToolbarActionListener?.onToolbarAction(toolbarAction)
    }

    fun setToolbarActionListener(toolbarActionListener: OnToolbarActionListener) {
        mToolbarActionListener = toolbarActionListener
    }

    interface OnToolbarActionListener {
        fun onToolbarAction(action: ToolbarAction)
    }

    fun highlightActionButtons(toolbarActions: ArrayList<ToolbarAction>) {
        ToolbarAction.values().forEach { action ->
            if (toolbarActions.contains(action)) {
                checkButton(findViewById(action.buttonId) as ToggleButton)
            } else {
                uncheckButton(findViewById(action.buttonId) as ToggleButton)
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


    private fun checkButton(button: ToggleButton?) {
        button?.isChecked = true
    }

    private fun uncheckButton(button: ToggleButton?) {
        button?.isChecked = false
    }
}
