package org.wordpress.aztec

import android.text.Spannable
import org.wordpress.aztec.formatting.BlockFormatter
import org.wordpress.aztec.spans.AztecListItemSpan
import org.wordpress.aztec.spans.AztecTaskListSpan

class TaskListClickHandler(val listStyle: BlockFormatter.ListStyle) {
    fun handleTaskListClick(text: Spannable, off: Int, x: Int, startMargin: Int): Boolean {
        val clickedList = text.getSpans(off, off, AztecTaskListSpan::class.java).maxByOrNull { it.nestingLevel }
                ?: return false
        // Account for nested leading margins: each nesting level adds one leadingMargin width
        val depthMultiplier = (clickedList.nestingLevel / 2) + 1
        val effectiveMargin = listStyle.leadingMargin() * depthMultiplier
        if (x + startMargin > (effectiveMargin + AztecTaskListSpan.PADDING_SPACE)) return false
        val clickedLines = text.getSpans(off, off, AztecListItemSpan::class.java)
        val clickedLine = clickedLines.find {
            val spanStart = text.getSpanStart(it)
            spanStart == off || (spanStart == off - 1 && text.getSpanEnd(it) == off)
        }
        if (clickedLine != null && clickedList.canToggle()) {
            clickedLine.toggleCheck()
            clickedList.refresh()
            return true
        }
        return false
    }
}

