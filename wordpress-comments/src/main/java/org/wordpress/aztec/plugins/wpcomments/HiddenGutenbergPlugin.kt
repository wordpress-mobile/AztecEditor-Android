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
        // Note on special case handling for closing </pre> (preformatted) spans:
        // Gutenberg expects to find nothing there between the closing </pre> tag and its proper block end delimiter.
        // Therefore, we need to check here whether this was a preformatted block, and shift <br> position if present,
        // so the ending Gutenberg delimiter is just right after the </pre> ending tag
        // check whether we're just about to process the Gutenberg block ending tag.
        //
        // For example, feed the following code into Aztec:
        //        <!-- wp:preformatted -->
        //        <pre class=\"wp-block-preformatted\">bla bla bla</pre>
        //        <!-- /wp:preformatted -->
        //
        // In visual editor, placing the cursor at the end and hitting ENTER twice to exit the block would end up
        // with this code:
        //        <!-- wp:preformatted -->
        //        <pre class=\"wp-block-preformatted\">bla bla bla</pre><br>
        //        <!-- /wp:preformatted -->
        // Note the misplaced <br> tag, which Gutenberg expects to be after the block end delimiter.
        //
        // As another example, same thing happens with Gutenberg Code blocks:
        // <!-- wp:code -->
        // <pre class="wp-block-code"><code> javascript code here </code></pre>
        // <!-- /wp:code -->
        var foundBreakAfterClosingPreTag = -1
        if (gutenbergSpan.content.trimStart().startsWith("/wp:")) {
            // now, strip the <br> after the last HTML closing </pre> tag if such a thing is found
            foundBreakAfterClosingPreTag = html.lastIndexOf("</pre><br>")
            if (foundBreakAfterClosingPreTag > -1) {
                html.delete(foundBreakAfterClosingPreTag + 6, foundBreakAfterClosingPreTag + 6 + 4)
            }
        }

        html.append("<!--${gutenbergSpan.content}-->")

        if (foundBreakAfterClosingPreTag > -1) {
            // re-attach the <br> tag
            html.append("<br>")
        }
    }

    override fun handleSpanEnd(html: StringBuilder, span: CharacterStyle) {
    }
}