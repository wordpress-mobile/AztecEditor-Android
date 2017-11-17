package org.wordpress.aztec.plugins.shortcodes.handlers

import android.text.SpannableStringBuilder
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import org.wordpress.aztec.handlers.BlockHandler
import org.wordpress.aztec.plugins.shortcodes.spans.CaptionShortcodeSpan
import org.wordpress.aztec.watchers.TextDeleter
import java.lang.ref.WeakReference

class CaptionHandler(aztecText: AztecText) : BlockHandler<CaptionShortcodeSpan>(CaptionShortcodeSpan::class.java) {

    private val aztecTextRef = WeakReference(aztecText)

    override fun handleNewlineInBody() {
        if (newlineIndex == block.start + 2 || newlineIndex == block.start + 1) {
            TextDeleter.mark(text, newlineIndex, newlineIndex + 1)

            val span = text as SpannableStringBuilder
            span.insert(block.end, Constants.NEWLINE_STRING)
            aztecTextRef.get()?.setSelection(block.end)
        } else {
            block.end = newlineIndex + 1
        }
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