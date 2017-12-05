package org.wordpress.aztec.plugins.visual2html

import android.annotation.SuppressLint
import android.text.style.CharacterStyle
import org.wordpress.aztec.plugins.IAztecPlugin

/**
 * An interface for processing spans during visual-to-HTML.
 */
@SuppressLint("NewApi")
interface IInlineSpanHandler : IAztecPlugin {
    /**
     * Determines, whether the content of a span (text, if any) should be parsed/rendered by [org.wordpress.aztec.AztecParser]
     *
     * @return true if content should be parsed, false otherwise.
     */
    fun shouldParseContent(): Boolean {
        return true
    }

    /**
     * Determines, whether the plugin can handle a particular [span] type.
     *
     * This method is called by [org.wordpress.aztec.AztecParser] during span-to-HTML parsing.
     *
     * @return true for compatible spans, false otherwise.
     */
    fun canHandleSpan(span: CharacterStyle): Boolean

    /**
     * A plugin handler used by [org.wordpress.aztec.AztecParser] during span-to-HTML parsing.
     *
     * This method is called when the beginning of a compatible span is encountered.
     *
     * @param html the resulting HTML string output.
     * @param span the encountered span.
     */
    fun handleSpanStart(html: StringBuilder, span: CharacterStyle)

    /**
     * A plugin handler used by [org.wordpress.aztec.AztecParser] during span-to-HTML parsing.
     *
     * This method is called when the ending of a compatible span is encountered.
     *
     * @param html the resulting HTML string output.
     * @param span the encountered span.
     */
    fun handleSpanEnd(html: StringBuilder, span: CharacterStyle)
}