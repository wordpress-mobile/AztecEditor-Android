package org.wordpress.aztec.spans

import android.content.Context
import android.graphics.drawable.Drawable
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText

class AztecImageSpan(context: Context, drawable: Drawable?,
                     override var nestingLevel: Int,
                     attributes: AztecAttributes = AztecAttributes(),
                     var onImageTappedListener: AztecText.OnImageTappedListener? = null,
                     onMediaDeletedListener: AztecText.OnMediaDeletedListener? = null,
                     editor: AztecText? = null) : IAztecFullWidthImageSpan,
        AztecMediaSpan(context, drawable, attributes, onMediaDeletedListener, editor) {
    override val TAG: String = "img"

    override fun onClick() {
        onImageTappedListener?.onImageTapped(attributes, getWidth(imageDrawable), getHeight(imageDrawable))
    }
}
