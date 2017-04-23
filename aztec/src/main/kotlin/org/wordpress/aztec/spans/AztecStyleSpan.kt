package org.wordpress.aztec.spans

import android.graphics.Typeface
import android.text.TextUtils
import android.text.style.StyleSpan
import org.wordpress.aztec.AztecAttributes

open class AztecStyleSpan : StyleSpan, AztecInlineSpan {

    var tag: String = ""
    override var attributes: AztecAttributes = AztecAttributes()

    constructor(style: Int, attributes: AztecAttributes = AztecAttributes()) : super(style) {
        this.attributes = attributes

        when (style) {
            Typeface.BOLD -> {
                tag = "b"
            }
            Typeface.ITALIC -> {
                tag = "i"
            }
        }
    }

    override fun getStartTag(): String {
        if (attributes.isEmpty()) {
            return tag
        }
        return tag + " " + attributes
    }

    override fun getEndTag(): String {
        return tag
    }
}