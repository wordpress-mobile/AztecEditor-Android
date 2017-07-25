package org.wordpress.aztec.spans

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.R

class AztecHorizontalRuleSpan(context: Context, resId : Int, override var nestingLevel: Int,
                              editor: AztecText? = null, override var attributes: AztecAttributes = AztecAttributes()) :
        AztecDynamicImageSpan(context, null, resId), IAztecFullWidthImageSpan, IAztecSpan {

    init {
        textView = editor
    }

    override val TAG: String = "hr"
}
