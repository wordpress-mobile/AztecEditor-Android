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
        if (end == spanEnd) {
            fm.descent += verticalPadding
            fm.bottom += verticalPadding
        }
    }
}
