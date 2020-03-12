package org.wordpress.aztec.plugins.wpcomments

import android.text.Editable
import android.text.Spannable
import android.text.style.CharacterStyle
import androidx.appcompat.content.res.AppCompatResources
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import org.wordpress.aztec.plugins.IAztecPlugin
import org.wordpress.aztec.plugins.html2visual.IHtmlCommentHandler
import org.wordpress.aztec.plugins.visual2html.IBlockSpanHandler
import org.wordpress.aztec.plugins.visual2html.IInlineSpanHandler
import org.wordpress.aztec.plugins.wpcomments.handlers.GutenbergCommentHandler
import org.wordpress.aztec.plugins.wpcomments.spans.GutenbergCommentSpan
import org.wordpress.aztec.plugins.wpcomments.spans.GutenbergInlineCommentSpan
import org.wordpress.aztec.spans.IAztecParagraphStyle
import org.wordpress.aztec.util.getLast
import org.wordpress.aztec.watchers.BlockElementWatcher

class HiddenGutenbergPlugin @JvmOverloads constructor(private val aztecText: AztecText? = null) :
        IAztecPlugin, IHtmlCommentHandler, IBlockSpanHandler, IInlineSpanHandler {

    init {
        aztecText?.let {
            BlockElementWatcher(aztecText)
                    .add(GutenbergCommentHandler())
                    .install(aztecText)
        }
    }

    override fun handleComment(text: String, output: Editable, nestingLevel: Int, updateNesting: (Int) -> Unit): Boolean {
        // Here we handle GB block comments
        if (text.trimStart().startsWith("wp:", true) && !text.trimEnd().endsWith("/")) {
            val spanStart = output.length
            output.setSpan(
                    GutenbergCommentSpan(text, nestingLevel + 1),
                    spanStart,
                    output.length,
                    Spannable.SPAN_MARK_MARK
            )
            updateNesting(nestingLevel + 1)

            return true
        } else if (text.trimStart().startsWith("/wp:", true)) {
            val span = output.getLast<GutenbergCommentSpan>()
            span?.let {
                span.endTag = text
                output.setSpan(span, output.getSpanStart(span), output.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            updateNesting(nestingLevel - 1)

            return true
        }

        // Here we handle GB inline comments
        if (aztecText != null && text.trimStart().startsWith("wp:", true)
                && text.trimEnd().endsWith("/")) {
            val spanStart = output.length
            output.append(Constants.MAGIC_CHAR)
            output.setSpan(
                    GutenbergInlineCommentSpan(
                            text,
                            aztecText.context,
                            AppCompatResources.getDrawable(aztecText.context, android.R.drawable.ic_menu_help)!!,
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

    /* GB block comments handling below */
    override fun canHandleSpan(span: IAztecParagraphStyle): Boolean {
        return span is GutenbergCommentSpan
    }

    override fun handleSpanStart(html: StringBuilder, span: IAztecParagraphStyle) {
        html.append("<!--${span.startTag}-->")
    }

    override fun handleSpanEnd(html: StringBuilder, span: IAztecParagraphStyle) {
        html.append("<!--${span.endTag}-->")
    }

    /* GB inline comments handling below */
    override fun canHandleSpan(span: CharacterStyle): Boolean {
        return span is GutenbergInlineCommentSpan
    }

    override fun handleSpanStart(html: StringBuilder, span: CharacterStyle) {
        html.append("<!--")
        html.append((span as GutenbergInlineCommentSpan).commentText)
    }
    override fun handleSpanEnd(html: StringBuilder, span: CharacterStyle) {
        html.append("-->")
    }
}
