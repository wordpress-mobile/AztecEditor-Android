package org.wordpress.aztec.formatting

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.StyleSpan
import org.wordpress.aztec.AztecPart
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.TextChangedEvent
import org.wordpress.aztec.TextFormat
import org.wordpress.aztec.spans.*
import java.util.*


class InlineFormatter(editor: AztecText, codeStyle: CodeStyle) : AztecFormatter(editor) {

    data class CarryOverSpan(val span: AztecInlineSpan, val start: Int, val end: Int)
    data class CodeStyle(val codeBackground: Int, val codeColor: Int)

    val carryOverSpans = ArrayList<CarryOverSpan>()
    val codeStyle: CodeStyle

    init {
        this.codeStyle = codeStyle
    }

    fun toggleBold() {
        if (!containsInlineStyle(TextFormat.FORMAT_BOLD)) {
            applyInlineStyle(TextFormat.FORMAT_BOLD)
        } else {
            removeInlineStyle(TextFormat.FORMAT_BOLD)
        }
    }

    fun toggleItalic() {
        if (!containsInlineStyle(TextFormat.FORMAT_ITALIC)) {
            applyInlineStyle(TextFormat.FORMAT_ITALIC)
        } else {
            removeInlineStyle(TextFormat.FORMAT_ITALIC)
        }
    }

    fun toggleUnderline() {
        if (!containsInlineStyle(TextFormat.FORMAT_UNDERLINED)) {
            applyInlineStyle(TextFormat.FORMAT_UNDERLINED)
        } else {
            removeInlineStyle(TextFormat.FORMAT_UNDERLINED)
        }
    }

    fun toggleStrikethrough() {
        if (!containsInlineStyle(TextFormat.FORMAT_STRIKETHROUGH)) {
            applyInlineStyle(TextFormat.FORMAT_STRIKETHROUGH)
        } else {
            removeInlineStyle(TextFormat.FORMAT_STRIKETHROUGH)
        }
    }

    fun toggleCode(){
        if (!containsInlineStyle(TextFormat.FORMAT_CODE)) {
            applyInlineStyle(TextFormat.FORMAT_CODE)
        } else {
            removeInlineStyle(TextFormat.FORMAT_CODE)
        }
    }

    fun carryOverInlineSpans(start: Int, count: Int, after: Int) {
        carryOverSpans.clear()

        val charsAdded = after - count
        if (charsAdded > 0 && count > 0) {
            editableText.getSpans(start, start + count, AztecInlineSpan::class.java).forEach {
                val spanStart = editableText.getSpanStart(it)
                val spanEnd = editableText.getSpanEnd(it)


                if ((spanStart == start || spanEnd == count + start) && spanEnd < after) {
                    editableText.removeSpan(it)
                    carryOverSpans.add(CarryOverSpan(it, spanStart, spanEnd))
                }
            }
        }
    }

    fun reapplyCarriedOverInlineSpans() {
        carryOverSpans.forEach {
            editableText.setSpan(it.span, it.start, it.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        carryOverSpans.clear()
    }


    fun handleInlineStyling(textChangedEvent: TextChangedEvent) {
        //because we use SPAN_INCLUSIVE_INCLUSIVE for inline styles
        //we need to make sure unselected styles are not applied
        clearInlineStyles(textChangedEvent.inputStart, textChangedEvent.inputEnd, textChangedEvent.isNewLine())

        //trailing styling
        if (!editor.formattingHasChanged() || textChangedEvent.isNewLine()) return

        if (editor.formattingIsApplied()) {
            for (item in editor.selectedStyles) {
                when (item) {
                    TextFormat.FORMAT_HEADING_1,
                    TextFormat.FORMAT_HEADING_2,
                    TextFormat.FORMAT_HEADING_3,
                    TextFormat.FORMAT_HEADING_4,
                    TextFormat.FORMAT_HEADING_5,
                    TextFormat.FORMAT_HEADING_6 -> if (editor.contains(item, textChangedEvent.inputStart, textChangedEvent.inputEnd)) {
                        applyInlineStyle(item, textChangedEvent.inputStart, textChangedEvent.inputEnd)
                    }
                    TextFormat.FORMAT_BOLD,
                    TextFormat.FORMAT_ITALIC,
                    TextFormat.FORMAT_STRIKETHROUGH,
                    TextFormat.FORMAT_CODE -> if (!editor.contains(item, textChangedEvent.inputStart, textChangedEvent.inputEnd)) {
                        applyInlineStyle(item, textChangedEvent.inputStart, textChangedEvent.inputEnd)
                    }
                    else -> {
                        //do nothing
                    }
                }
            }
        }

        editor.setFormattingChangesApplied()
    }


    private fun clearInlineStyles(start: Int, end: Int, ignoreSelectedStyles: Boolean) {
        val newStart = if (start > end) end else start

        editor.getAppliedStyles(start, end).forEach {
            if (!editor.selectedStyles.contains(it) || ignoreSelectedStyles || (start == 0 && end == 0) ||
                    (start > end && editableText.length > end && editableText[end] == '\n')) {
                when (it) {
                    TextFormat.FORMAT_HEADING_1,
                    TextFormat.FORMAT_HEADING_2,
                    TextFormat.FORMAT_HEADING_3,
                    TextFormat.FORMAT_HEADING_4,
                    TextFormat.FORMAT_HEADING_5,
                    TextFormat.FORMAT_HEADING_6,
                    TextFormat.FORMAT_BOLD,
                    TextFormat.FORMAT_ITALIC,
                    TextFormat.FORMAT_STRIKETHROUGH,
                    TextFormat.FORMAT_CODE -> removeInlineStyle(it, newStart, end)
                    else -> {
                        //do nothing
                    }
                }
            }
        }
    }


    fun applyInlineStyle(textFormat: TextFormat, start: Int = selectionStart, end: Int = selectionEnd) {
        val spanToApply = makeInlineSpan(textFormat)

        if (start >= end) {
            return
        }

        var precedingSpan: AztecInlineSpan? = null
        var followingSpan: AztecInlineSpan? = null

        if (start >= 1) {
            val previousSpans = editableText.getSpans(start - 1, start, AztecInlineSpan::class.java)
            previousSpans.forEach {
                if (isSameInlineSpanType(it, spanToApply)) {
                    precedingSpan = it
                    return@forEach
                }
            }

            if (precedingSpan != null) {
                val spanStart = editableText.getSpanStart(precedingSpan)
                val spanEnd = editableText.getSpanEnd(precedingSpan)

                if (spanEnd > start) {
                    return@applyInlineStyle  //we are adding text inside span - no need to do anything special
                } else {
                    editableText.setSpan(precedingSpan, spanStart, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
                }

            }
        }

        if (editor.length() > end) {
            val nextSpans = editableText.getSpans(end, end + 1, AztecInlineSpan::class.java)
            nextSpans.forEach {
                if (isSameInlineSpanType(it, spanToApply)) {
                    followingSpan = it
                    return@forEach
                }
            }

            if (followingSpan != null) {
                val spanEnd = editableText.getSpanEnd(followingSpan)
                editableText.setSpan(followingSpan, start, spanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
            }
        }

        if (precedingSpan == null && followingSpan == null) {
            var existingSpanOfSameStyle: AztecInlineSpan? = null

            val spans = editableText.getSpans(start, end, AztecInlineSpan::class.java)
            spans.forEach {
                if (isSameInlineSpanType(it, spanToApply)) {
                    existingSpanOfSameStyle = it
                    return@forEach
                }
            }

            //if we already have same span within selection - reuse it by changing it's bounds
            if (existingSpanOfSameStyle != null) {
                editableText.removeSpan(existingSpanOfSameStyle)
                editableText.setSpan(existingSpanOfSameStyle, start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
            } else {
                editableText.setSpan(spanToApply, start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
            }

        }

        joinStyleSpans(start, end)
    }

    fun removeInlineStyle(textFormat: TextFormat, start: Int = selectionStart, end: Int = selectionEnd) {
        //for convenience sake we are initializing the span of same type we are planing to remove
        val spanToRemove = makeInlineSpan(textFormat)

        val spans = editableText.getSpans(start, end, AztecInlineSpan::class.java)
        val list = ArrayList<AztecPart>()

        spans.forEach {
            if (isSameInlineSpanType(it, spanToRemove)) {
                list.add(AztecPart(editableText.getSpanStart(it), editableText.getSpanEnd(it)))
                editableText.removeSpan(it)
            }
        }

        list.forEach {
            if (it.isValid) {
                if (it.start < start) {
                    applyInlineStyle(textFormat, it.start, start)
                }
                if (it.end > end) {
                    applyInlineStyle(textFormat, end, it.end)
                }
            }
        }

        joinStyleSpans(start, end)
    }


    fun isSameInlineSpanType(firstSpan: AztecInlineSpan, secondSpan: AztecInlineSpan): Boolean {
        if (firstSpan.javaClass.equals(secondSpan.javaClass)) {
            //special check for StyleSpan
            if (firstSpan is StyleSpan && secondSpan is StyleSpan) {
                return firstSpan.style == secondSpan.style
            } else if (firstSpan is AztecHeadingSpan && secondSpan is AztecHeadingSpan) {
                return firstSpan.heading == secondSpan.heading
            } else {
                return true
            }

        }

        return false
    }

    //TODO: Check if there is more efficient way to tidy spans
    public fun joinStyleSpans(start: Int, end: Int) {
        //joins spans on the left
        if (start > 1) {
            val spansInSelection = editableText.getSpans(start, end, AztecInlineSpan::class.java)

            val spansBeforeSelection = editableText.getSpans(start - 1, start, AztecInlineSpan::class.java)
            spansInSelection.forEach { innerSpan ->
                val inSelectionSpanEnd = editableText.getSpanEnd(innerSpan)
                if (inSelectionSpanEnd == -1) return@forEach
                spansBeforeSelection.forEach { outerSpan ->
                    val outerSpanStart = editableText.getSpanStart(outerSpan)

                    if (isSameInlineSpanType(innerSpan, outerSpan)) {
                        editableText.removeSpan(outerSpan)
                        editableText.setSpan(innerSpan, outerSpanStart, inSelectionSpanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
                    }

                }
            }
        }

        //joins spans on the right
        if (editor.length() > end) {
            val spansInSelection = editableText.getSpans(start, end, AztecInlineSpan::class.java)
            val spansAfterSelection = editableText.getSpans(end, end + 1, AztecInlineSpan::class.java)
            spansInSelection.forEach { innerSpan ->
                val inSelectionSpanStart = editableText.getSpanStart(innerSpan)
                if (inSelectionSpanStart == -1) return@forEach
                spansAfterSelection.forEach { outerSpan ->
                    val outerSpanEnd = editableText.getSpanEnd(outerSpan)

                    if (isSameInlineSpanType(innerSpan, outerSpan)) {
                        editableText.removeSpan(outerSpan)
                        editableText.setSpan(innerSpan, inSelectionSpanStart, outerSpanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
                    }
                }
            }
        }


        //joins spans withing selected text
        val spansInSelection = editableText.getSpans(start, end, AztecInlineSpan::class.java)
        val spansToUse = editableText.getSpans(start, end, AztecInlineSpan::class.java)

        spansInSelection.forEach { appliedSpan ->

            val spanStart = editableText.getSpanStart(appliedSpan)
            val spanEnd = editableText.getSpanEnd(appliedSpan)

            var neighbourSpan: AztecInlineSpan? = null

            spansToUse.forEach inner@ {
                val aSpanStart = editableText.getSpanStart(it)
                val aSpanEnd = editableText.getSpanEnd(it)
                if (isSameInlineSpanType(it, appliedSpan)) {
                    if (aSpanStart == spanEnd || aSpanEnd == spanStart) {
                        neighbourSpan = it
                        return@inner
                    }

                }
            }

            if (neighbourSpan != null) {
                val neighbourSpanStart = editableText.getSpanStart(neighbourSpan)
                val neighbourSpanEnd = editableText.getSpanEnd(neighbourSpan)

                if (neighbourSpanStart == -1 || neighbourSpanEnd == -1)
                    return@forEach

                //span we want to join is on the left
                if (spanStart == neighbourSpanEnd) {
                    editableText.setSpan(appliedSpan, neighbourSpanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
                } else if (spanEnd == neighbourSpanStart) {
                    editableText.setSpan(appliedSpan, spanStart, neighbourSpanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
                }

                editableText.removeSpan(neighbourSpan)
            }
        }
    }

    fun makeInlineSpan(textFormat: TextFormat): AztecInlineSpan {
        when (textFormat) {
            TextFormat.FORMAT_HEADING_1,
            TextFormat.FORMAT_HEADING_2,
            TextFormat.FORMAT_HEADING_3,
            TextFormat.FORMAT_HEADING_4,
            TextFormat.FORMAT_HEADING_5,
            TextFormat.FORMAT_HEADING_6 -> return AztecHeadingSpan(textFormat)
            TextFormat.FORMAT_BOLD -> return AztecStyleSpan(Typeface.BOLD)
            TextFormat.FORMAT_ITALIC -> return AztecStyleSpan(Typeface.ITALIC)
            TextFormat.FORMAT_STRIKETHROUGH -> return AztecStrikethroughSpan()
            TextFormat.FORMAT_UNDERLINED -> return AztecUnderlineSpan()
            TextFormat.FORMAT_CODE -> return AztecCodeSpan(codeStyle)
            else -> return AztecStyleSpan(Typeface.NORMAL)
        }
    }

    fun makeInlineSpan(spanType: Class<AztecInlineSpan>, attrs: String? = null): AztecInlineSpan {
        when (spanType) {
            AztecCodeSpan::class.java -> return AztecCodeSpan(codeStyle, attrs)
            else -> return AztecStyleSpan(Typeface.NORMAL)
        }
    }

    fun containsInlineStyle(textFormat: TextFormat, start: Int = selectionStart, end: Int = selectionEnd): Boolean {
        val spanToCheck = makeInlineSpan(textFormat)

        if (start > end) {
            return false
        }

        if (start == end) {
            if (start - 1 < 0 || start + 1 > editableText.length) {
                return false
            } else {
                val before = editableText.getSpans(start - 1, start, AztecInlineSpan::class.java)
                        .filter { it -> isSameInlineSpanType(it, spanToCheck) }
                val after = editableText.getSpans(start, start + 1, AztecInlineSpan::class.java)
                        .filter { isSameInlineSpanType(it, spanToCheck) }
                return before.size > 0 && after.size > 0 && isSameInlineSpanType(before[0], after[0])
            }
        } else {
            val builder = StringBuilder()

            // Make sure no duplicate characters be added
            for (i in start..end - 1) {
                val spans = editableText.getSpans(i, i + 1, AztecInlineSpan::class.java)
                for (span in spans) {
                    if (isSameInlineSpanType(span, spanToCheck)) {
                        builder.append(editableText.subSequence(i, i + 1).toString())
                        break
                    }
                }
            }

            return editableText.subSequence(start, end).toString() == builder.toString()
        }

    }
}

