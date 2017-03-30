package org.wordpress.aztec.spans

import android.content.Context
import android.graphics.drawable.Drawable

class AztecCommentSpan(context: Context, drawable: Drawable, override var nestingLevel: Int) :
        AztecDynamicImageSpan(context, drawable), AztecFullWidthImageSpan {
    companion object {
        private val HTML_MORE: String = "more"
        private val HTML_PAGE: String = "nextpage"
    }

    enum class Comment constructor(val html: String) {
        MORE(HTML_MORE),
        PAGE(HTML_PAGE)
    }
}
