package org.wordpress.aztec.spans

import android.content.Context
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText

class AztecHorizontalRuleSpan(context: Context, imageProvider: IImageProvider, override var nestingLevel: Int,
                              editor: AztecText? = null, override var attributes: AztecAttributes = AztecAttributes()) :
        AztecDynamicImageSpan(context, imageProvider), IAztecFullWidthImageSpan, IAztecSpan {

    init {
        textView = editor
    }

    override val TAG: String = "hr"
}
