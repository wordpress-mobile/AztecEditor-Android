package org.wordpress.aztec.formatting

import android.text.Editable
import android.text.Spanned
import android.text.TextUtils
import org.wordpress.aztec.*
import org.wordpress.aztec.spans.*
import java.util.*


class BlockFormatter(editor: AztecText, listStyle: ListStyle, quoteStyle: QuoteStyle) : AztecFormatter(editor) {

    data class ListStyle(val indicatorColor: Int, val indicatorMargin: Int, val indicatorPadding: Int, val indicatorWidth: Int, val verticalPadding: Int)
    data class QuoteStyle(val quoteBackground: Int, val quoteColor: Int, val quoteBackgroundAlpha: Float, val quoteMargin: Int, val quotePadding: Int, val quoteWidth: Int, val verticalPadding: Int)

    val listStyle: ListStyle
    val quoteStyle: QuoteStyle

    init {
        this.listStyle = listStyle
        this.quoteStyle = quoteStyle
    }

    fun toggleOrderedList() {
        if (!containsList(TextFormat.FORMAT_ORDERED_LIST, 0)) {
            if (containsList(TextFormat.FORMAT_UNORDERED_LIST, 0)) {
                switchListType(TextFormat.FORMAT_ORDERED_LIST, 0)
            } else {
                applyBlockStyle(TextFormat.FORMAT_ORDERED_LIST, 0)
            }
        } else {
            removeBlockStyle(TextFormat.FORMAT_ORDERED_LIST, 0)
        }
    }

    fun toggleUnorderedList() {
        if (!containsList(TextFormat.FORMAT_UNORDERED_LIST, 0)) {
            if (containsList(TextFormat.FORMAT_ORDERED_LIST, 0)) {
                switchListType(TextFormat.FORMAT_UNORDERED_LIST, 0)
            } else {
                applyBlockStyle(TextFormat.FORMAT_UNORDERED_LIST, 0)
            }
        } else {
            removeBlockStyle(TextFormat.FORMAT_UNORDERED_LIST, 0)
        }
    }

    fun toggleQuote() {
        if (!containQuote()) {
            applyBlockStyle(TextFormat.FORMAT_QUOTE, 0)
        } else {
            removeBlockStyle(TextFormat.FORMAT_QUOTE, 0)
        }
    }

    fun tryRemoveBlockStyleFromFirstLine(): Boolean {
        val selectionStart = editor.selectionStart

        if (selectionStart != 0) {
            // only handle the edge case of start of text
            return false
        }

        var changed = false

        //try to remove block styling when pressing backspace at the beginning of the text
        editableText.getSpans(0, 0, AztecBlockSpan::class.java).forEach {
            val spanEnd = editableText.getSpanEnd(it)

            val indexOfNewline = editableText.indexOf('\n').let { if (it != -1) it else editableText.length }

            if (spanEnd <= indexOfNewline) {
                // block will collapse so, just remove it
                editableText.removeSpan(it)
                changed = true
                return@forEach
            }

            editableText.setSpan(it, indexOfNewline + 1, spanEnd, editableText.getSpanFlags(it))
            changed = true
        }

        return changed
    }

    fun removeBlockStyle(textFormat: TextFormat, nestingLevel: Int) {
        removeBlockStyle(selectionStart, selectionEnd, makeBlock(textFormat, nestingLevel).map { it -> it.javaClass })
    }

    fun removeBlockStyle(start: Int = selectionStart, end: Int = selectionEnd,
                         spanTypes: List<Class<AztecBlockSpan>> = Arrays.asList(AztecBlockSpan::class.java),
                         ignoreLineBounds: Boolean = false) {
        spanTypes.forEach { spanType ->
            val spans = editableText.getSpans(start, end, spanType)
            spans.forEach { span ->

                val spanStart = editableText.getSpanStart(span)
                val spanEnd = editableText.getSpanEnd(span)

                //if splitting block set a range that would be excluded from it
                val boundsOfSelectedText = if (ignoreLineBounds) IntRange(start, end) else getSelectedTextBounds(editableText, start, end)

                val startOfLine = boundsOfSelectedText.start
                val endOfLine = boundsOfSelectedText.endInclusive

                val spanPrecedesLine = spanStart < startOfLine
                val spanExtendsBeyondLine = endOfLine < spanEnd

                if (spanPrecedesLine && !spanExtendsBeyondLine) {
                    // pull back the end of the block span
                    ListHandler.set(editableText, span, spanStart, startOfLine)
                } else if (spanExtendsBeyondLine && !spanPrecedesLine) {
                    // push the start of the block span
                    ListHandler.set(editableText, span, spanStart, startOfLine)

//                    if (editableText[endOfLine] == '\n' && !ignoreLineBounds) {
//                        editor.disableTextChangedListener()
//                        editableText.delete(endOfLine, endOfLine + 1)
//                        editor.enableTextChangedListener()
//                        spanEnd--
//                    }
//
//                    editableText.setSpan(it, endOfLine, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                } else if (spanPrecedesLine && spanExtendsBeyondLine) {
                    // we need to split the span into two parts

                    // first, let's pull back the end of the existing span
                    ListHandler.set(editableText, span, spanStart, startOfLine)

                    // now, let's "clone" the span and set it
                    ListHandler.set(editableText, makeBlockSpan(spanType, span.nestingLevel, span.attributes),
                            endOfLine, spanEnd)
                } else {
                    // tough luck. The span is fully inside the line so it gets axed.
                    editableText.removeSpan(span)
                }
            }
        }
    }

    fun getOuterBlockSpanType(textFormat: TextFormat): Class<out AztecBlockSpan> {
        when (textFormat) {
            TextFormat.FORMAT_ORDERED_LIST -> return AztecOrderedListSpan::class.java
            TextFormat.FORMAT_UNORDERED_LIST -> return AztecUnorderedListSpan::class.java
            TextFormat.FORMAT_QUOTE -> return AztecQuoteSpan::class.java
            else -> return ParagraphSpan::class.java
        }
    }


    //TODO: Come up with a better way to init spans and get their classes (all the "make" methods)
    fun makeBlock(textFormat: TextFormat, nestingLevel: Int, attrs: String = ""): List<AztecBlockSpan> {
        when (textFormat) {
            TextFormat.FORMAT_ORDERED_LIST -> return Arrays.asList(AztecOrderedListSpan(nestingLevel, attrs, listStyle), AztecListItemSpan(nestingLevel + 1))
            TextFormat.FORMAT_UNORDERED_LIST -> return Arrays.asList(AztecUnorderedListSpan(nestingLevel, attrs, listStyle), AztecListItemSpan(nestingLevel + 1))
            TextFormat.FORMAT_QUOTE -> return Arrays.asList(AztecQuoteSpan(nestingLevel, attrs, quoteStyle))
            else -> return Arrays.asList(ParagraphSpan(nestingLevel, attrs))
        }
    }

    fun makeBlockSpan(textFormat: TextFormat, nestingLevel: Int, attrs: String = ""): AztecBlockSpan {
        return when (textFormat) {
            TextFormat.FORMAT_ORDERED_LIST -> makeBlockSpan(AztecOrderedListSpan::class.java, nestingLevel, attrs)
            TextFormat.FORMAT_UNORDERED_LIST -> makeBlockSpan(AztecUnorderedListSpan::class.java, nestingLevel, attrs)
            TextFormat.FORMAT_QUOTE -> makeBlockSpan(AztecQuoteSpan::class.java, nestingLevel, attrs)
            else -> ParagraphSpan(nestingLevel, attrs)
        }
    }

    fun <T : Class<out AztecBlockSpan>> makeBlockSpan(type: T, nestingLevel: Int, attrs: String = ""): AztecBlockSpan {
        return when (type) {
            AztecOrderedListSpan::class.java -> AztecOrderedListSpan(nestingLevel, attrs, listStyle)
            AztecUnorderedListSpan::class.java -> AztecUnorderedListSpan(nestingLevel, attrs, listStyle)
            AztecListItemSpan::class.java -> AztecListItemSpan(nestingLevel, attrs)
            else -> ParagraphSpan(nestingLevel, attrs)
        }
    }

    fun setBlockStyle(blockElement: AztecBlockSpan) {
        when (blockElement) {
            is AztecOrderedListSpan -> blockElement.listStyle = listStyle
            is AztecUnorderedListSpan -> blockElement.listStyle = listStyle
            is AztecQuoteSpan -> blockElement.quoteStyle = quoteStyle
        }
    }


    fun getSelectedTextBounds(editable: Editable, selectionStart: Int, selectionEnd: Int): IntRange {
        val startOfLine: Int
        val endOfLine: Int

        val indexOfFirstLineBreak: Int
        val indexOfLastLineBreak = editable.indexOf("\n", selectionEnd)

        if (indexOfLastLineBreak > 0) {
            val characterBeforeLastLineBreak = editable[indexOfLastLineBreak - 1]
            if (characterBeforeLastLineBreak != '\n') {
                indexOfFirstLineBreak = editable.lastIndexOf("\n", selectionStart - 1) + 1
            } else {
                indexOfFirstLineBreak = editable.lastIndexOf("\n", selectionStart)
            }
        } else {
            if (indexOfLastLineBreak == -1) {
                indexOfFirstLineBreak = editable.lastIndexOf("\n", selectionStart) + 1
            } else {
                indexOfFirstLineBreak = editable.lastIndexOf("\n", selectionStart)
            }
        }


        startOfLine = if (indexOfFirstLineBreak != -1) indexOfFirstLineBreak else 0
        endOfLine = if (indexOfLastLineBreak != -1) (indexOfLastLineBreak + 1) else editable.length

        return IntRange(startOfLine, endOfLine)
    }


    fun applyBlockStyle(blockElementType: TextFormat, nestingLevel: Int, start: Int = selectionStart, end: Int = selectionEnd) {
        if (start != end) {
            val selectedText = editableText.substring(start + 1..end - 1)

            //multiline text selected
            if (selectedText.indexOf("\n") != -1) {
                val indexOfFirstLineBreak = editableText.indexOf("\n", end)

                val endOfBlock = if (indexOfFirstLineBreak != -1) indexOfFirstLineBreak else editableText.length
                val startOfBlock = editableText.lastIndexOf("\n", start)

                val selectedLines = editableText.subSequence(startOfBlock + 1..endOfBlock - 1) as Editable

                var numberOfLinesWithSpanApplied = 0
                var numberOfLines = 0

                val lines = TextUtils.split(selectedLines.toString(), "\n")

                for (i in lines.indices) {
                    numberOfLines++
                    if (containsList(blockElementType, i, selectedLines, nestingLevel)) {
                        numberOfLinesWithSpanApplied++
                    }
                }

                if (numberOfLines == numberOfLinesWithSpanApplied) {
                    removeBlockStyle(blockElementType, nestingLevel)
                } else {
                    applyBlock(blockElementType, startOfBlock + 1, endOfBlock, nestingLevel)
                }
            }

        } else {
            val boundsOfSelectedText = getSelectedTextBounds(editableText, start, end)

            val startOfLine = boundsOfSelectedText.start
            val endOfLine = boundsOfSelectedText.endInclusive

            val spanToApply = getOuterBlockSpanType(blockElementType)

            var startOfBlock: Int = startOfLine
            var endOfBlock: Int = endOfLine


            if (startOfLine != 0) {
                val spansOnPreviousLine = editableText.getSpans(startOfLine - 1, startOfLine - 1, spanToApply).firstOrNull()
                if (spansOnPreviousLine != null) {
                    startOfBlock = editableText.getSpanStart(spansOnPreviousLine)
                    liftBlock(blockElementType, startOfBlock, endOfBlock)
                }
            }

            if (endOfLine != editableText.length) {
                val spanOnNextLine = editableText.getSpans(endOfLine + 1, endOfLine + 1, spanToApply).firstOrNull()
                if (spanOnNextLine != null) {
                    endOfBlock = editableText.getSpanEnd(spanOnNextLine)
                    liftBlock(blockElementType, startOfBlock, endOfBlock)
                }
            }

            applyBlock(blockElementType, startOfBlock, endOfBlock, nestingLevel)

            //if the line was empty trigger onSelectionChanged manually to update toolbar buttons status
//            if (isEmptyLine) {
                editor.onSelectionChanged(startOfLine, endOfLine)
//            }
        }
    }

    private fun applyBlock(textFormat: TextFormat, start: Int, end: Int, nestingLevel: Int, attrs: String = "") {
        when (textFormat) {
            TextFormat.FORMAT_ORDERED_LIST -> applyListBlock(AztecOrderedListSpan(nestingLevel, attrs, listStyle), start, end, nestingLevel)
            TextFormat.FORMAT_UNORDERED_LIST -> applyListBlock(AztecUnorderedListSpan(nestingLevel, attrs, listStyle), start, end, nestingLevel)
            TextFormat.FORMAT_QUOTE -> editableText.setSpan(AztecQuoteSpan(nestingLevel, attrs, quoteStyle), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            else -> editableText.setSpan(ParagraphSpan(nestingLevel, attrs), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun applyListBlock(listSpan: AztecListSpan, start: Int, end: Int, nestingLevel: Int) {
        ListHandler.set(editableText, listSpan, start, end)

        val lines = TextUtils.split(editableText.substring(start, end), "\n")
        for (i in lines.indices) {
            val lineLength = lines[i].length

            val lineStart = (0..i - 1).sumBy { lines[it].length + 1 }
            val lineEnd = (lineStart + lineLength).let {
                if ((start + it) != editableText.length) it + 1 else it // include the newline or not
            }

            if (lineLength == 0) continue

            ListHandler.newListItem(editableText, start + lineStart, start + lineEnd, nestingLevel + 1)
        }
    }

    private fun liftBlock(textFormat: TextFormat, start: Int, end: Int, attrs: String = "") {
        when (textFormat) {
            TextFormat.FORMAT_ORDERED_LIST -> liftListBlock(AztecOrderedListSpan::class.java, start, end)
            TextFormat.FORMAT_UNORDERED_LIST -> liftListBlock(AztecUnorderedListSpan::class.java, start, end)
            TextFormat.FORMAT_QUOTE -> editableText.getSpans(start, end, AztecQuoteSpan::class.java).forEach { editableText.removeSpan(it) }
            else -> editableText.getSpans(start, end, ParagraphSpan::class.java).forEach { editableText.removeSpan(it) }
        }
    }

    private fun liftListBlock(listSpan: Class<out AztecListSpan>, start: Int, end: Int) {
        editableText.getSpans(start, end, listSpan).forEach { editableText.removeSpan(it) }
        editableText.getSpans(start, end, AztecListItemSpan::class.java).forEach { editableText.removeSpan(it) }
    }

    fun containsList(textFormat: TextFormat, nestingLevel: Int, selStart: Int = selectionStart, selEnd: Int = selectionEnd): Boolean {
        val lines = TextUtils.split(editableText.toString(), "\n")
        val list = ArrayList<Int>()


        for (i in lines.indices) {
            val lineStart = (0..i - 1).sumBy { lines[it].length + 1 }
            val lineEnd = lineStart + lines[i].length

            if (lineStart > lineEnd) {
                continue
            }

            /**
             * lineStart  >= selStart && selEnd   >= lineEnd // single line, current entirely selected OR
             *                                                  multiple lines (before and/or after), current entirely selected
             * lineStart  <= selEnd   && selEnd   <= lineEnd // single line, current partially or entirely selected OR
             *                                                  multiple lines (after), current partially or entirely selected
             * lineStart  <= selStart && selStart <= lineEnd // single line, current partially or entirely selected OR
             *                                                  multiple lines (before), current partially or entirely selected
             */
            if ((lineStart >= selStart && selEnd >= lineEnd)
                    || (lineStart <= selEnd && selEnd <= lineEnd)
                    || (lineStart <= selStart && selStart <= lineEnd)) {
                list.add(i)
            }
        }

        if (list.isEmpty()) return false

        return list.any { containsList(textFormat, it, editableText, nestingLevel) }
    }

    fun containsList(textFormat: TextFormat, index: Int, text: Editable, nestingLevel: Int): Boolean {
        val lines = TextUtils.split(text.toString(), "\n")
        if (index < 0 || index >= lines.size) {
            return false
        }

        val start = (0..index - 1).sumBy { lines[it].length + 1 }
        val end = start + lines[index].length

        if (start > end) {
            return false
        }

        val spans = editableText.getSpans(start, end, makeBlockSpan(textFormat, nestingLevel).javaClass)
        return spans.isNotEmpty()
    }


    fun containQuote(selStart: Int = selectionStart, selEnd: Int = selectionEnd): Boolean {
        val lines = TextUtils.split(editableText.toString(), "\n")
        val list = ArrayList<Int>()

        for (i in lines.indices) {
            val lineStart = (0..i - 1).sumBy { lines[it].length + 1 }
            val lineEnd = lineStart + lines[i].length

            if (lineStart >= lineEnd) {
                continue
            }

            /**
             * lineStart  >= selStart && selEnd   >= lineEnd // single line, current entirely selected OR
             *                                                  multiple lines (before and/or after), current entirely selected
             * lineStart  <= selEnd   && selEnd   <= lineEnd // single line, current partially or entirely selected OR
             *                                                  multiple lines (after), current partially or entirely selected
             * lineStart  <= selStart && selStart <= lineEnd // single line, current partially or entirely selected OR
             *                                                  multiple lines (before), current partially or entirely selected
             */
            if ((lineStart >= selStart && selEnd >= lineEnd)
                    || (lineStart <= selEnd && selEnd <= lineEnd)
                    || (lineStart <= selStart && selStart <= lineEnd)) {
                list.add(i)
            }
        }

        if (list.isEmpty()) return false

        return list.any { containQuote(it) }
    }

    fun containQuote(index: Int): Boolean {
        val lines = TextUtils.split(editableText.toString(), "\n")
        if (index < 0 || index >= lines.size) {
            return false
        }

        val start = (0..index - 1).sumBy { lines[it].length + 1 }
        val end = start + lines[index].length

        if (start >= end) {
            return false
        }

        val spans = editableText.getSpans(start, end, AztecQuoteSpan::class.java)
        return spans.isNotEmpty()
    }

    fun switchListType(listTypeToSwitchTo: TextFormat, nestingLevel: Int, start: Int = selectionStart, end: Int = selectionEnd) {
        val existingListSpan = editableText.getSpans(start, end, AztecListSpan::class.java).firstOrNull()
        if (existingListSpan != null) {
            val spanStart = editableText.getSpanStart(existingListSpan)
            val spanEnd = editableText.getSpanEnd(existingListSpan)
            val spanFlags = editableText.getSpanFlags(existingListSpan)
            editableText.removeSpan(existingListSpan)

            editableText.setSpan(makeBlockSpan(listTypeToSwitchTo, nestingLevel), spanStart, spanEnd, spanFlags)
            editor.onSelectionChanged(start, end)
        }
    }
}
