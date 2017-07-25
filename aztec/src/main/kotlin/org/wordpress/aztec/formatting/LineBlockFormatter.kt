package org.wordpress.aztec.formatting

import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import org.wordpress.aztec.*
import org.wordpress.aztec.AztecText.OnImageTappedListener
import org.wordpress.aztec.AztecText.OnVideoTappedListener
import org.wordpress.aztec.spans.*
import org.wordpress.aztec.watchers.EndOfBufferMarkerAdder
import org.xml.sax.Attributes
import java.util.*

class LineBlockFormatter(editor: AztecText) : AztecFormatter(editor) {

    fun containsHeading(textFormat: ITextFormat, selStart: Int, selEnd: Int): Boolean {
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
                    || (selEnd in lineStart..lineEnd)
                    || (selStart in lineStart..lineEnd)) {
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

    fun applyHorizontalRule() {
        editor.removeInlineStylesFromRange(selectionStart, selectionEnd)
        editor.removeBlockStylesFromRange(selectionStart, selectionEnd, true)

        val nestingLevel = IAztecNestable.getNestingLevelAt(editableText, selectionStart)

        val span = AztecHorizontalRuleSpan(
                editor.context,
                R.drawable.img_hr,
                nestingLevel,
                editor
        )

        val builder = SpannableStringBuilder(Constants.MAGIC_STRING)
        builder.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        editableText.replace(selectionStart, selectionEnd, builder)

        editor.setSelection(
                if (selectionEnd < EndOfBufferMarkerAdder.safeLength(editor)) {
                    selectionEnd + 1
                } else {
                    selectionEnd
                }
        )
    }

    fun insertVideo(imageURI : String, attributes: Attributes, onVideoTappedListener: OnVideoTappedListener?) {
        val nestingLevel = IAztecNestable.getNestingLevelAt(editableText, selectionStart)
        val span = AztecVideoSpan(editor.context, imageURI, null, nestingLevel, AztecAttributes(attributes), onVideoTappedListener, editor)
        insertMedia(span)
    }

    fun insertImage(imageURI : String, attributes: Attributes, onImageTappedListener: OnImageTappedListener?) {
        val span = AztecImageSpan(editor.context, imageURI, null, AztecAttributes(attributes), onImageTappedListener, editor)
        insertMedia(span)
    }

    private fun insertMedia(span: AztecMediaSpan) {
        val spanBeforeMedia = editableText.getSpans(selectionStart, selectionEnd, IAztecBlockSpan::class.java)
                .firstOrNull {
                    selectionStart == editableText.getSpanEnd(it)
                }

        val spanAfterMedia = editableText.getSpans(selectionStart, selectionEnd, IAztecBlockSpan::class.java)
                .firstOrNull {
                    selectionStart == editableText.getSpanStart(it)
                }

        if (spanAfterMedia != null) {
            editableText.setSpan(spanAfterMedia, selectionStart, editableText.getSpanEnd(spanAfterMedia), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        if (spanBeforeMedia != null) {
            editableText.setSpan(spanBeforeMedia, editableText.getSpanStart(spanBeforeMedia), selectionEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        editor.removeInlineStylesFromRange(selectionStart, selectionEnd)

        val ssb = SpannableStringBuilder(Constants.IMG_STRING)

        ssb.setSpan(
                span,
                0,
                1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        ssb.setSpan(
                AztecMediaClickableSpan(span),
                0,
                1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        editableText.replace(selectionStart, selectionEnd, ssb)

        editor.setSelection(
                if (selectionEnd < EndOfBufferMarkerAdder.safeLength(editor)) selectionEnd + 1 else selectionEnd)
        editor.isMediaAdded = true
    }
}
