package org.wordpress.aztec.formatting

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.annotation.ColorRes
import org.wordpress.aztec.*
import org.wordpress.aztec.spans.*
import org.wordpress.aztec.watchers.TextChangedEvent

/**
 * <b>Important</b> - use [applySpan] to add new spans to the editor. This method will
 * make sure any attributes belonging to the span are processed.
 */
class InlineFormatter(
    editor: AztecText,
    val codeStyle: CodeStyle,
    private val highlightStyle: HighlightStyle,
) : AztecFormatter(editor) {

    data class CodeStyle(
        val codeBackground: Int,
        val codeBackgroundAlpha: Float,
        val codeColor: Int,
    )

    data class HighlightStyle(@ColorRes val color: Int)

    fun toggle(textFormat: ITextFormat) {
        if (!containsInlineStyle(textFormat)) {
            val inlineSpan = makeInlineSpan(textFormat)
            if (inlineSpan is IAztecExclusiveInlineSpan) {
                // If text format is exclusive, remove all the inclusive text formats already applied
                removeAllInclusiveFormats()
            } else {
                // If text format is inclusive, remove all the exclusive text formats already applied
                removeAllExclusiveFormats()
            }
            applyInlineStyle(textFormat)
        } else {
            removeInlineStyle(textFormat)
        }
    }

    private fun removeAllInclusiveFormats() {
        editableText.getSpans(selectionStart, selectionEnd, IAztecInlineSpan::class.java).filter {
            it !is IAztecExclusiveInlineSpan
        }.forEach { removeInlineStyle(it) }
    }

    /**
     * Removes all formats in the list but if none found, applies the first one
     */
    fun toggleAny(textFormats: Set<ITextFormat>) {
        if (!textFormats
                .filter { containsInlineStyle(it) }
                .fold(false) { found, containedTextFormat -> removeInlineStyle(containedTextFormat); true }
        ) {
            removeAllExclusiveFormats()
            applyInlineStyle(textFormats.first())
        }
    }

    private fun removeAllExclusiveFormats() {
        editableText.getSpans(selectionStart, selectionEnd, IAztecExclusiveInlineSpan::class.java)
            .forEach { removeInlineStyle(it) }
    }

    fun handleInlineStyling(textChangedEvent: TextChangedEvent) {
        if (textChangedEvent.isEndOfBufferMarker()) return

        // because we use SPAN_INCLUSIVE_INCLUSIVE for inline styles
        // we need to make sure unselected styles are not applied
        clearInlineStyles(textChangedEvent.inputStart,
            textChangedEvent.inputEnd,
            editor.newlineTerminatesInlineSpans && textChangedEvent.isNewLine())

        if (editor.newlineTerminatesInlineSpans && textChangedEvent.isNewLine()) return

        if (editor.formattingIsApplied()) {
            for (item in editor.selectedStyles) {
                when (item) {
                    AztecTextFormat.FORMAT_BOLD,
                    AztecTextFormat.FORMAT_STRONG,
                    AztecTextFormat.FORMAT_ITALIC,
                    AztecTextFormat.FORMAT_EMPHASIS,
                    AztecTextFormat.FORMAT_CITE,
                    AztecTextFormat.FORMAT_STRIKETHROUGH,
                    AztecTextFormat.FORMAT_UNDERLINE,
                    AztecTextFormat.FORMAT_CODE,
                    AztecTextFormat.FORMAT_HIGHLIGHT,
                    AztecTextFormat.FORMAT_COLOR,
                    AztecTextFormat.FORMAT_TYPEFACE,
                    AztecTextFormat.FORMAT_ABSOLUTE_FONT_SIZE,
                    AztecTextFormat.FORMAT_ABSOLUTE_LINE_HEIGHT,
                    -> {
                        applyInlineStyle(item,
                            textChangedEvent.inputStart,
                            textChangedEvent.inputEnd)
                    }
                    AztecTextFormat.FORMAT_MARK -> {
                        // For cases of an empty mark tag, either at the beginning of the text or in between
                        if (textChangedEvent.inputStart == 0 && textChangedEvent.inputEnd == 1) {
                            applyMarkInlineStyle(textChangedEvent.inputStart,
                                textChangedEvent.inputEnd)
                        } else {
                            applyInlineStyle(item,
                                textChangedEvent.inputStart,
                                textChangedEvent.inputEnd)
                        }
                    }
                    else -> {
                        // do nothing
                    }
                }
            }
        }

        editor.setFormattingChangesApplied()
    }

    private fun clearInlineStyles(start: Int, end: Int, ignoreSelectedStyles: Boolean) {
        val newStart = if (start > end) end else start
        // if there is END_OF_BUFFER_MARKER at the end of or range, extend the range to include it

        // remove lingering empty spans when removing characters
        if (start > end) {
            editableText.getSpans(newStart, end, IAztecInlineSpan::class.java)
                .filter { editableText.getSpanStart(it) == editableText.getSpanEnd(it) }
                .forEach { editableText.removeSpan(it) }
            return
        }

        editableText.getSpans(newStart, end, IAztecInlineSpan::class.java).forEach {
            if (!editor.selectedStyles.contains(spanToTextFormat(it)) || ignoreSelectedStyles || (newStart == 0 && end == 0) ||
                (newStart > end && editableText.length > end && editableText[end] == '\n')
            ) {
                removeInlineStyle(it, newStart, end)
            }
        }
    }

    private fun applyInlineStyle(
        textFormat: ITextFormat,
        start: Int = selectionStart,
        end: Int = selectionEnd,
        attrs: AztecAttributes = AztecAttributes(),
    ) {
        val spanToApply = makeInlineSpan(textFormat)
        spanToApply.attributes = attrs

        if (start >= end) {
            return
        }

        var precedingSpan: IAztecInlineSpan? = null
        var followingSpan: IAztecInlineSpan? = null

        if (start >= 1) {
            val previousSpans =
                editableText.getSpans(start - 1, start, IAztecInlineSpan::class.java)
            previousSpans.forEach {
                if (isSameInlineSpanType(it, spanToApply, false)) {
                    precedingSpan = it
                    return@forEach
                }
            }

            if (precedingSpan != null) {
                val spanStart = editableText.getSpanStart(precedingSpan)
                val spanEnd = editableText.getSpanEnd(precedingSpan)

                if (spanEnd > start) {
                    // ensure css style is applied
                    (precedingSpan as IAztecInlineSpan).applyInlineStyleAttributes(editableText,
                        start,
                        end)
                    return // we are adding text inside span - no need to do anything special
                } else {
                    applySpan(precedingSpan as IAztecInlineSpan,
                        spanStart,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }

        if (editor.length() > end) {
            val nextSpans = editableText.getSpans(end, end + 1, IAztecInlineSpan::class.java)
            nextSpans.forEach {
                if (isSameInlineSpanType(it, spanToApply, false)) {
                    followingSpan = it
                    return@forEach
                }
            }

            if (followingSpan != null) {
                val spanEnd = editableText.getSpanEnd(followingSpan)
                applySpan(followingSpan as IAztecInlineSpan,
                    start,
                    spanEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                editableText.setSpan(followingSpan,
                    start,
                    spanEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        if (precedingSpan == null && followingSpan == null) {
            var existingSpanOfSameStyle: IAztecInlineSpan? = null

            val spans = editableText.getSpans(start, end, IAztecInlineSpan::class.java)
            spans.forEach {
                if (isSameInlineSpanType(it, spanToApply, false)) {
                    existingSpanOfSameStyle = it
                    return@forEach
                }
            }

            // if we already have same span within selection - reuse its attributes
            if (existingSpanOfSameStyle != null) {
                editableText.removeSpan(existingSpanOfSameStyle)
                spanToApply.attributes = attrs
            }

            applySpan(spanToApply, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        joinStyleSpans(start, end)
    }

    private fun applyMarkInlineStyle(
        start: Int = selectionStart,
        end: Int = selectionEnd,
        attrs: AztecAttributes = AztecAttributes(),
    ) {
        val previousSpans = editableText.getSpans(start, end, MarkSpan::class.java)
        previousSpans.forEach {
            it.applyInlineStyleAttributes(editableText, start, end)
        }
    }

    private fun applySpan(span: IAztecInlineSpan, start: Int, end: Int, type: Int) {
        if (start > end || start < 0 || end > editableText.length) {
            // If an external logger is available log the error there.
            val extLogger = editor.externalLogger
            if (extLogger != null) {
                extLogger.log("InlineFormatter.applySpan - setSpan has end before start." +
                        " Start:" + start + " End:" + end)
                extLogger.log("Logging the whole content" + editor.toPlainHtml())
            }
            // Now log in the default log
            AppLog.w(AppLog.T.EDITOR, "InlineFormatter.applySpan - setSpan has end before start." +
                    " Start:" + start + " End:" + end)
            AppLog.w(AppLog.T.EDITOR, "Logging the whole content" + editor.toPlainHtml())
            return
        }
        editableText.setSpan(span, start, end, type)
        span.applyInlineStyleAttributes(editableText, start, end)
    }

    fun spanToTextFormat(span: IAztecInlineSpan): ITextFormat? {
        return when (span) {
            is AztecStyleBoldSpan -> AztecTextFormat.FORMAT_BOLD
            is AztecStyleStrongSpan -> AztecTextFormat.FORMAT_STRONG
            is AztecStyleItalicSpan -> AztecTextFormat.FORMAT_ITALIC
            is AztecStyleEmphasisSpan -> AztecTextFormat.FORMAT_EMPHASIS
            is AztecStyleCiteSpan -> AztecTextFormat.FORMAT_CITE
            is AztecStrikethroughSpan -> AztecTextFormat.FORMAT_STRIKETHROUGH
            is AztecUnderlineSpan -> AztecTextFormat.FORMAT_UNDERLINE
            is AztecCodeSpan -> AztecTextFormat.FORMAT_CODE
            is MarkSpan -> AztecTextFormat.FORMAT_MARK
            is HighlightSpan -> AztecTextFormat.FORMAT_HIGHLIGHT
            is AztecColorSpan -> AztecTextFormat.FORMAT_COLOR
            is AztecConstantTypefaceSpan -> AztecTextFormat.FORMAT_TYPEFACE
            is AztecAbsoluteSizeSpan -> AztecTextFormat.FORMAT_ABSOLUTE_FONT_SIZE
            is AztecLineHeightSpan -> AztecTextFormat.FORMAT_ABSOLUTE_LINE_HEIGHT
            else -> null
        }
    }

    fun removeInlineStyle(
        spanToRemove: IAztecInlineSpan,
        start: Int = selectionStart,
        end: Int = selectionEnd,
    ) {
        val textFormat = spanToTextFormat(spanToRemove) ?: return

        val spans = editableText.getSpans(start, end, IAztecInlineSpan::class.java)
        val list = ArrayList<AztecPart>()

        spans.forEach {
            if (isSameInlineSpanType(it, spanToRemove, false)) {
                list.add(AztecPart(editableText.getSpanStart(it),
                    editableText.getSpanEnd(it),
                    it.attributes))
                editableText.removeSpan(it)
            }
        }
        // remove the CSS style span
        removeInlineCssStyle()

        list.forEach {
            if (it.isValid) {
                if (it.start < start) {
                    applyInlineStyle(textFormat, it.start, start, it.attr)
                }
                if (it.end > end) {
                    applyInlineStyle(textFormat, end, it.end, it.attr)
                }
            }
        }

        joinStyleSpans(start, end)
    }

    fun removeInlineCssStyle(start: Int = selectionStart, end: Int = selectionEnd) {
        val spans = editableText.getSpans(start, end, ForegroundColorSpan::class.java)
        spans.forEach {
            if (it is AztecColorSpan) return@forEach
            editableText.removeSpan(it)
        }
    }

    fun removeInlineStyle(
        textFormat: ITextFormat,
        start: Int = selectionStart,
        end: Int = selectionEnd,
    ) {
        removeInlineStyle(makeInlineSpan(textFormat), start, end)
    }

    fun isSameInlineSpanType(
        firstSpan: IAztecInlineSpan,
        secondSpan: IAztecInlineSpan,
        deepEquivalence: Boolean,
    ): Boolean {
        // special check for StyleSpans
        if (firstSpan is StyleSpan && secondSpan is StyleSpan) {
            return firstSpan.style == secondSpan.style
        }
        if (deepEquivalence) {
            if (firstSpan is AztecColorSpan && secondSpan is AztecColorSpan) {
                return firstSpan.color == secondSpan.color
            }
            if (firstSpan is AztecConstantTypefaceSpan && secondSpan is AztecConstantTypefaceSpan) {
                return firstSpan.typeface == secondSpan.typeface
            }
            if (firstSpan is AztecAbsoluteSizeSpan && secondSpan is AztecAbsoluteSizeSpan) {
                return firstSpan.size == secondSpan.size && firstSpan.dip == secondSpan.dip
            }
            if (firstSpan is AztecLineHeightSpan && secondSpan is AztecLineHeightSpan) {
                return firstSpan.height == secondSpan.height
            }
        }

        return firstSpan.javaClass == secondSpan.javaClass
    }

    // TODO: Check if there is more efficient way to tidy spans
    fun joinStyleSpans(start: Int, end: Int) {
        // joins spans on the left
        if (start > 1) {
            val spansInSelection = editableText.getSpans(start, end, IAztecInlineSpan::class.java)

            val spansBeforeSelection =
                editableText.getSpans(start - 1, start, IAztecInlineSpan::class.java)
            spansInSelection.forEach { innerSpan ->
                val inSelectionSpanEnd = editableText.getSpanEnd(innerSpan)
                val inSelectionSpanStart = editableText.getSpanStart(innerSpan)
                if (inSelectionSpanEnd == -1 || inSelectionSpanStart == -1) return@forEach
                spansBeforeSelection.forEach { outerSpan ->
                    val outerSpanStart = editableText.getSpanStart(outerSpan)

                    if (isSameInlineSpanType(innerSpan, outerSpan, true) && inSelectionSpanEnd >= outerSpanStart
                    ) {
                        editableText.removeSpan(outerSpan)
                        applySpan(innerSpan,
                            outerSpanStart,
                            inSelectionSpanEnd,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            }
        }

        // joins spans on the right
        if (editor.length() > end) {
            val spansInSelection = editableText.getSpans(start, end, IAztecInlineSpan::class.java)
            val spansAfterSelection =
                editableText.getSpans(end, end + 1, IAztecInlineSpan::class.java)
            spansInSelection.forEach { innerSpan ->
                val inSelectionSpanEnd = editableText.getSpanEnd(innerSpan)
                val inSelectionSpanStart = editableText.getSpanStart(innerSpan)
                if (inSelectionSpanEnd == -1 || inSelectionSpanStart == -1) return@forEach
                spansAfterSelection.forEach { outerSpan ->
                    val outerSpanEnd = editableText.getSpanEnd(outerSpan)
                    if (isSameInlineSpanType(innerSpan,
                            outerSpan, true) && outerSpanEnd >= inSelectionSpanStart
                    ) {
                        editableText.removeSpan(outerSpan)
                        applySpan(innerSpan,
                            inSelectionSpanStart,
                            outerSpanEnd,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            }
        }

        // joins spans withing selected text
        val spansInSelection = editableText.getSpans(start, end, IAztecInlineSpan::class.java)
        val spansToUse = editableText.getSpans(start, end, IAztecInlineSpan::class.java)

        spansInSelection.forEach { appliedSpan ->

            val spanStart = editableText.getSpanStart(appliedSpan)
            val spanEnd = editableText.getSpanEnd(appliedSpan)

            var neighbourSpan: IAztecInlineSpan? = null

            spansToUse.forEach inner@{
                val aSpanStart = editableText.getSpanStart(it)
                val aSpanEnd = editableText.getSpanEnd(it)
                if (isSameInlineSpanType(it, appliedSpan, true)) {
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

                // span we want to join is on the left
                if (spanStart == neighbourSpanEnd) {
                    applySpan(appliedSpan,
                        neighbourSpanStart,
                        spanEnd,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                } else if (spanEnd == neighbourSpanStart) {
                    applySpan(appliedSpan,
                        spanStart,
                        neighbourSpanEnd,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                editableText.removeSpan(neighbourSpan)
            }
        }
    }

    fun makeInlineSpan(textFormat: ITextFormat): IAztecInlineSpan {
        return when (textFormat) {
            AztecTextFormat.FORMAT_BOLD -> AztecStyleBoldSpan()
            AztecTextFormat.FORMAT_STRONG -> AztecStyleStrongSpan()
            AztecTextFormat.FORMAT_ITALIC -> AztecStyleItalicSpan()
            AztecTextFormat.FORMAT_EMPHASIS -> AztecStyleEmphasisSpan()
            AztecTextFormat.FORMAT_CITE -> AztecStyleCiteSpan()
            AztecTextFormat.FORMAT_STRIKETHROUGH -> AztecStrikethroughSpan()
            AztecTextFormat.FORMAT_UNDERLINE -> AztecUnderlineSpan()
            AztecTextFormat.FORMAT_CODE -> AztecCodeSpan(codeStyle)
            AztecTextFormat.FORMAT_HIGHLIGHT -> {
                HighlightSpan(highlightStyle = highlightStyle, context = editor.context)
            }
            AztecTextFormat.FORMAT_MARK -> MarkSpan()
            // note(alex): The following 4 formats are used more as a facade and will need default styles seeded
            //             from the editor. For now, for our use, they can be any value and will still have the
            //             desired equivalence checks. An external html parser / span builder will create their
            //             own versions of these that will have actual values and at some point we'll want to
            //             implement "correctly". They would also cause a rather interesting change for the text
            //             format API as they will actually need to be seeded from user selected values rather than
            //             pre-defined defaults (e.g. highlights) or boolean only logic (e.g. style spans).
            AztecTextFormat.FORMAT_COLOR -> AztecColorSpan(tag = "", 0)
            AztecTextFormat.FORMAT_TYPEFACE -> AztecConstantTypefaceSpan("", Typeface.DEFAULT)
            AztecTextFormat.FORMAT_ABSOLUTE_FONT_SIZE -> AztecAbsoluteSizeSpan("", 16, true)
            AztecTextFormat.FORMAT_ABSOLUTE_LINE_HEIGHT -> AztecLineHeightSpan("", 40)
            else -> AztecStyleSpan(Typeface.NORMAL)
        }
    }

    fun containsInlineStyle(
        textFormat: ITextFormat,
        start: Int = selectionStart,
        end: Int = selectionEnd,
    ): Boolean {
        val spanToCheck = makeInlineSpan(textFormat)

        if (start > end) {
            return false
        }

        if (start == end) {
            if (start - 1 < 0 || start + 1 > editableText.length) {
                return false
            } else {
                val before = editableText.getSpans(start - 1, start, IAztecInlineSpan::class.java)
                    .firstOrNull { isSameInlineSpanType(it, spanToCheck, false) }
                val after = editableText.getSpans(start, start + 1, IAztecInlineSpan::class.java)
                    .firstOrNull { isSameInlineSpanType(it, spanToCheck, false) }
                return before != null && after != null && isSameInlineSpanType(before, after, false)
            }
        } else {
            val builder = StringBuilder()

            // Make sure no duplicate characters be added
            for (i in start until end) {
                val spans = editableText.getSpans(i, i + 1, IAztecInlineSpan::class.java)

                for (span in spans) {
                    if (isSameInlineSpanType(span, spanToCheck, false)) {
                        builder.append(editableText.subSequence(i, i + 1).toString())
                        break
                    }
                }
            }

            val originalText = editableText.subSequence(start, end).replace("\n".toRegex(), "")
            val textOfCombinedSpans = builder.toString().replace("\n".toRegex(), "")

            return originalText.isNotEmpty() && originalText == textOfCombinedSpans
        }
    }

    fun tryRemoveLeadingInlineStyle() {
        val selectionStart = editor.selectionStart
        val selectionEnd = editor.selectionEnd

        if (selectionStart == 1 && selectionEnd == selectionStart) {
            editableText.getSpans(0, 0, IAztecInlineSpan::class.java).forEach {
                if (editableText.getSpanEnd(it) == selectionEnd && editableText.getSpanEnd(it) == selectionStart) {
                    editableText.removeSpan(it)
                }
            }
        } else if (editor.length() == 1 && editor.text[0] == Constants.END_OF_BUFFER_MARKER) {
            editableText.getSpans(0, 1, IAztecInlineSpan::class.java).forEach {
                if (editableText.getSpanStart(it) == 1 && editableText.getSpanEnd(it) == 1) {
                    editableText.removeSpan(it)
                }
            }
        }
    }
}
