package org.wordpress.aztec.plugins

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.toolbar.AztecToolbar
import org.wordpress.aztec.toolbar.IToolbarAction
import org.wordpress.aztec.toolbar.RippleToggleButton

interface IAztecToolbarButton : IAztecPlugin {
    val action: IToolbarAction
    val context: Context

    fun onClick()

    fun matchesKeyShortcut(keyCode: Int, event: KeyEvent): Boolean {
        return false
    }

    fun inflateButton(parent: ViewGroup)
}