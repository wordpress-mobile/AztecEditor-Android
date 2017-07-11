package org.wordpress.aztec.plugins.wpcomments

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.Spannable
import android.text.style.CharacterStyle
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import org.wordpress.aztec.plugins.IAztecCommentHandler
import org.wordpress.aztec.plugins.wpcomments.spans.WordPressCommentSpan

class WordPressCommentsPlugin(val visualEditor: AztecText) : IAztecCommentHandler {

    override fun canHandle(span: CharacterStyle): Boolean {
        return span is WordPressCommentSpan
    }

    override fun shouldParseContent(): Boolean {
        return false
    }

    override fun handleCommentSpanStart(out: StringBuilder, span: CharacterStyle) {
        out.append("<!--")
        out.append((span as WordPressCommentSpan).commentText)
    }

    override fun handleCommentSpanEnd(out: StringBuilder, span: CharacterStyle){
        out.append("-->")
    }

    override fun handleCommentHtml(text: String, output: Editable, context: Context, nestingLevel: Int) : Boolean {

        val spanStart = output.length

        if (text.toLowerCase() == WordPressCommentSpan.Comment.MORE.html.toLowerCase()) {

            output.append(Constants.MAGIC_CHAR)

            output.setSpan(
                    WordPressCommentSpan(
                            text,
                            context,
                            ContextCompat.getDrawable(context, R.drawable.img_more),
                            nestingLevel
                    ),
                    spanStart,
                    output.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return true
        } else if (text.toLowerCase() == WordPressCommentSpan.Comment.PAGE.html.toLowerCase()) {

            output.append(Constants.MAGIC_CHAR)

            output.setSpan(
                    WordPressCommentSpan(
                            text,
                            context,
                            ContextCompat.getDrawable(context, R.drawable.img_page),
                            nestingLevel
                    ),
                    spanStart,
                    output.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return true
        }
        return false
    }
}