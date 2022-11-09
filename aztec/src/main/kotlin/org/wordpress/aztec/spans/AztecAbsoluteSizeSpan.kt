package org.wordpress.aztec.spans

import android.text.style.AbsoluteSizeSpan
import androidx.annotation.ColorInt
import org.wordpress.aztec.AztecAttributes

class AztecAbsoluteSizeSpan @JvmOverloads constructor(
    tag: String,
    @ColorInt size: Int,
    dip: Boolean = false,
    override var attributes: AztecAttributes = AztecAttributes(),
) : AbsoluteSizeSpan(size, dip), IAztecInlineSpan {
    override val TAG: String = tag
}