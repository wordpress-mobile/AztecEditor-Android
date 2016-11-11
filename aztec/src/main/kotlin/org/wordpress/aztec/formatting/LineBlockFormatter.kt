package org.wordpress.aztec.formatting

import android.text.Spanned
import android.text.TextUtils
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.R
import org.wordpress.aztec.TextFormat
import org.wordpress.aztec.spans.AztecBlockSpan
import org.wordpress.aztec.spans.AztecCommentSpan
import org.wordpress.aztec.spans.AztecHeadingSpan
import java.util.*


class LineBlockFormatter(editor: AztecText) {

    val editor: AztecText

    init {
        this.editor = editor
    }


     fun headingClear() {
        val lines = TextUtils.split(editor.editableText.toString(), "\n")

        for (i in lines.indices) {
            if (!containsHeading(i)) {
                continue
            }

            var lineStart = 0

            for (j in 0..i - 1) {
                lineStart += lines[j].length + 1
            }

            val lineEnd = lineStart + lines[i].length

            if (lineStart >= lineEnd) {
                continue
            }

            var headingStart = 0
            var headingEnd = 0

            if ((lineStart <= editor.selectionStart && editor.selectionEnd <= lineEnd) ||
                    (lineStart >= editor.selectionStart && editor.selectionEnd >= lineEnd) ||
                    (lineStart <= editor.selectionStart && editor.selectionEnd >= lineEnd && editor.selectionStart <= lineEnd) ||
                    (lineStart >= editor.selectionStart && editor.selectionEnd <= lineEnd && editor.selectionEnd >= lineStart)) {
                headingStart = lineStart
                headingEnd = lineEnd
            }

            if (headingStart < headingEnd) {
                val spans = editor.editableText.getSpans(headingStart, headingEnd, AztecHeadingSpan::class.java)

                for (span in spans) {
                    editor.editableText.removeSpan(span)
                }
            }
        }

        editor.refreshText()
    }

     fun headingFormat(textFormat: TextFormat) {
        val lines = TextUtils.split(editor.editableText.toString(), "\n")

        for (i in lines.indices) {
            var lineStart = 0

            for (j in 0..i - 1) {
                lineStart += lines[j].length + 1
            }

            val lineEnd = lineStart + lines[i].length

            if (lineStart >= lineEnd) {
                continue
            }

            var headingStart = 0
            var headingEnd = 0

            if ((lineStart <= editor.selectionStart && editor.selectionEnd <= lineEnd) ||
                    (lineStart >= editor.selectionStart && editor.selectionEnd >= lineEnd) ||
                    (lineStart <= editor.selectionStart && editor.selectionEnd >= lineEnd && editor.selectionStart <= lineEnd) ||
                    (lineStart >= editor.selectionStart && editor.selectionEnd <= lineEnd && editor.selectionEnd >= lineStart)) {
                headingStart = lineStart
                headingEnd = lineEnd
            }

            if (headingStart < headingEnd) {
                when (textFormat) {
                    TextFormat.FORMAT_HEADING_1 ->
                        editor.editableText.setSpan(AztecHeadingSpan(AztecHeadingSpan.Heading.H1), headingStart, headingEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    TextFormat.FORMAT_HEADING_2 ->
                        editor.editableText.setSpan(AztecHeadingSpan(AztecHeadingSpan.Heading.H2), headingStart, headingEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    TextFormat.FORMAT_HEADING_3 ->
                        editor.editableText.setSpan(AztecHeadingSpan(AztecHeadingSpan.Heading.H3), headingStart, headingEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    TextFormat.FORMAT_HEADING_4 ->
                        editor.editableText.setSpan(AztecHeadingSpan(AztecHeadingSpan.Heading.H4), headingStart, headingEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    TextFormat.FORMAT_HEADING_5 ->
                        editor.editableText.setSpan(AztecHeadingSpan(AztecHeadingSpan.Heading.H5), headingStart, headingEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    TextFormat.FORMAT_HEADING_6 ->
                        editor.editableText.setSpan(AztecHeadingSpan(AztecHeadingSpan.Heading.H6), headingStart, headingEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    else -> {
                    }
                }
            }
        }

        editor.refreshText()
    }

     fun containsHeading(textFormat: TextFormat, selStart: Int, selEnd: Int): Boolean {
        val lines = TextUtils.split(editor.editableText.toString(), "\n")
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
            if (!containHeadingType(textFormat, i)) {
                return false
            }
        }

        return true
    }

     fun containsHeading(index: Int): Boolean {
        val lines = TextUtils.split(editor.editableText.toString(), "\n")

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

        val spans = editor.editableText.getSpans(start, end, AztecHeadingSpan::class.java)
        return spans.size > 0
    }

    private fun containHeadingType(textFormat: TextFormat, index: Int): Boolean {
        val lines = TextUtils.split(editor.editableText.toString(), "\n")

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

        val spans = editor.editableText.getSpans(start, end, AztecHeadingSpan::class.java)

        for (span in spans) {
            when (textFormat) {
                TextFormat.FORMAT_HEADING_1 ->
                    return span.heading.equals(AztecHeadingSpan.Heading.H1)
                TextFormat.FORMAT_HEADING_2 ->
                    return span.heading.equals(AztecHeadingSpan.Heading.H2)
                TextFormat.FORMAT_HEADING_3 ->
                    return span.heading.equals(AztecHeadingSpan.Heading.H3)
                TextFormat.FORMAT_HEADING_4 ->
                    return span.heading.equals(AztecHeadingSpan.Heading.H4)
                TextFormat.FORMAT_HEADING_5 ->
                    return span.heading.equals(AztecHeadingSpan.Heading.H5)
                TextFormat.FORMAT_HEADING_6 ->
                    return span.heading.equals(AztecHeadingSpan.Heading.H6)
                else -> return false
            }
        }

        return false
    }



    fun applyComment(comment: AztecCommentSpan.Comment) {
        //check if we add a comment into a block element, at the end of the line, but not at the end of last line
        var applyingOnTheEndOfBlockLine = false
        editor.editableText.getSpans(editor.selectionStart, editor.selectionEnd, AztecBlockSpan::class.java).forEach {
            if (editor.editableText.getSpanEnd(it) > editor.selectionEnd && editor.editableText[editor.selectionEnd] == '\n') {
                applyingOnTheEndOfBlockLine = true
                return@forEach
            }
        }

        val commentStartIndex = editor.selectionStart + 1
        val commentEndIndex = editor.selectionStart + comment.html.length + 1

        editor.disableTextChangedListener()
        editor.editableText.replace(editor.selectionStart, editor.selectionEnd, "\n" + comment.html + if (applyingOnTheEndOfBlockLine) "" else "\n")

        editor.removeBlockStylesFromRange(commentStartIndex, commentEndIndex + 1, true)
        editor.removeHeadingStylesFromRange(commentStartIndex, commentEndIndex + 1)
        editor.removeInlineStylesFromRange(commentStartIndex, commentEndIndex + 1)

        val span = AztecCommentSpan(
                editor.context,
                when (comment) {
                    AztecCommentSpan.Comment.MORE -> editor.resources.getDrawable(R.drawable.img_more)
                    AztecCommentSpan.Comment.PAGE -> editor.resources.getDrawable(R.drawable.img_page)
                }
        )

        editor.editableText.setSpan(
                span,
                commentStartIndex,
                commentEndIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        editor.setSelection(commentEndIndex + 1)
    }


}