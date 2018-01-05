package org.wordpress.aztec.plugins.wpcomments

import android.text.Editable
import android.text.Spannable
import android.text.style.CharacterStyle
import org.wordpress.aztec.Constants
import org.wordpress.aztec.plugins.html2visual.IHtmlContentHandler
import org.wordpress.aztec.plugins.html2visual.IHtmlPreprocessor
import org.wordpress.aztec.plugins.visual2html.IInlineSpanHandler
import org.wordpress.aztec.plugins.wpcomments.spans.GutenbergBlockSpan

class HiddenGutenbergPlugin : IInlineSpanHandler, IHtmlContentHandler, IHtmlPreprocessor {
    private val TAG = "wp-gutenberg-block"

    override fun canHandleTag(tag: String): Boolean {
        return tag == TAG
    }

    override fun handleContent(content: String, output: Editable, nestingLevel: Int) {
        val spanStart = output.length
        output.append(Constants.ZERO_WIDTH_PLACEHOLDER_STRING)

        output.setSpan(
                GutenbergBlockSpan(content.replace(Regex("</?$TAG>"), "")),
                spanStart,
                output.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    override fun beforeHtmlProcessed(source: String): String {
        val firstChange = source.replace(Regex("(<!-- ?wp:)"), "<$TAG>$1")
        return firstChange.replace(Regex("(<!-- ?/wp:[^>]*-->)"), "$1</$TAG>")
    }

    override fun canHandleSpan(span: CharacterStyle): Boolean {
        return span is GutenbergBlockSpan
    }

    override fun shouldParseContent(): Boolean {
        return false
    }

    override fun handleSpanStart(html: StringBuilder, span: CharacterStyle) {
    }

    override fun handleSpanEnd(html: StringBuilder, span: CharacterStyle) {
        val gutenbergSpan = span as GutenbergBlockSpan
        html.append(gutenbergSpan.content)
    }
}