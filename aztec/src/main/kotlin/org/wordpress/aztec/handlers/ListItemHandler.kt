package org.wordpress.aztec.handlers

import android.text.Spannable
import org.wordpress.aztec.AlignmentApproach
import org.wordpress.aztec.spans.AztecListItemSpan
import org.wordpress.aztec.spans.IAztecNestable
import org.wordpress.aztec.spans.createListItemSpan
import org.wordpress.aztec.watchers.TextDeleter

class ListItemHandler(
        val alignmentApproach: AlignmentApproach
) : BlockHandler<AztecListItemSpan>(AztecListItemSpan::class.java) {

    override fun handleNewlineAtStartOfBlock() {
        // newline added at start of bullet so, add a new bullet
        newListItem(text, newlineIndex, newlineIndex + 1, block.span.nestingLevel, alignmentApproach)

        // push current bullet forward
        block.start = newlineIndex + 1
    }

    override fun handleNewlineAtEmptyLineAtBlockEnd() {
        val parent = IAztecNestable.getParent(text, block)

        if (parent == null || (parent.end == 0 && parent.start == 0)) {
            // no parent (or parent has already adjusted its bounds away from this list item so,
            // just remove ourselves and bail
            block.remove()
            return
        }

        if (block.end == parent.end) {
            // just remove list item when entering a newline on an empty item at the end of the list
            block.remove()
        }
    }

    override fun handleNewlineAtEmptyBody() {
        // just remove list item when entering a newline on an empty item at the end of the list
        block.remove()
    }

    // fun handleNewlineAtTextEnd()
    // got a newline while being at the end-of-text. We'll let the current list item engulf it and will wait
    // for the end-of-text marker event in order to attach the new list item to it when that happens.

    override fun handleNewlineInBody() {
        // newline added at some position inside the bullet so, end the current bullet and append a new one

        var newListItemStart = newlineIndex + 1

        if (TextDeleter.isMarkedForDeletion(text, newlineIndex, newlineIndex + 1)) {
            // this newline is marked for deletion (is a double-enter newline) so, let's avoid collapse by anchoring to
            //  the char just before the newline
            newListItemStart = newlineIndex
        }

        newListItem(text, newListItemStart, block.end, block.span.nestingLevel, alignmentApproach)
        block.end = newListItemStart
    }

    override fun handleEndOfBufferMarker() {
        if (block.start == markerIndex) {
            // ok, this list item has the marker as its first char so, nothing more to do here.
            return
        }

        // attach a new bullet around the end-of-text marker
        newListItem(text, markerIndex, markerIndex + 1, block.span.nestingLevel, alignmentApproach)

        // the current list item has bled over to the marker so, let's adjust its range to just before the marker.
        //  There's a newline there hopefully :)
        block.end = markerIndex
    }

    companion object {
        fun newListItem(
                text: Spannable,
                start: Int,
                end: Int,
                nestingLevel: Int,
                alignmentApproach: AlignmentApproach
        ) {
            set(text, createListItemSpan(nestingLevel, alignmentApproach), start, end)
        }
    }
}
