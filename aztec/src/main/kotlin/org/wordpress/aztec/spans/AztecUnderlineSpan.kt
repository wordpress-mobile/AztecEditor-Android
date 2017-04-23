package org.wordpress.aztec.spans

import android.text.TextUtils
import android.text.style.UnderlineSpan
import org.wordpress.aztec.AztecAttributes

class AztecUnderlineSpan(override var attributes: AztecAttributes = AztecAttributes()) : UnderlineSpan(), AztecInlineSpan {

    private val TAG = "u"

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