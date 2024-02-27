package org.wordpress.aztec.spans

import android.graphics.Color
import android.text.TextPaint
import android.text.style.CharacterStyle
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.source.CssStyleFormatter

class MarkSpan : CharacterStyle, IAztecInlineSpan {
    override var TAG = "mark"

    override var attributes: AztecAttributes = AztecAttributes()
    var textColor: Int? = null

    constructor(attributes: AztecAttributes = AztecAttributes()) : super() {
        this.attributes = attributes

        val color = CssStyleFormatter.getStyleAttribute(attributes,
            CssStyleFormatter.CSS_COLOR_ATTRIBUTE)
        if (color.isNotEmpty()) {
            textColor = Color.parseColor(color)
        }
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
        val currentColor = textColor ?: 0
        return String.format("#%06X", 0xFFFFFF and currentColor)
    }
}
