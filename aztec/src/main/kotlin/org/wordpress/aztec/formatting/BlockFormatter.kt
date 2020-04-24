package org.wordpress.aztec.formatting

import androidx.core.text.TextDirectionHeuristicsCompat
import android.text.Editable
import android.text.Layout
import android.text.Spanned
import android.text.TextUtils
import org.wordpress.android.util.AppLog
import org.wordpress.aztec.AlignmentRendering
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.AztecTextFormat
import org.wordpress.aztec.Constants
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.handlers.BlockHandler
import org.wordpress.aztec.handlers.HeadingHandler
import org.wordpress.aztec.handlers.ListItemHandler
import org.wordpress.aztec.spans.AztecHeadingSpan
import org.wordpress.aztec.spans.AztecListItemSpan
import org.wordpress.aztec.spans.AztecListSpan
import org.wordpress.aztec.spans.AztecOrderedListSpan
import org.wordpress.aztec.spans.AztecPreformatSpan
import org.wordpress.aztec.spans.AztecQuoteSpan
import org.wordpress.aztec.spans.AztecUnorderedListSpan
import org.wordpress.aztec.spans.IAztecAlignmentSpan
import org.wordpress.aztec.spans.IAztecBlockSpan
import org.wordpress.aztec.spans.IAztecCompositeBlockSpan
import org.wordpress.aztec.spans.IAztecLineBlockSpan
import org.wordpress.aztec.spans.IAztecNestable
import org.wordpress.aztec.spans.ParagraphSpan
import org.wordpress.aztec.spans.createAztecQuoteSpan
import org.wordpress.aztec.spans.createHeadingSpan
import org.wordpress.aztec.spans.createListItemSpan
import org.wordpress.aztec.spans.createOrderedListSpan
import org.wordpress.aztec.spans.createParagraphSpan
import org.wordpress.aztec.spans.createPreformatSpan
import org.wordpress.aztec.spans.createUnorderedListSpan
import org.wordpress.aztec.util.SpanWrapper
import java.util.Arrays
import kotlin.reflect.KClass

class BlockFormatter(editor: AztecText,
                     private val listStyle: ListStyle,
                     private val quoteStyle: QuoteStyle,
                     private val headerStyle: HeaderStyle,
                     private val preformatStyle: PreformatStyle,
                     private val alignmentRendering: AlignmentRendering
) : AztecFormatter(editor) {
    data class ListStyle(val indicatorColor: Int, val indicatorMargin: Int, val indicatorPadding: Int, val indicatorWidth: Int, val verticalPadding: Int)
    data class QuoteStyle(val quoteBackground: Int, val quoteColor: Int, val quoteBackgroundAlpha: Float, val quoteMargin: Int, val quotePadding: Int, val quoteWidth: Int, val verticalPadding: Int)
    data class PreformatStyle(val preformatBackground: Int, val preformatBackgroundAlpha: Float, val preformatColor: Int, val verticalPadding: Int)
    data class HeaderStyle(val verticalPadding: Int)

    fun toggleOrderedList() {
        if (!containsList(AztecTextFormat.FORMAT_ORDERED_LIST, 0)) {
            if (containsList(AztecTextFormat.FORMAT_UNORDERED_LIST, 0)) {
                switchListType(AztecTextFormat.FORMAT_ORDERED_LIST)
            } else {
                applyBlockStyle(AztecTextFormat.FORMAT_ORDERED_LIST)
            }
        } else {
            if (containsList(AztecTextFormat.FORMAT_UNORDERED_LIST, 0)) {
                switchListType(AztecTextFormat.FORMAT_ORDERED_LIST)
            } else {
                removeBlockStyle(AztecTextFormat.FORMAT_ORDERED_LIST)
            }
        }
    }

    fun toggleUnorderedList() {
        if (!containsList(AztecTextFormat.FORMAT_UNORDERED_LIST, 0)) {
            if (containsList(AztecTextFormat.FORMAT_ORDERED_LIST, 0)) {
                switchListType(AztecTextFormat.FORMAT_UNORDERED_LIST)
            } else {
                applyBlockStyle(AztecTextFormat.FORMAT_UNORDERED_LIST)
            }
        } else {
            if (containsList(AztecTextFormat.FORMAT_ORDERED_LIST, 0)) {
                switchListType(AztecTextFormat.FORMAT_UNORDERED_LIST)
            } else {
                removeBlockStyle(AztecTextFormat.FORMAT_UNORDERED_LIST)
            }
        }
    }

    fun toggleQuote() {
        if (!containsQuote()) {
            applyBlockStyle(AztecTextFormat.FORMAT_QUOTE)
        } else {
            removeEntireBlock(AztecQuoteSpan::class.java)
        }
    }

    fun toggleHeading(textFormat: ITextFormat) {
        when (textFormat) {
            AztecTextFormat.FORMAT_HEADING_1,
            AztecTextFormat.FORMAT_HEADING_2,
            AztecTextFormat.FORMAT_HEADING_3,
            AztecTextFormat.FORMAT_HEADING_4,
            AztecTextFormat.FORMAT_HEADING_5,
            AztecTextFormat.FORMAT_HEADING_6 -> {
                if (!containsHeadingOnly(textFormat)) {
                    if (containsPreformat()) {
                        switchPreformatToHeading(textFormat)
                    } else if (containsOtherHeadings(textFormat)) {
                        switchHeaderType(textFormat)
                    } else {
                        applyBlockStyle(textFormat)
                    }
                }
            }
            AztecTextFormat.FORMAT_PARAGRAPH -> {
                val span = editableText.getSpans(selectionStart, selectionEnd, AztecHeadingSpan::class.java).firstOrNull()

                if (span != null) {
                    removeBlockStyle(span.textFormat)
                }

                removeBlockStyle(AztecTextFormat.FORMAT_PREFORMAT)
            }
            AztecTextFormat.FORMAT_PREFORMAT -> {
                if (!containsPreformat()) {
                    if (containsOtherHeadings(AztecTextFormat.FORMAT_PREFORMAT)) {
                        switchHeadingToPreformat()
                    } else {
                        applyBlockStyle(textFormat)
                    }
                }
            }
            else -> {
            }
        }
    }

    fun toggleTextAlignment(textFormat: ITextFormat) {
        when (alignmentRendering) {
            AlignmentRendering.VIEW_LEVEL -> {
                val message = "cannot toggle text alignment when ${AlignmentRendering.VIEW_LEVEL} is being used"
                AppLog.d(AppLog.T.EDITOR, message)
            }

            AlignmentRendering.SPAN_LEVEL -> {
                when (textFormat) {
                    AztecTextFormat.FORMAT_ALIGN_LEFT,
                    AztecTextFormat.FORMAT_ALIGN_CENTER,
                    AztecTextFormat.FORMAT_ALIGN_RIGHT ->
                        if (containsAlignment(textFormat)) {
                            removeTextAlignment(textFormat)
                        } else {
                            applyTextAlignment(textFormat)
                        }
                }
            }
        }
    }

    fun removeTextAlignment(textFormat: ITextFormat) {
        getAlignedSpans(textFormat).forEach { changeAlignment(it, null) }
    }

    fun tryRemoveBlockStyleFromFirstLine(): Boolean {
        val selectionStart = editor.selectionStart

        if (selectionStart != 0) {
            // only handle the edge case of start of text
            return false
        }

        var changed = false

        // try to remove block styling when pressing backspace at the beginning of the text
        editableText.getSpans(0, 0, IAztecBlockSpan::class.java).forEach {
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

    fun removeBlockStyle(textFormat: ITextFormat) {
        removeBlockStyle(textFormat, selectionStart, selectionEnd, makeBlock(textFormat, 0).map { it -> it.javaClass })
    }

    fun <T : IAztecBlockSpan> removeEntireBlock(type: Class<T>) {
        editableText.getSpans(selectionStart, selectionEnd, type).forEach {
            IAztecNestable.pullUp(editableText, selectionStart, selectionEnd, it.nestingLevel)
            editableText.removeSpan(it)
        }
    }

    fun removeBlockStyle(textFormat: ITextFormat, originalStart: Int, originalEnd: Int,
                         spanTypes: List<Class<IAztecBlockSpan>> = Arrays.asList(IAztecBlockSpan::class.java),
                         ignoreLineBounds: Boolean = false) {
        var start = originalStart
        var end = originalEnd

        // if splitting block set a range that would be excluded from it
        val boundsOfSelectedText = if (ignoreLineBounds) {
            IntRange(start, end)
        } else {
            getBoundsOfText(editableText, start, end)
        }

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
            // when removing style from multiple selected lines, if the last selected line is empty
            // or at the end of editor the selection wont include the trailing newline/EOB marker
            // that will leave us with orphan <li> tag, so we need to shift index to the right
            val hasLingeringEmptyListItem = AztecListItemSpan::class.java.isAssignableFrom(spanType)
                    && editableText.length > end
                    && (editableText[end] == '\n' || editableText[end] == Constants.END_OF_BUFFER_MARKER)

            val endModifier = if (hasLingeringEmptyListItem) 1 else 0

            val spans = editableText.getSpans(start, end + endModifier, spanType)
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
                    BlockHandler.set(editableText, makeBlockSpan(span::class, textFormat, span.nestingLevel, span.attributes), endOfBounds, spanEnd)
                } else {
                    // tough luck. The span is fully inside the line so it gets axed.

                    IAztecNestable.pullUp(editableText, editableText.getSpanStart(span), editableText.getSpanEnd(span), span.nestingLevel)

                    editableText.removeSpan(span)
                }
            }
        }
    }

    // TODO: Come up with a better way to init spans and get their classes (all the "make" methods)
    fun makeBlock(textFormat: ITextFormat, nestingLevel: Int, attrs: AztecAttributes = AztecAttributes()): List<IAztecBlockSpan> {
        when (textFormat) {
            AztecTextFormat.FORMAT_ORDERED_LIST -> return Arrays.asList(createOrderedListSpan(nestingLevel, alignmentRendering, attrs, listStyle), createListItemSpan(nestingLevel + 1, alignmentRendering))
            AztecTextFormat.FORMAT_UNORDERED_LIST -> return Arrays.asList(createUnorderedListSpan(nestingLevel, alignmentRendering, attrs, listStyle), createListItemSpan(nestingLevel + 1, alignmentRendering))
            AztecTextFormat.FORMAT_QUOTE -> return Arrays.asList(createAztecQuoteSpan(nestingLevel, attrs, alignmentRendering, quoteStyle))
            AztecTextFormat.FORMAT_HEADING_1,
            AztecTextFormat.FORMAT_HEADING_2,
            AztecTextFormat.FORMAT_HEADING_3,
            AztecTextFormat.FORMAT_HEADING_4,
            AztecTextFormat.FORMAT_HEADING_5,
            AztecTextFormat.FORMAT_HEADING_6 -> return Arrays.asList(createHeadingSpan(nestingLevel, textFormat, attrs, alignmentRendering, headerStyle))
            AztecTextFormat.FORMAT_PREFORMAT -> return Arrays.asList(createPreformatSpan(nestingLevel, alignmentRendering, attrs, preformatStyle))
            else -> return Arrays.asList(createParagraphSpan(nestingLevel, alignmentRendering, attrs))
        }
    }

    fun getAlignment(textFormat: ITextFormat?, text: CharSequence) : Layout.Alignment? {
        val direction = TextDirectionHeuristicsCompat.FIRSTSTRONG_LTR
        val isRtl = direction.isRtl(text, 0, text.length)

        return when (textFormat) {
            AztecTextFormat.FORMAT_ALIGN_LEFT -> if (!isRtl) Layout.Alignment.ALIGN_NORMAL else Layout.Alignment.ALIGN_OPPOSITE
            AztecTextFormat.FORMAT_ALIGN_CENTER -> Layout.Alignment.ALIGN_CENTER
            AztecTextFormat.FORMAT_ALIGN_RIGHT -> if (isRtl) Layout.Alignment.ALIGN_NORMAL else Layout.Alignment.ALIGN_OPPOSITE
            else -> null
        }
    }

    fun makeBlockSpan(textFormat: ITextFormat, nestingLevel: Int, attrs: AztecAttributes = AztecAttributes()): IAztecBlockSpan {
        return when (textFormat) {
            AztecTextFormat.FORMAT_ORDERED_LIST -> makeBlockSpan(AztecOrderedListSpan::class, textFormat, nestingLevel, attrs)
            AztecTextFormat.FORMAT_UNORDERED_LIST -> makeBlockSpan(AztecUnorderedListSpan::class, textFormat, nestingLevel, attrs)
            AztecTextFormat.FORMAT_QUOTE -> makeBlockSpan(AztecQuoteSpan::class, textFormat, nestingLevel, attrs)
            AztecTextFormat.FORMAT_HEADING_1,
            AztecTextFormat.FORMAT_HEADING_2,
            AztecTextFormat.FORMAT_HEADING_3,
            AztecTextFormat.FORMAT_HEADING_4,
            AztecTextFormat.FORMAT_HEADING_5,
            AztecTextFormat.FORMAT_HEADING_6 -> makeBlockSpan(AztecHeadingSpan::class, textFormat, nestingLevel, attrs)
            AztecTextFormat.FORMAT_PREFORMAT -> makeBlockSpan(AztecPreformatSpan::class, textFormat, nestingLevel, attrs)
            else -> createParagraphSpan(nestingLevel, alignmentRendering, attrs)
        }
    }

    private fun <T : KClass<out IAztecBlockSpan>> makeBlockSpan(type: T, textFormat: ITextFormat, nestingLevel: Int, attrs: AztecAttributes = AztecAttributes()): IAztecBlockSpan {
        val typeIsAssignableTo = { clazz: KClass<out Any> -> clazz.java.isAssignableFrom(type.java) }
        return when {
            typeIsAssignableTo(AztecOrderedListSpan::class) -> createOrderedListSpan(nestingLevel, alignmentRendering, attrs, listStyle)
            typeIsAssignableTo(AztecUnorderedListSpan::class) -> createUnorderedListSpan(nestingLevel, alignmentRendering, attrs, listStyle)
            typeIsAssignableTo(AztecListItemSpan::class) -> createListItemSpan(nestingLevel, alignmentRendering, attrs)
            typeIsAssignableTo(AztecQuoteSpan::class) -> createAztecQuoteSpan(nestingLevel, attrs, alignmentRendering, quoteStyle)
            typeIsAssignableTo(AztecHeadingSpan::class) -> createHeadingSpan(nestingLevel, textFormat, attrs, alignmentRendering, headerStyle)
            typeIsAssignableTo(AztecPreformatSpan::class) -> createPreformatSpan(nestingLevel, alignmentRendering, attrs, preformatStyle)
            else -> createParagraphSpan(nestingLevel, alignmentRendering, attrs)
        }
    }

    fun setBlockStyle(blockElement: IAztecBlockSpan) {
        when (blockElement) {
            is AztecOrderedListSpan -> blockElement.listStyle = listStyle
            is AztecUnorderedListSpan -> blockElement.listStyle = listStyle
            is AztecQuoteSpan -> blockElement.quoteStyle = quoteStyle
            is AztecPreformatSpan -> blockElement.preformatStyle = preformatStyle
            is AztecHeadingSpan -> blockElement.headerStyle = headerStyle
        }
    }

    fun getTopBlockDelimiters(start: Int, end: Int): List<Int> {
        val delimiters = arrayListOf(start, end)

        val bounds = hashMapOf<Int, Int>()
        val startNesting = IAztecNestable.getMinNestingLevelAt(editableText, start)
        bounds[start] = startNesting

        val endNesting = IAztecNestable.getMinNestingLevelAt(editableText, end)
        bounds[end] = endNesting

        val blockSpans = editableText.getSpans(start, end, IAztecBlockSpan::class.java)
                .filter { editableText.getSpanStart(it) >= start && editableText.getSpanEnd(it) <= end }
                .sortedBy { editableText.getSpanStart(it) }

        blockSpans.forEach {
            var spanIndex = editableText.getSpanStart(it)
            var nesting = IAztecNestable.getMinNestingLevelAt(editableText, spanIndex)
            bounds[spanIndex] = nesting

            spanIndex = editableText.getSpanEnd(it)
            nesting = IAztecNestable.getMinNestingLevelAt(editableText, spanIndex)
            bounds[spanIndex] = nesting

            if (it is IAztecCompositeBlockSpan) {
                val wrapper = SpanWrapper(editableText, it)
                val parent = IAztecNestable.getParent(editableText, wrapper)
                parent?.let {
                    if (parent.start < start || parent.end > end) {
                        delimiters.add(wrapper.start)
                        delimiters.add(wrapper.end)
                    }
                }
            }
        }

        if (bounds.isNotEmpty()) {
            var lastIndex: Int = bounds.keys.first()

            bounds.keys.forEach { key ->
                val last = checkBound(bounds, key, delimiters, lastIndex)
                if (last > -1) {
                    lastIndex = last
                }
            }

            lastIndex = bounds.keys.last()
            bounds.keys.reversed().forEach { key ->
                val last = checkBound(bounds, key, delimiters, lastIndex)
                if (last > -1) {
                    lastIndex = last
                }
            }
        }

        return delimiters.distinct().sorted()
    }

    private fun checkBound(bounds: HashMap<Int, Int>, key: Int, delimiters: ArrayList<Int>, lastIndex: Int) : Int {
        if (bounds[key]!! != bounds[lastIndex]!!) {
            if (bounds[key]!! < bounds[lastIndex]!!) {
                delimiters.add(key)
                return key
            }
        }
        return -1
    }

    /**
     * Returns paragraph bounds (\n) to the left and to the right of selection.
     */
    fun getBoundsOfText(editable: Editable, selectionStart: Int, selectionEnd: Int): IntRange {
        val startOfBlock: Int
        val endOfBlock: Int

        val selectionStartIsOnTheNewLine = selectionStart != selectionEnd && selectionStart > 0
                && selectionStart < editableText.length
                && editable[selectionStart] == '\n'

        val selectionStartIsBetweenNewlines = selectionStartIsOnTheNewLine
                && selectionStart > 0
                && selectionStart < editableText.length
                && editable[selectionStart - 1] == '\n'

        val isTrailingNewlineAtTheEndOfSelection = selectionStart != selectionEnd
                && selectionEnd > 0
                && editableText.length > selectionEnd
                && editableText[selectionEnd] != Constants.END_OF_BUFFER_MARKER
                && editableText[selectionEnd] != '\n'
                && editableText[selectionEnd - 1] == '\n'

        val indexOfFirstLineBreak: Int
        var indexOfLastLineBreak = editable.indexOf("\n", selectionEnd)

        if (selectionStartIsBetweenNewlines) {
            indexOfFirstLineBreak = selectionStart
        } else if (selectionStartIsOnTheNewLine) {
            val isSingleCharacterLine = (selectionStart > 1 && editableText[selectionStart - 1] != '\n' && editableText[selectionStart - 2] == '\n') || selectionStart == 1

            if (isSingleCharacterLine) {
                indexOfFirstLineBreak = selectionStart - 1
            } else {
                indexOfFirstLineBreak = editable.lastIndexOf("\n", selectionStart - 1) + 1
            }
            if (isTrailingNewlineAtTheEndOfSelection) {
                indexOfLastLineBreak = editable.indexOf("\n", selectionEnd - 1)
            }
        } else if (isTrailingNewlineAtTheEndOfSelection) {
            indexOfFirstLineBreak = editable.lastIndexOf("\n", selectionStart - 1) + 1
            indexOfLastLineBreak = editable.indexOf("\n", selectionEnd - 1)
        } else if (indexOfLastLineBreak > 0) {
            indexOfFirstLineBreak = editable.lastIndexOf("\n", selectionStart - 1) + 1
        } else if (indexOfLastLineBreak == -1) {
            indexOfFirstLineBreak = if (selectionStart == 0) 0 else {
                editable.lastIndexOf("\n", selectionStart) + 1
            }
        } else {
            indexOfFirstLineBreak = editable.lastIndexOf("\n", selectionStart)
        }

        startOfBlock = if (indexOfFirstLineBreak != -1) indexOfFirstLineBreak else 0
        endOfBlock = if (indexOfLastLineBreak != -1) (indexOfLastLineBreak + 1) else editable.length

        return IntRange(startOfBlock, endOfBlock)
    }

    fun applyTextAlignment(textFormat: ITextFormat, start: Int = selectionStart, end: Int = selectionEnd) {
        if (editableText.isEmpty()) {
            editableText.append("" + Constants.END_OF_BUFFER_MARKER)
        }

        val boundsOfSelectedText = getBoundsOfText(editableText, start, end)
        var spans = getAlignedSpans(null, boundsOfSelectedText.start, boundsOfSelectedText.endInclusive)

        if (start == end) {
            if (start == boundsOfSelectedText.start && spans.size > 1) {
                spans = spans.filter { editableText.getSpanEnd(it) != start }
            } else if (start == boundsOfSelectedText.endInclusive && spans.size > 1) {
                spans = spans.filter { editableText.getSpanStart(it) != start }
            }
        }

        if (spans.isNotEmpty()) {
            spans.filter { it !is AztecListSpan }.forEach { changeAlignment(it, textFormat) }
        } else {
            val nestingLevel = IAztecNestable.getNestingLevelAt(editableText, boundsOfSelectedText.start)

            val alignment = getAlignment(textFormat,
                    editableText.subSequence(boundsOfSelectedText.start until boundsOfSelectedText.endInclusive))
            editableText.setSpan(createParagraphSpan(nestingLevel, alignment),
                    boundsOfSelectedText.start, boundsOfSelectedText.endInclusive, Spanned.SPAN_PARAGRAPH)
        }
    }

    private fun changeAlignment(it: IAztecAlignmentSpan, blockElementType: ITextFormat?) {
        val wrapper = SpanWrapper(editableText, it)
        it.align = getAlignment(blockElementType, editableText.substring(wrapper.start until wrapper.end))

        editableText.setSpan(it, wrapper.start, wrapper.end, wrapper.flags)
    }

    fun applyBlockStyle(blockElementType: ITextFormat, start: Int = selectionStart, end: Int = selectionEnd) {
        if (editableText.isEmpty()) {
            editableText.append("" + Constants.END_OF_BUFFER_MARKER)
        }

        val boundsOfSelectedText = getBoundsOfText(editableText, start, end)
        val nestingLevel = IAztecNestable.getNestingLevelAt(editableText, start) + 1
        val spanToApply = makeBlockSpan(blockElementType, nestingLevel)

        if (start != end) {
            // we want to push line blocks as deep as possible, because they can't contain other block elements (e.g. headings)
            if (spanToApply is IAztecLineBlockSpan) {
                applyLineBlock(blockElementType, boundsOfSelectedText.start, boundsOfSelectedText.endInclusive)
            } else {
                val delimiters = getTopBlockDelimiters(boundsOfSelectedText.start, boundsOfSelectedText.endInclusive)
                for (i in 0 until delimiters.size - 1) {
                    pushNewBlock(delimiters[i], delimiters[i + 1], blockElementType)
                }
            }

            editor.setSelection(editor.selectionStart)
        } else {
            val startOfLine = boundsOfSelectedText.start
            val endOfLine = boundsOfSelectedText.endInclusive

            // we can't add blocks around partial block elements (i.e. list items), everything must go inside
            val isWithinPartialBlock = editableText.getSpans(boundsOfSelectedText.start,
                    boundsOfSelectedText.endInclusive, IAztecCompositeBlockSpan::class.java)
                    .any { it.nestingLevel == nestingLevel - 1 }

            val startOfBlock = mergeWithBlockAbove(startOfLine, endOfLine, spanToApply, nestingLevel, isWithinPartialBlock, blockElementType)
            val endOfBlock = mergeWithBlockBelow(endOfLine, startOfBlock, spanToApply, nestingLevel, isWithinPartialBlock, blockElementType)

            if (spanToApply is IAztecLineBlockSpan) {
                applyBlock(spanToApply, startOfBlock, endOfBlock)
            } else {
                pushNewBlock(startOfBlock, endOfBlock, blockElementType)
            }
        }

        editor.setSelection(editor.selectionStart, editor.selectionEnd)
    }

    private fun pushNewBlock(start: Int, end: Int, blockElementType: ITextFormat) {
        var nesting = IAztecNestable.getMinNestingLevelAt(editableText, start, end) + 1

        // we can't add blocks around composite block elements (i.e. list items), everything must go inside
        val isListItem = editableText.getSpans(start, end, IAztecCompositeBlockSpan::class.java)
                .any { it.nestingLevel == nesting }

        if (isListItem) {
            nesting++
        }

        val newBlock = makeBlockSpan(blockElementType, nesting)
        val pushBy = if (newBlock is AztecListSpan) 2 else 1

        val spans = IAztecNestable.pushDeeper(editableText, start, end, nesting, pushBy)
        spans.forEach {
            it.remove()
        }

        applyBlock(newBlock, start, end)

        spans.forEach {
            it.reapply()
        }
    }

    private fun mergeWithBlockAbove(startOfLine: Int, endOfLine: Int, spanToApply: IAztecBlockSpan, nestingLevel: Int, isWithinList: Boolean, blockElementType: ITextFormat): Int {
        var startOfBlock = startOfLine
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
            } else if (!isWithinList) {
                // expand the start
                startOfBlock = editableText.getSpanStart(spansOnPreviousLine)
                liftBlock(blockElementType, startOfBlock, endOfLine)
            }
        }
        return startOfBlock
    }

    private fun mergeWithBlockBelow(endOfLine: Int, startOfBlock: Int, spanToApply: IAztecBlockSpan, nestingLevel: Int, isWithinList: Boolean, blockElementType: ITextFormat): Int {
        var endOfBlock = endOfLine
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
            } else if (!isWithinList) {
                // expand the end
                endOfBlock = editableText.getSpanEnd(spanOnNextLine)
                liftBlock(blockElementType, startOfBlock, endOfBlock)
            }
        }
        return endOfBlock
    }

    private fun applyBlock(blockSpan: IAztecBlockSpan, start: Int, end: Int) {
        when (blockSpan) {
            is AztecOrderedListSpan -> applyListBlock(blockSpan, start, end)
            is AztecUnorderedListSpan -> applyListBlock(blockSpan, start, end)
            is AztecQuoteSpan -> applyQuote(blockSpan, start, end)
            is AztecHeadingSpan -> applyHeadingBlock(blockSpan, start, end)
            is AztecPreformatSpan -> BlockHandler.set(editableText, blockSpan, start, end)
            else -> editableText.setSpan(blockSpan, start, end, Spanned.SPAN_PARAGRAPH)
        }
    }

    private fun applyQuote(blockSpan: AztecQuoteSpan, start: Int, end: Int) {
        BlockHandler.set(editableText, blockSpan, start, end)
    }

    private fun applyListBlock(listSpan: AztecListSpan, start: Int, end: Int) {
        BlockHandler.set(editableText, listSpan, start, end)
        // special case for styling single empty lines
        if (end - start == 1 && (editableText[end - 1] == '\n' || editableText[end - 1] == Constants.END_OF_BUFFER_MARKER)) {
            ListItemHandler.newListItem(editableText, start, end, listSpan.nestingLevel + 1, alignmentRendering)
        } else {
            val listEnd = if (end == editableText.length) end else end - 1
            val listContent = editableText.substring(start, listEnd)
            val lines = TextUtils.split(listContent, "\n")

            for (i in lines.indices) {
                val lineLength = lines[i].length

                val lineStart = (0..i - 1).sumBy { lines[it].length + 1 }

                val lineEnd = (lineStart + lineLength).let {
                    if ((start + it) != editableText.length) it + 1 else it // include the newline or not
                }

                ListItemHandler.newListItem(
                        editableText,
                        start + lineStart,
                        start + lineEnd,
                        listSpan.nestingLevel + 1,
                        alignmentRendering)
            }
        }
    }

    private fun applyLineBlock(format: ITextFormat, start: Int, end: Int) {
        val lines = TextUtils.split(editableText.substring(start, end), "\n")
        for (i in lines.indices) {
            val splitLength = lines[i].length

            val lineStart = start + (0 until i).sumBy { lines[it].length + 1 }
            val lineEnd = Math.min(lineStart + splitLength + 1, end) // +1 to include the newline

            val lineLength = lineEnd - lineStart
            if (lineLength == 0) continue

            val nesting = IAztecNestable.getNestingLevelAt(editableText, lineStart) + 1
            val block = makeBlockSpan(format, nesting)
            applyBlock(block, lineStart, lineEnd)
        }
    }

    private fun applyHeadingBlock(headingSpan: AztecHeadingSpan, start: Int, end: Int) {
        val lines = TextUtils.split(editableText.substring(start, end), "\n")
        for (i in lines.indices) {
            val splitLength = lines[i].length

            val lineStart = start + (0 until i).sumBy { lines[it].length + 1 }
            val lineEnd = Math.min(lineStart + splitLength + 1, end) // +1 to include the newline

            val lineLength = lineEnd - lineStart
            if (lineLength == 0) continue

            HeadingHandler.cloneHeading(editableText, headingSpan, alignmentRendering, lineStart, lineEnd)
        }
    }

    private fun liftBlock(textFormat: ITextFormat, start: Int, end: Int) {
        when (textFormat) {
            AztecTextFormat.FORMAT_ORDERED_LIST -> liftListBlock(AztecOrderedListSpan::class.java, start, end)
            AztecTextFormat.FORMAT_UNORDERED_LIST -> liftListBlock(AztecUnorderedListSpan::class.java, start, end)
            AztecTextFormat.FORMAT_QUOTE -> editableText.getSpans(start, end, AztecQuoteSpan::class.java).forEach {
                IAztecNestable.pullUp(editableText, start, end, it.nestingLevel)
                editableText.removeSpan(it)
            }
            else -> editableText.getSpans(start, end, ParagraphSpan::class.java).forEach {
                IAztecNestable.pullUp(editableText, start, end, it.nestingLevel)
                editableText.removeSpan(it)
            }
        }
    }

    private fun liftListBlock(listSpan: Class<out AztecListSpan>, start: Int, end: Int) {
        editableText.getSpans(start, end, listSpan).forEach {
            val wrapper = SpanWrapper(editableText, it)
            editableText.getSpans(wrapper.start, wrapper.end, AztecListItemSpan::class.java).forEach { editableText.removeSpan(it) }

            IAztecNestable.pullUp(editableText, start, end, wrapper.span.nestingLevel)
            wrapper.remove()
        }
    }

    fun containsList(textFormat: ITextFormat, nestingLevel: Int, selStart: Int = selectionStart, selEnd: Int = selectionEnd): Boolean {
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

        return list.any { containsBlockElement(textFormat, it, editableText, nestingLevel) }
    }

    fun containsBlockElement(textFormat: ITextFormat, index: Int, text: Editable, nestingLevel: Int): Boolean {
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

    fun containsQuote(selStart: Int = selectionStart, selEnd: Int = selectionEnd): Boolean {
        if (selStart < 0 || selEnd < 0) return false

        return editableText.getSpans(selStart, selEnd, AztecQuoteSpan::class.java)
                .any {
                    val spanStart = editableText.getSpanStart(it)
                    val spanEnd = editableText.getSpanEnd(it)

                    if (selStart == selEnd) {
                        if (editableText.length == selStart) {
                            selStart in spanStart..spanEnd
                        } else {
                            (spanEnd != selStart) && selStart in spanStart..spanEnd
                        }
                    } else {
                        (selStart in spanStart..spanEnd || selEnd in spanStart..spanEnd) ||
                                (spanStart in selStart..selEnd || spanEnd in spanStart..spanEnd)
                    }
                }
    }

    fun containsHeading(textFormat: ITextFormat, selStart: Int = selectionStart, selEnd: Int = selectionEnd): Boolean {
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

    private fun containHeadingType(textFormat: ITextFormat, index: Int): Boolean {
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
                AztecTextFormat.FORMAT_HEADING_1 ->
                    return span.heading == AztecHeadingSpan.Heading.H1
                AztecTextFormat.FORMAT_HEADING_2 ->
                    return span.heading == AztecHeadingSpan.Heading.H2
                AztecTextFormat.FORMAT_HEADING_3 ->
                    return span.heading == AztecHeadingSpan.Heading.H3
                AztecTextFormat.FORMAT_HEADING_4 ->
                    return span.heading == AztecHeadingSpan.Heading.H4
                AztecTextFormat.FORMAT_HEADING_5 ->
                    return span.heading == AztecHeadingSpan.Heading.H5
                AztecTextFormat.FORMAT_HEADING_6 ->
                    return span.heading == AztecHeadingSpan.Heading.H6
                else -> return false
            }
        }

        return false
    }

    fun containsOtherHeadings(textFormat: ITextFormat, selStart: Int = selectionStart, selEnd: Int = selectionEnd): Boolean {
        arrayOf(AztecTextFormat.FORMAT_HEADING_1,
                AztecTextFormat.FORMAT_HEADING_2,
                AztecTextFormat.FORMAT_HEADING_3,
                AztecTextFormat.FORMAT_HEADING_4,
                AztecTextFormat.FORMAT_HEADING_5,
                AztecTextFormat.FORMAT_HEADING_6,
                AztecTextFormat.FORMAT_PREFORMAT)
                .filter { it != textFormat }
                .forEach {
                    if (containsHeading(it, selStart, selEnd)) {
                        return true
                    }
                }

        return false
    }

    fun containsHeadingOnly(textFormat: ITextFormat, selStart: Int = selectionStart, selEnd: Int = selectionEnd): Boolean {
        val otherHeadings = arrayOf(
                AztecTextFormat.FORMAT_HEADING_1,
                AztecTextFormat.FORMAT_HEADING_2,
                AztecTextFormat.FORMAT_HEADING_3,
                AztecTextFormat.FORMAT_HEADING_4,
                AztecTextFormat.FORMAT_HEADING_5,
                AztecTextFormat.FORMAT_HEADING_6,
                AztecTextFormat.FORMAT_PREFORMAT)
                .filter { it != textFormat }

        return containsHeading(textFormat, selStart, selEnd) && otherHeadings.none { containsHeading(it, selStart, selEnd) }
    }

    fun containsAlignment(textFormat: ITextFormat, selStart: Int = selectionStart, selEnd: Int = selectionEnd): Boolean {
        return getAlignedSpans(textFormat, selStart, selEnd).isNotEmpty()
    }

    private fun getAlignedSpans(textFormat: ITextFormat?, selStart: Int = selectionStart, selEnd: Int = selectionEnd): List<IAztecAlignmentSpan> {
        if (selStart < 0 || selEnd < 0) return emptyList()

        return editableText.getSpans(selStart, selEnd, IAztecAlignmentSpan::class.java)
                .filter {
                    textFormat == null || it.align == getAlignment(textFormat,
                        editableText.substring(editableText.getSpanStart(it) until editableText.getSpanEnd(it)))
                }
                .filter {
                    val spanStart = editableText.getSpanStart(it)
                    val spanEnd = editableText.getSpanEnd(it)

                    if (selStart == selEnd) {
                        if (editableText.length == selStart) {
                            selStart in spanStart..spanEnd
                        } else {
                            (spanEnd != selStart) && selStart in spanStart..spanEnd
                        }
                    } else {
                        (selStart in spanStart..spanEnd || selEnd in spanStart..spanEnd) ||
                                (spanStart in selStart..selEnd || spanEnd in selStart..selEnd)
                    }
                }
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

        return list.any { containsPreformat(it) }
    }

    fun containsPreformat(index: Int): Boolean {
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

    fun switchListType(listTypeToSwitchTo: ITextFormat, start: Int = selectionStart, end: Int = selectionEnd) {
        var spans = editableText.getSpans(start, end, AztecListSpan::class.java)
        if (start == end && spans.size > 1) {
            spans = spans.filter { editableText.getSpanStart(it) == start }.toTypedArray()
        }

        spans.forEach { existingListSpan ->
            if (existingListSpan != null) {
                val spanStart = editableText.getSpanStart(existingListSpan)
                val spanEnd = editableText.getSpanEnd(existingListSpan)
                val spanFlags = editableText.getSpanFlags(existingListSpan)
                editableText.removeSpan(existingListSpan)

                editableText.setSpan(makeBlockSpan(listTypeToSwitchTo, existingListSpan.nestingLevel), spanStart, spanEnd, spanFlags)
                editor.onSelectionChanged(start, end)
            }
        }
    }

    fun switchHeaderType(headerTypeToSwitchTo: ITextFormat, start: Int = selectionStart, end: Int = selectionEnd) {
        var spans = editableText.getSpans(start, end, AztecHeadingSpan::class.java)
        if (start == end && spans.size > 1) {
            spans = spans.filter { editableText.getSpanStart(it) == start }.toTypedArray()
        }

        spans.forEach { existingHeaderSpan ->
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

    fun switchHeadingToPreformat(start: Int = selectionStart, end: Int = selectionEnd) {
        var spans = editableText.getSpans(start, end, AztecHeadingSpan::class.java)
        if (start == end && spans.size > 1) {
            spans = spans.filter { editableText.getSpanStart(it) == start }.toTypedArray()
        }

        spans.forEach { heading ->
            if (heading != null) {
                val spanStart = editableText.getSpanStart(heading)
                val spanEnd = editableText.getSpanEnd(heading)
                val spanFlags = editableText.getSpanFlags(heading)
                val spanType = makeBlock(heading.textFormat, 0).map { it -> it.javaClass }

                removeBlockStyle(heading.textFormat, spanStart, spanEnd, spanType)
                editableText.setSpan(AztecPreformatSpan(heading.nestingLevel, heading.attributes, preformatStyle), spanStart, spanEnd, spanFlags)
                editor.onSelectionChanged(start, end)
            }
        }
    }

    fun switchPreformatToHeading(headingTextFormat: ITextFormat, start: Int = selectionStart, end: Int = selectionEnd) {
        var spans = editableText.getSpans(start, end, AztecPreformatSpan::class.java)
        if (start == end && spans.size > 1) {
            spans = spans.filter { editableText.getSpanStart(it) == start }.toTypedArray()
        }

        spans.forEach { preformat ->
            if (preformat != null) {
                val spanStart = editableText.getSpanStart(preformat)
                val spanEnd = editableText.getSpanEnd(preformat)
                val spanFlags = editableText.getSpanFlags(preformat)
                val spanType = makeBlock(AztecTextFormat.FORMAT_PREFORMAT, 0).map { it -> it.javaClass }

                removeBlockStyle(AztecTextFormat.FORMAT_PREFORMAT, spanStart, spanEnd, spanType)
                val headingSpan = createHeadingSpan(
                        preformat.nestingLevel,
                        headingTextFormat,
                        preformat.attributes,
                        alignmentRendering)
                editableText.setSpan(headingSpan, spanStart, spanEnd, spanFlags)
                editor.onSelectionChanged(start, end)
            }
        }
    }
}
