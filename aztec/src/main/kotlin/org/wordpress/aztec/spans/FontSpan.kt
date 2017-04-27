package org.wordpress.aztec.spans

import android.text.TextPaint
import android.text.style.CharacterStyle
import org.wordpress.aztec.AztecAttributes
import org.xml.sax.Attributes

class FontSpan : CharacterStyle, AztecInlineSpan {

    private var TAG: String = "font"
    override var attributes: AztecAttributes = AztecAttributes()

    @JvmOverloads
    constructor(attrs: Attributes) : super() {
        this.attributes = AztecAttributes(attrs)
    }

    override fun getStartTag(): String {
        if (attributes.isEmpty()) {
            return TAG
        }
        return TAG + " " + attributes
    }

    override fun getEndTag(): String {
        return TAG
    }

    override fun updateDrawState(tp: TextPaint?) {
    }
}