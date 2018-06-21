package org.wordpress.aztec.demo

import org.wordpress.aztec.AztecTextFormat
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.toolbar.IToolbarAction
import org.wordpress.aztec.toolbar.ToolbarActionType

enum class MediaToolbarAction constructor(override val buttonId: Int, override val actionType: ToolbarActionType,
                                          override val textFormats: Set<ITextFormat> = setOf()) : IToolbarAction {
    GALLERY(R.id.media_bar_button_gallery, ToolbarActionType.OTHER, setOf(AztecTextFormat.FORMAT_NONE)),
    CAMERA(R.id.media_bar_button_camera, ToolbarActionType.OTHER, setOf(AztecTextFormat.FORMAT_NONE))
}
