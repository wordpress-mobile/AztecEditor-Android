package org.wordpress.aztec.spans

import android.text.TextUtils
import android.text.style.RelativeSizeSpan

open class AztecRelativeSizeSpan @JvmOverloads constructor(override var nestingLevel: Int = 0, var tag: String, size: Float, override var attributes: String = "") : RelativeSizeSpan(size), AztecInlineSpan {

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
