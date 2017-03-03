package org.wordpress.aztec.spans

import android.text.TextUtils
import android.text.style.SubscriptSpan

class AztecSubscriptSpan @JvmOverloads
constructor(override var nestingLevel: Int = 0, override var attributes: String = "") : SubscriptSpan(), AztecInlineSpan {

    private var TAG: String = "sub"

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