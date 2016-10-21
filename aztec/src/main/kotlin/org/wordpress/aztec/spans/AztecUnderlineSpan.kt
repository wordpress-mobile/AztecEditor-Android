package org.wordpress.aztec.spans

import android.text.TextUtils
import android.text.style.UnderlineSpan

class AztecUnderlineSpan(override var attributes: String? = null) : UnderlineSpan(), AztecContentSpan {

    private final val TAG = "u"

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