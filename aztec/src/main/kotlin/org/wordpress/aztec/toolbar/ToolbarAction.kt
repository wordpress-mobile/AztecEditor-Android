package org.wordpress.aztec.toolbar

import org.wordpress.aztec.R
import org.wordpress.aztec.TextFormat
import java.util.*

/**
 * Describes what actions toolbar can perform and what buttons they are bound to
 */
enum class ToolbarAction constructor(val buttonId: Int, val actionType: ToolbarActionType, val textFormat: TextFormat?) {
    ADD_MEDIA(R.id.format_bar_button_media, ToolbarActionType.OTHER, null),
    HEADING(R.id.format_bar_button_heading, ToolbarActionType.OTHER, null),
//    PARAGRAPH(R.id.paragraph, ToolbarActionType.LINE_BLOCK, TextFormat.FORMAT_PARAGRAPH),
//    HEADING_1(R.id.heading_1, ToolbarActionType.LINE_BLOCK, TextFormat.FORMAT_HEADING_1),
//    HEADING_2(R.id.heading_2, ToolbarActionType.LINE_BLOCK, TextFormat.FORMAT_HEADING_2),
//    HEADING_3(R.id.heading_3, ToolbarActionType.LINE_BLOCK, TextFormat.FORMAT_HEADING_3),
//    HEADING_4(R.id.heading_4, ToolbarActionType.LINE_BLOCK, TextFormat.FORMAT_HEADING_4),
//    HEADING_5(R.id.heading_5, ToolbarActionType.LINE_BLOCK, TextFormat.FORMAT_HEADING_5),
//    HEADING_6(R.id.heading_6, ToolbarActionType.LINE_BLOCK, TextFormat.FORMAT_HEADING_6),
    BOLD(R.id.format_bar_button_bold, ToolbarActionType.INLINE_STYLE, TextFormat.FORMAT_BOLD),
    ITALIC(R.id.format_bar_button_italic, ToolbarActionType.INLINE_STYLE, TextFormat.FORMAT_ITALIC),
    STRIKETHROUGH(R.id.format_bar_button_strikethrough, ToolbarActionType.INLINE_STYLE, TextFormat.FORMAT_STRIKETHROUGH),
    UNORDERED_LIST(R.id.format_bar_button_ul, ToolbarActionType.BLOCK_STYLE, TextFormat.FORMAT_UNORDERED_LIST),
    ORDERED_LIST(R.id.format_bar_button_ol, ToolbarActionType.BLOCK_STYLE, TextFormat.FORMAT_ORDERED_LIST),
    QUOTE(R.id.format_bar_button_quote, ToolbarActionType.BLOCK_STYLE, TextFormat.FORMAT_QUOTE),
    LINK(R.id.format_bar_button_link, ToolbarActionType.OTHER, TextFormat.FORMAT_LINK),
    MORE(R.id.format_bar_button_more, ToolbarActionType.LINE_BLOCK, TextFormat.FORMAT_MORE),
    PAGE(R.id.format_bar_button_page, ToolbarActionType.LINE_BLOCK, TextFormat.FORMAT_PAGE),
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
        return actionType != ToolbarActionType.OTHER
    }
}
