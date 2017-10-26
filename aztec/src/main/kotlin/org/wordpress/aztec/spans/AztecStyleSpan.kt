package org.wordpress.aztec.spans

import android.graphics.Typeface
import android.text.style.StyleSpan
import org.wordpress.aztec.AztecAttributes

open class AztecStyleSpan(style: Int, override var attributes: AztecAttributes = AztecAttributes())
    : StyleSpan(style), IAztecInlineSpan {
    override val TAG by lazy {
        when (style) {
            Typeface.BOLD -> {
                return@lazy "b"
            }
            Typeface.ITALIC -> {
                return@lazy "i"
            }
        }
        throw IllegalArgumentException()
    }
}
