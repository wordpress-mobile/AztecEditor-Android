package org.wordpress.aztec.plugins.wpcomments.toolbar

import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.plugins.wpcomments.CommentsTextFormat
import org.wordpress.aztec.plugins.wpcomments.R
import org.wordpress.aztec.toolbar.IToolbarAction
import org.wordpress.aztec.toolbar.ToolbarActionType

enum class CommentsToolbarAction constructor(override val buttonId: Int, override val actionType: ToolbarActionType,
                                             override val textFormats: Set<ITextFormat> = setOf()) : IToolbarAction {
    MORE(R.id.format_bar_button_more, ToolbarActionType.LINE_BLOCK, setOf(CommentsTextFormat.FORMAT_MORE)),
    PAGE(R.id.format_bar_button_page, ToolbarActionType.LINE_BLOCK, setOf(CommentsTextFormat.FORMAT_PAGE))
}
