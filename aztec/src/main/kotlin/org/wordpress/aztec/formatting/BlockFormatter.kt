package org.wordpress.aztec.formatting

import android.text.Editable
import android.text.Spanned
import android.text.TextUtils
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import org.wordpress.aztec.TextChangedEvent
import org.wordpress.aztec.TextFormat
import org.wordpress.aztec.spans.*
import java.util.*


class BlockFormatter(editor: AztecText, listStyle: ListStyle, quoteStyle: QuoteStyle):AztecFormatter(editor) {

    data class ListStyle(val indicatorColor: Int, val indicatorMargin: Int, val indicatorPadding: Int, val indicatorWidth: Int)
    data class QuoteStyle(val quoteBackground: Int, val quoteColor: Int, val quoteMargin: Int, val quotePadding: Int, val quoteWidth: Int)

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

    fun handleBlockStyling(text: Editable, textChangedEvent: TextChangedEvent) {
        // preserve the attributes on the previous list item when adding a new one
        if (textChangedEvent.isNewLineButNotAtTheBeginning() && textChangedEvent.inputEnd < text.length && text[textChangedEvent.inputEnd] == '\n') {
            val spans = text.getSpans(textChangedEvent.inputEnd, textChangedEvent.inputEnd + 1, AztecListItemSpan::class.java)
            if (spans.size == 1) {
                text.setSpan(spans[0], textChangedEvent.inputStart, textChangedEvent.inputEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        val inputStart = textChangedEvent.inputStart

        val spanToClose = getBlockSpansToClose(text, textChangedEvent)
        spanToClose.forEach {
            var spanEnd = text.getSpanEnd(it)
            var spanStart = text.getSpanStart(it)

            if (spanEnd == spanStart) {
                editableText.removeSpan(it)
            } else if (spanEnd <= text.length) {
                //case for when we remove block element row from first line of EditText end the next line is empty
                if (inputStart == 0 && spanStart > 0 && text[spanStart] == '\n') {
                    spanEnd += 1
                    editor.disableTextChangedListener()
                    text.insert(spanStart, Constants.ZWJ_STRING)
                } else
                //case for when we remove block element row from other lines of EditText end the next line is empty
                    if (text[spanStart] == '\n') {
                        spanStart += 1

                        if (spanStart < text.length && text[spanStart] == '\n' &&
                                text.length >= spanEnd && text.length > spanStart) {
                            spanEnd += 1
                            editor.disableTextChangedListener()
                            text.insert(spanStart, Constants.ZWJ_STRING)
                        }
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
            } else {
                val indexOfLineEnd = text.indexOf('\n', spanEnd - 1, true)

                if (indexOfLineEnd == spanEnd) {
                    spanEnd += textChangedEvent.count
                } else if (indexOfLineEnd == -1) {
                    spanEnd = text.length
                } else {
                    spanEnd = indexOfLineEnd
                }

                if (spanEnd <= textLength) {
                    editableText.setSpan(it,
                            text.getSpanStart(it),
                            spanEnd,
                            Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
                }
            }
        }

        if (textChangedEvent.isAfterZeroWidthJoiner() && !textChangedEvent.isNewLineButNotAtTheBeginning()) {
            editor.disableTextChangedListener()
            text.delete(inputStart - 1, inputStart)
        } else if (textChangedEvent.isAfterZeroWidthJoiner() && textChangedEvent.isNewLineButNotAtTheBeginning()) {
            removeBlockStyle()
            editor.disableTextChangedListener()

            if (inputStart == 1) {
                text.delete(inputStart - 1, inputStart + 1)
            } else {
                text.delete(inputStart - 2, inputStart)
            }

        } else if (!textChangedEvent.isAfterZeroWidthJoiner() && textChangedEvent.isNewLineButNotAtTheBeginning()) {
            //Add ZWJ to the new line at the end of block spans
            val blockSpans = editableText.getSpans(inputStart, inputStart, AztecBlockSpan::class.java)
            if (!blockSpans.isEmpty() && text.getSpanEnd(blockSpans[0]) == inputStart + 1) {
                editor.disableTextChangedListener()
                text.insert(inputStart + 1, Constants.ZWJ_STRING)
            }
        }
    }

    fun removeBlockStyle(textFormat: TextFormat) {
        removeBlockStyle(selectionStart, selectionEnd, makeBlockSpan(textFormat).javaClass)
    }

    fun removeBlockStyle(start: Int = selectionStart, end: Int = selectionEnd,
                         spanType: Class<AztecBlockSpan> = AztecBlockSpan::class.java, ignoreLineBounds: Boolean = false) {
        val spans = editableText.getSpans(start, end, spanType)
        spans.forEach {

            val spanStart = editableText.getSpanStart(it)
            var spanEnd = editableText.getSpanEnd(it)

            //if splitting block set a range that would be excluded from it
            val boundsOfSelectedText = if (ignoreLineBounds) IntRange(start, end) else getSelectedTextBounds(editableText, start, end)

            val startOfLine = boundsOfSelectedText.start
            val endOfLine = boundsOfSelectedText.endInclusive

            val spanPrecedesLine = spanStart < startOfLine
            val spanExtendsBeyondLine = endOfLine < spanEnd

            //remove the span from all the selected lines
            editableText.removeSpan(it)


            //reapply span top "top" and "bottom"
            if (spanPrecedesLine) {
                editableText.setSpan(makeBlockSpan(it.javaClass), spanStart, startOfLine - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            if (spanExtendsBeyondLine) {
                if (editableText[endOfLine] == '\n' && !ignoreLineBounds) {
                    editor.disableTextChangedListener()
                    editableText.delete(endOfLine, endOfLine + 1)
                    spanEnd--
                }

                editableText.setSpan(makeBlockSpan(it.javaClass), endOfLine, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }


    //TODO: Come up with a better way to init spans and get their classes (all the "make" methods)
    fun makeBlockSpan(textFormat: TextFormat, lastItem: AztecListItemSpan = AztecListItemSpan(), attrs: String = ""): AztecBlockSpan {
        when (textFormat) {
            TextFormat.FORMAT_ORDERED_LIST -> return AztecOrderedListSpan(listStyle, attrs, lastItem)
            TextFormat.FORMAT_UNORDERED_LIST -> return AztecUnorderedListSpan(listStyle, attrs, lastItem)
            TextFormat.FORMAT_QUOTE -> return AztecQuoteSpan(quoteStyle, attrs)
            else -> return ParagraphSpan(attrs)
        }
    }


    fun makeBlockSpan(spanType: Class<AztecBlockSpan>, attrs: String = "", lastItem: AztecListItemSpan = AztecListItemSpan()): AztecBlockSpan {
        when (spanType) {
            AztecOrderedListSpan::class.java -> return AztecOrderedListSpan(listStyle, attrs, lastItem)
            AztecUnorderedListSpan::class.java -> return AztecUnorderedListSpan(listStyle, attrs, lastItem)
            AztecQuoteSpan::class.java -> return AztecQuoteSpan(quoteStyle, attrs)
            else -> return ParagraphSpan(attrs)
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
        endOfLine = if (indexOfLastLineBreak != -1) indexOfLastLineBreak else editable.length

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
                    editableText.setSpan(makeBlockSpan(blockElementType), startOfBlock + 1, endOfBlock, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
                }
            }

        } else {
            val boundsOfSelectedText = getSelectedTextBounds(editableText, start, end)

            val startOfLine = boundsOfSelectedText.start
            var endOfLine = boundsOfSelectedText.endInclusive

            val isEmptyLine = startOfLine == endOfLine

            if (isEmptyLine) {
                editor.disableTextChangedListener()
                editableText.insert(startOfLine, Constants.ZWJ_STRING)
                endOfLine += 1
            }

            val spanToApply = makeBlockSpan(blockElementType)

            var startOfBlock: Int = startOfLine
            var endOfBlock: Int = endOfLine


            if (startOfLine != 0) {
                val spansOnPreviousLine = editableText.getSpans(startOfLine - 1, startOfLine - 1, spanToApply.javaClass)
                if (!spansOnPreviousLine.isEmpty()) {
                    startOfBlock = editableText.getSpanStart(spansOnPreviousLine[0])
                    editableText.removeSpan(spansOnPreviousLine[0])
                }
            }

            if (endOfLine != editableText.length) {
                val spanOnNextLine = editableText.getSpans(endOfLine + 1, endOfLine + 1, spanToApply.javaClass)
                if (!spanOnNextLine.isEmpty()) {
                    endOfBlock = editableText.getSpanEnd(spanOnNextLine[0])
                    editableText.removeSpan(spanOnNextLine[0])
                }
            }

            editableText.setSpan(spanToApply, startOfBlock, endOfBlock, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)

            //if the line was empty trigger onSelectionChanged manually to update toolbar buttons status
            if (isEmptyLine) {
                editor.onSelectionChanged(startOfLine, endOfLine)
            }
        }
    }


    fun containsList(textFormat: TextFormat, selStart: Int = selectionStart, selEnd: Int = selectionEnd): Boolean {
        val lines = TextUtils.split(editableText.toString(), "\n")
        val list = ArrayList<Int>()


        for (i in lines.indices) {
            var lineStart = 0
            for (j in 0..i - 1) {
                lineStart += lines[j].length + 1
            }

            val lineEnd = lineStart + lines[i].length
            if (lineStart > lineEnd) {
                continue
            }

            if (lineStart <= selStart && selEnd <= lineEnd) {
                list.add(i)
            } else if (selStart <= lineStart && lineEnd <= selEnd) {
                list.add(i)
            }
        }

        if (list.isEmpty()) return false

        for (i in list) {
            if (!containsList(textFormat, i, editableText)) {
                return false
            }
        }

        return true
    }

    fun containsList(textFormat: TextFormat, index: Int, text: Editable): Boolean {
        val lines = TextUtils.split(text.toString(), "\n")
        if (index < 0 || index >= lines.size) {
            return false
        }

        var start = 0
        for (i in 0..index - 1) {
            start += lines[i].length + 1
        }

        val end = start + lines[index].length
        if (start > end) {
            return false
        }

        val spans = editableText.getSpans(start, end, makeBlockSpan(textFormat).javaClass)
        return spans.size > 0
    }


    fun containQuote(selStart: Int = selectionStart, selEnd: Int = selectionEnd): Boolean {
        val lines = TextUtils.split(editableText.toString(), "\n")
        val list = ArrayList<Int>()

        for (i in lines.indices) {
            var lineStart = 0
            for (j in 0..i - 1) {
                lineStart += lines[j].length + 1
            }

            val lineEnd = lineStart + lines[i].length
            if (lineStart >= lineEnd) {
                continue
            }

            if (lineStart <= selStart && selEnd <= lineEnd) {
                list.add(i)
            } else if (selStart <= lineStart && lineEnd <= selEnd) {
                list.add(i)
            }
        }

        if (list.isEmpty()) return false

        for (i in list) {
            if (!containQuote(i)) {
                return false
            }
        }

        return true
    }

    fun containQuote(index: Int): Boolean {
        val lines = TextUtils.split(editableText.toString(), "\n")
        if (index < 0 || index >= lines.size) {
            return false
        }

        var start = 0
        for (i in 0..index - 1) {
            start += lines[i].length + 1
        }

        val end = start + lines[index].length
        if (start >= end) {
            return false
        }

        val spans = editableText.getSpans(start, end, AztecQuoteSpan::class.java)
        return spans.size > 0
    }

    fun switchListType(listTypeToSwitchTo: TextFormat, start: Int = selectionStart, end: Int = selectionEnd) {
        val spans = editableText.getSpans(start, end, AztecListSpan::class.java)

        if (spans.isEmpty()) return

        val existingListSpan = spans[0]

        val spanStart = editableText.getSpanStart(existingListSpan)
        val spanEnd = editableText.getSpanEnd(existingListSpan)
        val spanFlags = editableText.getSpanFlags(existingListSpan)
        editableText.removeSpan(existingListSpan)

        editableText.setSpan(makeBlockSpan(listTypeToSwitchTo), spanStart, spanEnd, spanFlags)
        editor.onSelectionChanged(start, end)
    }


    fun getBlockSpanToOpen(editableText: Editable, textChangedEvent: TextChangedEvent): ArrayList<AztecLineBlockSpan> {
        val spansToClose = ArrayList<AztecLineBlockSpan>()
        if (textChangedEvent.count >= 0) {
            if (editableText.length > textChangedEvent.inputStart) {
                val spans = editableText.getSpans(textChangedEvent.inputStart, textChangedEvent.inputStart, AztecLineBlockSpan::class.java)

                spans.forEach {
                    val previousCharacter =
                            if (textChangedEvent.isAddingCharacters) editableText[textChangedEvent.inputStart - 1]
                            else editableText[textChangedEvent.inputEnd]

                    if (previousCharacter == '\n') return@forEach

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


        } else if (startIndex == 0 && textChangedEvent.count == 1 && editableText.length > 0) {
            val spansAfterInput = editableText.getSpans(startIndex + 1, startIndex + 1, AztecBlockSpan::class.java)
            spansAfterInput.forEach {
                spansToClose.add(it)
            }
        }

        return spansToClose

    }
}