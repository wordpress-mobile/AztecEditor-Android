package org.wordpress.aztec.spans

import android.content.Context
import android.text.style.ImageSpan

class UnknownHtmlSpan @JvmOverloads constructor(
        override var nestingLevel: Int,
        var rawHtml: StringBuilder,
        context: Context,
        drawable: Int,
        var onUnknownHtmlTappedListener: OnUnknownHtmlTappedListener? = null
    ) : ImageSpan(context, drawable), IAztecParagraphStyle, IAztecNestable {

    fun onClick() {
        onUnknownHtmlTappedListener?.onUnknownHtmlTapped(this)
    }

    companion object {
        // Following tags are ignored
        val KNOWN_TAGS = setOf("html", "body")
    }

    interface OnUnknownHtmlTappedListener {
        fun onUnknownHtmlTapped(unknownHtmlSpan: UnknownHtmlSpan)
    }
}
