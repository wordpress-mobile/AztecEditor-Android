package org.wordpress.aztec.spans

import android.text.style.SuperscriptSpan
import org.wordpress.aztec.AztecAttributes

class AztecSuperscriptSpan @JvmOverloads constructor(override var attributes: AztecAttributes = AztecAttributes())
    : SuperscriptSpan(), IAztecInlineSpan {
    override val TAG = "sup"
}
