package org.wordpress.aztec.formatting

import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import org.wordpress.aztec.*
import org.wordpress.aztec.AztecText.OnMediaTappedListener
import org.wordpress.aztec.spans.*
import org.xml.sax.Attributes
import java.util.*


class LineBlockFormatter(editor: AztecText) : AztecFormatter(editor) {

//    fun applyHeading(textFormat: TextFormat) {
//        headingClear()
//
//        if (textFormat != TextFormat.FORMAT_PARAGRAPH) {
//            headingFormat(textFormat)
//        }
//    }

    fun applyMoreComment() {
        applyComment(AztecCommentSpan.Comment.MORE)
    }

    fun applyPageComment() {
        applyComment(AztecCommentSpan.Comment.PAGE)
    }

    fun handleLineBlockStyling(textChangedEvent: TextChangedEvent) {
        if (textChangedEvent.isAddingCharacters && textChangedEvent.isNewLineButNotAtTheBeginning()) {
            val spanAtNewLIne = editableText.getSpans(textChangedEvent.inputStart, textChangedEvent.inputStart, AztecHeadingSpan::class.java).getOrNull(0)
            if (spanAtNewLIne != null) {
                val spanStart = editableText.getSpanStart(spanAtNewLIne)
                val spanEnd = editableText.getSpanEnd(spanAtNewLIne)

                val isHeadingSplitRequired = spanStart <= textChangedEvent.inputStart && spanEnd > textChangedEvent.inputEnd
                //split heading span
                if (isHeadingSplitRequired && editableText[textChangedEvent.inputStart - 1] != '\n') {
                    editableText.setSpan(spanAtNewLIne, spanStart, textChangedEvent.inputStart, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    editableText.setSpan(spanAtNewLIne.clone(), textChangedEvent.inputStart + 1, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                //avoid applying heading span to newline breaks
                else if (isHeadingSplitRequired && editableText[textChangedEvent.inputStart - 1] == '\n') {
                    editableText.setSpan(spanAtNewLIne, spanStart + 1, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                } else if (!isHeadingSplitRequired && editableText.length > textChangedEvent.inputStart + 1 && editableText[textChangedEvent.inputStart + 1] == '\n') {
                    editableText.setSpan(spanAtNewLIne, spanStart, spanEnd - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                } else if (!isHeadingSplitRequired && textChangedEvent.inputStart + 1 == spanEnd && editableText[textChangedEvent.inputStart] == '\n') {
                    editableText.setSpan(spanAtNewLIne, spanStart, spanEnd - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        } else if (!textChangedEvent.isAddingCharacters && editableText.length > textChangedEvent.inputEnd && textChangedEvent.inputEnd > 0) {
            val charBeforeInputEnd = textChangedEvent.text[textChangedEvent.inputEnd - 1]
            if (charBeforeInputEnd == '\n') return

            val spanOnTheLeft = editableText.getSpans(textChangedEvent.inputEnd - 1, textChangedEvent.inputEnd, AztecHeadingSpan::class.java).getOrNull(0)
            val spanOnTheRight = editableText.getSpans(textChangedEvent.inputEnd, textChangedEvent.inputEnd + 1, AztecHeadingSpan::class.java).getOrNull(0)

            //remove heading span if we move it up to line without another heading style applied
            if (spanOnTheLeft == null && spanOnTheRight != null) {
                editableText.removeSpan(spanOnTheRight)
            } else if (spanOnTheLeft != null && spanOnTheRight != null) {
                //change the heading span style if we move it up to line with diferent heading style applied
                if (spanOnTheLeft != spanOnTheRight) {
                    val leftSpanStart = editableText.getSpanStart(spanOnTheLeft)
                    val rightSpanEnd = editableText.getSpanEnd(spanOnTheRight)
                    editableText.removeSpan(spanOnTheRight)
                    editableText.setSpan(spanOnTheLeft, leftSpanStart, rightSpanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }
    }

    fun headingClear() {
        val lines = TextUtils.split(editableText.toString(), "\n")

        for (i in lines.indices) {
            if (!containsHeading(i)) {
                continue
            }

            val lineStart = (0..i - 1).sumBy { lines[it].length + 1 }
            val lineEnd = lineStart + lines[i].length

            if (lineStart >= lineEnd) {
                continue
            }

            var headingStart = 0
            var headingEnd = 0

            if ((lineStart <= selectionStart && selectionEnd <= lineEnd) ||
                    (lineStart >= selectionStart && selectionEnd >= lineEnd) ||
                    (lineStart <= selectionStart && selectionEnd >= lineEnd && selectionStart <= lineEnd) ||
                    (lineStart >= selectionStart && selectionEnd <= lineEnd && selectionEnd >= lineStart)) {
                headingStart = lineStart
                headingEnd = lineEnd
            }

            if (headingStart < headingEnd) {
                val spans = editableText.getSpans(headingStart, headingEnd, AztecHeadingSpan::class.java)

                for (span in spans) {
                    editableText.removeSpan(span)
                }
            }
        }

        editor.refreshText()
    }

//    fun headingFormat(textFormat: TextFormat) {
//        val lines = TextUtils.split(editableText.toString(), "\n")
//
//        for (i in lines.indices) {
//            val lineStart = (0..i - 1).sumBy { lines[it].length + 1 }
//            val lineEnd = lineStart + lines[i].length
//
//            if (lineStart >= lineEnd) {
//                continue
//            }
//
//            var headingStart = 0
//            var headingEnd = 0
//
//            if ((lineStart <= selectionStart && selectionEnd <= lineEnd) ||
//                    (lineStart >= selectionStart && selectionEnd >= lineEnd) ||
//                    (lineStart <= selectionStart && selectionEnd >= lineEnd && selectionStart <= lineEnd) ||
//                    (lineStart >= selectionStart && selectionEnd <= lineEnd && selectionEnd >= lineStart)) {
//                headingStart = lineStart
//                headingEnd = lineEnd
//            }
//
//            if (headingStart < headingEnd) {
//                HeadingHandler.set(editableText, AztecHeadingSpan(textFormat, "", headerStyle.verticalPadding), headingStart, headingEnd)
//            }
//        }
//
//        editor.refreshText()
//    }

    fun containsHeading(textFormat: TextFormat, selStart: Int, selEnd: Int): Boolean {
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

    fun containsHeading(index: Int): Boolean {
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
        return spans.isNotEmpty()
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

    private fun applyComment(comment: AztecCommentSpan.Comment) {
        editor.removeInlineStylesFromRange(selectionStart, selectionEnd)
        editor.removeBlockStylesFromRange(selectionStart, selectionEnd, true)

        val span = AztecCommentSpan(
                editor.context,
                when (comment) {
                    AztecCommentSpan.Comment.MORE -> ContextCompat.getDrawable(editor.context, R.drawable.img_more)
                    AztecCommentSpan.Comment.PAGE -> ContextCompat.getDrawable(editor.context, R.drawable.img_page)
                }
        )
        val ssb = SpannableStringBuilder(comment.html)
        ssb.setSpan(span, 0, comment.html.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        editableText.replace(selectionStart, selectionEnd, ssb)

        editor.setSelection(selectionEnd + 1)
    }

    fun insertMedia(drawable: Drawable?, attributes: Attributes, onMediaTappedListener: OnMediaTappedListener?) {
        val span = AztecMediaSpan(editor.context, drawable, attributes, onMediaTappedListener)
        span.textView = editor

        val spanBeforeMedia = editableText.getSpans(selectionStart, selectionEnd, AztecBlockSpan::class.java)
        .firstOrNull {
            selectionStart == editableText.getSpanEnd(it)
        }

        val spanAfterMedia = editableText.getSpans(selectionStart, selectionEnd, AztecBlockSpan::class.java)
                .firstOrNull {
                    selectionStart == editableText.getSpanStart(it)
                }

        val mediaStartIndex = selectionStart
        val mediaEndIndex = selectionStart + 1

        editor.disableTextChangedListener()
        editableText.replace(selectionStart, selectionEnd, Constants.IMG_STRING)
        editor.enableTextChangedListener()

        if (spanAfterMedia != null) {
            editableText.setSpan(spanAfterMedia, mediaStartIndex, editableText.getSpanEnd(spanAfterMedia), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        if (spanBeforeMedia != null) {
            editableText.setSpan(spanBeforeMedia, editableText.getSpanStart(spanBeforeMedia), mediaEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        editor.removeInlineStylesFromRange(mediaStartIndex, mediaEndIndex)

        editableText.setSpan(
                span,
                mediaStartIndex,
                mediaEndIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        editableText.setSpan(
                AztecMediaClickableSpan(span),
                mediaStartIndex,
                mediaEndIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        editor.setSelection(mediaEndIndex)
        editor.isMediaAdded = true
    }
}
