package org.wordpress.aztec.plugins

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.AztecTextFormat
import org.wordpress.aztec.R
import org.wordpress.aztec.toolbar.AztecToolbar
import org.wordpress.aztec.toolbar.IToolbarAction
import org.wordpress.aztec.toolbar.ToolbarActionType

class BackgroundColorButton(val visualEditor: AztecText) : IToolbarButton {

    override val action: IToolbarAction = BackgroundColorAction()
    override val context = visualEditor.context!!

    override fun toggle() {
        visualEditor.toggleFormatting(action.textFormats.first())
    }

    override fun inflateButton(parent: ViewGroup) {
        LayoutInflater.from(context).inflate(R.layout.background_color_button, parent)
    }

    override fun toolbarStateAboutToChange(toolbar: AztecToolbar, enable: Boolean) {
        toolbar.findViewById<View>(action.buttonId).isEnabled = enable
    }

    inner class BackgroundColorAction : IToolbarAction {
        override val buttonId = R.id.button_background_color
        override val actionType = ToolbarActionType.INLINE_STYLE
        override val textFormats = setOf(AztecTextFormat.FORMAT_BACKGROUND)
        override val buttonDrawableRes = R.drawable.format_bar_button_background_selector
    }
}