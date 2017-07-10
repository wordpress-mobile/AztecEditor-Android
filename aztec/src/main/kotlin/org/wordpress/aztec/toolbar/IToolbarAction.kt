package org.wordpress.aztec.toolbar

import org.wordpress.aztec.ITextFormat

interface IToolbarAction {
    val buttonId: Int
    val actionType: ToolbarActionType
    val textFormat: ITextFormat

    fun isStylingAction(): Boolean {
        return actionType != ToolbarActionType.OTHER
    }
}