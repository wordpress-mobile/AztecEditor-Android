package org.wordpress.aztec.toolbar

import org.wordpress.aztec.R
import org.wordpress.aztec.TextFormat
import java.util.*

/**
 * Describes what actions toolbar can perform and what buttons they are bound to
 */
enum class ToolbarAction constructor(val buttonId: Int, val actionType: ToolbarActionType, val textFormat: TextFormat?) {
    ADD_MEDIA(R.id.format_bar_button_media, ToolbarActionType.OTHER, null),
    HEADER(R.id.format_bar_button_header, ToolbarActionType.OTHER, null),
    BOLD(R.id.format_bar_button_bold, ToolbarActionType.INLINE_STYLE, TextFormat.FORMAT_BOLD),
    ITALIC(R.id.format_bar_button_italic, ToolbarActionType.INLINE_STYLE, TextFormat.FORMAT_ITALIC),
    STRIKETHROUGH(R.id.format_bar_button_strikethrough, ToolbarActionType.INLINE_STYLE, TextFormat.FORMAT_STRIKETHROUGH),
    BULLET_LIST(R.id.format_bar_button_ul, ToolbarActionType.BLOCK_STYLE, TextFormat.FORMAT_BULLET),
    NUMBERED_LIST(R.id.format_bar_button_ol, ToolbarActionType.OTHER, null), //temporary
    LINK(R.id.format_bar_button_link, ToolbarActionType.OTHER, TextFormat.FORMAT_LINK),
    QUOTE(R.id.format_bar_button_quote, ToolbarActionType.BLOCK_STYLE, TextFormat.FORMAT_QUOTE),
    HTML(R.id.format_bar_button_html, ToolbarActionType.OTHER, null);

    companion object {
        fun getToolbarActionForStyle(style: TextFormat): ToolbarAction? {
            ToolbarAction.values().forEach { if (it.textFormat != null && it.textFormat == style) return it }
            return null
        }

        fun getToolbarActionsForStyles(styles: ArrayList<TextFormat>): ArrayList<ToolbarAction> {
            val actions = ArrayList<ToolbarAction>()
            styles.forEach {
                val action = getToolbarActionForStyle(it)
                if (action != null) {
                    actions.add(action)
                }
            }
            return actions
        }

    }

    fun isStylingAction(): Boolean {
        return actionType == ToolbarActionType.INLINE_STYLE || actionType == ToolbarActionType.BLOCK_STYLE
    }
}
