package org.wordpress.aztec.spans

import android.text.TextUtils
import android.text.style.TypefaceSpan

open class AztecTypefaceSpan @JvmOverloads constructor(override var nestingLevel: Int = 0, var tag: String, family: String, override var attributes: String = "") : TypefaceSpan(family), AztecInlineSpan {

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