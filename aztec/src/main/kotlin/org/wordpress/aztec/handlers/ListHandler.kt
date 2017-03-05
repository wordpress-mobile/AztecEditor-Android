package org.wordpress.aztec.handlers

import org.wordpress.aztec.spans.AztecListSpan

class ListHandler(val textDeleter: TextDeleter) : BlockHandler<AztecListSpan>(AztecListSpan::class.java) {

    // fun handleNewlineAtStartOfBlock()
    // we got a newline at the start of the list. Nothing special to do here since the list can have multiple lines
    //  and/or list items

    override fun handleNewlineAtEmptyLineAtBlockEnd() {
        // adjust the list end to only include the chars before the newline just added
        block.end = newlineIndex

        // delete the newline
        textDeleter.delete(newlineIndex, newlineIndex + 1)
    }

    override fun handleNewlineAtEmptyBody() {
        // list only has the empty list item so, remove the list itself as well!
        block.remove()

        // delete the newline
        textDeleter.delete(newlineIndex, newlineIndex + 1)
    }

    // fun handleNewlineAtTextEnd()
    // got a newline while being at the end-of-text. We'll let the block engulf it and handle the . Nothing special to do here.

    // fun handleNewlineInBody()
    // got a newline while at some point in the list. Nothing special to do for the list.

    // fun handleEndOfBufferMarker()
    // nothing special to do
}
