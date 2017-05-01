package org.wordpress.aztec.spans

import android.content.Context
import android.graphics.drawable.Drawable
import org.wordpress.aztec.AztecAttributes

class AztecHorizontalLineSpan(context: Context, drawable: Drawable, override var nestingLevel: Int,
                              override var attributes: AztecAttributes = AztecAttributes()) :
        AztecDynamicImageSpan(context, drawable), AztecFullWidthImageSpan, AztecSpan {

    override val TAG: String = "hr"
}