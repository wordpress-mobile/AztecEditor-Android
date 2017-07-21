package org.wordpress.aztec.plugins.shortcodes.spans

import android.text.TextPaint
import android.text.style.CharacterStyle

class CaptionShortcodeSpan(val attrs: Map<String, String>) : CharacterStyle() {
    var caption: String = ""

    override fun updateDrawState(tp: TextPaint?) {
    }
}