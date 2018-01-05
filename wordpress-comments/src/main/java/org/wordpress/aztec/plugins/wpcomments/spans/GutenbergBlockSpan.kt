package org.wordpress.aztec.plugins.wpcomments.spans

import android.text.TextPaint
import android.text.style.CharacterStyle

class GutenbergBlockSpan(var content: String) : CharacterStyle() {
    override fun updateDrawState(tp: TextPaint) {
    }
}