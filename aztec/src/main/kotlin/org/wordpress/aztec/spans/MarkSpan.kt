package org.wordpress.aztec.spans

import android.graphics.Color
import android.text.TextPaint
import android.text.style.CharacterStyle
import org.wordpress.aztec.AztecAttributes


class MarkSpan : CharacterStyle, IAztecInlineSpan {
    override var TAG = "mark"

    override var attributes: AztecAttributes = AztecAttributes()
    var textColor: Int? = null

    constructor(attributes: AztecAttributes = AztecAttributes()) : super() {
        this.attributes = attributes
    }

    constructor(attributes: AztecAttributes = AztecAttributes(), colorString: String?) : super() {
        this.attributes = attributes

        if (colorString != null) {
            textColor = Color.parseColor(colorString)
        }
    }

    override fun updateDrawState(tp: TextPaint) {
        configureTextPaint(tp)
    }

    private fun configureTextPaint(tp: TextPaint) {
        if (textColor != null) {
            tp.color = textColor as Int
        }
    }

    fun getTextColor(): String {
        return java.lang.String.format("#%06X", 0xFFFFFF and textColor!!);
    }
}
