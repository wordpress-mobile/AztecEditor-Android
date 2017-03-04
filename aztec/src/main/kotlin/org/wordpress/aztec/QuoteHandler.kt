package org.wordpress.aztec

import android.text.Spannable
import android.text.Spanned
import org.wordpress.aztec.BlockElementWatcher.TextChangeHandler
import org.wordpress.aztec.spans.AztecBlockSpan
import org.wordpress.aztec.spans.AztecQuoteSpan

class QuoteHandler : TextChangeHandler {
    private enum class PositionType {
        START,
        EMPTY_AT_END,
        TEXT_END,
        BODY
    }

    override fun handleTextChanged(text: Spannable, inputStart: Int, count: Int, textDeleter: TextDeleter) {
        // use charsNew to get the spans at the input point. It appears to be more reliable vs the whole Editable.
        var charsNew = text.subSequence(inputStart, inputStart + count) as Spanned

        val quotes = charsNew.getSpans<AztecQuoteSpan>(0, 0, AztecQuoteSpan::class.java)
        if (quotes == null || quotes.isEmpty()) {
            // no quotes so, bail.
            return
        }

        val quote = SpanWrapper(text, quotes[0]) // TODO: handle nesting
        val childNestingLevel = quote.span.nestingLevel + 1

        val charsNewString = charsNew.toString()
        var newlineOffset = charsNewString.indexOf(Constants.NEWLINE)
        while (newlineOffset > -1 && newlineOffset < charsNew.length) {
            val newlineIndex = inputStart + newlineOffset

            // re-subsequence to get the newer state of the spans
            charsNew = text.subSequence(inputStart, inputStart + count) as Spanned
            when (getNewlinePositionType(text, quote, newlineIndex)) {
                PositionType.START -> handleNewlineAtStart(text, newlineIndex, childNestingLevel)
                PositionType.EMPTY_AT_END -> handleNewlineAtEmptyAtEnd(text, quote, newlineIndex, textDeleter)
                PositionType.TEXT_END -> handleNewlineAtTextEnd()
                PositionType.BODY -> handleNewlineInBody(text, newlineIndex, childNestingLevel)
            }

            newlineOffset = charsNewString.indexOf(Constants.NEWLINE, newlineOffset + 1)
        }

        val gotEndOfBufferMarker = charsNew.length == 1 && charsNew[0] == Constants.END_OF_BUFFER_MARKER
        if (gotEndOfBufferMarker) {
            handleEndOfBufferIn(text, inputStart, childNestingLevel)
        }
    }

    private fun getNewlinePositionType(text: Spannable, quote: SpanWrapper<AztecQuoteSpan>, newlineIndex: Int)
            : PositionType {
        val atEndOfQuote = newlineIndex == quote.end - 2 || newlineIndex == text.length - 1

        if (newlineIndex == quote.start && !atEndOfQuote) {
            return PositionType.START
        }

        if (newlineIndex == quote.start && atEndOfQuote) {
            return PositionType.EMPTY_AT_END
        }

        if (text[newlineIndex - 1] == Constants.NEWLINE && atEndOfQuote) {
            return PositionType.EMPTY_AT_END
        }

        if (newlineIndex == text.length - 1) {
            return PositionType.TEXT_END
        }

        // no special case applied so, newline is in the "body" of the quote
        return PositionType.BODY
    }

    private fun handleNewlineAtStart(text: Spannable, newlineIndex: Int, childNestingLevel: Int) {
        // nothing special to do
    }

    private fun handleNewlineAtEmptyAtEnd(text: Spannable, quote: SpanWrapper<AztecQuoteSpan>, newlineIndex: Int,
                                          textDeleter: TextDeleter) {
        if ((quote.end - quote.start === 1)
                || (quote.end - quote.start === 2 && text[quote.end - 1] == Constants.END_OF_BUFFER_MARKER)) {
            // quote is empty so, remove it
            quote.remove()
        } else {
            // adjust the quote end to only include the chars before the newline just added
            quote.end = newlineIndex
        }

        // delete the newline
        textDeleter.delete(newlineIndex, newlineIndex + 1)
    }

    private fun handleNewlineAtTextEnd() {
        // got a newline while being at the end-of-text. We'll let the quote engulf it.
    }

    private fun handleNewlineInBody(text: Spannable, newlineIndex: Int, childNestingLevel: Int) {
        // newline added at some position inside the quote. Nothing special to do.
    }

    private fun handleEndOfBufferIn(text: Spannable, markerIndex: Int, childNestingLevel: Int): Boolean {
        // nothing special to do

        return false
    }

    companion object {
        fun set(text: Spannable, aztecBlockSpan: AztecBlockSpan, start: Int, end: Int) {
            text.setSpan(aztecBlockSpan, start, end, Spanned.SPAN_PARAGRAPH)
        }
    }
}
