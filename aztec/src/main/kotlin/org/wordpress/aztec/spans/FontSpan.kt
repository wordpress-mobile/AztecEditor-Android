package org.wordpress.aztec.spans

import android.text.TextPaint
import android.text.style.CharacterStyle
import org.wordpress.aztec.AztecAttributes
import org.xml.sax.Attributes

class FontSpan(override var attributes: AztecAttributes = AztecAttributes()) : CharacterStyle(), AztecInlineSpan {

    override var TAG = "font"

    override fun updateDrawState(tp: TextPaint?) {
    }
}