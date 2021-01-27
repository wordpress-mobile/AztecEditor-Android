package org.wordpress.aztec.plugins

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.R
import org.wordpress.aztec.toolbar.AztecToolbar
import org.wordpress.aztec.toolbar.IToolbarAction
import org.wordpress.aztec.toolbar.ToolbarAction

class BackgroundColorButton(private val visualEditor: AztecText) : IToolbarButton {

    override val action: IToolbarAction = ToolbarAction.BACKGROUND
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

}
