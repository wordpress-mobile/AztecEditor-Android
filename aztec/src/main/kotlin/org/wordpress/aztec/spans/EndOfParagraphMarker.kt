package org.wordpress.aztec.spans

import android.graphics.Paint
import android.text.Spanned
import android.text.style.LineHeightSpan
import android.text.style.UpdateLayout

//Used to mark newline at the end of calypso paragraphs.We
class EndOfParagraphMarker(var verticalPadding: Int = 0) : LineHeightSpan, UpdateLayout {

    override fun chooseHeight(text: CharSequence?, start: Int, end: Int, spanstartv: Int, v: Int, fm: Paint.FontMetricsInt) {
        val spanned = text as Spanned
        val spanEnd = spanned.getSpanEnd(this)

        val actualPadding: Int

        if (spanned.getSpans(spanEnd, spanEnd, AztecQuoteSpan::class.java).any { spanned.getSpanEnd(it) == spanEnd }) {
            actualPadding = 0
        } else {
            actualPadding = verticalPadding * 2
        }

        if (end == spanEnd) {
            //padding is applied only to the bottom of paragraph, so we multiply it by 2
            fm.descent += actualPadding
            fm.bottom += actualPadding
        }
    }
}
