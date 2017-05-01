package org.wordpress.aztec.spans

import android.content.Context
import android.text.style.ImageSpan

class UnknownHtmlSpan(
        override var nestingLevel: Int,
        var rawHtml: StringBuilder,
        context: Context,
        drawable: Int,
        private val onClickListener: OnUnknownHtmlClickListener?
    ) : ImageSpan(context, drawable), AztecParagraphStyle, AztecNestable {

    fun onClick() {
        onClickListener?.onUnknownHtmlClicked(this)
    }

    companion object {
        // Following tags are ignored
        val KNOWN_TAGS = setOf("html", "body")
    }

    interface OnUnknownHtmlClickListener {
        fun onUnknownHtmlClicked(unknownHtmlSpan: UnknownHtmlSpan)
    }
}
