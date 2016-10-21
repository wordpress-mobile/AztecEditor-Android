package org.wordpress.aztec.spans

import android.text.TextUtils
import android.text.style.StrikethroughSpan

class AztecStrikethroughSpan() : StrikethroughSpan(), AztecContentSpan {

    private var tag: String = "del"

    override var attributes: String? = null

    constructor(tag: String, attributes: String?) : this() {
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
