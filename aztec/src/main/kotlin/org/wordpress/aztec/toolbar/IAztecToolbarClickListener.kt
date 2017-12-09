package org.wordpress.aztec.toolbar

import android.view.View
import org.wordpress.aztec.ITextFormat

interface IAztecToolbarClickListener {
    fun onToolbarCollapseButtonClicked()
    fun onToolbarExpandButtonClicked()
    fun onToolbarFormatButtonClicked(format: ITextFormat, isKeyboardShortcut: Boolean)
    fun onToolbarHeadingButtonClicked()
    fun onToolbarHtmlButtonClicked()
    fun onToolbarListButtonClicked()
    fun onToolbarMediaButtonClicked(button: View)
}
