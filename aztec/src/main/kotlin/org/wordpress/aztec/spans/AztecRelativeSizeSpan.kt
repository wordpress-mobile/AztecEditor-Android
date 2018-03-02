package org.wordpress.aztec.spans

import android.text.style.RelativeSizeSpan
import org.wordpress.aztec.AztecAttributes

open class AztecRelativeSizeSpan @JvmOverloads constructor(tag: String, size: Float, override var attributes: AztecAttributes = AztecAttributes()) : RelativeSizeSpan(size), IAztecInlineSpan {
    override val TAG = tag
}
