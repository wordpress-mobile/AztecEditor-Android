package org.wordpress.aztec.toolbar

import org.wordpress.aztec.AztecTextFormat
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.R
import java.util.*

/**
 * Describes what actions toolbar can perform and what buttons they are bound to
 */
enum class ToolbarAction constructor(override val buttonId: Int, override val actionType: ToolbarActionType,
                                     override val textFormat: ITextFormat) : IToolbarAction {

    ADD_MEDIA(R.id.format_bar_button_media, ToolbarActionType.OTHER, AztecTextFormat.FORMAT_NONE),
    HEADING(R.id.format_bar_button_heading, ToolbarActionType.LINE_BLOCK, AztecTextFormat.FORMAT_NONE),
    LIST(R.id.format_bar_button_list, ToolbarActionType.BLOCK_STYLE, AztecTextFormat.FORMAT_NONE),
    BOLD(R.id.format_bar_button_bold, ToolbarActionType.INLINE_STYLE, AztecTextFormat.FORMAT_BOLD),
    ITALIC(R.id.format_bar_button_italic, ToolbarActionType.INLINE_STYLE, AztecTextFormat.FORMAT_ITALIC),
    STRIKETHROUGH(R.id.format_bar_button_strikethrough, ToolbarActionType.INLINE_STYLE, AztecTextFormat.FORMAT_STRIKETHROUGH),
    UNDERLINE(R.id.format_bar_button_underline, ToolbarActionType.INLINE_STYLE, AztecTextFormat.FORMAT_UNDERLINE),
    QUOTE(R.id.format_bar_button_quote, ToolbarActionType.BLOCK_STYLE, AztecTextFormat.FORMAT_QUOTE),
    LINK(R.id.format_bar_button_link, ToolbarActionType.OTHER, AztecTextFormat.FORMAT_LINK),
    HORIZONTAL_RULE(R.id.format_bar_button_horizontal_rule, ToolbarActionType.LINE_BLOCK, AztecTextFormat.FORMAT_HORIZONTAL_RULE),
    HTML(R.id.format_bar_button_html, ToolbarActionType.OTHER, AztecTextFormat.FORMAT_NONE),
    ELLIPSIS_COLLAPSE(R.id.format_bar_button_ellipsis_collapse, ToolbarActionType.OTHER, AztecTextFormat.FORMAT_NONE),
    ELLIPSIS_EXPAND(R.id.format_bar_button_ellipsis_expand, ToolbarActionType.OTHER, AztecTextFormat.FORMAT_NONE);

    companion object {
        fun getToolbarActionForStyle(style: ITextFormat): IToolbarAction? {
            ToolbarAction.values().forEach { if (it.textFormat == style) return it }
            return null
        }

        fun getToolbarActionsForStyles(styles: ArrayList<ITextFormat>): ArrayList<IToolbarAction> {
            val actions = ArrayList<IToolbarAction>()
            styles.forEach {
                val action = getToolbarActionForStyle(it)
                if (action != null) {
                    actions.add(action)
                }
            }
            return actions
        }
    }
}
