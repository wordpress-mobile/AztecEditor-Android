package org.wordpress.aztec

import android.text.Spannable
import android.text.Spanned
import org.wordpress.aztec.BlockElementWatcher.TextChangeHandler
import org.wordpress.aztec.spans.AztecBlockSpan
import org.wordpress.aztec.spans.AztecHeadingSpan

class HeadingHandler : TextChangeHandler {
    private enum class PositionType {
        START,
        EMPTY_AT_END,
        TEXT_END,
        BODY
    }

    override fun handleTextChanged(text: Spannable, inputStart: Int, count: Int, textDeleter: TextDeleter) {
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
                PositionType.START -> handleNewlineAtStart(text, heading, newlineIndex, childNestingLevel)
                PositionType.EMPTY_AT_END -> handleNewlineAtEmptyAtEnd(text, heading, newlineIndex, textDeleter)
                PositionType.TEXT_END -> handleNewlineAtTextEnd(text, heading, newlineIndex)
                PositionType.BODY -> handleNewlineInBody(text, heading, newlineIndex, childNestingLevel)
            }

            newlineOffset = charsNewString.indexOf(Constants.NEWLINE, newlineOffset + 1)
        }

        val gotEndOfBufferMarker = charsNew.length == 1 && charsNew[0] == Constants.END_OF_BUFFER_MARKER
        if (gotEndOfBufferMarker) {
            handleEndOfBufferIn(text, heading, inputStart, childNestingLevel)
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

    private fun handleNewlineAtStart(text: Spannable, heading: SpanWrapper<AztecHeadingSpan>, newlineIndex: Int,
                                     childNestingLevel: Int) {
        // we got a newline at the start of the heading. Let's just push the heading after the newline
        heading.start = newlineIndex + 1
    }

    private fun handleNewlineAtEmptyAtEnd(text: Spannable, heading: SpanWrapper<AztecHeadingSpan>, newlineIndex: Int,
            textDeleter: TextDeleter) {
        // just remote the heading since it's empty
        heading.remove()

        // delete the newline as it's purpose was served (to translate it as a command to close the heading)
        textDeleter.delete(newlineIndex, newlineIndex + 1)
    }

    private fun handleNewlineAtTextEnd(text: Spannable, heading: SpanWrapper<AztecHeadingSpan>, newlineIndex: Int) {
        // got a newline while being at the end-of-text. We'll let the current list item engulf it and will wait
        //  for the end-of-text marker event in order to attach the new list item to it when that happens.
    }

    private fun handleNewlineInBody(text: Spannable, heading: SpanWrapper<AztecHeadingSpan>, newlineIndex: Int, childNestingLevel: Int) {
        if (newlineIndex == heading.end - 2) {
            // newline added at the end of the heading (right before its visual newline) so, just end the heading and
            //  not add a new heading after it
        } else {
            // newline added at some position inside the heading. Let's split the heading into two
            newHeading(text, heading.span, newlineIndex + 1, heading.end, childNestingLevel)
        }

        heading.end = newlineIndex + 1
    }

    private fun handleEndOfBufferIn(text: Spannable, heading: SpanWrapper<AztecHeadingSpan>, markerIndex: Int,
                                    childNestingLevel: Int): Boolean {
        // adjust the heading end to only include the chars before the end-of-text marker. A newline will be there.
        heading.end = markerIndex

        return true
    }

    companion object {
        fun set(text: Spannable, aztecBlockSpan: AztecBlockSpan, start: Int, end: Int) {
            text.setSpan(aztecBlockSpan, start, end, Spanned.SPAN_PARAGRAPH)
        }

        fun newHeading(text: Spannable, heading: AztecHeadingSpan, start: Int, end: Int, nestingLevel: Int) {
            set(text, AztecHeadingSpan(nestingLevel, heading.textFormat, heading.attributes), start, end)
        }
    }
}
