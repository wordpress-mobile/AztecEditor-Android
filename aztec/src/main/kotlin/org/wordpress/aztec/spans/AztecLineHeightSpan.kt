package org.wordpress.aztec.spans

import android.graphics.Paint.FontMetricsInt
import android.text.style.LineHeightSpan
import androidx.annotation.Px
import org.wordpress.aztec.AztecAttributes
import kotlin.math.roundToInt

class AztecLineHeightSpan @JvmOverloads constructor(
    tag: String,
    @Px val height: Int,
    override var attributes: AztecAttributes = AztecAttributes(),
) : LineHeightSpan, IAztecInlineSpan {

    override val TAG: String = tag

    override fun chooseHeight(
        text: CharSequence,
        start: Int,
        end: Int,
        spanstartv: Int,
        lineHeight: Int,
        fm: FontMetricsInt,
    ) {
        val originHeight = fm.descent - fm.ascent
        // If original height is not positive, do nothing.
        if (originHeight <= 0) {
            return
        }
        val ratio = height * 1.0f / originHeight
        fm.descent = (fm.descent * ratio).roundToInt()
        fm.ascent = fm.descent - height
    }
}
