package org.wordpress.aztec.spans

import android.graphics.Typeface
import org.wordpress.aztec.AztecAttributes

class AztecStyleStrongSpan(attributes: AztecAttributes = AztecAttributes())
    : AztecStyleSpan(Typeface.BOLD, attributes) {

    override val TAG by lazy {
        when (style) {
            Typeface.BOLD -> {
                return@lazy "strong"
            }
        }
        throw IllegalArgumentException()
    }
}
