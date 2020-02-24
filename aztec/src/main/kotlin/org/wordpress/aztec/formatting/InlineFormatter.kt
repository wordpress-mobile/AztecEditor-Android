package org.wordpress.aztec.formatting

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import org.wordpress.android.util.AppLog
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecPart
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.AztecTextFormat
import org.wordpress.aztec.Constants
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.spans.AztecCodeSpan
import org.wordpress.aztec.spans.AztecStrikethroughSpan
import org.wordpress.aztec.spans.AztecStyleBoldSpan
import org.wordpress.aztec.spans.AztecStyleCiteSpan
import org.wordpress.aztec.spans.AztecStyleItalicSpan
import org.wordpress.aztec.spans.AztecStyleEmphasisSpan
import org.wordpress.aztec.spans.AztecStyleStrongSpan
import org.wordpress.aztec.spans.AztecStyleSpan
import org.wordpress.aztec.spans.AztecUnderlineSpan
import org.wordpress.aztec.spans.IAztecInlineSpan
import org.wordpress.aztec.watchers.TextChangedEvent
import java.util.ArrayList

/**
 * <b>Important</b> - use [applySpan] to add new spans to the editor. This method will
 * make sure any attributes belonging to the span are processed.
 */
class InlineFormatter(editor: AztecText, val codeStyle: CodeStyle) : AztecFormatter(editor) {

    data class CodeStyle(val codeBackground: Int, val codeBackgroundAlpha: Float, val codeColor: Int)

    fun toggle(textFormat: ITextFormat) {
        if (!containsInlineStyle(textFormat)) {
            applyInlineStyle(textFormat)
        } else {
            removeInlineStyle(textFormat)
        }
    }

    /**
     * Removes all formats in the list but if none found, applies the first one
     */
    fun toggleAny(textFormats: Set<ITextFormat>) {
        if (!textFormats
                .filter { containsInlineStyle(it) }
                .fold(false, { found, containedTextFormat -> removeInlineStyle(containedTextFormat); true })) {
            applyInlineStyle(textFormats.first())
        }
    }

    fun handleInlineStyling(textChangedEvent: TextChangedEvent) {
        if (textChangedEvent.isEndOfBufferMarker()) return

        // because we use SPAN_INCLUSIVE_INCLUSIVE for inline styles
        // we need to make sure unselected styles are not applied
        clearInlineStyles(textChangedEvent.inputStart, textChangedEvent.inputEnd, textChangedEvent.isNewLine())

        if (textChangedEvent.isNewLine()) return

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
                    AztecTextFormat.FORMAT_CODE -> {
                        applyInlineStyle(item, textChangedEvent.inputStart, textChangedEvent.inputEnd)
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
                    (newStart > end && editableText.length > end && editableText[end] == '\n')) {
                removeInlineStyle(it, newStart, end)
            }
        }
    }

    fun applyInlineStyle(textFormat: ITextFormat, start: Int = selectionStart, end: Int = selectionEnd, attrs: AztecAttributes = AztecAttributes()) {
        val spanToApply = makeInlineSpan(textFormat)
        spanToApply.attributes = attrs

        if (start >= end) {
            return
        }

        var precedingSpan: IAztecInlineSpan? = null
        var followingSpan: IAztecInlineSpan? = null

        if (start >= 1) {
            val previousSpans = editableText.getSpans(start - 1, start, IAztecInlineSpan::class.java)
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
                    // ensure css style is applied
                    (precedingSpan as IAztecInlineSpan).applyInlineStyleAttributes(editableText, start, end)
                    return@applyInlineStyle // we are adding text inside span - no need to do anything special
                } else {
                    applySpan(precedingSpan as IAztecInlineSpan, spanStart, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }

        if (editor.length() > end) {
            val nextSpans = editableText.getSpans(end, end + 1, IAztecInlineSpan::class.java)
            nextSpans.forEach {
                if (isSameInlineSpanType(it, spanToApply)) {
                    followingSpan = it
                    return@forEach
                }
            }

            if (followingSpan != null) {
                val spanEnd = editableText.getSpanEnd(followingSpan)
                applySpan(followingSpan as IAztecInlineSpan, start, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                editableText.setSpan(followingSpan, start, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        if (precedingSpan == null && followingSpan == null) {
            var existingSpanOfSameStyle: IAztecInlineSpan? = null

            val spans = editableText.getSpans(start, end, IAztecInlineSpan::class.java)
            spans.forEach {
                if (isSameInlineSpanType(it, spanToApply)) {
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
        when (span::class.java) {
            AztecStyleBoldSpan::class.java -> return AztecTextFormat.FORMAT_BOLD
            AztecStyleStrongSpan::class.java -> return AztecTextFormat.FORMAT_STRONG
            AztecStyleItalicSpan::class.java -> return AztecTextFormat.FORMAT_ITALIC
            AztecStyleEmphasisSpan::class.java -> return AztecTextFormat.FORMAT_EMPHASIS
            AztecStyleCiteSpan::class.java -> return AztecTextFormat.FORMAT_CITE
            AztecStrikethroughSpan::class.java -> return AztecTextFormat.FORMAT_STRIKETHROUGH
            AztecUnderlineSpan::class.java -> return AztecTextFormat.FORMAT_UNDERLINE
            AztecCodeSpan::class.java -> return AztecTextFormat.FORMAT_CODE
            else -> return null
        }
    }

    fun removeInlineStyle(spanToRemove: IAztecInlineSpan, start: Int = selectionStart, end: Int = selectionEnd) {
        val textFormat = spanToTextFormat(spanToRemove) ?: return

        val spans = editableText.getSpans(start, end, IAztecInlineSpan::class.java)
        val list = ArrayList<AztecPart>()

        spans.forEach {
            if (isSameInlineSpanType(it, spanToRemove)) {
                list.add(AztecPart(editableText.getSpanStart(it), editableText.getSpanEnd(it), it.attributes))
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
            editableText.removeSpan(it)
        }
    }

    fun removeInlineStyle(textFormat: ITextFormat, start: Int = selectionStart, end: Int = selectionEnd) {
        removeInlineStyle(makeInlineSpan(textFormat), start, end)
    }

    fun isSameInlineSpanType(firstSpan: IAztecInlineSpan, secondSpan: IAztecInlineSpan): Boolean {
        // special check for StyleSpans
        if (firstSpan is StyleSpan && secondSpan is StyleSpan) {
            return firstSpan.style == secondSpan.style
        }

        return firstSpan.javaClass == secondSpan.javaClass
    }

    // TODO: Check if there is more efficient way to tidy spans
    fun joinStyleSpans(start: Int, end: Int) {
        // joins spans on the left
        if (start > 1) {
            val spansInSelection = editableText.getSpans(start, end, IAztecInlineSpan::class.java)

            val spansBeforeSelection = editableText.getSpans(start - 1, start, IAztecInlineSpan::class.java)
            spansInSelection.forEach { innerSpan ->
                val inSelectionSpanEnd = editableText.getSpanEnd(innerSpan)
                val inSelectionSpanStart = editableText.getSpanStart(innerSpan)
                if (inSelectionSpanEnd == -1 || inSelectionSpanStart == -1) return@forEach
                spansBeforeSelection.forEach { outerSpan ->
                    val outerSpanStart = editableText.getSpanStart(outerSpan)

                    if (isSameInlineSpanType(innerSpan, outerSpan) && inSelectionSpanEnd >= outerSpanStart) {
                        editableText.removeSpan(outerSpan)
                        applySpan(innerSpan, outerSpanStart, inSelectionSpanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            }
        }

        // joins spans on the right
        if (editor.length() > end) {
            val spansInSelection = editableText.getSpans(start, end, IAztecInlineSpan::class.java)
            val spansAfterSelection = editableText.getSpans(end, end + 1, IAztecInlineSpan::class.java)
            spansInSelection.forEach { innerSpan ->
                val inSelectionSpanEnd = editableText.getSpanEnd(innerSpan)
                val inSelectionSpanStart = editableText.getSpanStart(innerSpan)
                if (inSelectionSpanEnd == -1 || inSelectionSpanStart == -1) return@forEach
                spansAfterSelection.forEach { outerSpan ->
                    val outerSpanEnd = editableText.getSpanEnd(outerSpan)
                    if (isSameInlineSpanType(innerSpan, outerSpan) && outerSpanEnd >= inSelectionSpanStart) {
                        editableText.removeSpan(outerSpan)
                        applySpan(innerSpan, inSelectionSpanStart, outerSpanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
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

                // span we want to join is on the left
                if (spanStart == neighbourSpanEnd) {
                    applySpan(appliedSpan, neighbourSpanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                } else if (spanEnd == neighbourSpanStart) {
                    applySpan(appliedSpan, spanStart, neighbourSpanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                editableText.removeSpan(neighbourSpan)
            }
        }
    }

    fun makeInlineSpan(textFormat: ITextFormat): IAztecInlineSpan {
        when (textFormat) {
            AztecTextFormat.FORMAT_BOLD -> return AztecStyleBoldSpan()
            AztecTextFormat.FORMAT_STRONG -> return AztecStyleStrongSpan()
            AztecTextFormat.FORMAT_ITALIC -> return AztecStyleItalicSpan()
            AztecTextFormat.FORMAT_EMPHASIS -> return AztecStyleEmphasisSpan()
            AztecTextFormat.FORMAT_CITE -> return AztecStyleCiteSpan()
            AztecTextFormat.FORMAT_STRIKETHROUGH -> return AztecStrikethroughSpan()
            AztecTextFormat.FORMAT_UNDERLINE -> return AztecUnderlineSpan()
            AztecTextFormat.FORMAT_CODE -> return AztecCodeSpan(codeStyle)
            else -> return AztecStyleSpan(Typeface.NORMAL)
        }
    }

    fun makeInlineSpan(spanType: Class<IAztecInlineSpan>, attrs: AztecAttributes = AztecAttributes()): IAztecInlineSpan {
        when (spanType) {
            AztecCodeSpan::class.java -> return AztecCodeSpan(codeStyle, attrs)
            else -> return AztecStyleSpan(Typeface.NORMAL)
        }
    }

    fun containsInlineStyle(textFormat: ITextFormat, start: Int = selectionStart, end: Int = selectionEnd): Boolean {
        val spanToCheck = makeInlineSpan(textFormat)

        if (start > end) {
            return false
        }

        if (start == end) {
            if (start - 1 < 0 || start + 1 > editableText.length) {
                return false
            } else {
                val before = editableText.getSpans(start - 1, start, IAztecInlineSpan::class.java)
                        .filter { it -> isSameInlineSpanType(it, spanToCheck) }
                        .firstOrNull()
                val after = editableText.getSpans(start, start + 1, IAztecInlineSpan::class.java)
                        .filter { isSameInlineSpanType(it, spanToCheck) }
                        .firstOrNull()
                return before != null && after != null && isSameInlineSpanType(before, after)
            }
        } else {
            val builder = StringBuilder()

            // Make sure no duplicate characters be added
            for (i in start..end - 1) {
                val spans = editableText.getSpans(i, i + 1, IAztecInlineSpan::class.java)

                for (span in spans) {
                    if (isSameInlineSpanType(span, spanToCheck)) {
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
