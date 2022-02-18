package org.wordpress.aztec.toolbar

import org.junit.Test

class ToolbarItemsTest {
    @Test
    fun `given a basic layout with duplicate actions, when created, then remove the duplicate actions`() {
        val basicLayout = ToolbarItems.BasicLayout(
                ToolbarAction.BOLD, ToolbarAction.ITALIC, ToolbarAction.BOLD, ToolbarItems.PLUGINS
        )

        assert(basicLayout.sanitizedInput.size == 3)
        assert(basicLayout.sanitizedInput[0] == ToolbarAction.BOLD)
        assert(basicLayout.sanitizedInput[1] == ToolbarAction.ITALIC)
        assert(basicLayout.sanitizedInput[2] == ToolbarItems.PLUGINS)
    }

    @Test
    fun `given a basic layout with duplicate plugins, when created, then remove the duplicate plugins`() {
        val basicLayout = ToolbarItems.BasicLayout(
                ToolbarItems.PLUGINS, ToolbarAction.ITALIC, ToolbarAction.BOLD, ToolbarItems.PLUGINS
        )

        assert(basicLayout.sanitizedInput.size == 3)
        assert(basicLayout.sanitizedInput[0] == ToolbarItems.PLUGINS)
        assert(basicLayout.sanitizedInput[1] == ToolbarAction.ITALIC)
        assert(basicLayout.sanitizedInput[2] == ToolbarAction.BOLD)
    }

    @Test
    fun `given a basic layout with missing plugins, when created, then add the plugins at the end`() {
        val basicLayout = ToolbarItems.BasicLayout(
                ToolbarAction.BOLD, ToolbarAction.ITALIC
        )

        assert(basicLayout.sanitizedInput.size == 3)
        assert(basicLayout.sanitizedInput[0] == ToolbarAction.BOLD)
        assert(basicLayout.sanitizedInput[1] == ToolbarAction.ITALIC)
        assert(basicLayout.sanitizedInput[2] == ToolbarItems.PLUGINS)
    }

    @Test
    fun `given a basic layout with multiple dividers, when created, then vertical dividers are preserved`() {
        val basicLayout = ToolbarItems.BasicLayout(
                ToolbarItems.DIVIDER, ToolbarAction.BOLD, ToolbarItems.DIVIDER, ToolbarItems.PLUGINS, ToolbarItems.DIVIDER
        )

        assert(basicLayout.sanitizedInput.size == 5)
        assert(basicLayout.sanitizedInput[0] == ToolbarItems.DIVIDER)
        assert(basicLayout.sanitizedInput[1] == ToolbarAction.BOLD)
        assert(basicLayout.sanitizedInput[2] == ToolbarItems.DIVIDER)
        assert(basicLayout.sanitizedInput[3] == ToolbarItems.PLUGINS)
        assert(basicLayout.sanitizedInput[4] == ToolbarItems.DIVIDER)
    }

    @Test
    fun `given an advanced layout with duplicate actions, when created, then remove the duplicate actions`() {
        val advancedLayout = ToolbarItems.AdvancedLayout(
                expandedItems = listOf(ToolbarAction.BOLD, ToolbarAction.ITALIC, ToolbarAction.BOLD, ToolbarItems.PLUGINS),
                collapsedItems = listOf(ToolbarAction.HEADING, ToolbarAction.HTML, ToolbarAction.HEADING)
        )

        assert(advancedLayout.sanitizedExpandedItems.size == 3)
        assert(advancedLayout.sanitizedExpandedItems[0] == ToolbarAction.BOLD)
        assert(advancedLayout.sanitizedExpandedItems[1] == ToolbarAction.ITALIC)
        assert(advancedLayout.sanitizedExpandedItems[2] == ToolbarItems.PLUGINS)
        assert(advancedLayout.sanitizedCollapsedItems.size == 2)
        assert(advancedLayout.sanitizedCollapsedItems[0] == ToolbarAction.HEADING)
        assert(advancedLayout.sanitizedCollapsedItems[1] == ToolbarAction.HTML)
    }

    @Test
    fun `given an advanced layout with plugins in collapsed items, when created, then add plugins to expanded items and remove from collapsed items`() {
        val advancedLayout = ToolbarItems.AdvancedLayout(
                expandedItems = listOf(ToolbarAction.BOLD),
                collapsedItems = listOf(ToolbarAction.HEADING, ToolbarItems.PLUGINS)
        )

        assert(advancedLayout.sanitizedExpandedItems.size == 2)
        assert(advancedLayout.sanitizedExpandedItems[0] == ToolbarAction.BOLD)
        assert(advancedLayout.sanitizedExpandedItems[1] == ToolbarItems.PLUGINS)
        assert(advancedLayout.sanitizedCollapsedItems.size == 1)
        assert(advancedLayout.sanitizedCollapsedItems[0] == ToolbarAction.HEADING)
    }

    @Test
    fun `given an advanced layout with plugins in both lists, when created, then remove plugins from collapsed items`() {
        val advancedLayout = ToolbarItems.AdvancedLayout(
                expandedItems = listOf(ToolbarAction.BOLD, ToolbarItems.PLUGINS),
                collapsedItems = listOf(ToolbarAction.HEADING, ToolbarItems.PLUGINS)
        )

        assert(advancedLayout.sanitizedExpandedItems.size == 2)
        assert(advancedLayout.sanitizedExpandedItems[0] == ToolbarAction.BOLD)
        assert(advancedLayout.sanitizedExpandedItems[1] == ToolbarItems.PLUGINS)
        assert(advancedLayout.sanitizedCollapsedItems.size == 1)
        assert(advancedLayout.sanitizedCollapsedItems[0] == ToolbarAction.HEADING)
    }

    @Test
    fun `given an advanced layout with duplicate actions from expanded items in collapsed items, when created, then remove the duplicate collapsed actions`() {
        val advancedLayout = ToolbarItems.AdvancedLayout(
                expandedItems = listOf(ToolbarAction.BOLD, ToolbarItems.PLUGINS),
                collapsedItems = listOf(ToolbarAction.BOLD, ToolbarAction.HEADING)
        )

        assert(advancedLayout.sanitizedExpandedItems.size == 2)
        assert(advancedLayout.sanitizedExpandedItems[0] == ToolbarAction.BOLD)
        assert(advancedLayout.sanitizedExpandedItems[1] == ToolbarItems.PLUGINS)
        assert(advancedLayout.sanitizedCollapsedItems.size == 1)
        assert(advancedLayout.sanitizedCollapsedItems[0] == ToolbarAction.HEADING)
    }

    @Test
    fun `given an advanced layout with duplicate dividers, when created, then dividers not removed`() {
        val advancedLayout = ToolbarItems.AdvancedLayout(
                expandedItems = listOf(ToolbarAction.BOLD, ToolbarItems.DIVIDER, ToolbarItems.PLUGINS, ToolbarItems.DIVIDER),
                collapsedItems = listOf(ToolbarItems.DIVIDER, ToolbarAction.HEADING, ToolbarItems.DIVIDER)
        )

        assert(advancedLayout.sanitizedExpandedItems.size == 4)
        assert(advancedLayout.sanitizedExpandedItems[0] == ToolbarAction.BOLD)
        assert(advancedLayout.sanitizedExpandedItems[1] == ToolbarItems.DIVIDER)
        assert(advancedLayout.sanitizedExpandedItems[2] == ToolbarItems.PLUGINS)
        assert(advancedLayout.sanitizedExpandedItems[3] == ToolbarItems.DIVIDER)
        assert(advancedLayout.sanitizedCollapsedItems.size == 3)
        assert(advancedLayout.sanitizedCollapsedItems[0] == ToolbarItems.DIVIDER)
        assert(advancedLayout.sanitizedCollapsedItems[1] == ToolbarAction.HEADING)
        assert(advancedLayout.sanitizedCollapsedItems[2] == ToolbarItems.DIVIDER)
    }
}
