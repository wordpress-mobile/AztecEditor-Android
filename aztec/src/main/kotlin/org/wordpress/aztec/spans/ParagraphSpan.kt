package org.wordpress.aztec.spans

import android.text.TextUtils
import android.text.style.ParagraphStyle

class ParagraphSpan(
        override var nestingLevel: Int,
        override var attributes: String = ""
    ) : ParagraphStyle, AztecBlockSpan {

    private var TAG: String = "p"

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