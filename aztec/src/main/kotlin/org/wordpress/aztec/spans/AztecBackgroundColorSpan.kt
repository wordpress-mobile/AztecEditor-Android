package org.wordpress.aztec.spans

import android.graphics.Color
import android.text.TextPaint
import android.text.style.BackgroundColorSpan
import org.wordpress.aztec.AztecAttributes

class AztecBackgroundColorSpan(
        val color: Int
) : BackgroundColorSpan(color), IAztecInlineSpan {

    var alpha: Int = 220
    var tag: String = "span"
    override var attributes: AztecAttributes = AztecAttributes()

    fun getColorHex(): String {
        return java.lang.String.format("#%06X", 0xFFFFFF and color)
    }

    override fun updateDrawState(textPaint: TextPaint) {
        textPaint.bgColor = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    override val TAG = tag
}