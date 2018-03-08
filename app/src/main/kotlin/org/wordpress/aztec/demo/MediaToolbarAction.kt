package org.wordpress.aztec.demo

import org.wordpress.aztec.AztecTextFormat
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.toolbar.IToolbarAction
import org.wordpress.aztec.toolbar.ToolbarActionType

enum class MediaToolbarAction constructor(override val buttonId: Int, override val actionType: ToolbarActionType,
                                          override val textFormat: ITextFormat) : IToolbarAction {
    GALLERY(R.id.media_bar_button_gallery, ToolbarActionType.OTHER, AztecTextFormat.FORMAT_NONE),
    CAMERA(R.id.media_bar_button_camera, ToolbarActionType.OTHER, AztecTextFormat.FORMAT_NONE),
    MORE(R.id.media_bar_button_more, ToolbarActionType.OTHER, AztecTextFormat.FORMAT_NONE);
}