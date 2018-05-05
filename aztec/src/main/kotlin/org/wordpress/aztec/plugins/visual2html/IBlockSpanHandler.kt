package org.wordpress.aztec.plugins.visual2html

import android.annotation.SuppressLint
import org.wordpress.aztec.plugins.IAztecPlugin
import org.wordpress.aztec.spans.IAztecParagraphStyle

/**
 * An interface for processing block spans during visual-to-HTML.
 */
@SuppressLint("NewApi")
interface IBlockSpanHandler : IAztecPlugin {
    /**
     * Determines, whether the plugin can handle a particular [span] type.
     *
     * This method is called by [org.wordpress.aztec.AztecParser] during span-to-HTML parsing.
     *
     * @return true for compatible spans, false otherwise.
     */
    fun canHandleSpan(span: IAztecParagraphStyle): Boolean

    /**
     * A plugin handler used by [org.wordpress.aztec.AztecParser] during span-to-HTML parsing.
     *
     * This method is called when the beginning of a compatible span is encountered.
     *
     * @param html the resulting HTML string output.
     * @param span the encountered span.
     */
    fun handleSpanStart(html: StringBuilder, span: IAztecParagraphStyle)

    /**
     * A plugin handler used by [org.wordpress.aztec.AztecParser] during span-to-HTML parsing.
     *
     * This method is called when the ending of a compatible span is encountered.
     *
     * @param html the resulting HTML string output.
     * @param span the encountered span.
     */
    fun handleSpanEnd(html: StringBuilder, span: IAztecParagraphStyle)
}