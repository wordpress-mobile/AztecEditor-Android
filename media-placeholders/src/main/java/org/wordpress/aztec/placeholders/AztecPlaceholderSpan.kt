package org.wordpress.aztec.placeholders

import android.content.Context
import android.graphics.drawable.Drawable
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.spans.AztecMediaSpan
import org.wordpress.aztec.spans.IAztecFullWidthImageSpan
import org.wordpress.aztec.spans.IAztecSpan

class AztecPlaceholderSpan(
        context: Context,
        drawable: Drawable?,
        override var nestingLevel: Int,
        attributes: AztecAttributes = AztecAttributes(),
        onMediaDeletedListener: AztecText.OnMediaDeletedListener? = null,
        editor: AztecText? = null) :
        AztecMediaSpan(context, drawable, attributes, onMediaDeletedListener, editor), IAztecFullWidthImageSpan, IAztecSpan {
    override val TAG: String = "placeholder"
    override fun onClick() {

    }
}
