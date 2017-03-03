package org.wordpress.aztec.spans

import android.text.TextPaint
import android.text.TextUtils
import android.text.style.CharacterStyle
import org.xml.sax.Attributes

class FontSpan @JvmOverloads constructor(override var nestingLevel: Int = 0, override var attributes: String = "", val attrs: Attributes) : CharacterStyle(), AztecInlineSpan {

    private var TAG: String = "font"

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