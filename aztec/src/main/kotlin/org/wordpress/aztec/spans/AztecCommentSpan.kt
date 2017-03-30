package org.wordpress.aztec.spans

import android.content.Context
import android.graphics.drawable.Drawable
import org.wordpress.aztec.AztecText

class AztecCommentSpan @JvmOverloads constructor(context: Context, drawable: Drawable, override var nestingLevel: Int, editor: AztecText? = null) :
        AztecDynamicImageSpan(context, drawable), AztecFullWidthImageSpan {

    init {
        textView = editor
    }

    companion object {
        private val HTML_MORE: String = "more"
        private val HTML_PAGE: String = "nextpage"
    }

    enum class Comment constructor(val html: String) {
        MORE(HTML_MORE),
        PAGE(HTML_PAGE)
    }
}
