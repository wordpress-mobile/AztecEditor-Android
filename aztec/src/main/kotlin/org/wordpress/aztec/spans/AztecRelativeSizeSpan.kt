package org.wordpress.aztec.spans

import android.text.TextUtils
import android.text.style.RelativeSizeSpan

class AztecRelativeSizeSpan @JvmOverloads constructor(var tag: String, size: Float, override var attributes: String = "") : RelativeSizeSpan(size), AztecContentSpan, AztecInlineSpan {

    override fun getStartTag(): String {
        if (TextUtils.isEmpty(attributes)) {
            return tag
        }
        return tag + attributes
    }

    override fun getEndTag(): String {
        return tag
    }
}