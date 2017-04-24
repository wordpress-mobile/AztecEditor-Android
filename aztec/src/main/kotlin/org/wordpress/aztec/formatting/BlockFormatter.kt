package org.wordpress.aztec.formatting

import android.text.Editable
import android.text.Spanned
import android.text.TextUtils
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import org.wordpress.aztec.TextFormat
import org.wordpress.aztec.handlers.BlockHandler
import org.wordpress.aztec.handlers.HeadingHandler
import org.wordpress.aztec.handlers.ListItemHandler
import org.wordpress.aztec.spans.*
import java.util.*

class BlockFormatter(editor: AztecText, val listStyle: ListStyle, val quoteStyle: QuoteStyle, val headerStyle: HeaderStyle, val preformatStyle: PreformatStyle) : AztecFormatter(editor) {

    data class ListStyle(val indicatorColor: Int, val indicatorMargin: Int, val indicatorPadding: Int, val indicatorWidth: Int, val verticalPadding: Int)
    data class QuoteStyle(val quoteBackground: Int, val quoteColor: Int, val quoteBackgroundAlpha: Float, val quoteMargin: Int, val quotePadding: Int, val quoteWidth: Int, val verticalPadding: Int)
    data class PreformatStyle(val preformatBackground: Int, val preformatBackgroundAlpha: Float, val preformatColor: Int, val verticalPadding: Int)
    data class HeaderStyle(val verticalPadding: Int)

    fun toggleOrderedList() {
        if (!containsList(TextFormat.FORMAT_ORDERED_LIST, 0)) {
            if (containsList(TextFormat.FORMAT_UNORDERED_LIST, 0)) {
                switchListType(TextFormat.FORMAT_ORDERED_LIST)
            } else {
                applyBlockStyle(TextFormat.FORMAT_ORDERED_LIST)
            }
        } else {
            removeBlockStyle(TextFormat.FORMAT_ORDERED_LIST)
        }
    }

    fun toggleUnorderedList() {
        if (!containsList(TextFormat.FORMAT_UNORDERED_LIST, 0)) {
            if (containsList(TextFormat.FORMAT_ORDERED_LIST, 0)) {
                switchListType(TextFormat.FORMAT_UNORDERED_LIST)
            } else {
                applyBlockStyle(TextFormat.FORMAT_UNORDERED_LIST)
            }
        } else {
            removeBlockStyle(TextFormat.FORMAT_UNORDERED_LIST)
        }
    }

    fun toggleQuote() {
        if (!containQuote()) {
            applyBlockStyle(TextFormat.FORMAT_QUOTE)
        } else {
            removeBlockStyle(TextFormat.FORMAT_QUOTE)
        }
    }

    fun toggleHeading(textFormat: TextFormat) {
        when (textFormat) {
            TextFormat.FORMAT_HEADING_1,
            TextFormat.FORMAT_HEADING_2,
            TextFormat.FORMAT_HEADING_3,
            TextFormat.FORMAT_HEADING_4,
            TextFormat.FORMAT_HEADING_5,
            TextFormat.FORMAT_HEADING_6 -> {
                if (!containsHeadingOnly(textFormat)) {
                    if (containsOtherHeadings(textFormat)) {
                        switchHeaderType(textFormat)
                    } else {
                        applyBlockStyle(textFormat)
                    }
                }
            }
            TextFormat.FORMAT_PARAGRAPH -> {
                val span = editableText.getSpans(selectionStart, selectionEnd, AztecHeadingSpan::class.java).firstOrNull()

                if (span != null) {
                    removeBlockStyle(span.textFormat)
                }

                removeBlockStyle(TextFormat.FORMAT_PREFORMAT)
            }
            TextFormat.FORMAT_PREFORMAT -> {
                if (!containPreformat()) {
                    if (containsOtherHeadings(TextFormat.FORMAT_PREFORMAT)) {
                        val span = editableText.getSpans(selectionStart, selectionEnd, AztecHeadingSpan::class.java).firstOrNull()

                        if (span != null) {
                            removeBlockStyle(span.textFormat)
                        }
                    }

                    applyBlockStyle(textFormat)
                }
            }
            else -> { }
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

            if (spanEnd <= indexOfNewline + 1) {
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

    fun removeBlockStyle(textFormat: TextFormat) {
        removeBlockStyle(selectionStart, selectionEnd, makeBlock(textFormat, 0).map { it -> it.javaClass })
    }

    fun removeBlockStyle(originalStart: Int, originalEnd: Int,
                         spanTypes: List<Class<AztecBlockSpan>> = Arrays.asList(AztecBlockSpan::class.java),
                         ignoreLineBounds: Boolean = false) {
        var start = originalStart
        var end = originalEnd

        //if splitting block set a range that would be excluded from it
        val boundsOfSelectedText = if (ignoreLineBounds) IntRange(start, end)
                else getSelectedTextBounds(editableText, start, end)

        var startOfBounds = boundsOfSelectedText.start
        var endOfBounds = boundsOfSelectedText.endInclusive

        if (ignoreLineBounds) {
            val hasPrecedingSpans = spanTypes.any { spanType ->
                editableText.getSpans(start, end, spanType)
                        .any { span -> editableText.getSpanStart(span) < startOfBounds }
            }

            if (hasPrecedingSpans) {
                // let's make sure there's a newline before bounds start
                if (editableText[startOfBounds - 1] != Constants.NEWLINE) {
                    // insert a newline in the start of (inside) the bounds
                    editableText.insert(startOfBounds, "" + Constants.NEWLINE)

                    // the insertion will have pushed everything forward so, adjust indices
                    start++
                    end++
                    startOfBounds++
                    endOfBounds++
                }
            }

            val hasExtendingBeyondSpans = spanTypes.any { spanType ->
                editableText.getSpans(start, end, spanType)
                        .any { span -> endOfBounds < editableText.getSpanEnd(span) }
            }

            if (hasExtendingBeyondSpans) {
                // let's make sure there's a newline before bounds end
                if (editableText[endOfBounds] != Constants.NEWLINE) {
                    // insert a newline before the bounds
                    editableText.insert(endOfBounds, "" + Constants.NEWLINE)

                    // the insertion will have pushed the end forward so, adjust the indices
                    end++
                    endOfBounds++

                    if (selectionEnd == endOfBounds) {
                        // apparently selection end moved along when we inserted the newline but we need it to stay
                        //  back in order to save the newline from potential removal
                        editor.setSelection(if (selectionStart != selectionEnd) selectionStart else selectionEnd - 1,
                                selectionEnd - 1)
                    }
                }
            }
        }

        spanTypes.forEach { spanType ->
            val spans = editableText.getSpans(start, end, spanType)
            spans.forEach { span ->

                val spanStart = editableText.getSpanStart(span)
                val spanEnd = editableText.getSpanEnd(span)

                val spanPrecedesLine = spanStart < startOfBounds
                val spanExtendsBeyondLine = endOfBounds < spanEnd

                if (spanPrecedesLine && !spanExtendsBeyondLine) {
                    // pull back the end of the block span
                    BlockHandler.set(editableText, span, spanStart, startOfBounds)
                } else if (spanExtendsBeyondLine && !spanPrecedesLine) {
                    // push the start of the block span
                    BlockHandler.set(editableText, span, endOfBounds, spanEnd)
                } else if (spanPrecedesLine && spanExtendsBeyondLine) {
                    // we need to split the span into two parts

                    // first, let's pull back the end of the existing span
                    BlockHandler.set(editableText, span, spanStart, startOfBounds)

                    // now, let's "clone" the span and set it
                    BlockHandler.set(editableText, makeBlockSpan(span.javaClass, span.nestingLevel, span.attributes),
                            endOfBounds, spanEnd)
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
            TextFormat.FORMAT_HEADING_1,
            TextFormat.FORMAT_HEADING_2,
            TextFormat.FORMAT_HEADING_3,
            TextFormat.FORMAT_HEADING_4,
            TextFormat.FORMAT_HEADING_5,
            TextFormat.FORMAT_HEADING_6 -> return AztecHeadingSpan::class.java
            else -> return ParagraphSpan::class.java
        }
    }


    //TODO: Come up with a better way to init spans and get their classes (all the "make" methods)
    fun makeBlock(textFormat: TextFormat, nestingLevel: Int, attrs: AztecAttributes = AztecAttributes()): List<AztecBlockSpan> {
        when (textFormat) {
            TextFormat.FORMAT_ORDERED_LIST -> return Arrays.asList(AztecOrderedListSpan(nestingLevel, attrs, listStyle), AztecListItemSpan(nestingLevel + 1))
            TextFormat.FORMAT_UNORDERED_LIST -> return Arrays.asList(AztecUnorderedListSpan(nestingLevel, attrs, listStyle), AztecListItemSpan(nestingLevel + 1))
            TextFormat.FORMAT_QUOTE -> return Arrays.asList(AztecQuoteSpan(nestingLevel, attrs, quoteStyle))
            TextFormat.FORMAT_HEADING_1,
            TextFormat.FORMAT_HEADING_2,
            TextFormat.FORMAT_HEADING_3,
            TextFormat.FORMAT_HEADING_4,
            TextFormat.FORMAT_HEADING_5,
            TextFormat.FORMAT_HEADING_6 -> return Arrays.asList(AztecHeadingSpan(nestingLevel, textFormat, attrs, headerStyle))
            TextFormat.FORMAT_PREFORMAT -> return Arrays.asList(AztecPreformatSpan(nestingLevel, attrs, preformatStyle))
            else -> return Arrays.asList(ParagraphSpan(nestingLevel, attrs))
        }
    }

    fun makeBlockSpan(textFormat: TextFormat, nestingLevel: Int, attrs: AztecAttributes = AztecAttributes()): AztecBlockSpan {
        return when (textFormat) {
            TextFormat.FORMAT_ORDERED_LIST -> makeBlockSpan(AztecOrderedListSpan::class.java, nestingLevel, attrs)
            TextFormat.FORMAT_UNORDERED_LIST -> makeBlockSpan(AztecUnorderedListSpan::class.java, nestingLevel, attrs)
            TextFormat.FORMAT_QUOTE -> makeBlockSpan(AztecQuoteSpan::class.java, nestingLevel, attrs)
            TextFormat.FORMAT_HEADING_1,
            TextFormat.FORMAT_HEADING_2,
            TextFormat.FORMAT_HEADING_3,
            TextFormat.FORMAT_HEADING_4,
            TextFormat.FORMAT_HEADING_5,
            TextFormat.FORMAT_HEADING_6 -> return AztecHeadingSpan(nestingLevel, textFormat, attrs, headerStyle)
            TextFormat.FORMAT_PREFORMAT -> return AztecPreformatSpan(nestingLevel, attrs, preformatStyle)
            else -> ParagraphSpan(nestingLevel, attrs)
        }
    }

    fun <T : Class<out AztecBlockSpan>> makeBlockSpan(type: T, nestingLevel: Int, attrs: AztecAttributes = AztecAttributes()): AztecBlockSpan {
        return when (type) {
            AztecOrderedListSpan::class.java -> AztecOrderedListSpan(nestingLevel, attrs, listStyle)
            AztecUnorderedListSpan::class.java -> AztecUnorderedListSpan(nestingLevel, attrs, listStyle)
            AztecListItemSpan::class.java -> AztecListItemSpan(nestingLevel, attrs)
            AztecQuoteSpan::class.java -> AztecQuoteSpan(nestingLevel, attrs, quoteStyle)
            AztecHeadingSpan::class.java -> AztecHeadingSpan(nestingLevel, "", attrs)
            else -> ParagraphSpan(nestingLevel, attrs)
        }
    }

    fun setBlockStyle(blockElement: AztecBlockSpan) {
        when (blockElement) {
            is AztecOrderedListSpan -> blockElement.listStyle = listStyle
            is AztecUnorderedListSpan -> blockElement.listStyle = listStyle
            is AztecQuoteSpan -> blockElement.quoteStyle = quoteStyle
            is AztecPreformatSpan -> blockElement.preformatStyle = preformatStyle
            is AztecHeadingSpan -> blockElement.headerStyle = headerStyle
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


    fun applyBlockStyle(blockElementType: TextFormat, start: Int = selectionStart, end: Int = selectionEnd) {
        if (start != end) {
            val nestingLevel = AztecNestable.getNestingLevelAt(editableText, start)

            if (AztecNestable.getNestingLevelAt(editableText, end) != nestingLevel) {
                // TODO: styling across multiple nesting levels not support yet
                return
            }

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
                removeBlockStyle(blockElementType)
            } else {
                applyBlock(makeBlockSpan(blockElementType, nestingLevel), startOfBlock + 1,
                        (if (endOfBlock == editableText.length) endOfBlock else endOfBlock + 1))
            }

        } else {
            val boundsOfSelectedText = getSelectedTextBounds(editableText, start, end)

            val startOfLine = boundsOfSelectedText.start
            val endOfLine = boundsOfSelectedText.endInclusive

            val nestingLevel = AztecNestable.getNestingLevelAt(editableText, start) + 1

            val spanToApply = makeBlockSpan(blockElementType, nestingLevel)

            var startOfBlock: Int = startOfLine
            var endOfBlock: Int = endOfLine


            if (startOfLine != 0) {
                val spansOnPreviousLine = editableText.getSpans(startOfLine - 1, startOfLine - 1, spanToApply.javaClass)
                        .firstOrNull()

                if (spansOnPreviousLine == null) {
                    // no similar blocks before us so, don't expand
                } else if (spansOnPreviousLine.nestingLevel != nestingLevel) {
                    // other block is at a different nesting level so, don't expand
                } else if (spansOnPreviousLine is AztecHeadingSpan
                        && spansOnPreviousLine.heading != (spanToApply as AztecHeadingSpan).heading) {
                    // Heading span is of different style so, don't expand
                } else {
                    // expand the start
                    startOfBlock = editableText.getSpanStart(spansOnPreviousLine)
                    liftBlock(blockElementType, startOfBlock, endOfBlock)
                }
            }

            if (endOfLine != editableText.length) {
                val spanOnNextLine = editableText.getSpans(endOfLine + 1, endOfLine + 1, spanToApply.javaClass)
                        .firstOrNull()

                if (spanOnNextLine == null) {
                    // no similar blocks after us so, don't expand
                } else if (spanOnNextLine.nestingLevel != nestingLevel) {
                    // other block is at a different nesting level so, don't expand
                } else if (spanOnNextLine is AztecHeadingSpan
                        && spanOnNextLine.heading != (spanToApply as AztecHeadingSpan).heading) {
                    // Heading span is of different style so, don't expand
                } else {
                    // expand the end
                    endOfBlock = editableText.getSpanEnd(spanOnNextLine)
                    liftBlock(blockElementType, startOfBlock, endOfBlock)
                }
            }

            applyBlock(spanToApply, startOfBlock, endOfBlock)

            //if the line was empty trigger onSelectionChanged manually to update toolbar buttons status
//            if (isEmptyLine) {
                editor.onSelectionChanged(startOfLine, endOfLine)
//            }
        }
    }

    private fun applyBlock(blockSpan: AztecBlockSpan, start: Int, end: Int) {
        when (blockSpan) {
            is AztecOrderedListSpan -> applyListBlock(blockSpan, start, end)
            is AztecUnorderedListSpan -> applyListBlock(blockSpan, start, end)
            is AztecQuoteSpan -> BlockHandler.set(editableText, blockSpan, start, end)
            is AztecHeadingSpan -> applyHeadingBlock(blockSpan, start, end)
            is AztecPreformatSpan -> BlockHandler.set(editableText, blockSpan, start, end)
            else -> editableText.setSpan(blockSpan, start, end, Spanned.SPAN_PARAGRAPH)
        }
    }

    private fun applyListBlock(listSpan: AztecListSpan, start: Int, end: Int) {
        BlockHandler.set(editableText, listSpan, start, end)

        val lines = TextUtils.split(editableText.substring(start, end), "\n")
        for (i in lines.indices) {
            val lineLength = lines[i].length

            val lineStart = (0..i - 1).sumBy { lines[it].length + 1 }
            val lineEnd = (lineStart + lineLength).let {
                if ((start + it) != editableText.length) it + 1 else it // include the newline or not
            }

            if (lineLength == 0) continue

            ListItemHandler.newListItem(editableText, start + lineStart, start + lineEnd, listSpan.nestingLevel + 1)
        }
    }

    private fun applyHeadingBlock(headingSpan: AztecHeadingSpan, start: Int, end: Int) {
        val lines = TextUtils.split(editableText.substring(start, end), "\n")
        for (i in lines.indices) {
            val splitLength = lines[i].length

            val lineStart = start + (0..i - 1).sumBy { lines[it].length + 1 }
            val lineEnd = Math.min(lineStart + splitLength + 1, end) // +1 to include the newline

            val lineLength = lineEnd - lineStart
            if (lineLength == 0) continue

            HeadingHandler.cloneHeading(editableText, headingSpan, lineStart, lineEnd)
        }
    }

    private fun liftBlock(textFormat: TextFormat, start: Int, end: Int) {
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

    fun containsHeading(textFormat: TextFormat, selStart: Int = selectionStart, selEnd: Int = selectionEnd): Boolean {
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

        return list.any { containHeadingType(textFormat, it) }
    }

    private fun containHeadingType(textFormat: TextFormat, index: Int): Boolean {
        val lines = TextUtils.split(editableText.toString(), "\n")

        if (index < 0 || index >= lines.size) {
            return false
        }

        val start = (0..index - 1).sumBy { lines[it].length + 1 }
        val end = start + lines[index].length

        if (start >= end) {
            return false
        }

        val spans = editableText.getSpans(start, end, AztecHeadingSpan::class.java)

        for (span in spans) {
            when (textFormat) {
                TextFormat.FORMAT_HEADING_1 ->
                    return span.heading == AztecHeadingSpan.Heading.H1
                TextFormat.FORMAT_HEADING_2 ->
                    return span.heading == AztecHeadingSpan.Heading.H2
                TextFormat.FORMAT_HEADING_3 ->
                    return span.heading == AztecHeadingSpan.Heading.H3
                TextFormat.FORMAT_HEADING_4 ->
                    return span.heading == AztecHeadingSpan.Heading.H4
                TextFormat.FORMAT_HEADING_5 ->
                    return span.heading == AztecHeadingSpan.Heading.H5
                TextFormat.FORMAT_HEADING_6 ->
                    return span.heading == AztecHeadingSpan.Heading.H6
                else -> return false
            }
        }

        return false
    }

    fun containsOtherHeadings(textFormat: TextFormat, selStart: Int = selectionStart, selEnd: Int = selectionEnd): Boolean {
        arrayOf(TextFormat.FORMAT_HEADING_1,
                TextFormat.FORMAT_HEADING_2,
                TextFormat.FORMAT_HEADING_3,
                TextFormat.FORMAT_HEADING_4,
                TextFormat.FORMAT_HEADING_5,
                TextFormat.FORMAT_HEADING_6,
                TextFormat.FORMAT_PREFORMAT)
            .filter { it != textFormat }
            .forEach {
                if (containsHeading(it, selStart, selEnd)) {
                    return true
                }
            }

        return false
    }

    fun containsHeadingOnly(textFormat: TextFormat, selStart: Int = selectionStart, selEnd: Int = selectionEnd): Boolean {
        val otherHeadings = arrayOf(
                TextFormat.FORMAT_HEADING_1,
                TextFormat.FORMAT_HEADING_2,
                TextFormat.FORMAT_HEADING_3,
                TextFormat.FORMAT_HEADING_4,
                TextFormat.FORMAT_HEADING_5,
                TextFormat.FORMAT_HEADING_6,
                TextFormat.FORMAT_PREFORMAT)
            .filter { it != textFormat }

        return containsHeading(textFormat, selStart, selEnd) && otherHeadings.none { containsHeading(it, selStart, selEnd) }
    }

    fun containsPreformat(selStart: Int = selectionStart, selEnd: Int = selectionEnd): Boolean {
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

        return list.any { containPreformat(it) }
    }

    fun containPreformat(index: Int): Boolean {
        val lines = TextUtils.split(editableText.toString(), "\n")
        if (index < 0 || index >= lines.size) {
            return false
        }

        val start = (0..index - 1).sumBy { lines[it].length + 1 }
        val end = start + lines[index].length

        if (start >= end) {
            return false
        }

        val spans = editableText.getSpans(start, end, AztecPreformatSpan::class.java)
        return spans.isNotEmpty()
    }

    fun switchListType(listTypeToSwitchTo: TextFormat, start: Int = selectionStart, end: Int = selectionEnd) {
        val existingListSpan = editableText.getSpans(start, end, AztecListSpan::class.java).firstOrNull()
        if (existingListSpan != null) {
            val spanStart = editableText.getSpanStart(existingListSpan)
            val spanEnd = editableText.getSpanEnd(existingListSpan)
            val spanFlags = editableText.getSpanFlags(existingListSpan)
            editableText.removeSpan(existingListSpan)

            editableText.setSpan(makeBlockSpan(listTypeToSwitchTo, existingListSpan.nestingLevel), spanStart, spanEnd, spanFlags)
            editor.onSelectionChanged(start, end)
        }
    }

    fun switchHeaderType(headerTypeToSwitchTo: TextFormat, start: Int = selectionStart, end: Int = selectionEnd) {
        val existingHeaderSpan = editableText.getSpans(start, end, AztecHeadingSpan::class.java).firstOrNull()
        if (existingHeaderSpan != null) {
            val spanStart = editableText.getSpanStart(existingHeaderSpan)
            val spanEnd = editableText.getSpanEnd(existingHeaderSpan)
            val spanFlags = editableText.getSpanFlags(existingHeaderSpan)

            existingHeaderSpan.textFormat = headerTypeToSwitchTo

            editableText.setSpan(existingHeaderSpan, spanStart, spanEnd, spanFlags)
            editor.onSelectionChanged(start, end)
        }
    }
}
