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
    class BasicLayout(vararg toolbarItems: IToolbarItem) : ToolbarItems() {
        val sanitizedInput = toolbarItems.toList().sanitize(containsPlugins = true)

        /**
         * Draws toolbar elements into the given layout
         */
        fun addInto(toolbarContainer: LinearLayout, inflater: LayoutInflater) {
            sanitizedInput.forEachIndexed { index, toolbarElement ->
                toolbarElement.addInto(toolbarContainer, inflater, index)
            }
        }
    }

    /**
     * This class defines the elements in the advanced layout. The advanced layout has two sets of items.
     * Collapsed items are visible by default and the expanded items are visible on button click. The Plugins element
     * is added to the expanded items if missing. Plugins are never visible in the collapsed items.
     */
    class AdvancedLayout(expandedItems: List<IToolbarItem>, collapsedItems: List<IToolbarItem>) : ToolbarItems() {
        val sanitizedExpandedItems = expandedItems.sanitize(containsPlugins = true)
        val sanitizedCollapsedItems = collapsedItems.sanitize(containsPlugins = false, listOfUsedItems = sanitizedExpandedItems)

        /**
         * Draws the expanded items into the expanded container and collapsed items into the collapsed container.
         */
        fun addInto(expandedContainer: LinearLayout, collapsedContainer: LinearLayout, inflater: LayoutInflater) {
            sanitizedExpandedItems.forEachIndexed { index, toolbarElement ->
                toolbarElement.addInto(expandedContainer, inflater, index)
            }
            sanitizedCollapsedItems.forEachIndexed { index, toolbarElement ->
                toolbarElement.addInto(collapsedContainer, inflater, index)
            }
        }
    }

    companion object {
        /**
         * The default order of elements in the basic layout. It's used when there is no order added by the user.
         */
        val defaultBasicLayout = BasicLayout(
                ToolbarAction.HEADING,
                ToolbarAction.LIST,
                ToolbarAction.QUOTE,
                ToolbarAction.BOLD,
                ToolbarAction.ITALIC,
                ToolbarAction.LINK,
                ToolbarAction.UNDERLINE,
                ToolbarAction.STRIKETHROUGH,
                ToolbarAction.ALIGN_LEFT,
                ToolbarAction.ALIGN_CENTER,
                ToolbarAction.ALIGN_RIGHT,
                ToolbarAction.HORIZONTAL_RULE,
                PLUGINS,
                ToolbarAction.HTML
        )

        /**
         * The default order of elements in the advanced layout. It's used when there is no order added by the user.
         */
        val defaultAdvancedLayout = AdvancedLayout(
                expandedItems = mutableListOf(
                        ToolbarAction.LINK,
                        ToolbarAction.UNDERLINE,
                        ToolbarAction.STRIKETHROUGH,
                        ToolbarAction.ALIGN_LEFT,
                        ToolbarAction.ALIGN_CENTER,
                        ToolbarAction.ALIGN_RIGHT,
                        ToolbarAction.HORIZONTAL_RULE,
                        PLUGINS,
                        ToolbarAction.HTML
                ),
                collapsedItems = mutableListOf(
                        ToolbarAction.HEADING,
                        ToolbarAction.LIST,
                        ToolbarAction.QUOTE,
                        ToolbarAction.BOLD,
                        ToolbarAction.ITALIC
                ))
    }

    /**
     * A list of supported default toolbar elements. If you need to add custom toolbar elements, create a new Plugin.
     */
    fun IToolbarItem.addInto(toolbarContainer: LinearLayout, inflater: LayoutInflater, index: Int) {
        this.layout?.let {
            val view = inflater.inflate(it, null)
            toolbarContainer.addView(view, index)
        }
    }

    protected fun List<IToolbarItem>.sanitize(containsPlugins: Boolean, listOfUsedItems: List<IToolbarItem> = emptyList()): List<IToolbarItem> {
        val usedToolbarActions = mutableSetOf<ToolbarAction>()
        usedToolbarActions.addAll(listOfUsedItems.filterIsInstance(ToolbarAction::class.java).toSet())
        val result = mutableListOf<IToolbarItem>()
        var hasPlugins = false
        for (toolbarItem in this) {
            when (toolbarItem) {
                is ToolbarAction -> {
                    if (toolbarItem.layout != null && !usedToolbarActions.contains(toolbarItem)) {
                        usedToolbarActions.add(toolbarItem)
                        result.add(toolbarItem)
                    }
                }
                is PLUGINS -> {
                    if (!hasPlugins && containsPlugins) {
                        hasPlugins = true
                        result.add(toolbarItem)
                    }
                }
                is DIVIDER -> {
                    result.add(toolbarItem)
                }
            }
        }
        if (!hasPlugins && containsPlugins) {
            result.add(PLUGINS)
        }
        return result
    }

    interface IToolbarItem {
        val layout: Int?
    }
    object PLUGINS : IToolbarItem {
        override val layout: Int? = null
    }
    object DIVIDER : IToolbarItem {
        override val layout: Int = R.layout.format_bar_vertical_divider
    }
}
