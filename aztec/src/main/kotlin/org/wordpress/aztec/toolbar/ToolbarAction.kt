package org.wordpress.aztec.toolbar

import org.wordpress.aztec.R


enum class ToolbarAction constructor(val buttonId: Int) {
    ADD_MEDIA(R.id.format_bar_button_media),
    BOLD(R.id.format_bar_button_bold),
    ITALIC(R.id.format_bar_button_italic),
    BULLET_LIST(R.id.format_bar_button_ul),
    NUMBERED_LIST(R.id.format_bar_button_ol),
    LINK(R.id.format_bar_button_link),
    BLOCKQUOTE(R.id.format_bar_button_quote)
}
