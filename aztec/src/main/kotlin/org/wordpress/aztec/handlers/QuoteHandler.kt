package org.wordpress.aztec.handlers

import org.wordpress.aztec.spans.AztecQuoteSpan

class QuoteHandler(val textDeleter: TextDeleter) : BlockHandler<AztecQuoteSpan>(AztecQuoteSpan::class.java) {
    // fun handleNewlineAtStartOfBlock()
    // nothing special to do

    override fun handleNewlineAtEmptyLineAtBlockEnd() {
        // adjust the quote end to only include the chars before the newline just added
        block.end = newlineIndex

        // delete the newline
        textDeleter.delete(newlineIndex, newlineIndex + 1)
    }

    override fun handleNewlineAtEmptyBody() {
        // quote is empty so, remove it
        block.remove()

        // delete the newline
        textDeleter.delete(newlineIndex, newlineIndex + 1)
    }

    // fun handleNewlineAtTextEnd()
    // got a newline while being at the end-of-text. We'll let the quote engulf it.

    // fun handleNewlineInBody()
    // newline added at some position inside the quote. Nothing special to do.

    // fun handleEndOfBufferMarker()
    // nothing special to do
}
