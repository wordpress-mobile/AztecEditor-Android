package org.wordpress.aztec.spans

import android.content.Context
import android.text.style.ImageSpan
import android.text.style.ParagraphStyle
import android.view.View
import android.widget.Toast

class UnknownHtmlSpan(rawHtml: StringBuilder, context: Context, drawable: Int) : ImageSpan(context, drawable), ParagraphStyle {

    val mRawHtml: StringBuilder = rawHtml

    fun getRawHtml() : StringBuilder {
        return mRawHtml
    }

    fun onClick(view: View) {
        Toast.makeText(view.context, mRawHtml.toString(), Toast.LENGTH_SHORT).show()
    }

    companion object {
        // Following tags are ignored
        val KNOWN_TAGS = setOf("html", "body")
    }
}
