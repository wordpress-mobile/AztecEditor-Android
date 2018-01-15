package org.wordpress.aztec.toolbar

import org.wordpress.aztec.ITextFormat

interface IAztecToolbarClickListener {
    fun onToolbarCollapseButtonClicked()
    fun onToolbarExpandButtonClicked()
    fun onToolbarFormatButtonClicked(format: ITextFormat, isKeyboardShortcut: Boolean)
    fun onToolbarHeadingButtonClicked()
    fun onToolbarHtmlButtonClicked()
    fun onToolbarListButtonClicked()

    /**
     * Called when media button in toolbar is clicked. This allows listeners to
     * get a chance to respond before the target view.
     *
     * @return True if the listener has consumed the event, false otherwise.
     */
    fun onToolbarMediaButtonClicked(): Boolean
}
