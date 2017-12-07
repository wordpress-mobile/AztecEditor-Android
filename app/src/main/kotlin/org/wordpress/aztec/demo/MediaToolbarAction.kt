package org.wordpress.aztec.demo

import org.wordpress.aztec.AztecTextFormat
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.toolbar.IToolbarAction
import org.wordpress.aztec.toolbar.ToolbarActionType

enum class MediaToolbarAction constructor(override val buttonId: Int, override val actionType: ToolbarActionType,
                                          override val textFormat: ITextFormat) : IToolbarAction {
    PHOTO(R.id.format_bar_button_photo, ToolbarActionType.OTHER, AztecTextFormat.FORMAT_NONE),
    VIDEO(R.id.format_bar_button_video, ToolbarActionType.OTHER, AztecTextFormat.FORMAT_NONE)
}