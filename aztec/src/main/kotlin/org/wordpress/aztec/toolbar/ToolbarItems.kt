package org.wordpress.aztec.toolbar

import android.view.LayoutInflater
import android.widget.LinearLayout
import org.wordpress.aztec.R

/**
 * Use this class to define order of items in the Aztec toolbar.
 */
sealed class ToolbarItems {
    /**
     * This class defines the elements in the basic layout. It contains a set of visible items in a given order.
     * The plugins are added automatically at the end if they are missing from the toolbarElements object.
     */
    data class BasicLayout(val toolbarItems: LinkedHashSet<ToolbarItem>) : ToolbarItems() {
        init {
            if (!toolbarItems.contains(ToolbarItem.PLUGINS)) {
                toolbarItems.add(ToolbarItem.PLUGINS)
            }
        }

        /**
         * Draws toolbar elements into the given layout
         */
        fun addInto(toolbarContainer: LinearLayout, inflater: LayoutInflater) {
            toolbarItems.forEachIndexed { index, toolbarElement ->
                toolbarElement.addInto(toolbarContainer, inflater, index)
            }
        }
    }

    /**
     * This class defines the elements in the advanced layout. The advanced layout has two sets of items.
     * Collapsed items are visible by default and the expanded items are visible on button click. The Plugins element
     * is added to the expanded items if missing. Plugins are never visible in the collapsed items.
     */
    data class AdvancedLayout(val expandedItems: LinkedHashSet<ToolbarItem>, val collapsedItems: LinkedHashSet<ToolbarItem>) : ToolbarItems() {
        init {
            if (!expandedItems.contains(ToolbarItem.PLUGINS)) {
                expandedItems.add(ToolbarItem.PLUGINS)
            }
            if (collapsedItems.contains(ToolbarItem.PLUGINS)) {
                collapsedItems.remove(ToolbarItem.PLUGINS)
            }
        }

        /**
         * Draws the expanded items into the expanded container and collapsed items into the collapsed container.
         */
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
        /**
         * The default order of elements in the basic layout. It's used when there is no order added by the user.
         */
        val defaultBasicLayout = BasicLayout(toolbarItems = linkedSetOf(
                ToolbarItem.HEADING,
                ToolbarItem.LIST,
                ToolbarItem.QUOTE,
                ToolbarItem.BOLD,
                ToolbarItem.ITALIC,
                ToolbarItem.LINK,
                ToolbarItem.UNDERLINE,
                ToolbarItem.STRIKETHROUGH,
                ToolbarItem.ALIGN_LEFT,
                ToolbarItem.ALIGN_CENTER,
                ToolbarItem.ALIGN_RIGHT,
                ToolbarItem.HORIZONTAL_RULE,
                ToolbarItem.PLUGINS,
                ToolbarItem.HTML
        ))

        /**
         * The default order of elements in the advanced layout. It's used when there is no order added by the user.
         */
        val defaultAdvancedLayout = AdvancedLayout(
                expandedItems = linkedSetOf(
                        ToolbarItem.LINK,
                        ToolbarItem.UNDERLINE,
                        ToolbarItem.STRIKETHROUGH,
                        ToolbarItem.ALIGN_LEFT,
                        ToolbarItem.ALIGN_CENTER,
                        ToolbarItem.ALIGN_RIGHT,
                        ToolbarItem.HORIZONTAL_RULE,
                        ToolbarItem.PLUGINS,
                        ToolbarItem.HTML
                ),
                collapsedItems = linkedSetOf(
                        ToolbarItem.HEADING,
                        ToolbarItem.LIST,
                        ToolbarItem.QUOTE,
                        ToolbarItem.BOLD,
                        ToolbarItem.ITALIC
                ))
    }

    /**
     * A list of supported default toolbar elements. If you need to add custom toolbar elements, create a new Plugin.
     */
    enum class ToolbarItem(val layout: Int? = null) {
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
        PLUGINS,
        VERTICAL_DIVIDER(R.layout.format_bar_vertical_divider);

        fun addInto(toolbarContainer: LinearLayout, inflater: LayoutInflater, index: Int) {
            layout?.let {
                val view = inflater.inflate(layout, null)
                toolbarContainer.addView(view, index)
            }
        }
    }
}
