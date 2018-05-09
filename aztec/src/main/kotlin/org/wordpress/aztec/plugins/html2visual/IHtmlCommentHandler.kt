package org.wordpress.aztec.plugins.html2visual

import android.annotation.SuppressLint
import android.text.Editable

/**
 * An interface for HTML comment processing plugins.
 */
@SuppressLint("NewApi")
interface IHtmlCommentHandler {
    /**
     * A plugin handler used by [org.wordpress.aztec.Html] parser during HTML-to-span parsing.
     *
     * This method is called when a comment is encountered in HTML.
     *
     * @param text the content/text of the comment.
     * @param output the parsed output [Editable], used for span manipulation.
     * @param nestingLevel the nesting level within the HTML DOM tree.
     *
     * @return true if this plugin handled the comment and no other handler should be called, false otherwise.
     */
    fun handleComment(text: String, output: Editable, nestingLevel: Int, updateNesting: (Int) -> Unit): Boolean {
        return true
    }
}