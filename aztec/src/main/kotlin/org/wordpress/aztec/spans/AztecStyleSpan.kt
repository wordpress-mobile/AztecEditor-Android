package org.wordpress.aztec.spans

import android.graphics.Typeface
import android.text.TextUtils
import android.text.style.StyleSpan

open class AztecStyleSpan(override var nestingLevel: Int = 0, style: Int, override var attributes: String = "") : StyleSpan(style), AztecInlineSpan {

    var tag: String = ""

    override fun getStartTag(): String {
        if (TextUtils.isEmpty(attributes)) {
            return tag
        }
        return tag + attributes
    }

    override fun getEndTag(): String {
        return tag
    }

    init {
        when (style) {
            Typeface.BOLD -> {
                tag = "b"
            }
            Typeface.ITALIC -> {
                tag = "i"
            }
        }
    }
}