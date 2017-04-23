package org.wordpress.aztec.spans

import android.text.TextUtils
import android.text.style.RelativeSizeSpan
import org.wordpress.aztec.AztecAttributes

open class AztecRelativeSizeSpan @JvmOverloads constructor(var tag: String, size: Float, override var attributes: AztecAttributes = AztecAttributes()) : RelativeSizeSpan(size), AztecInlineSpan {

    override fun getStartTag(): String {
        if (attributes.isEmpty()) {
            return tag
        }
        return tag + " " + attributes
    }

    override fun getEndTag(): String {
        return tag
    }
}
