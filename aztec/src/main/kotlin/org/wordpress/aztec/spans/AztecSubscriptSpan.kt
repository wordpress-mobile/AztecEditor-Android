package org.wordpress.aztec.spans

import android.text.style.SubscriptSpan
import org.wordpress.aztec.AztecAttributes

class AztecSubscriptSpan @JvmOverloads constructor(override var attributes: AztecAttributes = AztecAttributes()) : SubscriptSpan(), AztecInlineSpan {

    override val TAG = "sub"
}