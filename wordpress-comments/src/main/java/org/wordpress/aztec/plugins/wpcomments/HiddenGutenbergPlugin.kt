package org.wordpress.aztec.plugins.wpcomments

import android.text.Editable
import android.text.Spannable
import android.text.style.CharacterStyle
import org.wordpress.aztec.Constants
import org.wordpress.aztec.plugins.html2visual.IHtmlCommentHandler
import org.wordpress.aztec.plugins.visual2html.IInlineSpanHandler
import org.wordpress.aztec.plugins.wpcomments.spans.GutenbergCommentSpan

class HiddenGutenbergPlugin : IHtmlCommentHandler, IInlineSpanHandler {

    override fun handleComment(text: String, output: Editable, nestingLevel: Int): Boolean {
        if (text.trimStart().startsWith("wp:", true) ||
                text.trimStart().startsWith("/wp:", true)) {
            val spanStart = output.length
            output.append(Constants.MAGIC_CHAR)

            output.setSpan(
                    GutenbergCommentSpan(text),
                    spanStart,
                    output.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return true
        }
        return false
    }

    override fun canHandleSpan(span: CharacterStyle): Boolean {
        return span is GutenbergCommentSpan
    }

    override fun shouldParseContent(): Boolean {
        return true
    }

    override fun handleSpanStart(html: StringBuilder, span: CharacterStyle) {
        val gutenbergSpan = span as GutenbergCommentSpan
        html.append("<!--${gutenbergSpan.content}-->")
    }

    override fun handleSpanEnd(html: StringBuilder, span: CharacterStyle) {
    }
}