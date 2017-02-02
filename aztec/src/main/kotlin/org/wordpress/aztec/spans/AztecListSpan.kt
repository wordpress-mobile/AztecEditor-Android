package org.wordpress.aztec.spans

import android.graphics.Paint
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import android.text.style.LineHeightSpan
import android.text.style.UpdateLayout


abstract class AztecListSpan(val verticalPadding: Int) : LeadingMarginSpan.Standard(0), AztecBlockSpan, LineHeightSpan, UpdateLayout {

    abstract var lastItem: AztecListItemSpan

    override fun chooseHeight(text: CharSequence, start: Int, end: Int, spanstartv: Int, v: Int, fm: Paint.FontMetricsInt) {
        val spanned = text as Spanned
        val spanStart = spanned.getSpanStart(this)
        val spanEnd = spanned.getSpanEnd(this)

        if (start === spanStart || start < spanStart) {
            fm.ascent -= verticalPadding
            fm.top -= verticalPadding
        }
        if (end === spanEnd || spanEnd < end) {
            fm.descent += verticalPadding
            fm.bottom += verticalPadding
        }
    }

    fun getNumberOfProcessedLine(text: CharSequence, end: Int): Int {
        val spanStart = (text as Spanned).getSpanStart(this)
        val spanEnd = text.getSpanEnd(this)

        val listText = text.subSequence(spanStart, spanEnd)

        val textBeforeBeforeEnd = listText.substring(0, end - spanStart)
        val lineIndex = textBeforeBeforeEnd.length - textBeforeBeforeEnd.replace("\n", "").length
        return lineIndex + 1
    }

    fun getTotalNumberOfLines(text: CharSequence): Int {
        val spanStart = (text as Spanned).getSpanStart(this)
        val spanEnd = text.getSpanEnd(this)

        return text.substring(spanStart..spanEnd - 2).split("\n").count()
    }

    fun getIndicatorAdjustment(text: CharSequence, end: Int): Int {
        var adjustment = 0

        val totalNumberOfLinesInList = getTotalNumberOfLines(text)
        val currentLineNumber = getNumberOfProcessedLine(text, end)

        if (totalNumberOfLinesInList > 1 && currentLineNumber == 1) {
            adjustment = if (this is AztecOrderedListSpan) 0 else verticalPadding
        } else if ((totalNumberOfLinesInList > 1 && totalNumberOfLinesInList == currentLineNumber) || totalNumberOfLinesInList == 1) {
            adjustment = -verticalPadding
        }

        return adjustment
    }

}
