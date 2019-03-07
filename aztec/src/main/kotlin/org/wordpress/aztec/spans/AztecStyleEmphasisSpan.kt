package org.wordpress.aztec.spans

import android.graphics.Typeface
import org.wordpress.aztec.AztecAttributes

class AztecStyleEmphasisSpan(attributes: AztecAttributes = AztecAttributes())
    : AztecStyleSpan(Typeface.ITALIC, attributes) {

    override val TAG by lazy {
        when (style) {
            Typeface.ITALIC -> {
                return@lazy "em"
            }
        }
        throw IllegalArgumentException()
    }
}

