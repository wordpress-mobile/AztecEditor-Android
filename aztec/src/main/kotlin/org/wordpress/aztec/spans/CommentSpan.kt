package org.wordpress.aztec.spans

import android.text.TextPaint
import android.text.style.CharacterStyle

class CommentSpan(val text: String) : CharacterStyle() {
    var isHidden: Boolean = false
    override fun updateDrawState(tp: TextPaint) {
    }
}
