package org.wordpress.aztec

import android.graphics.Paint.FontMetricsInt
import android.text.TextPaint
import android.text.style.LineHeightSpan
import android.text.style.MetricAffectingSpan

class AztecHeaderSpan(val mHeader: AztecHeaderSpan.Header) : MetricAffectingSpan(), LineHeightSpan {
    enum class Header constructor(internal val mScale: Float) {
        H1(2.0f),
        H2(1.8f),
        H3(1.6f),
        H4(1.4f),
        H5(1.2f),
        H6(1.0f)
    }

    override fun chooseHeight(text: CharSequence, start: Int, end: Int, startspanv: Int, v: Int, fm: FontMetricsInt) {
        fm.bottom = (fm.bottom * 1.5f).toInt()
        fm.descent = (fm.descent * 1.5f).toInt()
    }

    override fun updateDrawState(textPaint: TextPaint) {
        textPaint.textSize *= mHeader.mScale
        textPaint.isFakeBoldText = true
    }

    override fun updateMeasureState(textPaint: TextPaint) {
        textPaint.textSize *= mHeader.mScale
    }
}
