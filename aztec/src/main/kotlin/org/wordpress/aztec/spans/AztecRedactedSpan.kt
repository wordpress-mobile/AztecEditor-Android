package org.wordpress.aztec.spans

import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.text.style.CharacterStyle
import org.wordpress.aztec.AztecAttributes

class AztecRedactedSpan(
    override var attributes: AztecAttributes = AztecAttributes()
) : CharacterStyle(), IAztecInlineSpan {

    override val TAG = "del"

    init {
        if (attributes.getValue("class").isNullOrEmpty()) {
            attributes.setValue("class", "redacted")
        }
    }

    override fun updateDrawState(tp: TextPaint) {
        tp.bgColor = Color.RED
        tp.flags = tp.flags or Paint.STRIKE_THRU_TEXT_FLAG
    }
}
