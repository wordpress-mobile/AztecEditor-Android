package org.wordpress.aztec.spans

import android.graphics.Paint
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import android.text.style.LineHeightSpan
import android.text.style.UpdateLayout
import org.wordpress.aztec.ParagraphFlagged


abstract class AztecListSpan(override var nestingLevel: Int, var verticalPadding: Int = 0) : LeadingMarginSpan.Standard(0),
        AztecBlockSpan, ParagraphFlagged, LineHeightSpan, UpdateLayout {

    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1

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

    fun getIndexOfProcessedLine(text: CharSequence, end: Int): Int {
        val spanStart = (text as Spanned).getSpanStart(this)
        val spanEnd = text.getSpanEnd(this)

        val listText = text.subSequence(spanStart, spanEnd)

        val textBeforeBeforeEnd = listText.substring(0, end - spanStart)
        val lineIndex = textBeforeBeforeEnd.length - textBeforeBeforeEnd.replace("\n", "").length
        return lineIndex + 1
    }

}
