package org.wordpress.aztec.spans

import android.text.TextUtils
import android.text.style.TypefaceSpan

class AztecTypefaceSpan : TypefaceSpan, AztecContentSpan {

    var tag: String
    override var attributes: String?

    @JvmOverloads
    constructor(tag: String, family: String, attributes: String? = null) : super(family) {
        this.tag = tag
        this.attributes = attributes
    }

    override fun getStartTag(): String {
        if (TextUtils.isEmpty(attributes)) {
            return tag
        }
        return tag + attributes
    }

    override fun getEndTag(): String {
        return tag
    }
}