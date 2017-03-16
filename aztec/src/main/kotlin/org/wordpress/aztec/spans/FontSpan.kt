package org.wordpress.aztec.spans

import android.text.TextPaint
import android.text.TextUtils
import android.text.style.CharacterStyle
import org.xml.sax.Attributes

class FontSpan : CharacterStyle, AztecInlineSpan {

    private var TAG: String = "font"
    override var attributes: String
    val attrs: Attributes

    @JvmOverloads
    constructor(attributes: String = "", attrs: Attributes) : super() {
        this.attributes = attributes
        this.attrs = attrs
    }

    override fun getStartTag(): String {
        if (TextUtils.isEmpty(attributes)) {
            return TAG
        }
        return TAG + attributes
    }

    override fun getEndTag(): String {
        return TAG
    }

    override fun updateDrawState(tp: TextPaint?) {
    }
}