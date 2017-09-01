package org.wordpress.aztec.plugins.wpcomments

import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.Spannable
import android.text.style.CharacterStyle
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import org.wordpress.aztec.plugins.html2visual.IHtmlCommentHandler
import org.wordpress.aztec.plugins.visual2html.IInlineSpanHandler
import org.wordpress.aztec.plugins.wpcomments.spans.WordPressCommentSpan
import org.wordpress.aztec.spans.AztecDynamicImageSpan

class WordPressCommentsPlugin(val visualEditor: AztecText) : IInlineSpanHandler, IHtmlCommentHandler {

    override fun canHandleSpan(span: CharacterStyle): Boolean {
        return span is WordPressCommentSpan
    }

    override fun shouldParseContent(): Boolean {
        return false
    }

    override fun handleSpanStart(html: StringBuilder, span: CharacterStyle) {
        html.append("<!--")
        html.append((span as WordPressCommentSpan).commentText)
    }

    override fun handleSpanEnd(html: StringBuilder, span: CharacterStyle){
        html.append("-->")
    }

    override fun handleComment(text: String, output: Editable, nestingLevel: Int) : Boolean {

        val spanStart = output.length

        if (text.toLowerCase() == WordPressCommentSpan.Comment.MORE.html.toLowerCase()) {

            output.append(Constants.MAGIC_CHAR)

            output.setSpan(
                    WordPressCommentSpan(
                            text,
                            visualEditor.context,
                            object : AztecDynamicImageSpan.IImageProvider {
                                override fun requestImage(span: AztecDynamicImageSpan) {
                                    span.drawable = ContextCompat.getDrawable(visualEditor.context, R.drawable.img_more)
                                }
                            },
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
                            visualEditor.context,
                            object : AztecDynamicImageSpan.IImageProvider {
                                override fun requestImage(span: AztecDynamicImageSpan) {
                                    span.drawable = ContextCompat.getDrawable(visualEditor.context, R.drawable.img_page)
                                }
                            },
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