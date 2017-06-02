package org.wordpress.aztec.spans

import android.content.Context
import android.graphics.drawable.Drawable
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText

class AztecImageSpan(context: Context, drawable: Drawable?, attributes: AztecAttributes = AztecAttributes(),
                     val onImageTappedListener: AztecText.OnImageTappedListener?,
                     editor: AztecText? = null) :
        AztecMediaSpan(context, drawable, attributes, editor) {

    override val TAG: String = "img"

    override fun onClick() {
        onImageTappedListener?.onImageTapped(attributes, getWidth(imageDrawable), getHeight(imageDrawable))
    }
}