package org.wordpress.aztec.toolbar

import android.view.LayoutInflater
import android.widget.LinearLayout
import org.wordpress.aztec.R
import org.wordpress.aztec.toolbar.ToolbarOrder.ToolbarElement.*

sealed class ToolbarOrder {
    data class BasicOrder(val toolbarElements: LinkedHashSet<ToolbarElement>) : ToolbarOrder() {
        init {
            if (!toolbarElements.contains(PLUGINS)) {
                toolbarElements.add(PLUGINS)
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
            if (!expandedItems.contains(PLUGINS)) {
                expandedItems.add(PLUGINS)
            }
            if (collapsedItems.contains(PLUGINS)) {
                collapsedItems.remove(PLUGINS)
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
                HEADING,
                LIST,
                QUOTE,
                BOLD,
                ITALIC,
                LINK,
                UNDERLINE,
                STRIKETHROUGH,
                ALIGN_LEFT,
                ALIGN_CENTER,
                ALIGN_RIGHT,
                HORIZONTAL_RULE,
                PLUGINS,
                HTML
        ))
        val defaultAdvancedOrder = AdvancedOrder(
                expandedItems = linkedSetOf(
                        LINK,
                        UNDERLINE,
                        STRIKETHROUGH,
                        ALIGN_LEFT,
                        ALIGN_CENTER,
                        ALIGN_RIGHT,
                        HORIZONTAL_RULE,
                        PLUGINS,
                        HTML
                ),
                collapsedItems = linkedSetOf(
                        HEADING,
                        LIST,
                        QUOTE,
                        BOLD,
                        ITALIC
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
