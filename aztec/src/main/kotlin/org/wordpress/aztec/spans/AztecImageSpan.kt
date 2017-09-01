package org.wordpress.aztec.spans

import android.content.Context
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText

class AztecImageSpan(context: Context, imageProvider: IImageProvider, attributes: AztecAttributes = AztecAttributes(),
                     var onImageTappedListener: AztecText.OnImageTappedListener? = null,
                     onMediaDeletedListener: AztecText.OnMediaDeletedListener? = null,
                     editor: AztecText? = null) :
        AztecMediaSpan(context, imageProvider, attributes, onMediaDeletedListener, editor) {

    override val TAG: String = "img"

    override fun onClick() {
        onImageTappedListener?.onImageTapped(attributes, getWidth(drawable), getHeight(drawable))
    }
}