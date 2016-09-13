package org.wordpress.aztec.spans

import android.content.Context
import android.text.style.ImageSpan
import android.text.style.ParagraphStyle
import android.view.View
import android.widget.Toast

internal class AztecCommentSpan(html: StringBuilder, context: Context, drawable: Int) : ImageSpan(context, drawable), ParagraphStyle {
    val mHtml: StringBuilder = html

    fun getHtml(): StringBuilder {
        return mHtml
    }

    fun onClick(view: View) {
        Toast.makeText(view.context, mHtml.toString(), Toast.LENGTH_SHORT).show()
    }
}
