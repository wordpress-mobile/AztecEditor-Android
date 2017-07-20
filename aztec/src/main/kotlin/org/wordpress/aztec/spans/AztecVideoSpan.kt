package org.wordpress.aztec.spans

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.view.Gravity
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText

class AztecVideoSpan(context: Context, drawable: Drawable?, override var nestingLevel: Int,
                     attributes: AztecAttributes = AztecAttributes(),
                     var onVideoTappedListener: AztecText.OnVideoTappedListener? = null,
                     editor: AztecText? = null) :
        AztecMediaSpan(context, drawable, attributes, editor), IAztecFullWidthImageSpan, IAztecSpan {

    override val TAG: String = "a"

    init {
        setOverlay(0, ContextCompat.getDrawable(context, android.R.drawable.ic_media_play), Gravity.CENTER)
    }

    override fun getHtml(): String {
        val linkText = attributes.getValue("href") ?: ""

        val sb = StringBuilder()
        sb.append("<")
        sb.append(TAG)
        sb.append(' ')

        attributes.removeAttribute("aztec_id")

        sb.append(attributes)
        sb.append(">")
        sb.append(linkText)
        sb.append("</")
        sb.append(TAG)
        sb.append(">")
        return sb.toString()
    }

    override fun onClick() {
        onVideoTappedListener?.onVideoTapped(attributes)
    }
}