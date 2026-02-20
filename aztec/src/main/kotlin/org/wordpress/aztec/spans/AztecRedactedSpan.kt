package org.wordpress.aztec.spans

import android.graphics.Color
import android.text.TextPaint
import android.text.style.CharacterStyle
import org.wordpress.aztec.AztecAttributes

class AztecRedactedSpan(
    override var attributes: AztecAttributes = AztecAttributes()
) : CharacterStyle(), IAztecInlineSpan {

    override val TAG = "span"

    override fun updateDrawState(tp: TextPaint) {
        tp.bgColor = Color.RED
        tp.isStrikeThruText = true
    }

    init {
        attributes.setValue("class", "redacted")
    }
}
