package org.wordpress.aztec.toolbar

import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.TextFormat

interface AztecToolbarClickListener {
    fun onToolbarCollapseButtonClicked()
    fun onToolbarExpandButtonClicked()
    fun onToolbarFormatButtonClicked(format: ITextFormat, isKeyboardShortcut: Boolean)
    fun onToolbarHeadingButtonClicked()
    fun onToolbarHtmlButtonClicked()
    fun onToolbarListButtonClicked()
    fun onToolbarMediaButtonClicked()
}
