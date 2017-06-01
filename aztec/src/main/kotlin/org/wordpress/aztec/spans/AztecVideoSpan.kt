package org.wordpress.aztec.spans

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.Gravity
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText

class AztecVideoSpan(context: Context, drawable: Drawable, override var nestingLevel: Int,
                     attributes: AztecAttributes = AztecAttributes(), onMediaTappedListener: AztecText.OnMediaTappedListener?,
                     editor: AztecText? = null) :
        AztecMediaSpan(context, drawable, attributes, onMediaTappedListener, editor), AztecFullWidthImageSpan, AztecSpan {

    override val TAG: String = "video"

    init {
        setOverlay(0, ContextCompat.getDrawable(context, android.R.drawable.ic_media_play), Gravity.CENTER)
    }
}