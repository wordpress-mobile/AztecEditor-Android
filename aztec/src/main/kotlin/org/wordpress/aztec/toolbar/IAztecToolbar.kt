package org.wordpress.aztec.toolbar

import android.view.KeyEvent
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.plugins.IToolbarButton
import org.wordpress.aztec.source.SourceViewEditText

interface IAztecToolbar {
    fun onKeyUp(keyCode: Int, keyEvent: KeyEvent): Boolean
    fun addButton(buttonPlugin: IToolbarButton)
    fun setEditor(editor: AztecText, sourceEditor: SourceViewEditText?)
    fun setToolbarListener(listener: IAztecToolbarClickListener)
    fun toggleMediaToolbar()
    fun toggleEditorMode()
}
