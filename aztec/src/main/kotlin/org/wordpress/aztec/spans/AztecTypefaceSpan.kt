package org.wordpress.aztec.spans

import android.text.TextUtils
import android.text.style.TypefaceSpan
import org.wordpress.aztec.AztecAttributes

open class AztecTypefaceSpan : TypefaceSpan, AztecInlineSpan {

    var tag: String
    override var attributes: AztecAttributes = AztecAttributes()

    @JvmOverloads
    constructor(tag: String, family: String, attributes: AztecAttributes = AztecAttributes()) : super(family) {
        this.tag = tag
        this.attributes = attributes
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