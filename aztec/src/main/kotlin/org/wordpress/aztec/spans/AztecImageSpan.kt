package org.wordpress.aztec.spans

import android.content.Context
import android.graphics.drawable.Drawable
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText

class AztecImageSpan(context: Context, drawable: Drawable?, attributes: AztecAttributes = AztecAttributes(),
                     onMediaTappedListener: AztecText.OnMediaTappedListener?,
                     editor: AztecText? = null) :
        AztecMediaSpan(context, drawable, attributes, onMediaTappedListener, editor) {

    override val TAG: String = "img"
}