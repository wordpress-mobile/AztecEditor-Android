package org.wordpress.aztec.plugins.wpcomments.spans

import android.content.Context
import android.graphics.drawable.Drawable
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.spans.AztecDynamicImageSpan
import org.wordpress.aztec.spans.IAztecFullWidthImageSpan

class GutenbergInlineCommentSpan @JvmOverloads constructor(val commentText: String, context: Context, drawable: Drawable, override var nestingLevel: Int, editor: AztecText? = null) :
        AztecDynamicImageSpan(context, drawable), IAztecFullWidthImageSpan {

    init {
        textView = editor
    }
}
