package org.wordpress.aztec.demo

import org.wordpress.aztec.AztecTextFormat
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.toolbar.IToolbarAction
import org.wordpress.aztec.toolbar.ToolbarActionType

enum class MediaToolbarAction constructor(
        override val buttonId: Int,
        override val buttonDrawableRes: Int,
        override val actionType: ToolbarActionType,
        override val textFormats: Set<ITextFormat> = setOf()
) : IToolbarAction {
    GALLERY(R.id.media_bar_button_gallery, R.drawable.media_bar_button_image_multiple_selector, ToolbarActionType.OTHER, setOf(AztecTextFormat.FORMAT_NONE)),
    CAMERA(R.id.media_bar_button_camera, R.drawable.media_bar_button_camera_selector, ToolbarActionType.OTHER, setOf(AztecTextFormat.FORMAT_NONE))
}
