package org.wordpress.aztec.toolbar

import org.wordpress.aztec.AztecTextFormat
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.R
import java.util.ArrayList

/**
 * Describes what actions toolbar can perform and what buttons they are bound to
 */
enum class ToolbarAction constructor(override val buttonId: Int, override val actionType: ToolbarActionType,
                                     override val textFormats: Set<ITextFormat> = setOf())
    : IToolbarAction {

    ADD_MEDIA_COLLAPSE(
            R.id.format_bar_button_media_collapsed,
            ToolbarActionType.OTHER,
            setOf(AztecTextFormat.FORMAT_NONE)),
    ADD_MEDIA_EXPAND(
            R.id.format_bar_button_media_expanded,
            ToolbarActionType.OTHER,
            setOf(AztecTextFormat.FORMAT_NONE)),
    HEADING(R.id.format_bar_button_heading,
            ToolbarActionType.LINE_BLOCK,
            setOf(AztecTextFormat.FORMAT_NONE)),
    LIST(
            R.id.format_bar_button_list,
            ToolbarActionType.BLOCK_STYLE,
            setOf(AztecTextFormat.FORMAT_NONE)),
    BOLD(
            R.id.format_bar_button_bold,
            ToolbarActionType.INLINE_STYLE,
            setOf(AztecTextFormat.FORMAT_STRONG, AztecTextFormat.FORMAT_BOLD)),
    ITALIC(
            R.id.format_bar_button_italic,
            ToolbarActionType.INLINE_STYLE,
            setOf(AztecTextFormat.FORMAT_EMPHASIS, AztecTextFormat.FORMAT_ITALIC)),
    STRIKETHROUGH(
            R.id.format_bar_button_strikethrough,
            ToolbarActionType.INLINE_STYLE,
            setOf(AztecTextFormat.FORMAT_STRIKETHROUGH)),
    ALIGN_LEFT(R.id.format_bar_button_align_left,
            ToolbarActionType.BLOCK_STYLE,
            setOf(AztecTextFormat.FORMAT_ALIGN_LEFT)),
    ALIGN_CENTER(
            R.id.format_bar_button_align_center,
            ToolbarActionType.BLOCK_STYLE,
            setOf(AztecTextFormat.FORMAT_ALIGN_CENTER)),
    ALIGN_RIGHT(
            R.id.format_bar_button_align_right,
            ToolbarActionType.BLOCK_STYLE,
            setOf(AztecTextFormat.FORMAT_ALIGN_RIGHT)),
    UNDERLINE(
            R.id.format_bar_button_underline,
            ToolbarActionType.INLINE_STYLE,
            setOf(AztecTextFormat.FORMAT_UNDERLINE)),
    QUOTE(
            R.id.format_bar_button_quote,
            ToolbarActionType.BLOCK_STYLE,
            setOf(AztecTextFormat.FORMAT_QUOTE)),
    LINK(
            R.id.format_bar_button_link,
            ToolbarActionType.OTHER,
            setOf(AztecTextFormat.FORMAT_LINK)),
    HORIZONTAL_RULE(
            R.id.format_bar_button_horizontal_rule,
            ToolbarActionType.LINE_BLOCK,
            setOf(AztecTextFormat.FORMAT_HORIZONTAL_RULE)),
    HTML(
            R.id.format_bar_button_html,
            ToolbarActionType.OTHER,
            setOf(AztecTextFormat.FORMAT_NONE)),
    ELLIPSIS_COLLAPSE(
            R.id.format_bar_button_ellipsis_collapsed,
            ToolbarActionType.OTHER,
            setOf(AztecTextFormat.FORMAT_NONE)),
    ELLIPSIS_EXPAND(
            R.id.format_bar_button_ellipsis_expanded,
            ToolbarActionType.OTHER,
            setOf(AztecTextFormat.FORMAT_NONE));

    companion object {
        fun getToolbarActionForStyle(style: ITextFormat): IToolbarAction? {
            ToolbarAction.values().forEach {
                if (it.textFormats.contains(style)) return it
            }
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
