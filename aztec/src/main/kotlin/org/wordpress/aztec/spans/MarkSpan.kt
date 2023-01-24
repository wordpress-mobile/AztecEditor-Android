package org.wordpress.aztec.spans

import android.text.TextPaint
import android.text.style.CharacterStyle
import org.wordpress.aztec.AztecAttributes

class MarkSpan(override var attributes: AztecAttributes = AztecAttributes()) : CharacterStyle(), IAztecInlineSpan {
    override var TAG = "mark"

    override fun updateDrawState(tp: TextPaint?) {
    }
}
