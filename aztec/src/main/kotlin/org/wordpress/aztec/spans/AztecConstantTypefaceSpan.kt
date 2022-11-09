package org.wordpress.aztec.spans

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import org.wordpress.aztec.AztecAttributes

open class AztecConstantTypefaceSpan @JvmOverloads constructor(
    tag: String,
    val typeface: Typeface,
    override var attributes: AztecAttributes = AztecAttributes(),
) : MetricAffectingSpan(), IAztecInlineSpan {
    override val TAG = tag

    override fun updateDrawState(ds: TextPaint) {
        applyTypeface(ds)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyTypeface(paint)
    }

    private fun applyTypeface(paint: Paint) {
        val style: Int
        val old = paint.typeface
        style = old?.style ?: Typeface.NORMAL
        val styledTypeface = Typeface.create(typeface, style)
        val fake = style and styledTypeface.style.inv()
        if (fake and Typeface.BOLD != 0) {
            paint.isFakeBoldText = true
        }
        if (fake and Typeface.ITALIC != 0) {
            paint.textSkewX = -0.25f
        }
        paint.typeface = styledTypeface
    }
}
