package org.wordpress.aztec.spans

import android.graphics.Color
import android.text.TextPaint
import android.text.style.CharacterStyle
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.source.CssStyleFormatter

class MarkSpan : CharacterStyle, IAztecInlineSpan {
    override var TAG = "mark"

    override var attributes: AztecAttributes = AztecAttributes()
    private val textColorValue: Int?

    constructor(attributes: AztecAttributes = AztecAttributes()) : super() {
        this.attributes = attributes

        val color = CssStyleFormatter.getStyleAttribute(attributes,
            CssStyleFormatter.CSS_COLOR_ATTRIBUTE)
        textColorValue = if (color.isNotEmpty()) {
            Color.parseColor(color)
        } else {
            null
        }
    }

    constructor(attributes: AztecAttributes = AztecAttributes(), colorString: String?) : super() {
        this.attributes = attributes

        textColorValue = if (colorString != null) {
            Color.parseColor(colorString)
        } else {
            null
        }
    }

    override fun updateDrawState(tp: TextPaint) {
        textColorValue?.let { tp.color = it }
    }

    fun getTextColor(): String {
        val currentColor = textColorValue ?: 0
        return String.format("#%06X", 0xFFFFFF and currentColor)
    }
}
