package org.wordpress.aztec.handlers

import android.text.Spannable
import android.text.Spanned
import org.wordpress.aztec.Constants
import org.wordpress.aztec.spans.AztecBlockSpan
import org.wordpress.aztec.util.SpanWrapper
import org.wordpress.aztec.watchers.BlockElementWatcher.TextChangeHandler

abstract class BlockHandler<SpanType : AztecBlockSpan>(val clazz: Class<SpanType>) : TextChangeHandler {
    private enum class PositionType {
        START_OF_BLOCK,
        EMPTY_LINE_AT_BLOCK_END,
        EMPTY_LINE_AT_EMPTY_BODY,
        BUFFER_END,
        BODY
    }

    lateinit var text: Spannable
    lateinit var block: SpanWrapper<SpanType>
    var newlineIndex: Int = -1
    var markerIndex: Int = -1

    override fun handleTextChanged(text: Spannable, inputStart: Int, count: Int) {
        this.text = text

        // use charsNew to get the spans at the input point. It appears to be more reliable vs the whole Editable.
        var charsNew = text.subSequence(inputStart, inputStart + count) as Spanned

        SpanWrapper.getSpans(text, charsNew.getSpans<SpanType>(0, 0, clazz)).firstOrNull().let { // TODO: handle nesting
            if (it == null) {
                // no block elements of this type found so, bail.
                return
            }

            block = it

            val charsNewString = charsNew.toString()
            var newlineOffset = charsNewString.indexOf(Constants.NEWLINE)
            while (newlineOffset > -1 && newlineOffset < charsNew.length) {
                newlineIndex = inputStart + newlineOffset

                // re-subsequence to get the newer state of the spans
                charsNew = text.subSequence(inputStart, inputStart + count) as Spanned
                when (getNewlinePositionType(text, block, newlineIndex)) {
                    PositionType.START_OF_BLOCK -> handleNewlineAtStartOfBlock()
                    PositionType.EMPTY_LINE_AT_BLOCK_END -> handleNewlineAtEmptyLineAtBlockEnd()
                    PositionType.EMPTY_LINE_AT_EMPTY_BODY -> handleNewlineAtEmptyBody()
                    PositionType.BUFFER_END -> handleNewlineAtTextEnd()
                    PositionType.BODY -> handleNewlineInBody()
                }

                newlineOffset = charsNewString.indexOf(Constants.NEWLINE, newlineOffset + 1)
            }

            val gotEndOfBufferMarker = charsNew.length == 1 && charsNew[0] == Constants.END_OF_BUFFER_MARKER
            if (gotEndOfBufferMarker) {
                markerIndex = inputStart
                handleEndOfBufferMarker()
            }
        }
    }

    private fun getNewlinePositionType(text: Spannable, block: SpanWrapper<SpanType>, newlineIndex: Int)
            : PositionType {
        val isEmptyBody = (block.end - block.start == 1)
                || (block.end - block.start == 2 && text[block.end - 1] == Constants.END_OF_BUFFER_MARKER)

        if (newlineIndex == block.start && isEmptyBody) {
            return PositionType.EMPTY_LINE_AT_EMPTY_BODY
        }

        val atEndOfblock = newlineIndex == block.end - 2 || newlineIndex == text.length - 1

        if (newlineIndex == block.start && !atEndOfblock) {
            return PositionType.START_OF_BLOCK
        }

        if (newlineIndex == block.start && atEndOfblock) {
            return PositionType.EMPTY_LINE_AT_BLOCK_END
        }

        if (text[newlineIndex - 1] == Constants.NEWLINE && atEndOfblock) {
            return PositionType.EMPTY_LINE_AT_BLOCK_END
        }

        if (newlineIndex == text.length - 1) {
            return PositionType.BUFFER_END
        }

        // no special case applied so, newline is in the "body" of the block
        return PositionType.BODY
    }

    open fun handleNewlineAtStartOfBlock() { /* nothing special to do*/ }
    open fun handleNewlineAtEmptyLineAtBlockEnd() { /* nothing special to do*/ }
    open fun handleNewlineAtEmptyBody() { /* nothing special to do*/ }
    open fun handleNewlineAtTextEnd() { /* nothing special to do*/ }
    open fun handleNewlineInBody() { /* nothing special to do*/ }
    open fun handleEndOfBufferMarker() { /* nothing special to do*/ }

    companion object {
        fun set(text: Spannable, block: AztecBlockSpan, start: Int, end: Int) {
            text.setSpan(block, start, end, Spanned.SPAN_PARAGRAPH)
        }
    }
}
