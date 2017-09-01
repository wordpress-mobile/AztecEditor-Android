package org.wordpress.aztec.spans

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.Gravity
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText

class AztecAudioSpan(context: Context, imageProvider: IImageProvider, override var nestingLevel: Int,
                     attributes: AztecAttributes = AztecAttributes(),
                     var onAudioTappedListener: AztecText.OnAudioTappedListener? = null,
                     onMediaDeletedListener: AztecText.OnMediaDeletedListener? = null,
                     editor: AztecText? = null) :
        AztecMediaSpan(context, imageProvider, attributes, onMediaDeletedListener, editor), IAztecFullWidthImageSpan, IAztecSpan {

    override val TAG: String = "audio"

    init {
        setOverlay(0, ContextCompat.getDrawable(context, android.R.drawable.ic_lock_silent_mode_off), Gravity.CENTER)
    }

    override fun onClick() {
        onAudioTappedListener?.onAudioTapped(attributes)
    }
}