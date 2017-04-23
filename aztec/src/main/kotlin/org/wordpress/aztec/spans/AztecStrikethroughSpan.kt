package org.wordpress.aztec.spans

import android.text.TextUtils
import android.text.style.StrikethroughSpan
import org.wordpress.aztec.AztecAttributes

class AztecStrikethroughSpan() : StrikethroughSpan(), AztecInlineSpan {

    private var tag: String = "del"

    override var attributes: AztecAttributes = AztecAttributes()

    constructor(tag: String, attributes: AztecAttributes = AztecAttributes()) : this() {
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
