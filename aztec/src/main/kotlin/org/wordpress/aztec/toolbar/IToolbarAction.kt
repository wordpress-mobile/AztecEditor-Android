package org.wordpress.aztec.toolbar

import android.annotation.SuppressLint
import org.wordpress.aztec.ITextFormat

/**
 * Defines the toolbar button behavior.
 *
 * @property buttonId the resource ID of a button.
 * @property actionType one of: INLINE_STYLE, BLOCK_STYLE, LINE_BLOCK or OTHER.
 * @property textFormat the type of text formatting this action performs.
 * @property buttonDrawableRes the icon resourceId displayed for the button.
 */
@SuppressLint("NewApi")
interface IToolbarAction {
    val buttonId: Int
    val actionType: ToolbarActionType
    val textFormats: Set<ITextFormat>
    val buttonDrawableRes: Int

    /**
     * Determines, whether this action performs any text styling.
     *
     * This method is called when the action is invoked.
     *
     * @return true, if the action performs any text styling, false otherwise.
     */
    fun isStylingAction(): Boolean {
        return actionType != ToolbarActionType.OTHER
    }
}
