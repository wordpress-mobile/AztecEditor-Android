package org.wordpress.aztec.plugins.wpcomments

import android.text.Editable
import android.text.Spannable
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.plugins.IAztecPlugin
import org.wordpress.aztec.plugins.html2visual.IHtmlCommentHandler
import org.wordpress.aztec.plugins.visual2html.IBlockSpanHandler
import org.wordpress.aztec.plugins.wpcomments.handlers.GutenbergCommentHandler
import org.wordpress.aztec.plugins.wpcomments.spans.GutenbergCommentSpan
import org.wordpress.aztec.spans.IAztecParagraphStyle
import org.wordpress.aztec.util.getLast
import org.wordpress.aztec.watchers.BlockElementWatcher

class HiddenGutenbergPlugin @JvmOverloads constructor(private val aztecText: AztecText? = null) :
        IAztecPlugin, IHtmlCommentHandler, IBlockSpanHandler {

    init {
        aztecText?.let {
            BlockElementWatcher(aztecText)
                    .add(GutenbergCommentHandler())
                    .install(aztecText)
        }
    }

    override fun handleComment(text: String, output: Editable, nestingLevel: Int, updateNesting: (Int) -> Unit): Boolean {
        if (text.trimStart().startsWith("wp:", true)) {
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

        return false
    }

    override fun canHandleSpan(span: IAztecParagraphStyle): Boolean {
        return span is GutenbergCommentSpan
    }

    override fun handleSpanStart(html: StringBuilder, span: IAztecParagraphStyle) {
        html.append("<!--${span.startTag}-->")
    }

    override fun handleSpanEnd(html: StringBuilder, span: IAztecParagraphStyle) {
        html.append("<!--${span.endTag}-->")
    }
}