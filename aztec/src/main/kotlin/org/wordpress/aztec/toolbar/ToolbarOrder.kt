package org.wordpress.aztec.toolbar

import android.view.LayoutInflater
import android.widget.LinearLayout
import org.wordpress.aztec.R

sealed class ToolbarOrder {
    data class BasicOrder(val toolbarElements: LinkedHashSet<ToolbarElement>) : ToolbarOrder() {
        init {
            if (!toolbarElements.contains(ToolbarElement.PLUGINS)) {
                toolbarElements.add(ToolbarElement.PLUGINS)
            }
        }

        fun addInto(toolbarContainer: LinearLayout, inflater: LayoutInflater) {
            toolbarElements.forEachIndexed { index, toolbarElement ->
                toolbarElement.addInto(toolbarContainer, inflater, index)
            }
        }
    }

    data class AdvancedOrder(val expandedItems: LinkedHashSet<ToolbarElement>, val collapsedItems: LinkedHashSet<ToolbarElement>) : ToolbarOrder() {
        init {
            if (!expandedItems.contains(ToolbarElement.PLUGINS)) {
                expandedItems.add(ToolbarElement.PLUGINS)
            }
            if (collapsedItems.contains(ToolbarElement.PLUGINS)) {
                collapsedItems.remove(ToolbarElement.PLUGINS)
            }
        }

        fun addInto(expandedContainer: LinearLayout, collapsedContainer: LinearLayout, inflater: LayoutInflater) {
            expandedItems.forEachIndexed { index, toolbarElement ->
                toolbarElement.addInto(expandedContainer, inflater, index)
            }
            collapsedItems.forEachIndexed { index, toolbarElement ->
                toolbarElement.addInto(collapsedContainer, inflater, index)
            }
        }
    }

    companion object {
        val defaultBasicOrder = BasicOrder(toolbarElements = linkedSetOf(
                ToolbarElement.HEADING,
                ToolbarElement.LIST,
                ToolbarElement.QUOTE,
                ToolbarElement.BOLD,
                ToolbarElement.ITALIC,
                ToolbarElement.LINK,
                ToolbarElement.UNDERLINE,
                ToolbarElement.STRIKETHROUGH,
                ToolbarElement.ALIGN_LEFT,
                ToolbarElement.ALIGN_CENTER,
                ToolbarElement.ALIGN_RIGHT,
                ToolbarElement.HORIZONTAL_RULE,
                ToolbarElement.PLUGINS,
                ToolbarElement.HTML
        ))
        val defaultAdvancedOrder = AdvancedOrder(
                expandedItems = linkedSetOf(
                        ToolbarElement.LINK,
                        ToolbarElement.UNDERLINE,
                        ToolbarElement.STRIKETHROUGH,
                        ToolbarElement.ALIGN_LEFT,
                        ToolbarElement.ALIGN_CENTER,
                        ToolbarElement.ALIGN_RIGHT,
                        ToolbarElement.HORIZONTAL_RULE,
                        ToolbarElement.PLUGINS,
                        ToolbarElement.HTML
                ),
                collapsedItems = linkedSetOf(
                        ToolbarElement.HEADING,
                        ToolbarElement.LIST,
                        ToolbarElement.QUOTE,
                        ToolbarElement.BOLD,
                        ToolbarElement.ITALIC
                ))
    }

    enum class ToolbarElement(val layout: Int?) {
        HEADING(R.layout.format_bar_button_heading),
        LIST(R.layout.format_bar_button_list),
        QUOTE(R.layout.format_bar_button_quote),
        BOLD(R.layout.format_bar_button_bold),
        ITALIC(R.layout.format_bar_button_italic),
        LINK(R.layout.format_bar_button_link),
        UNDERLINE(R.layout.format_bar_button_underline),
        STRIKETHROUGH(R.layout.format_bar_button_strikethrough),
        ALIGN_LEFT(R.layout.format_bar_button_align_left),
        ALIGN_CENTER(R.layout.format_bar_button_align_center),
        ALIGN_RIGHT(R.layout.format_bar_button_align_right),
        HORIZONTAL_RULE(R.layout.format_bar_button_horizontal_line),
        HTML(R.layout.format_bar_button_html),
        PLUGINS(null),
        VERTICAL_DIVIDER(R.layout.format_bar_vertical_divider);

        fun addInto(toolbarContainer: LinearLayout, inflater: LayoutInflater, index: Int) {
            layout?.let {
                val view = inflater.inflate(layout, null)
                toolbarContainer.addView(view, index)
            }
        }
    }
}
