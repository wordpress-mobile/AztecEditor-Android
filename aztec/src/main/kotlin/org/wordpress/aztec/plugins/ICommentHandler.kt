package org.wordpress.aztec.plugins

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.Spanned
import android.text.style.CharacterStyle
import org.wordpress.aztec.plugins.IAztecPlugin

/**
 * An interface for HTML comment processing plugins.
 */
@SuppressLint("NewApi")
interface ICommentHandler : IAztecPlugin {
    /**
     * Determines, whether the content of a comment (the text) should be parsed/rendered by [org.wordpress.aztec.AztecParser]
     *
     * @return true if text should be parsed, false otherwise.
     */
    fun shouldParseContent(): Boolean {
        return true
    }

    /**
     * A plugin handler used by [org.wordpress.aztec.Html] parser during HTML-to-span parsing.
     *
     * This method is called when a comment is encountered in HTML.
     *
     * @param text the content/text of the comment.
     * @param output the parsed output [Editable], used for span manipulation.
     * @param context the Android context.
     * @param nestingLevel the nesting level within the HTML DOM tree.
     *
     * @return true if this plugin handled the comment and no other handler should be called, false otherwise.
     */
    fun handleCommentHtml(text: String, output: Editable, context: Context, nestingLevel: Int) : Boolean {
        return true
    }

    /**
     * Determines, whether the plugin can handle a particular [span] type.
     *
     * This method is called by [org.wordpress.aztec.AztecParser] during span-to-HTML parsing.
     *
     * @return true for compatible spans, false otherwise.
     */
    fun canHandle(span: CharacterStyle): Boolean {
        return true
    }

    /**
     * A plugin handler used by [org.wordpress.aztec.AztecParser] during span-to-HTML parsing.
     *
     * This method is called when the beginning of a compatible span is encountered.
     *
     * @param html the resulting HTML string output.
     * @param span the encountered span.
     */
    fun handleCommentSpanStart(html: StringBuilder, span: CharacterStyle)

    /**
     * A plugin handler used by [org.wordpress.aztec.AztecParser] during span-to-HTML parsing.
     *
     * This method is called when the ending of a compatible span is encountered.
     *
     * @param html the resulting HTML string output.
     * @param span the encountered span.
     */
    fun handleCommentSpanEnd(html: StringBuilder, span: CharacterStyle)
}