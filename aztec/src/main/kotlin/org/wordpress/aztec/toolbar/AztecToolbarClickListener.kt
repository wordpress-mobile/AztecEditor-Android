package org.wordpress.aztec.toolbar

import org.wordpress.aztec.TextFormat

interface AztecToolbarClickListener {
    fun onToolbarCollapseButtonClicked()
    fun onToolbarExpandButtonClicked()
    fun onToolbarFormatButtonClicked(format: TextFormat, isKeyboardShortcut: Boolean)
    fun onToolbarHeadingButtonClicked()
    fun onToolbarHtmlButtonClicked()
    fun onToolbarListButtonClicked()
    fun onToolbarMediaButtonClicked()
}
