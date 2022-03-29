package org.wordpress.aztec

import android.text.Spannable
import org.wordpress.aztec.formatting.BlockFormatter
import org.wordpress.aztec.spans.AztecListItemSpan
import org.wordpress.aztec.spans.AztecTaskListSpan

class TaskListClickHandler(val listStyle: BlockFormatter.ListStyle) {
    fun handleTaskListClick(text: Spannable, off: Int, x: Int, startMargin: Int): Boolean {
        // We want to make sure that text click will not trigger the checked change
        if (x + startMargin > listStyle.leadingMargin()) return false
        val clickedList = text.getSpans(off, off, AztecTaskListSpan::class.java).firstOrNull()
        val clickedLines = text.getSpans(off, off, AztecListItemSpan::class.java)
        val clickedLine = clickedLines.find {
            val spanStart = text.getSpanStart(it)
            spanStart == off || (spanStart == off - 1 && text.getSpanEnd(it) == off)
        }
        if (clickedList != null && clickedLine != null && clickedList.canToggle()) {
            clickedLine.toggleCheck()
            clickedList.refresh()
            return true
        }
        return false
    }
}

