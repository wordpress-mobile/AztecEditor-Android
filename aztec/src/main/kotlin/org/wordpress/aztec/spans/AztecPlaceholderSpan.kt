package org.wordpress.aztec.spans

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import androidx.appcompat.content.res.AppCompatResources
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText

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
