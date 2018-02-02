package org.wordpress.aztec.spans

import android.graphics.Paint
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import android.text.style.LineHeightSpan
import android.text.style.UpdateLayout
import java.util.ArrayList

abstract class AztecListSpan(override var nestingLevel: Int,
                             var verticalPadding: Int = 0,
                             override var align: Layout.Alignment? = null) : LeadingMarginSpan.Standard(0),
        IAztecBlockSpan, LineHeightSpan, UpdateLayout {
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

    fun getIndexOfProcessedLine(text: CharSequence, end: Int): Int {
        val spanStart = (text as Spanned).getSpanStart(this)
        val spanEnd = text.getSpanEnd(this)

        val listText = text.subSequence(spanStart, spanEnd) as Spanned

        if (nestingDepth(listText, end - spanStart, end - spanStart + 1) != nestingLevel + 1) {
            // this line has nesting deeper than our own (item) nesting level so, don't display bullet/number
            return -1
        }

        val textBeforeBeforeEnd = listText.subSequence(0, end - spanStart) as Spanned

        // gather the nesting depth for each line
        val nestingDepth = ArrayList<Int>()
        textBeforeBeforeEnd.forEachIndexed {
            i, c -> if (c == '\n') nestingDepth.add(nestingDepth(textBeforeBeforeEnd, i, i + 1))
        }

        // count the lines that have the same nesting depth as us
        var otherLinesAtSameNestingLevel = 0
        for (maxNestingLevel in nestingDepth) {
            if (maxNestingLevel - 1 == nestingLevel) { // the -1 is because the list is one level up than the listitem
                otherLinesAtSameNestingLevel++
            }
        }

        return otherLinesAtSameNestingLevel + 1
    }

    fun nestingDepth(text: Spanned, index: Int, nextIndex: Int): Int {
        val finalNextIndex = if (nextIndex > text.length) index else nextIndex
        return IAztecNestable.getNestingLevelAt(text, index, finalNextIndex)
    }
}
