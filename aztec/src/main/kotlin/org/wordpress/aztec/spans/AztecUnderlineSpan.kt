package org.wordpress.aztec.spans

import android.text.style.UnderlineSpan
import org.wordpress.aztec.AztecAttributes

class AztecUnderlineSpan(override var attributes: AztecAttributes = AztecAttributes()) : UnderlineSpan(), IAztecInlineSpan {
    override val TAG = "u"
}
