package org.wordpress.aztec.spans

import android.text.style.StrikethroughSpan
import org.wordpress.aztec.AztecAttributes

class AztecStrikethroughSpan(tag: String = "del",
                             override var attributes: AztecAttributes = AztecAttributes())
    : StrikethroughSpan(), IAztecInlineSpan {
    override val TAG = tag
}
