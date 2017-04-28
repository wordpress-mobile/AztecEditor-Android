package org.wordpress.aztec.spans

import android.content.Context
import android.graphics.drawable.Drawable
import org.wordpress.aztec.AztecAttributes

class AztecHorizontalLineSpan(context: Context, drawable: Drawable, override var nestingLevel: Int) :
        AztecDynamicImageSpan(context, drawable), AztecFullWidthImageSpan, AztecSpan {

    private val TAG: String = "hr"

    override var attributes: AztecAttributes = AztecAttributes()

    override fun getStartTag(): String {
        if (attributes.isEmpty()) {
            return TAG
        }
        return TAG + " " + attributes
    }

    override fun getEndTag(): String {
        return TAG
    }
}