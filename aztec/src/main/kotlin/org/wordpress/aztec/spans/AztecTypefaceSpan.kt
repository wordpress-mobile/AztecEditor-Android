package org.wordpress.aztec.spans

import android.text.style.TypefaceSpan
import org.wordpress.aztec.AztecAttributes

open class AztecTypefaceSpan @JvmOverloads constructor(tag: String, family: String, override var attributes: AztecAttributes = AztecAttributes()) : TypefaceSpan(family), AztecInlineSpan {

    override val TAG = tag
}