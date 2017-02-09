package org.wordpress.aztec.spans

import android.content.Context
import android.content.DialogInterface
import android.text.style.ImageSpan
import android.view.View
import android.widget.Toast
import org.wordpress.aztec.AztecText

class UnknownHtmlSpan(var rawHtml: StringBuilder, context: Context, drawable: Int, private val onClickListener: OnUnknownHtmlClickListener?) : ImageSpan(context, drawable), AztecParagraphStyle {

    fun onClick(view: View) {
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
