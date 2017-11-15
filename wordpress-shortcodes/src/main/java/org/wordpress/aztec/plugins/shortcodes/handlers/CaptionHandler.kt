package org.wordpress.aztec.plugins.shortcodes.handlers

import org.wordpress.aztec.handlers.BlockHandler
import org.wordpress.aztec.handlers.GenericBlockHandler
import org.wordpress.aztec.plugins.shortcodes.spans.CaptionShortcodeSpan
import org.wordpress.aztec.watchers.TextDeleter

class CaptionHandler : BlockHandler<CaptionShortcodeSpan>(CaptionShortcodeSpan::class.java) {
    override fun handleNewlineInBody() {
        block.end = newlineIndex + 1
    }

    override fun handleNewlineAtEmptyLineAtBlockEnd() {
        // just remote the block since it's empty
        block.remove()

        // delete the newline as it's purpose was served (to translate it as a command to close the block)
        TextDeleter.mark(text, newlineIndex, newlineIndex + 1)
    }

    override fun handleEndOfBufferMarker() {
        if (block.start == markerIndex) {
            // ok, this list item has the marker as its first char so, nothing more to do here.
            return
        }

        // the heading has bled over to the marker so, let's adjust its range to just before the marker.
        //  There's a newline there hopefully :)
        block.end = markerIndex
    }

    override fun handleNewlineAtEmptyBody() {
        // just remote the block since it's empty
        block.remove()

        // delete the newline as it's purpose was served (to translate it as a command to close the block)
        TextDeleter.mark(text, newlineIndex, newlineIndex + 1)
    }

}