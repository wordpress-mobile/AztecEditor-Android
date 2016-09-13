package org.wordpress.aztec.spans

import android.content.Context
import android.text.style.ImageSpan
import android.view.View
import android.widget.Toast

internal class AztecCommentSpan(val mComment: AztecCommentSpan.Comment, context: Context, drawable: Int) : ImageSpan(context, drawable) {
    companion object {
        private val HTML_MORE: String = "<!--more-->"
        private val HTML_PAGE: String = "<!--nextpage-->"
    }

    enum class Comment constructor(internal val mHtml: String) {
        MORE(HTML_MORE),
        PAGE(HTML_PAGE)
    }

    fun onClick(view: View) {
        Toast.makeText(view.context, mComment.mHtml.toString(), Toast.LENGTH_SHORT).show()
    }
}
