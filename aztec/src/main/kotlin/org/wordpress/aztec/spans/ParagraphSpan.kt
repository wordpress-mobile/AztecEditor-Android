package org.wordpress.aztec.spans

import android.text.TextUtils
import android.text.style.ParagraphStyle

class ParagraphSpan : ParagraphStyle, AztecBlockSpan {

    private var TAG: String = "p"
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