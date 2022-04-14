package org.wordpress.aztec.formatting

import org.wordpress.aztec.AztecText
import org.wordpress.aztec.spans.AztecListItemSpan
import org.wordpress.aztec.spans.AztecListSpan
import org.wordpress.aztec.spans.AztecOrderedListSpan
import org.wordpress.aztec.spans.AztecOrderedListSpanAligned
import org.wordpress.aztec.spans.AztecUnorderedListSpan
import org.wordpress.aztec.spans.AztecUnorderedListSpanAligned
import org.wordpress.aztec.spans.IAztecBlockSpan

class ListFormatter(editor: AztecText) : AztecFormatter(editor) {
    /**
     * This method attempts to indent a selection in a list. This is a complicated problem because we have the following
     * rules:
     * - the first item of any list cannot be indented - there is never more than a single indentation in place on one
     *   item
     * - the list structure is nested like in HTML - the outer list is never changed by indentation, indenting an item
     *   could create a new item if the siblings are on the same level. If the following item is indented as well,
     *   the currently selected item should join its list
     *   @return true if the selection was a list. It returns true even if the indentation wasn't possible
     */
    fun indentList(selStart: Int = selectionStart, selEnd: Int = selectionEnd): Boolean {
        val listSpans = editableText.getSpans(selStart, selEnd, AztecListSpan::class.java).filterCorrectSpans(selectionStart = selStart, selectionEnd = selEnd)
        if (listSpans.isEmpty()) return false
        buildListState(listSpans, selStart, selEnd)?.apply {
            // You cannot indent the first list item
            if (listItemSpanBeforeSelection == null) {
                return@apply
            }
            // In order to indent the previous list item has to be on the same level as the first selected item
            val nextItemLevel = nestingLevel + 2
            if (listItemSpanBeforeSelection.nestingLevel == nestingLevel) {
                // If the following list item is missing or it's on the same level as the current list item, create a new span
                if ((listItemSpanAfterSelection == null || listItemSpanAfterSelection.nestingLevel <= nestingLevel)) {
                    selectedListItems.indentAll()
                    val wrapper = directParentList.copyList(increaseNestingLevel = true) ?: return@apply
                    editableText.setSpan(wrapper, firstSelectedItemStart, selectionEnd, directParentFlags)

                } else if (listSpanAfterSelection != null && listSpanAfterSelection.nestingLevel > nestingLevel) {
                    selectedListItems.indentAll()
                    listSpanAfterSelection.changeSpanStart(firstSelectedItemStart)
                    selectedListItems.last().trimEnd(editableText.getSpanStart(listItemSpanAfterSelection))
                }
            } else if (deeperListSpanBeforeSelection?.nestingLevel == nestingLevel + 1) {
                // In this case the previous list span is indented by one level, we can indent current span on the same level
                if ((listItemSpanAfterSelection == null || listItemSpanAfterSelection.nestingLevel <= nestingLevel)) {
                    selectedListItems.indentAll()
                    deeperListSpanBeforeSelection.changeSpanEnd(selectionEnd)
                } else if (listItemSpanAfterSelection.nestingLevel == nextItemLevel) {
                    // Merge previous and following list before and after the selection
                    selectedListItems.indentAll()
                    val followingSpanEnd = editableText.getSpanEnd(listSpanAfterSelection)
                    editableText.removeSpan(listSpanAfterSelection)
                    deeperListSpanBeforeSelection.changeSpanEnd(followingSpanEnd)
                    selectedListItems.last().trimEnd(editableText.getSpanStart(listItemSpanAfterSelection))
                }
            }
            listItemSpansBeforeSelection.filter { it.nestingLevel < nextItemLevel }.forEach {
                it.stretchEnd(selectionEnd)
            }
        }
        return true
    }

    /**
     * Returns true if the current position contains a list item that can be indented. It contains a copy of logic
     * from `indentList` function without the actual list idents.
     */
    fun isIndentAvailable(selStart: Int = selectionStart, selEnd: Int = selectionEnd): Boolean {
        val listSpans = editableText.getSpans(selStart, selEnd, AztecListSpan::class.java).filterCorrectSpans(selectionStart = selStart, selectionEnd = selEnd)
        if (listSpans.isEmpty()) return false
        buildListState(listSpans, selStart, selEnd)?.apply {
            if (listItemSpanBeforeSelection == null) {
                return false
            }
            return if (listItemSpanBeforeSelection.nestingLevel == nestingLevel) {
                if ((listItemSpanAfterSelection == null || listItemSpanAfterSelection.nestingLevel <= nestingLevel)) {
                    true
                } else listSpanAfterSelection != null && listSpanAfterSelection.nestingLevel > nestingLevel
            } else if (deeperListSpanBeforeSelection?.nestingLevel == nestingLevel + 1) {
                if ((listItemSpanAfterSelection == null || listItemSpanAfterSelection.nestingLevel <= nestingLevel)) {
                    true
                } else listItemSpanAfterSelection.nestingLevel == nestingLevel + 2
            } else {
                false
            }
        }
        return false
    }

    private fun AztecListSpan.copyList(increaseNestingLevel: Boolean = false): AztecListSpan? {
        val updatedNestingLevel = if (increaseNestingLevel) nestingLevel + 2 else nestingLevel
        return when (this) {
            is AztecOrderedListSpanAligned -> AztecOrderedListSpanAligned(updatedNestingLevel, attributes, listStyle, alignment)
            is AztecOrderedListSpan -> AztecOrderedListSpan(updatedNestingLevel, attributes, listStyle)
            is AztecUnorderedListSpanAligned -> AztecUnorderedListSpanAligned(updatedNestingLevel, attributes, listStyle, alignment)
            is AztecUnorderedListSpan -> AztecUnorderedListSpan(updatedNestingLevel, attributes, listStyle)
            else -> null
        }
    }

    /**
     * This method attempts to outdent a selection in a list. This is a complicated problem because we have the following
     * rules:
     * - when outdenting the only/first/last item of a list on the highest level, remove the list from that item
     * - you cannot outdent an item that has a child on deeper level (to avoid 2-level indents)
     *   @return true if the selection was a list. It returns true even if the outdent wasn't possible
     */
    fun outdentList(selStart: Int = selectionStart, selEnd: Int = selectionEnd): Boolean {
        val listSpans = editableText.getSpans(selStart, selEnd, AztecListSpan::class.java).filterCorrectSpans(selectionStart = selStart, selectionEnd = selEnd)
        if (listSpans.isEmpty()) return false
        buildListState(listSpans, selStart, selEnd)?.apply {
            when {
                listItemSpanBeforeSelection == null && listItemSpanAfterSelection == null -> {
                    // In case of the selected list spam doesn't have any predecessor or successor, remove the list container
                    editableText.removeSpan(directParentList)
                    selectedListItems.forEach { editableText.removeSpan(it) }
                }
                listItemSpanBeforeSelection == null && listItemSpanAfterSelection != null -> {
                    if (listItemSpanAfterSelection.nestingLevel == nestingLevel) {
                        // In case there is no predecessor and the successor has the same nesting level, move the list wrapper
                        // to the end of the current selection and remove the selection from the list
                        selectedListItems.outdentAll()
                        directParentList.changeSpanStart(selectionEnd)
                    }
                }
                listItemSpanBeforeSelection != null && listItemSpanAfterSelection == null -> {
                    if (listItemSpanBeforeSelection.nestingLevel >= nestingLevel) {
                        // In case there is no successor and the predecessor has the same nesting level, move the list wrapper
                        // to the start of the current selection and remove the selection from the list
                        selectedListItems.outdentAll()
                        directParentList.changeSpanEnd(firstSelectedItemStart)
                    } else {
                        // Predecessor has a lower nesting level and there is no successor, this means that the currently
                        // selected items can be all moved to lower nesting level and their wrapper can be removed
                        selectedListItems.outdentAll()
                        editableText.removeSpan(directParentList)
                    }
                    directParentListItem?.trimEnd(firstSelectedItemStart)
                }
                listItemSpanBeforeSelection != null && listItemSpanAfterSelection != null -> {
                    if (listItemSpanBeforeSelection.nestingLevel >= nestingLevel) {
                        if (listItemSpanAfterSelection.nestingLevel == nestingLevel) {
                            // Predecessor and successor are on the same level as selected items, this means we have to split
                            // the current list wrapper in half and move the selected items to the lower nesting level
                            selectedListItems.outdentAll()
                            val spanStart = editableText.getSpanStart(directParentList)
                            val spanFlags = editableText.getSpanFlags(directParentList)
                            editableText.setSpan(directParentList.copyList(), spanStart, firstSelectedItemStart, spanFlags)
                            directParentListItem?.apply {
                                val listItemEnd = editableText.getSpanEnd(this)
                                selectedListItems.last().changeSpanEnd(listItemEnd)
                            }
                            directParentList.changeSpanStart(selectionEnd)
                            directParentListItem?.changeSpanEnd(firstSelectedItemStart)
                        } else if (listItemSpanAfterSelection.nestingLevel < nestingLevel) {
                            // In case the span after selection has lower nesting level, we don't have to worry about it
                            selectedListItems.outdentAll()
                            directParentList.changeSpanEnd(firstSelectedItemStart)
                            directParentListItem?.changeSpanEnd(firstSelectedItemStart)
                        }
                    } else if (listItemSpanBeforeSelection.nestingLevel < nestingLevel && listItemSpanAfterSelection.nestingLevel == nestingLevel) {
                        // Predecessor is on lower level and successor is on the same level, this means we can move all the
                        // selected items to lower level and leave the successor on the current level
                        selectedListItems.outdentAll()
                        directParentList.changeSpanStart(selectionEnd)
                        directParentListItem?.changeSpanEnd(firstSelectedItemStart)
                        selectedListItems.last().changeSpanEnd(editableText.getSpanEnd(directParentList))
                    } else if (listItemSpanBeforeSelection.nestingLevel < nestingLevel && listItemSpanAfterSelection.nestingLevel < nestingLevel) {
                        // In this case the selected items are the only items on the current level. Both the successor and
                        // the predecessor are on a lower level. This means we can remove the wrapping span and move all
                        // the selected items to the lower level.
                        selectedListItems.outdentAll()
                        editableText.removeSpan(directParentList)
                        directParentListItem?.changeSpanEnd(firstSelectedItemStart)
                    }
                }
            }
        }
        return true
    }

    /**
     * Returns true if the current position contains a list item that can be outdented. It contains a copy of logic
     * from `outdentList` function without the actual list idents.
     */
    fun isOutdentAvailable(selStart: Int = selectionStart, selEnd: Int = selectionEnd): Boolean {
        val listSpans = editableText.getSpans(selStart, selEnd, AztecListSpan::class.java).filterCorrectSpans(selectionStart = selStart, selectionEnd = selEnd)
        if (listSpans.isEmpty()) return false
        buildListState(listSpans, selStart, selEnd)?.apply {
            when {
                listItemSpanBeforeSelection == null && listItemSpanAfterSelection == null -> {
                    return true
                }
                listItemSpanBeforeSelection == null && listItemSpanAfterSelection != null -> {
                    if (listItemSpanAfterSelection.nestingLevel == nestingLevel) {
                        return true
                    }
                }
                listItemSpanBeforeSelection != null && listItemSpanAfterSelection == null -> {
                    return true
                }
                listItemSpanBeforeSelection != null && listItemSpanAfterSelection != null -> {
                    return if (listItemSpanBeforeSelection.nestingLevel >= nestingLevel) {
                        if (listItemSpanAfterSelection.nestingLevel == nestingLevel) {
                            true
                        } else listItemSpanAfterSelection.nestingLevel < nestingLevel
                    } else if (listItemSpanBeforeSelection.nestingLevel < nestingLevel && listItemSpanAfterSelection.nestingLevel == nestingLevel) {
                        true
                    } else listItemSpanBeforeSelection.nestingLevel < nestingLevel && listItemSpanAfterSelection.nestingLevel < nestingLevel
                }
                else -> return false
            }
        }
        return true
    }

    private data class ListState(
            val nestingLevel: Int,
            val directParentList: AztecListSpan,
            val directParentListItem: AztecListItemSpan?,
            val directParentFlags: Int,
            val selectedListItems: List<AztecListItemSpan>,
            val deeperListSpanBeforeSelection: AztecListSpan?,
            val listSpanAfterSelection: AztecListSpan?,
            val listItemSpanBeforeSelection: AztecListItemSpan?,
            val listItemSpansBeforeSelection: List<AztecListItemSpan>,
            val listItemSpanAfterSelection: AztecListItemSpan?,
            val firstSelectedItemStart: Int,
            val selectionEnd: Int
    )

    private fun buildListState(listSpans: List<AztecListSpan>, selStart: Int, selEnd: Int): ListState? {
        val directParent = listSpans.maxByOrNull { it.nestingLevel } ?: return null
        val topLevelParent = listSpans.minByOrNull { it.nestingLevel } ?: return null
        val fullListStart = editableText.getSpanStart(topLevelParent)
        val fullListEnd = editableText.getSpanEnd(topLevelParent)
        val directParentFlags = editableText.getSpanFlags(directParent)

        var startIndex = selStart
        val selectedItems = editableText.getSpans(selStart, selEnd, AztecListItemSpan::class.java).filterCorrectSpans(selectionStart = selStart, selectionEnd = selEnd)
        val selectedListItems = selectedItems.filter {
            it.nestingLevel > directParent.nestingLevel
        }
        var countdown = selectedListItems.size
        while (countdown > 0) {
            selectedListItems.find { startIndex in editableText.getSpanStart(it) until editableText.getSpanEnd(it) }?.let {
                startIndex = editableText.getSpanEnd(it)
            }
            countdown -= 1
        }
        if (startIndex < selEnd) return null

        val directParentListItem = selectedItems.filter { it.nestingLevel < directParent.nestingLevel }.maxByOrNull { it.nestingLevel }
        if (selectedListItems.isEmpty()) return null
        val nestingLevel = selectedListItems.first().nestingLevel
        if (selectedListItems.any { it.nestingLevel != nestingLevel }) return null
        val firstSelectedItemStart = editableText.getSpanStart(selectedListItems.first())
        val selectionEnd = editableText.getSpanEnd(selectedListItems.last())

        val allLists = editableText.getSpans(fullListStart, fullListEnd, AztecListSpan::class.java)
        val allListItems = editableText.getSpans(fullListStart, fullListEnd, AztecListItemSpan::class.java)
        val deeperListSpanBeforeSelection: AztecListSpan? = allLists.find {
            it.nestingLevel == nestingLevel + 1 && editableText.getSpanEnd(it) == firstSelectedItemStart
        }
        val listSpanAfterSelection: AztecListSpan? = allLists.find {
            val spanStart = editableText.getSpanStart(it)
            val spanEnd = editableText.getSpanEnd(it)
            spanStart in (firstSelectedItemStart + 1)..selectionEnd && spanEnd >= selectionEnd
        }
        val listItemSpansBeforeSelection: List<AztecListItemSpan> = allListItems.filter {
            editableText.getSpanStart(it) < firstSelectedItemStart && editableText.getSpanEnd(it) >= firstSelectedItemStart
        }
        val listItemSpanAfterSelection: AztecListItemSpan? = allListItems.find {
            val spanStart = editableText.getSpanStart(it)
            spanStart in (firstSelectedItemStart + 1)..selectionEnd
        }
        return ListState(
                nestingLevel = nestingLevel,
                directParentList = directParent,
                directParentListItem = directParentListItem,
                directParentFlags = directParentFlags,
                selectedListItems = selectedListItems,
                deeperListSpanBeforeSelection = deeperListSpanBeforeSelection,
                listSpanAfterSelection = listSpanAfterSelection,
                listItemSpanBeforeSelection = listItemSpansBeforeSelection.maxByOrNull { it.nestingLevel },
                listItemSpansBeforeSelection = listItemSpansBeforeSelection,
                listItemSpanAfterSelection = listItemSpanAfterSelection,
                firstSelectedItemStart = firstSelectedItemStart,
                selectionEnd = selectionEnd
        )
    }

    private fun validateSelection(selectedItems: List<AztecListItemSpan>, directParent: AztecListSpan): Boolean {
        val selectedParentItems = selectedItems.filter { it.nestingLevel < directParent.nestingLevel }
        // This means multiple items on several list levels are selected
        if (selectedParentItems.size > 1) {
            return false
        }
        return true
    }

    private fun <T : IAztecBlockSpan> Array<T>.filterCorrectSpans(selectionStart: Int, selectionEnd: Int): List<T> {
        return this.filterIndexed { _, span ->
            val spanStart = editableText.getSpanStart(span)
            var spanEnd = editableText.getSpanEnd(span)
            val endsWithNewLine = editableText.toString().substring(spanStart, spanEnd).endsWith("\n")
            if (endsWithNewLine) {
                spanEnd -= 1
            }
            (spanStart <= selectionEnd && spanEnd >= selectionStart)
        }
    }

    private fun IAztecBlockSpan.changeSpanEnd(newEnd: Int) {
        val spanStart = editableText.getSpanStart(this)
        val spanFlags = editableText.getSpanFlags(this)
        editableText.removeSpan(this)
        editableText.setSpan(this, spanStart, newEnd, spanFlags)
    }

    private fun IAztecBlockSpan.changeSpanStart(newStart: Int) {
        val spanEnd = editableText.getSpanEnd(this)
        val spanFlags = editableText.getSpanFlags(this)
        editableText.removeSpan(this)
        editableText.setSpan(this, newStart, spanEnd, spanFlags)
    }

    private fun List<IAztecBlockSpan>.outdentAll() {
        val nestingLevel = first().nestingLevel
        forEach {
            if (nestingLevel == 2) {
                editableText.removeSpan(it)
            } else {
                it.nestingLevel = nestingLevel - 2
            }
        }
    }

    private fun List<IAztecBlockSpan>.indentAll() {
        val nestingLevel = first().nestingLevel
        forEach {
            it.nestingLevel = nestingLevel + 2
        }
    }

    private fun IAztecBlockSpan.stretchEnd(newEnd: Int) {
        val end = editableText.getSpanEnd(this)
        if (end >= newEnd) {
            return
        }
        val start = editableText.getSpanStart(this)
        val flags = editableText.getSpanFlags(this)
        editableText.removeSpan(this)
        editableText.setSpan(this, start, newEnd, flags)
    }

    private fun IAztecBlockSpan.trimEnd(newEnd: Int) {
        val end = editableText.getSpanEnd(this)
        if (end <= newEnd) {
            return
        }
        val start = editableText.getSpanStart(this)
        val flags = editableText.getSpanFlags(this)
        editableText.removeSpan(this)
        editableText.setSpan(this, start, newEnd, flags)
    }
}

