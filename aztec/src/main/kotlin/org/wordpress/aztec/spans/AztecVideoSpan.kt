package org.wordpress.aztec.spans

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.view.Gravity
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText

class AztecVideoSpan(context: Context, imageURI: String?, resId : Int?, override var nestingLevel: Int,
                     attributes: AztecAttributes = AztecAttributes(),
                     var onVideoTappedListener: AztecText.OnVideoTappedListener? = null,
                     editor: AztecText? = null) :
        AztecMediaSpan(context, imageURI, resId, attributes, editor), IAztecFullWidthImageSpan, IAztecSpan {

    override val TAG: String = "video"

    init {
        setOverlay(0, ContextCompat.getDrawable(context, android.R.drawable.ic_media_play), Gravity.CENTER)
    }

    override fun onClick() {
        onVideoTappedListener?.onVideoTapped(attributes)
    }
}