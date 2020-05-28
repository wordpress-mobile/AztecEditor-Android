package org.wordpress.aztec.spans

import android.graphics.Paint
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import android.text.style.LineHeightSpan
import android.text.style.UpdateLayout
import org.wordpress.aztec.Constants

abstract class AztecListSpan(override var nestingLevel: Int,
                             var verticalPadding: Int = 0
) : LeadingMarginSpan.Standard(0),
        LineHeightSpan,
        UpdateLayout,
        IAztecBlockSpan {
    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1

    override fun chooseHeight(text: CharSequence, start: Int, end: Int, spanstartv: Int, v: Int, fm: Paint.FontMetricsInt) {
        val spanned = text as Spanned
        val spanStart = spanned.getSpanStart(this)
        val spanEnd = spanned.getSpanEnd(this)

        if (start == spanStart || start < spanStart) {
            fm.ascent -= verticalPadding
            fm.top -= verticalPadding
        }
        if (end == spanEnd || spanEnd < end) {
            fm.descent += verticalPadding
            fm.bottom += verticalPadding
        }
    }

    fun getIndexOfProcessedLine(text: CharSequence, end: Int): Int? {
        val spanStart = (text as Spanned).getSpanStart(this)
        val spanEnd = text.getSpanEnd(this)

        val listText = text.subSequence(spanStart, spanEnd) as Spanned

        if (end - spanStart - 1 >= 0 && end - spanStart <= listText.length) {
            val hasSublist = listText.getSpans(end - spanStart - 1, end - spanStart, AztecListSpan::class.java)
                    .any { it.nestingLevel > nestingLevel }
            if (hasSublist) {
                return null
            }
        }

        // only display a line indicator when it's the first line of a list item
        val textBeforeBeforeEnd = listText.subSequence(0, end - spanStart) as Spanned
        val startOfLine = textBeforeBeforeEnd.lastIndexOf(Constants.NEWLINE) + 1
        val isValidListItem = listText.getSpans(0, listText.length, AztecListItemSpan::class.java)
                .any { it.nestingLevel == nestingLevel + 1 && listText.getSpanStart(it) == startOfLine }

        if (!isValidListItem) {
            return null
        }

        // count the list item spans up to the current line with the expected nesting level => item number
        val checkEnd = Math.min(textBeforeBeforeEnd.length + 1, listText.length)
        return listText.getSpans(0, checkEnd, AztecListItemSpan::class.java)
                .filter { it.nestingLevel == nestingLevel + 1 }
                .size
    }

    fun getNumberOfItemsInProcessedLine(text: CharSequence): Int {
        val spanStart = (text as Spanned).getSpanStart(this)
        val spanEnd = text.getSpanEnd(this)

        val listText = text.subSequence(spanStart, spanEnd) as Spanned

        return listText.getSpans(0, listText.length, AztecListItemSpan::class.java)
                .filter { it.nestingLevel == nestingLevel + 1 }
                .size
    }

    fun nestingDepth(text: Spanned, index: Int, nextIndex: Int): Int {
        val finalNextIndex = if (nextIndex > text.length) index else nextIndex
        return IAztecNestable.getNestingLevelAt(text, index, finalNextIndex)
    }
}
