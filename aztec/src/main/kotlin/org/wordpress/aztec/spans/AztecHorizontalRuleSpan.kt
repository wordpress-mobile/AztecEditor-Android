package org.wordpress.aztec.spans

import android.content.Context
import android.graphics.drawable.Drawable
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText

class AztecHorizontalRuleSpan(context: Context, drawable: Drawable, override var nestingLevel: Int,
                              editor: AztecText? = null, override var attributes: AztecAttributes = AztecAttributes()) :
        AztecDynamicImageSpan(context, drawable), AztecFullWidthImageSpan, AztecSpan {

    init {
        textView = editor
    }

    override val TAG: String = "hr"
}
