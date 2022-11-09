package org.wordpress.aztec.spans

import android.text.style.ForegroundColorSpan
import androidx.annotation.ColorInt
import org.wordpress.aztec.AztecAttributes

class AztecColorSpan @JvmOverloads constructor(
    tag: String,
    @ColorInt val color: Int,
    override var attributes: AztecAttributes = AztecAttributes(),
) : ForegroundColorSpan(color), IAztecInlineSpan {
    override val TAG: String = tag
}