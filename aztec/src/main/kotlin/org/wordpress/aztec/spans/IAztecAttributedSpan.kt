package org.wordpress.aztec.spans

import android.text.Editable
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.source.InlineCssStyleFormatter

interface IAztecAttributedSpan {
    var attributes: AztecAttributes

    /**
     * Parses and applies the HTML 'style' attribute.
     * @param output An [Editable] containing an [IAztecAttributedSpan] for processing.
     * @param start The index where the [IAztecAttributedSpan] starts inside the [text]
     */
    fun applyInlineStyleAttributes(output: Editable, start: Int, end: Int) {
        val attr = this.attributes
        if (attr.hasAttribute("style")) {
            InlineCssStyleFormatter.applyInlineStyleAttributes(output, attr, start, end)
        }
    }
}