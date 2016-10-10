package org.wordpress.aztec.spans

import android.text.TextUtils
import android.text.style.StrikethroughSpan

class AztecStrikethroughSpan() : StrikethroughSpan(), AztecSpan {

    private var tag: String = "del"

    override var attributes: String? = null

    constructor(tag: String, attributes: String?) : this() {
        this.tag = tag
        this.attributes = attributes
    }

    override fun getStartTag(): String {
        return tag + attributes
    }

    override fun getEndTag(): String {
        return tag
    }
}
