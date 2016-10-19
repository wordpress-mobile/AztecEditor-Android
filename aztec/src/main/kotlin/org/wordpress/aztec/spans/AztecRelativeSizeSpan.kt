package org.wordpress.aztec.spans

import android.text.TextUtils
import android.text.style.RelativeSizeSpan

class AztecRelativeSizeSpan : RelativeSizeSpan, AztecContentSpan {

    var tag: String = ""
    override var attributes: String?

    @JvmOverloads
    constructor(tag: String, size: Float, attributes: String? = null) : super(size) {
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