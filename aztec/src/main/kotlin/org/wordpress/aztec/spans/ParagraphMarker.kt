package org.wordpress.aztec.spans

import android.graphics.Paint
import android.text.Spanned
import android.text.style.LineHeightSpan
import android.text.style.UpdateLayout


class ParagraphMarker(var verticalPadding: Int = 0) : LineHeightSpan, UpdateLayout {

    override fun chooseHeight(text: CharSequence?, start: Int, end: Int, spanstartv: Int, v: Int, fm: Paint.FontMetricsInt) {
        fm.ascent -= verticalPadding
        fm.top -= verticalPadding
        fm.descent += verticalPadding
        fm.bottom += verticalPadding
    }
}