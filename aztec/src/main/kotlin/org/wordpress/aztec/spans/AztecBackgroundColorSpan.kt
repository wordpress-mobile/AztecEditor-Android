package org.wordpress.aztec.spans

import android.text.style.BackgroundColorSpan
import org.wordpress.aztec.AztecAttributes

class AztecBackgroundColorSpan(
        bgColor: Int,
        val bgColorHex: String,
        tag: String = "span",
        override var attributes: AztecAttributes = AztecAttributes()
) : BackgroundColorSpan(bgColor), IAztecInlineSpan {
    constructor(color: Int) : this(color, java.lang.String.format("#%06X", 0xFFFFFF and color))

    override val TAG = tag
}