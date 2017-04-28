package org.wordpress.aztec.spans

import android.text.style.SubscriptSpan
import org.wordpress.aztec.AztecAttributes

class AztecSubscriptSpan : SubscriptSpan, AztecInlineSpan {

    private var TAG: String = "sub"
    override var attributes: AztecAttributes = AztecAttributes()

    @JvmOverloads
    constructor(attributes: AztecAttributes = AztecAttributes()) : super() {
        this.attributes = attributes
    }

    override fun getStartTag(): String {
        if (attributes.isEmpty()) {
            return TAG
        }
        return TAG + " " + attributes
    }

    override fun getEndTag(): String {
        return TAG
    }
}