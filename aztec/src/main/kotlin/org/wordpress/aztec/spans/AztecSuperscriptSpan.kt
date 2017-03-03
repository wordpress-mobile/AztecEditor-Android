package org.wordpress.aztec.spans

import android.text.TextUtils
import android.text.style.SuperscriptSpan

class AztecSuperscriptSpan @JvmOverloads constructor(override var nestingLevel: Int = 0, override var attributes: String = "") : SuperscriptSpan(), AztecInlineSpan {

    private var TAG: String = "sup"

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