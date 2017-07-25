package org.wordpress.aztec.plugins.wpcomments.spans

import android.content.Context
import android.graphics.drawable.Drawable
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.spans.AztecDynamicImageSpan
import org.wordpress.aztec.spans.IAztecFullWidthImageSpan

class WordPressCommentSpan @JvmOverloads constructor(val commentText: String, context: Context, imageURI: String?, resId : Int?, override var nestingLevel: Int, editor: AztecText? = null) :
        AztecDynamicImageSpan(context, imageURI, resId), IAztecFullWidthImageSpan {

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
