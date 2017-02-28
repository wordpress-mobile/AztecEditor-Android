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
        if (!containsList(TextFormat.FORMAT_ORDERED_LIST)) {
            if (containsList(TextFormat.FORMAT_UNORDERED_LIST)) {
                switchListType(TextFormat.FORMAT_ORDERED_LIST)
            } else {
                applyBlockStyle(TextFormat.FORMAT_ORDERED_LIST)
            }
        } else {
            removeBlockStyle(TextFormat.FORMAT_ORDERED_LIST)
        }
    }

    fun toggleUnorderedList() {
        if (!containsList(TextFormat.FORMAT_UNORDERED_LIST)) {
            if (containsList(TextFormat.FORMAT_ORDERED_LIST)) {
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

    fun tryRemoveBlockStyleFromFirstLine(): Boolean {
        val selectionStart = editor.selectionStart

        //try to remove block styling when pressing backspace at the beginning of the span
        editableText.getSpans(selectionStart, selectionStart, AztecBlockSpan::class.java).forEach {
            val spanStart = editableText.getSpanStart(it)
            var spanEnd = editableText.getSpanEnd(it)

            if (spanStart != selectionStart) return@forEach

            val indexOfNewline = editableText.indexOf('\n', spanStart)

            if (spanStart != 0 && spanEnd == indexOfNewline + 1) {
                spanEnd--
            }

            if (spanEnd == indexOfNewline || indexOfNewline == -1) {
                editableText.removeSpan(it)
                editor.onSelectionChanged(editor.selectionStart, editor.selectionEnd)
            } else {
                editableText.setSpan(it, indexOfNewline + 1, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                editor.onSelectionChanged(editor.selectionStart, editor.selectionEnd)
            }

            return true
        }

        return false
    }

    fun handleBlockStyling(text: Editable, textChangedEvent: TextChangedEvent) {
        val inputStart = textChangedEvent.inputStart
        val inputEnd = textChangedEvent.inputEnd

        val spanToClose = getBlockSpansToClose(text, textChangedEvent)
        spanToClose.forEach {
            var spanEnd = text.getSpanEnd(it)
            val spanStart = text.getSpanStart(it)

            if (spanEnd != spanStart && spanEnd <= text.length) {
                //case for when we remove block element row from first line of EditText end the next line is empty
                if (inputStart == 0 && spanStart > 0 && text[spanStart] == '\n' && !textChangedEvent.isAddingCharacters) {
                    spanEnd += 1
                    editor.disableTextChangedListener()
                    text.insert(spanStart, Constants.ZWJ_STRING)
                }

                editableText.setSpan(it,
                        spanStart,
                        spanEnd,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        val spanToOpen = getBlockSpanToOpen(text, textChangedEvent)
        spanToOpen.forEach {
            val textLength = text.length

            var spanEnd = text.getSpanEnd(it)
            val spanStart = text.getSpanStart(it)

            if (inputStart < spanStart) {
                editableText.setSpan(it,
                        inputStart,
                        spanEnd,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else if (textChangedEvent.isAddingCharacters) {

                val spanEndsWithNewline = editableText.indexOf('\n', textChangedEvent.inputStart, true) == spanEnd - textChangedEvent.numberOfAddedCharacters

                //if span we are inserting text into is ending with newline we don't need to extend it
                if (spanEndsWithNewline) {
                    return@forEach
                }

                val indexOfLineEnd = text.indexOf('\n', spanEnd - 1, true)

                if (indexOfLineEnd == spanEnd) {
                    spanEnd += textChangedEvent.count
                } else if (indexOfLineEnd == -1) {
                    spanEnd = text.length
                } else {
                    spanEnd = indexOfLineEnd + if (textChangedEvent.isAfterZeroWidthJoiner()) 1 else 0
                }

                if (spanEnd <= textLength) {
                    editableText.setSpan(it,
                            text.getSpanStart(it),
                            spanEnd,
                            Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
                }
            }
        }

        if (!textChangedEvent.isAfterZeroWidthJoiner() && textChangedEvent.isNewLineAtTheBeginning()) {
            val listSpan = text.getSpans(inputStart, inputStart, AztecListSpan::class.java).firstOrNull()
            if (listSpan != null) {
                val item = editableText.getSpans(inputStart, inputStart, AztecListItemSpan::class.java).firstOrNull()
                if (item != null) {
                    val itemEnd = editableText.getSpanEnd(item)

                    // adding a new item at the beginning of the current so, push the current one
                    text.setSpan(item, inputEnd, itemEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                    // add a ZWJ for the new item
                    editor.disableTextChangedListener()
                    text.insert(inputStart, Constants.ZWJ_STRING)

                    // adjust parent list span to include the ZWJ
                    if (text.getSpanStart(listSpan) > inputStart) {
                        text.setSpan(listSpan, inputStart, text.getSpanEnd(listSpan), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    // and add the new one at the start
                    text.setSpan(AztecListItemSpan(), inputStart, inputStart + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                } else {
                    // add a ZWJ for the new item
                    editor.disableTextChangedListener()
                    text.insert(inputStart, Constants.ZWJ_STRING)

                    // and add the new item
                    text.setSpan(AztecListItemSpan(), inputStart, inputStart + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

        } else if (textChangedEvent.isAfterZeroWidthJoiner() && !textChangedEvent.isNewLineButNotAtTheBeginning()) {
            // adding a character next to a ZWJ char deletes it
            editor.disableTextChangedListener()
            val before = Math.min(inputStart, inputEnd)
            text.delete(before - 1, before)

            if (!textChangedEvent.isAddingCharacters) {
                val blockSpan = editableText.getSpans(before - 1, before - 1, AztecBlockSpan::class.java).firstOrNull()
                if (blockSpan != null) {
                    val newline = text.indexOf('\n', before - 1)
                    val end = if (newline != -1) Math.min(text.length, newline) else text.length
                    text.setSpan(blockSpan, text.getSpanStart(blockSpan), end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

        } else if (textChangedEvent.isAfterZeroWidthJoiner() && textChangedEvent.isNewLineButNotAtTheBeginning()) {
            val blockSpan = text.getSpans(inputStart, inputStart, AztecBlockSpan::class.java).firstOrNull()
            if (blockSpan != null) {
                if (blockSpan is AztecListSpan) {
                    if (text.getSpanEnd(blockSpan) == inputEnd) {
                        val item = text.getSpans(inputStart, inputStart, AztecListItemSpan::class.java).firstOrNull()
                        if (item != null) {
                            // remove the current item
                            text.removeSpan(item)
                        }

                        val listStart = text.getSpanStart(blockSpan)

                        // remove the whole list if no other content!
                        if (listStart == inputStart - 1) {
                            text.removeSpan(blockSpan)
                        } else {
                            // adjust the list element back now that the last item is gone
                            text.setSpan(blockSpan, listStart, inputStart - 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }

                        // remove the existing ZWJ of the empty list item line and the newline that was just entered.
                        editor.disableTextChangedListener()
                        text.delete(inputStart - 1, inputEnd)
                    } else {
                        // shorten the end of the current list item
                        val item = text.getSpans(inputStart, inputStart, AztecListItemSpan::class.java).firstOrNull()
                        if (item != null) {
                            text.setSpan(item, text.getSpanStart(item), inputStart, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }

                        // add a ZWJ for the new item
                        editor.disableTextChangedListener()
                        text.insert(inputEnd, Constants.ZWJ_STRING)

                        // and add the new item
                        text.setSpan(AztecListItemSpan(), inputEnd, inputEnd + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                } else {
                    // double enter deletes the last item and adds a linebreak
                    removeBlockStyle()
                    editor.disableTextChangedListener()

                    if (inputStart == 1) {
                        text.delete(inputStart - 1, inputStart + 1)
                    } else {
                        text.delete(inputStart - 2, inputStart)

                        // After closing a list add an extra newline
                        if (text.getSpans(inputStart - 2, inputStart - 2, AztecListSpan::class.java).isNotEmpty()) {
                            editor.disableTextChangedListener()
                            text.insert(inputStart - 2, "\n")
                        }
                    }
                }
            }

        } else if (!textChangedEvent.isAfterZeroWidthJoiner() && textChangedEvent.isNewLineButNotAtTheBeginning()) {
            // add ZWJ to the new line at the end of block spans
            val blockSpan = editableText.getSpans(inputStart, inputStart, AztecBlockSpan::class.java).firstOrNull()
            if (blockSpan != null && text.getSpanEnd(blockSpan) == inputStart + 1 ||
                    (text.getSpanEnd(blockSpan) == inputStart + 2 && text[inputStart + 1] == '\n')) {
                if (inputEnd == text.length || text[inputEnd] == '\n') {
                    editor.disableTextChangedListener()
                    text.insert(inputStart + 1, Constants.ZWJ_STRING)
                    text.setSpan(blockSpan, text.getSpanStart(blockSpan), inputStart + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                } else {
                    text.setSpan(blockSpan, text.getSpanStart(blockSpan), inputEnd + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            } else {
                if (blockSpan is AztecListSpan) {
                    val item = editableText.getSpans(inputStart, inputStart, AztecListItemSpan::class.java).firstOrNull()
                    if (item != null) {
                        val itemStart = editableText.getSpanStart(item)
                        val itemEnd = editableText.getSpanEnd(item)

                        if (inputStart == itemStart) {
                            // adding a new item at the beginning of the current so, push the current one
                            text.setSpan(item, inputEnd, itemEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                            // add a ZWJ for the new item
                            editor.disableTextChangedListener()
                            text.insert(inputStart, Constants.ZWJ_STRING)

                            // adjust parent list span to include the ZWJ
                            if (text.getSpanStart(blockSpan) > inputStart) {
                                text.setSpan(blockSpan, inputStart, text.getSpanEnd(blockSpan), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }

                            // and add the new one at the start
                            text.setSpan(AztecListItemSpan(), inputStart, inputStart + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        } else if (inputStart > itemStart && inputStart < itemEnd) {
                            // adding a new item in the middle of the current so, make a split
                            text.setSpan(item, itemStart, inputStart, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                            text.setSpan(AztecListItemSpan(), inputStart + 1, itemEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        } else if (inputStart > itemStart && inputStart == itemEnd) {
                            // adding a new item at the end of the current so, retrack the current one a bit
                            text.setSpan(item, itemStart, inputStart, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                            // add a ZWJ for the new item
                            editor.disableTextChangedListener()
                            text.insert(inputEnd, Constants.ZWJ_STRING)

                            text.setSpan(AztecListItemSpan(), inputEnd, inputEnd + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    } else {
                        // add a ZWJ for the new item
                        editor.disableTextChangedListener()
                        text.insert(inputStart, Constants.ZWJ_STRING)

                        // and add the new item
                        text.setSpan(AztecListItemSpan(), inputStart, inputStart + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            }
        } else if (textChangedEvent.deletedFromBlockEnd) {
            var addedZwJAftEnd = false
            // when deleting characters, manage closing of lists
            text.getSpans(inputEnd, inputEnd, AztecBlockSpan::class.java).forEach {
                val spanStart = text.getSpanStart(it)
                val spanEndOriginal = text.getSpanEnd(it)
                if (textChangedEvent.textBefore[inputEnd] != Constants.ZWJ_CHAR) {
                    if (!addedZwJAftEnd) {
                        // add ZWJ at the beginning of line when last regular char deleted
                        editor.disableTextChangedListener()
                        text.insert(inputEnd, Constants.ZWJ_STRING)
                        addedZwJAftEnd = true
                    }

                    val newSpanStart = if (spanStart > inputEnd) inputEnd else spanStart

                    val spanEnd = text.getSpanEnd(it)
                    val newSpanEnd = if (inputEnd >= spanEnd) inputEnd + 1 else spanEnd
                    text.setSpan(it, newSpanStart, newSpanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                } else if ((inputEnd - 2 >= spanStart && text[inputEnd - 2] == '\n') || inputEnd - 1 == spanStart) {
                    // if ZWJ char got just deleted, add it to the line above if it's empty
                    editor.disableTextChangedListener()
                    text.insert(inputEnd - 1, Constants.ZWJ_STRING)

                    if (inputEnd - 1 < spanStart + 1) {
                        text.setSpan(it, inputEnd - 1, spanEndOriginal, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    // delete the last newline
                    editor.disableTextChangedListener()
                    text.delete(inputEnd, inputEnd + 1)
                } else if (spanEndOriginal > 0 && spanStart != spanEndOriginal) {
                    // delete the last newline
                    editor.disableTextChangedListener()
                    text.delete(spanEndOriginal - 1, spanEndOriginal)
                } else {
                    text.removeSpan(it)
                }
            }
        } else if (!textChangedEvent.isAddingCharacters && !textChangedEvent.isNewLineButNotAtTheBeginning()) {
            val deletedCharacterIsNewline = textChangedEvent.textBefore.length > textChangedEvent.inputEnd &&
                    textChangedEvent.textBefore[textChangedEvent.inputEnd] == '\n'
            if (!deletedCharacterIsNewline) return

            // backspace on a line right after a list attaches the line to the last item
            editableText.getSpans(inputEnd, inputEnd, AztecBlockSpan::class.java).forEach {
                val before = Math.min(inputStart, inputEnd)
                val spanEnd = text.getSpanEnd(it)
                val spanStart = text.getSpanStart(it)

                if (spanEnd - 1 > 0 && spanStart < spanEnd && spanEnd > 0 && text[spanEnd - 1] == '\n') {
                    text.setSpan(it, spanStart, spanEnd - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                if (it != null && before - 1 > 0 && text.getSpanEnd(it) == before) {
                    if (textChangedEvent.textBefore[before] == Constants.ZWJ_CHAR) {
                        editor.disableTextChangedListener()
                        text.delete(before - 1, before)
                    }

                    val newline = text.indexOf('\n', before)
                    val end = if (newline != -1) Math.min(text.length, text.indexOf('\n', before)) else text.length
                    text.setSpan(it, text.getSpanStart(it), end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }

        //remove inline styles from the ZWJ characters we inserted
        var indexOfZWJ = editableText.indexOf(Constants.ZWJ_CHAR)
        while (indexOfZWJ >= 0) {
            editor.removeInlineStylesFromRange(indexOfZWJ, indexOfZWJ + 1)
            indexOfZWJ = editableText.indexOf(Constants.ZWJ_CHAR, indexOfZWJ + 1)
        }

    }

    fun removeBlockStyle(textFormat: TextFormat) {
        removeBlockStyle(selectionStart, selectionEnd, makeBlockSpan(textFormat).map { it -> it.javaClass })
    }

    fun removeBlockStyle(start: Int = selectionStart, end: Int = selectionEnd,
                         spanTypes: List<Class<AztecBlockSpan>> = Arrays.asList(AztecBlockSpan::class.java),
                         ignoreLineBounds: Boolean = false) {
        spanTypes.forEach {
            val spans = editableText.getSpans(start, end, it)
            spans.forEach {

                val spanStart = editableText.getSpanStart(it)
                var spanEnd = editableText.getSpanEnd(it)

                //if splitting block set a range that would be excluded from it
                val boundsOfSelectedText = if (ignoreLineBounds) IntRange(start, end) else getSelectedTextBounds(editableText, start, end)

                val startOfLine = boundsOfSelectedText.start
                val endOfLine = boundsOfSelectedText.endInclusive

                val spanPrecedesLine = spanStart < startOfLine
                val spanExtendsBeyondLine = endOfLine < spanEnd

                //reapply span top "top" and "bottom"
                if (spanPrecedesLine) {
                    editableText.setSpan(it, spanStart, startOfLine - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                if (spanExtendsBeyondLine) {
                    if (editableText[endOfLine] == '\n' && !ignoreLineBounds) {
                        editor.disableTextChangedListener()
                        editableText.delete(endOfLine, endOfLine + 1)
                        spanEnd--
                    }

                    editableText.setSpan(it, endOfLine, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
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
    fun makeBlockSpan(textFormat: TextFormat, attrs: String = ""): List<AztecBlockSpan> {
        when (textFormat) {
            TextFormat.FORMAT_ORDERED_LIST -> return Arrays.asList(AztecOrderedListSpan(listStyle, attrs), AztecListItemSpan())
            TextFormat.FORMAT_UNORDERED_LIST -> return Arrays.asList(AztecUnorderedListSpan(listStyle, attrs), AztecListItemSpan())
            TextFormat.FORMAT_QUOTE -> return Arrays.asList(AztecQuoteSpan(quoteStyle, attrs))
            else -> return Arrays.asList(ParagraphSpan(attrs))
        }
    }

    fun setBlockStyle(blockElement: AztecBlockSpan) {
        when (blockElement) {
            is AztecOrderedListSpan -> blockElement.setStyle(listStyle)
            is AztecUnorderedListSpan -> blockElement.setStyle(listStyle)
            is AztecQuoteSpan -> blockElement.setStyle(quoteStyle)
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
                    if (containsList(blockElementType, i, selectedLines)) {
                        numberOfLinesWithSpanApplied++
                    }
                }

                if (numberOfLines == numberOfLinesWithSpanApplied) {
                    removeBlockStyle(blockElementType)
                } else {
                    applyBlock(blockElementType, startOfBlock + 1, endOfBlock)
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

            applyBlock(blockElementType, startOfBlock, endOfBlock)

            //if the line was empty trigger onSelectionChanged manually to update toolbar buttons status
//            if (isEmptyLine) {
                editor.onSelectionChanged(startOfLine, endOfLine)
//            }
        }
    }

    private fun applyBlock(textFormat: TextFormat, start: Int, end: Int, attrs: String = "") {
        when (textFormat) {
            TextFormat.FORMAT_ORDERED_LIST -> applyListBlock(AztecOrderedListSpan(listStyle, attrs), start, end)
            TextFormat.FORMAT_UNORDERED_LIST -> applyListBlock(AztecUnorderedListSpan(listStyle, attrs), start, end)
            TextFormat.FORMAT_QUOTE -> editableText.setSpan(AztecQuoteSpan(quoteStyle, attrs), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            else -> editableText.setSpan(ParagraphSpan(attrs), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun applyListBlock(listSpan: AztecListSpan, start: Int, end: Int) {
        ListHandler.newList(editableText, listSpan, start, end)

        val lines = TextUtils.split(editableText.substring(start, end), "\n")
        for (i in lines.indices) {
            val lineLength = lines[i].length

            val lineStart = (0..i - 1).sumBy { lines[it].length + 1 }
            val lineEnd = (lineStart + lineLength).let {
                if (it != editableText.length) it + 1 else it // include the newline or not
            }

            if (lineLength == 0) continue

            ListHandler.newListItem(editableText, start + lineStart, start + lineEnd)
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

    fun containsList(textFormat: TextFormat, selStart: Int = selectionStart, selEnd: Int = selectionEnd): Boolean {
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

        return list.any { containsList(textFormat, it, editableText) }
    }

    fun containsList(textFormat: TextFormat, index: Int, text: Editable): Boolean {
        val lines = TextUtils.split(text.toString(), "\n")
        if (index < 0 || index >= lines.size) {
            return false
        }

        val start = (0..index - 1).sumBy { lines[it].length + 1 }
        val end = start + lines[index].length

        if (start > end) {
            return false
        }

        val spans = editableText.getSpans(start, end, makeBlockSpan(textFormat).first().javaClass)
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

    fun switchListType(listTypeToSwitchTo: TextFormat, start: Int = selectionStart, end: Int = selectionEnd) {
        val existingListSpan = editableText.getSpans(start, end, AztecListSpan::class.java).firstOrNull()
        if (existingListSpan != null) {
            val spanStart = editableText.getSpanStart(existingListSpan)
            val spanEnd = editableText.getSpanEnd(existingListSpan)
            val spanFlags = editableText.getSpanFlags(existingListSpan)
            editableText.removeSpan(existingListSpan)

            editableText.setSpan(makeBlockSpan(listTypeToSwitchTo).first(), spanStart, spanEnd, spanFlags)
            editor.onSelectionChanged(start, end)
        }
    }


    fun getBlockSpanToOpen(editableText: Editable, textChangedEvent: TextChangedEvent): ArrayList<AztecLineBlockSpan> {
        val spansToClose = ArrayList<AztecLineBlockSpan>()
        if (textChangedEvent.count >= 0) {
            if (editableText.length > textChangedEvent.inputStart) {
                val spans = editableText.getSpans(textChangedEvent.inputStart, textChangedEvent.inputStart, AztecLineBlockSpan::class.java)

                spans.forEach {
                    if (textChangedEvent.inputStart > 0) {
                        val previousCharacter =
                                if (textChangedEvent.isAddingCharacters) editableText[textChangedEvent.inputStart - 1]
                                else editableText[textChangedEvent.inputEnd]

                        if (previousCharacter == '\n') return@forEach
                    }

                    val deletingLastCharacter = !textChangedEvent.isAddingCharacters && editableText.length == textChangedEvent.inputEnd
                    if (deletingLastCharacter) return@forEach

                    if (!textChangedEvent.isAddingCharacters && editableText.length > textChangedEvent.inputEnd) {
                        val lastCharacter = editableText[textChangedEvent.inputEnd]
                        if (lastCharacter == '\n') return@forEach
                    }


                    val flags = editableText.getSpanFlags(spans[0])
                    if ((flags and Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) == Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) {
                        spansToClose.add(it)
                    }
                }

                if (spans.isEmpty()) {
                    val spansAfterInput = editableText.getSpans(textChangedEvent.inputEnd, textChangedEvent.inputEnd, AztecLineBlockSpan::class.java)
                    spansAfterInput.forEach {
                        val flags = editableText.getSpanFlags(spansAfterInput[0])
                        if (((flags and Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) == Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) ||
                                (flags and Spanned.SPAN_EXCLUSIVE_INCLUSIVE) == Spanned.SPAN_EXCLUSIVE_INCLUSIVE) {
                            spansToClose.add(it)
                        }
                    }
                }
            }
        }

        return spansToClose

    }

    fun getBlockSpansToClose(editableText: Editable, textChangedEvent: TextChangedEvent): ArrayList<AztecLineBlockSpan> {
        val spansToClose = ArrayList<AztecLineBlockSpan>()

        val startIndex = if (textChangedEvent.isAddingCharacters) textChangedEvent.inputStart else textChangedEvent.inputEnd
        if (startIndex > 0 && textChangedEvent.count == 1) {
            if (editableText[startIndex - 1] != '\n') return spansToClose

            val spans = editableText.getSpans(startIndex, startIndex, AztecLineBlockSpan::class.java)
            spans.forEach {
                val spanStart = editableText.getSpanStart(spans[0])
                val spanEnd = editableText.getSpanEnd(spans[0])

                if (startIndex == spanStart) {
                    spansToClose.add(it)
                } else if (startIndex == spanEnd) {
                    val flags = editableText.getSpanFlags(spans[0])
                    if ((flags and Spanned.SPAN_EXCLUSIVE_INCLUSIVE) == Spanned.SPAN_EXCLUSIVE_INCLUSIVE) {
                        spansToClose.add(it)
                    }
                }

            }


        } else if (startIndex == 0 && textChangedEvent.count == 1 && editableText.isNotEmpty()) {
            val spansAfterInput = editableText.getSpans(startIndex + 1, startIndex + 1, AztecBlockSpan::class.java)
            spansAfterInput.forEach {
                spansToClose.add(it)
            }
        }

        return spansToClose

    }

    fun realignAttributesWhenAddingItem(text: Editable, textChangedEventDetails: TextChangedEvent, newline: Boolean) {
        if (newline) {
            val list = text.getSpans(textChangedEventDetails.inputStart, textChangedEventDetails.inputStart, AztecListSpan::class.java).firstOrNull()
            if (list != null) {
                val listEnd = text.getSpanEnd(list)

                // when newline inserted before the list item's newline the item's attributes must be shifted up
                if (textChangedEventDetails.inputEnd < text.length && text[textChangedEventDetails.inputEnd] == '\n') {
                    val spans = text.getSpans(textChangedEventDetails.inputEnd, textChangedEventDetails.inputEnd + 1, AztecListItemSpan::class.java)
                    spans.forEach {
                        if (text.getSpanStart(it) == textChangedEventDetails.inputEnd && text.getSpanEnd(it) == textChangedEventDetails.inputEnd + 1) {
                            var spanStart = textChangedEventDetails.inputStart
                            if (text[textChangedEventDetails.inputStart] == Constants.ZWJ_CHAR) {
                                spanStart--
                            }
                            text.setSpan(it, spanStart, spanStart + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                }

                if (textChangedEventDetails.inputEnd == listEnd) {
                    var prevNewline = textChangedEventDetails.inputStart
                    if (text[prevNewline] == Constants.ZWJ_CHAR) {
                        prevNewline--
                    }

                    val listItem = text.getSpans(textChangedEventDetails.inputStart, textChangedEventDetails.inputStart,
                            AztecListItemSpan::class.java).firstOrNull()
                    if (listItem != null) {
                        // reset the previous list item's end
                        text.setSpan(listItem, text.getSpanStart(listItem), prevNewline, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    val newListItem = AztecListItemSpan()
                    text.setSpan(newListItem, prevNewline + 1, listEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }
    }

    fun carryOverDeletedListItemAttributes(count: Int, start: Int, changedText: CharSequence, text: Editable) {
        // when deleting a list item we need to preserve the top item's span
        if (count != 0 && changedText[start] == '\n') {
            val item = text.getSpans(start, start + count, AztecListItemSpan::class.java).firstOrNull()
            if (item != null) {
                val list = text.getSpans(start, start + count, AztecListSpan::class.java).firstOrNull()
                if (list != null) {
                    val listEnd = text.getSpanEnd(list)

                    // find the index of the next remaining list item's newline
                    val next = changedText.indexOf('\n', start + count)
                    if (next != -1 && next < listEnd) {
                        // remove the old list item's span
                        val oldSpan = text.getSpans(next, next + 1, AztecListItemSpan::class.java).firstOrNull()
                        if (oldSpan != null) {
                            text.removeSpan(oldSpan)
                        }

                        // reapply the top item's span
                        text.setSpan(item, next, next + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            }
        }
    }
}
