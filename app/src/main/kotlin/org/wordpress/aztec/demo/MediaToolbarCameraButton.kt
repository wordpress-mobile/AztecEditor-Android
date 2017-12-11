package org.wordpress.aztec.demo

import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.wordpress.aztec.plugins.IMediaToolbarButton
import org.wordpress.aztec.toolbar.AztecToolbar
import org.wordpress.aztec.toolbar.IToolbarAction

class MediaToolbarCameraButton(val toolbar: AztecToolbar) : IMediaToolbarButton {

    private var clickListener: IMediaToolbarButton.IMediaToolbarClickListener? = null

    override val action: IToolbarAction = MediaToolbarAction.CAMERA
    override val context = toolbar.context!!

    override fun setMediaToolbarButtonClickListener(clickListener: IMediaToolbarButton.IMediaToolbarClickListener) {
        this.clickListener = clickListener
    }

    override fun toggle() {
        clickListener?.onClick(toolbar.findViewById(action.buttonId))
    }

    override fun matchesKeyShortcut(keyCode: Int, event: KeyEvent): Boolean {
        return false
    }

    override fun inflateButton(parent: ViewGroup) {
        LayoutInflater.from(context).inflate(R.layout.media_toobar_camera_button, parent)
    }

    override fun toolbarStateAboutToChange(toolbar: AztecToolbar, enable: Boolean) {
        toolbar.findViewById<View>(action.buttonId).isEnabled = enable
    }
}