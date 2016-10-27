package org.wordpress.aztec.spans

import android.text.TextPaint
import android.text.style.CharacterStyle


class AztecCursorSpan : CharacterStyle() {
    override fun updateDrawState(tp: TextPaint?) {
    }

    val TAG = "aztec_cursor"
}