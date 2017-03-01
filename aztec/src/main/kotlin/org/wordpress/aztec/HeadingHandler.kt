package org.wordpress.aztec

import android.text.Spannable
import android.text.Spanned
import org.wordpress.aztec.spans.AztecBlockSpan
import org.wordpress.aztec.spans.AztecHeadingSpan

class HeadingHandler {
    private enum class PositionType {
        START,
        EMPTY_AT_END,
        TEXT_END,
        BODY
    }

    fun handleTextChangeForHeadings(text: Spannable, inputStart: Int, count: Int, textDeleter: TextDeleter) {
        // use charsNew to get the spans at the input point. It appears to be more reliable vs the whole Editable.
        var charsNew = text.subSequence(inputStart, inputStart + count) as Spanned

        val headings = charsNew.getSpans<AztecHeadingSpan>(0, 0, AztecHeadingSpan::class.java)
        if (headings == null || headings.isEmpty()) {
            // no headings so, bail.
            return
        }

        val heading = SpanWrapper(text, headings[0]) // TODO: handle nesting
        val childNestingLevel = heading.span.nestingLevel + 1

        val charsNewString = charsNew.toString()
        var newlineOffset = charsNewString.indexOf(Constants.NEWLINE)
        while (newlineOffset > -1 && newlineOffset < charsNew.length) {
            val newlineIndex = inputStart + newlineOffset

            // re-subsequence to get the newer state of the spans
            charsNew = text.subSequence(inputStart, inputStart + count) as Spanned
            when (getNewlinePositionType(text, heading, newlineIndex)) {
                PositionType.START -> handleNewlineAtStart(text, newlineIndex, childNestingLevel)
                PositionType.EMPTY_AT_END -> handleNewlineAtEmptyAtEnd(text, heading, newlineIndex, textDeleter)
                PositionType.TEXT_END -> handleNewlineAtTextEnd(text, heading, newlineIndex)
                PositionType.BODY -> handleNewlineInBody(text, heading, newlineIndex, childNestingLevel)
            }

            newlineOffset = charsNewString.indexOf(Constants.NEWLINE, newlineOffset + 1)
        }

        val gotEndOfBufferMarker = charsNew.length == 1 && charsNew[0] == Constants.END_OF_BUFFER_MARKER
        if (gotEndOfBufferMarker) {
            handleEndOfBufferIn(text, inputStart, childNestingLevel)
        }
    }

    private fun getNewlinePositionType(text: Spannable, heading: SpanWrapper<AztecHeadingSpan>, newlineIndex: Int)
            : PositionType {
        val atEndOfHeading = newlineIndex == heading.end - 2 || newlineIndex == text.length - 1

        if (newlineIndex == heading.start && !atEndOfHeading) {
            return PositionType.START
        }

        if (newlineIndex == heading.start && atEndOfHeading) {
            return PositionType.EMPTY_AT_END
        }

        if (text[newlineIndex - 1] == Constants.NEWLINE && atEndOfHeading) {
            return PositionType.EMPTY_AT_END
        }

        if (newlineIndex == text.length - 1) {
            return PositionType.TEXT_END
        }

        // no special case applied so, newline is in the "body" of the heading
        return PositionType.BODY
    }

    private fun handleNewlineAtStart(text: Spannable, newlineIndex: Int, childNestingLevel: Int) {
        // nothing special to do
    }

    private fun handleNewlineAtEmptyAtEnd(text: Spannable, heading: SpanWrapper<AztecHeadingSpan>, newlineIndex: Int,
                                          textDeleter: TextDeleter) {
        close(text, heading, newlineIndex)

        // delete the newline
        textDeleter.delete(newlineIndex, newlineIndex + 1)
    }

    private fun handleNewlineAtTextEnd(text: Spannable, heading: SpanWrapper<AztecHeadingSpan>, newlineIndex: Int) {
        // got a newline while being at the end-of-text. Just close the heading
        close(text, heading, newlineIndex)
    }

    private fun handleNewlineInBody(text: Spannable, heading: SpanWrapper<AztecHeadingSpan>, newlineIndex: Int, childNestingLevel: Int) {
        // newline added at some position inside the heading. Let's split the heading into two
        newHeading(text, heading, newlineIndex + 1, heading.end, childNestingLevel)
        heading.end = newlineIndex + 1
    }

    private fun handleEndOfBufferIn(text: Spannable, markerIndex: Int, childNestingLevel: Int): Boolean {
        // nothing special to do

        return false
    }

    private fun close(text: Spannable, heading: SpanWrapper<AztecHeadingSpan>, newlineIndex: Int) {
        if ((heading.end - heading.start === 1)
                || (heading.end - heading.start === 2 && text[heading.end - 1] == Constants.END_OF_BUFFER_MARKER)) {
            // heading is empty so, remove it
            heading.remove()
        } else {
            // adjust the heading end to only include the chars before the newline just added
            heading.end = newlineIndex
        }
    }

    companion object {
        fun set(text: Spannable, aztecBlockSpan: AztecBlockSpan, start: Int, end: Int) {
            text.setSpan(aztecBlockSpan, start, end, Spanned.SPAN_PARAGRAPH)
        }

        fun newHeading(text: Spannable, heading: SpanWrapper<AztecHeadingSpan>, start: Int, end: Int, nestingLevel: Int) {
            set(text, AztecHeadingSpan(nestingLevel, heading.span.textFormat, heading.span.attributes), start, end)
        }
    }
}
