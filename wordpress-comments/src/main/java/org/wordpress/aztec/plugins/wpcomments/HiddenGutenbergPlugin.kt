package org.wordpress.aztec.plugins.wpcomments

import android.text.Editable
import android.text.Spannable
import android.text.style.CharacterStyle
import org.wordpress.aztec.Constants
import org.wordpress.aztec.plugins.html2visual.IHtmlCommentHandler
import org.wordpress.aztec.plugins.visual2html.IInlineSpanHandler
import org.wordpress.aztec.plugins.wpcomments.spans.GutenbergCommentSpan
import java.util.regex.Matcher
import java.util.regex.Pattern

class HiddenGutenbergPlugin : IHtmlCommentHandler, IInlineSpanHandler {

    private val REGEX_PREFORMATTED_BLOCK_ENDING =
            "<\\/pre>((?:(?!<\\/pre>|<!-- \\/wp:preformatted -->).)*?)<!-- \\/wp:preformatted -->"
    private val REGEX_CODE_BLOCK_ENDING =
            "<\\/pre>((?:(?!<\\/pre>|<!-- \\/wp:code -->).)*?)<!-- \\/wp:code -->"

    private val patternPreformattedBlock = Pattern.compile(REGEX_PREFORMATTED_BLOCK_ENDING)
    private val patternCodeBlock = Pattern.compile(REGEX_CODE_BLOCK_ENDING)

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

        // Note on special case handling for closing </pre> (preformatted) spans:
        // Gutenberg expects to find nothing there between the closing </pre> tag and its proper block end delimiter.
        // Therefore, we need to check here whether this was a preformatted block, and shift the in-between content
        // position if present, so the ending Gutenberg delimiter is just right after the </pre> ending tag
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

        // check whether we're just about to process the Gutenberg block ending tag.
        if (gutenbergSpan.content.trimStart().startsWith("/wp:")) {
            // we could have one regex match both (and future) cases, but given we need to only work
            // within the same StringBuffer to avoid having a copy of it, it's better to work in
            // separate passes, one for each kind of block end delimiter, and do only one for each
            // as handleSpanStart() gets called

            when (gutenbergSpan.content) {
                " /wp:preformatted " -> handleGutenbergBlockEnclosingTags(html, span, patternPreformattedBlock)
                " /wp:code " -> handleGutenbergBlockEnclosingTags(html, span, patternCodeBlock)
            }
        }
    }

    override fun handleSpanEnd(html: StringBuilder, span: CharacterStyle) {
    }

    fun handleGutenbergBlockEnclosingTags(html: StringBuilder, gutenbergSpan: GutenbergCommentSpan, pattern: Pattern) {
        val matcher : Matcher = pattern.matcher(html)
        var tmpFoundGroup : String
        while (matcher.find()) {
            tmpFoundGroup = matcher.group(1)
            if (tmpFoundGroup.length > 0) {
                // now, take whatever content has been found between the 2 tags </pre> and <!-- /wp:preformatted -->
                // and shift it after the enclosing block end delimiter <!-- /wp:preformatted -->

                // 1st step: delete the content to be shifted from within the passed StringBuffer
                var foundBreakAfterClosingPreTag = html.lastIndexOf(tmpFoundGroup)
                if (foundBreakAfterClosingPreTag > -1) {
                    html.delete(foundBreakAfterClosingPreTag,
                            foundBreakAfterClosingPreTag + tmpFoundGroup.length)
                }

                //2nd step: insert the content right after the GB block end delimiter
                html.insert(foundBreakAfterClosingPreTag + "<!--${gutenbergSpan.content}-->".length,
                        tmpFoundGroup)
            }
        }
    }
}