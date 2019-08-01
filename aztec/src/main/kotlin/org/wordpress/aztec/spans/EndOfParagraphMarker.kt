package org.wordpress.aztec.spans

import android.graphics.Paint
import android.text.Spanned
import android.text.style.LineHeightSpan
import android.text.style.UpdateLayout
import org.wordpress.aztec.Constants.ZWJ_CHAR

// Used to mark newline at the end of calypso paragraphs
class EndOfParagraphMarker(var verticalPadding: Int = 0) : LineHeightSpan, UpdateLayout {
    override fun chooseHeight(text: CharSequence?, start: Int, end: Int, spanstartv: Int, v: Int,
                              fm: Paint.FontMetricsInt) {
        val spanned = text as Spanned
        val spanEnd = spanned.getSpanEnd(this)

        val actualPadding: Int

        if (spanned.getSpans(spanEnd, spanEnd, AztecQuoteSpan::class.java).any { spanned.getSpanEnd(it) == spanEnd }) {
            actualPadding = 0
        } else {
            actualPadding = if (spanned.length >= spanEnd && spanned[spanEnd - 1] == ZWJ_CHAR) {
                0
            } else {
                verticalPadding * 2
            }
        }

        if (end == spanEnd) {
            fm.descent += actualPadding
            fm.bottom += actualPadding
        }
    }
}
