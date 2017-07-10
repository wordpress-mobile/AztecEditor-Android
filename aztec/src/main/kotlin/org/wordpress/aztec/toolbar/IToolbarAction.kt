package org.wordpress.aztec.toolbar

import android.annotation.SuppressLint
import org.wordpress.aztec.ITextFormat

@SuppressLint("NewApi")
interface IToolbarAction {
    val buttonId: Int
    val actionType: ToolbarActionType
    val textFormat: ITextFormat

    fun isStylingAction(): Boolean {
        return actionType != ToolbarActionType.OTHER
    }
}