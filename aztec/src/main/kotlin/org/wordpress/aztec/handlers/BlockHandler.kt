package org.wordpress.aztec.handlers

import android.text.Spannable
import android.text.Spanned
import org.wordpress.android.util.AppLog
import org.wordpress.aztec.Constants
import org.wordpress.aztec.spans.IAztecBlockSpan
import org.wordpress.aztec.spans.IAztecNestable
import org.wordpress.aztec.util.AztecLog
import org.wordpress.aztec.util.SpanWrapper
import org.wordpress.aztec.watchers.BlockElementWatcher.TextChangeHandler

abstract class BlockHandler<SpanType : IAztecBlockSpan>(val clazz: Class<SpanType>) : TextChangeHandler {
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
    var nestingLevel = 0
    var markerIndex: Int = -1
    var isReplay = false

    override fun handleTextChanged(text: Spannable, inputStart: Int, count: Int, nestingLevel: Int, isReplay: Boolean) {
        this.text = text
        this.isReplay = isReplay
        this.nestingLevel = nestingLevel

        if (text.getSpans<SpanType>(inputStart, inputStart + count, clazz).isEmpty()) {
            return
        }

        // use charsNew to get the spans at the input point. It appears to be more reliable vs the whole Editable.
        var charsNew = text.subSequence(inputStart, inputStart + count) as Spanned

        SpanWrapper.getSpans(text, charsNew.getSpans<SpanType>(0, 0, clazz)).forEach {
            block = it

            val gotEndOfBufferMarker = charsNew.length == 1 && charsNew[0] == Constants.END_OF_BUFFER_MARKER
            if (gotEndOfBufferMarker) {
                markerIndex = inputStart
            }

            val charsNewString = charsNew.toString()
            var newlineOffset = charsNewString.indexOf(Constants.NEWLINE)
            while (newlineOffset > -1 && newlineOffset < charsNew.length) {
                newlineIndex = inputStart + newlineOffset
                newlineOffset = charsNewString.indexOf(Constants.NEWLINE, newlineOffset + 1)

                if (!shouldHandle()) {
                    continue
                }

                // re-subsequence to get the newer state of the spans
                charsNew = text.subSequence(inputStart, inputStart + count) as Spanned
                when (getNewlinePositionType(text, block, newlineIndex)) {
                    PositionType.START_OF_BLOCK -> handleNewlineAtStartOfBlock()
                    PositionType.EMPTY_LINE_AT_BLOCK_END -> handleNewlineAtEmptyLineAtBlockEnd()
                    PositionType.EMPTY_LINE_AT_EMPTY_BODY -> handleNewlineAtEmptyBody()
                    PositionType.BUFFER_END -> handleNewlineAtTextEnd()
                    PositionType.BODY -> handleNewlineInBody()
                }
            }

            if (gotEndOfBufferMarker && shouldHandle()) {
                handleEndOfBufferMarker()
            }
        }
    }

    private fun getNewlinePositionType(text: Spannable, block: SpanWrapper<SpanType>, newlineIndex: Int): PositionType {
        val isEmptyBody = (block.end - block.start == 1)
                || (block.end - block.start == 2 && text[block.end - 1] == Constants.END_OF_BUFFER_MARKER)

        if (newlineIndex == block.start && isEmptyBody) {
            return PositionType.EMPTY_LINE_AT_EMPTY_BODY
        }

        // if the new newline is the second-last character of the block and the last one is a newline (which
        // is a visual newline) or the end-of-buffer marker, or it's the last character of the text then it's the last
        // actual character of the block
        val atEndOfBlock = (newlineIndex == block.end - 2 &&
                (text[block.end - 1] == Constants.NEWLINE || text[block.end - 1] == Constants.END_OF_BUFFER_MARKER)) ||
                newlineIndex == text.length - 1

        if (newlineIndex == block.start && !atEndOfBlock) {
            return PositionType.START_OF_BLOCK
        }

        if (newlineIndex == block.start && atEndOfBlock) {
            return PositionType.EMPTY_LINE_AT_BLOCK_END
        }

        // prev newline needs to be at the same nesting level to account for "double-enter"
        val prevNewlineNesting = IAztecNestable.getNestingLevelAt(text, newlineIndex - 1, newlineIndex)
        val currentNewlineNesting = IAztecNestable.getNestingLevelAt(text, newlineIndex, newlineIndex + 1)
        if (text[newlineIndex - 1] == Constants.NEWLINE
                && (prevNewlineNesting == currentNewlineNesting || prevNewlineNesting > currentNewlineNesting && !isReplay)
                && atEndOfBlock) {
            return PositionType.EMPTY_LINE_AT_BLOCK_END
        }

        if (newlineIndex == text.length - 1) {
            return PositionType.BUFFER_END
        }

        // no special case applied so, newline is in the "body" of the block
        return PositionType.BODY
    }

    open fun shouldHandle(): Boolean { return nestingLevel == block.span.nestingLevel }
    open fun handleNewlineAtStartOfBlock() { /* nothing special to do*/ }
    open fun handleNewlineAtEmptyLineAtBlockEnd() { /* nothing special to do*/ }
    open fun handleNewlineAtEmptyBody() { /* nothing special to do*/ }
    open fun handleNewlineAtTextEnd() { /* nothing special to do*/ }
    open fun handleNewlineInBody() { /* nothing special to do*/ }
    open fun handleEndOfBufferMarker() { /* nothing special to do*/ }

    companion object {
        fun set(text: Spannable, block: IAztecBlockSpan, start: Int, end: Int) {
            if (start > end) {
                AppLog.w(AppLog.T.EDITOR, "BlockHandler.set static method called with start > end. Start: " + start + " End: " + end)
                AppLog.w(AppLog.T.EDITOR, "Invoked with block type of " + block.javaClass.canonicalName)
                AztecLog.logContentDetails(text)
                return
            }

            val flags = Spanned.SPAN_PARAGRAPH
            if (SpanWrapper.isInvalidParagraph(text, start, end, flags)) return

            text.setSpan(block, start, end, flags)
        }
    }
}
