package org.wordpress.aztec.spans

import android.text.TextUtils
import android.text.style.SubscriptSpan

class AztecSubscriptSpan : SubscriptSpan, AztecContentSpan {

    private final var TAG: String = "sub"
    override var attributes: String?

    @JvmOverloads
    constructor(attributes: String? = null) : super() {
        this.attributes = attributes
    }

    override fun getStartTag(): String {
        if (TextUtils.isEmpty(attributes)) {
            return TAG
        }
        return TAG + attributes
    }

    override fun getEndTag(): String {
        return TAG
    }
}