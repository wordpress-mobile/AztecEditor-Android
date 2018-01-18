package org.wordpress.aztec.spans

import android.text.style.UnderlineSpan
import org.wordpress.aztec.AztecAttributes

class AztecUnderlineSpan(val isCssStyle: Boolean = isCssStyleByDefault, override var attributes: AztecAttributes = AztecAttributes()) : UnderlineSpan(), IAztecInlineSpan {
    companion object {
        @JvmStatic
        var isCssStyleByDefault = false
    }
    override val TAG = "u"
}
