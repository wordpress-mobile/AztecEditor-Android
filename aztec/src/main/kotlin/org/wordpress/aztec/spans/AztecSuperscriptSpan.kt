package org.wordpress.aztec.spans

import android.text.TextUtils
import android.text.style.SuperscriptSpan

class AztecSuperscriptSpan : SuperscriptSpan, AztecContentSpan, AztecInlineSpan {

    private var TAG: String = "sup"
    override var attributes: String

    @JvmOverloads
    constructor(attributes: String = "") : super() {
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