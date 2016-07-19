package org.wordpress.aztec

import android.content.Context
import android.text.style.ImageSpan
import android.text.style.ParagraphStyle

class UnknownHtmlSpan(rawHtml: StringBuilder, context: Context, drawable: Int) : ImageSpan(context, drawable), ParagraphStyle {

    val mRawHtml: StringBuilder = rawHtml

    fun getRawHtml() : StringBuilder {
        return mRawHtml
    }

    companion object {
        // Following tags are ignored
        val KNOWN_TAGS = setOf("html", "body")
    }
}
