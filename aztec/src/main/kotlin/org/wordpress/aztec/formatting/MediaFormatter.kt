package org.wordpress.aztec.formatting

import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import androidx.appcompat.content.res.AppCompatResources
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import org.wordpress.aztec.R
import org.wordpress.aztec.spans.AztecHorizontalRuleSpan
import org.wordpress.aztec.spans.AztecImageSpan
import org.wordpress.aztec.spans.AztecMediaClickableSpan
import org.wordpress.aztec.spans.AztecMediaSpan
import org.wordpress.aztec.spans.AztecVideoSpan
import org.wordpress.aztec.spans.IAztecBlockSpan
import org.wordpress.aztec.spans.IAztecNestable
import org.wordpress.aztec.watchers.EndOfBufferMarkerAdder
import org.xml.sax.Attributes

/**
 * This class contains methods to add media and horizontal rule to the editor.
 */
class MediaFormatter(editor: AztecText) : AztecFormatter(editor) {
    fun applyHorizontalRule(inline: Boolean) {
        val nestingLevel = if (inline) {
            editor.removeInlineStylesFromRange(selectionStart, selectionEnd)
            editor.removeBlockStylesFromRange(selectionStart, selectionEnd, true)
            IAztecNestable.getNestingLevelAt(editableText, selectionStart)
        } else {
            0
        }

        val span = AztecHorizontalRuleSpan(
                editor.context,
                AppCompatResources.getDrawable(editor.context, R.drawable.img_hr)!!,
                nestingLevel,
                AztecAttributes(),
                editor
        )

        val builder = SpannableStringBuilder(Constants.MAGIC_STRING)
        builder.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        if (inline) {
            editableText.replace(selectionStart, selectionEnd, builder)
            val newSelectionPosition = editableText.indexOf(Constants.MAGIC_CHAR, selectionStart) + 1
            editor.setSelection(newSelectionPosition)
        } else {
            builder.append("\n")
            insertSpanAfterBlock(builder)
        }
    }

    fun insertVideo(inline: Boolean, drawable: Drawable?, attributes: Attributes, onVideoTappedListener: AztecText.OnVideoTappedListener?,
                    onMediaDeletedListener: AztecText.OnMediaDeletedListener?) {
        val nestingLevel = if (inline) IAztecNestable.getNestingLevelAt(editableText, selectionStart) else 0
        val span = AztecVideoSpan(editor.context, drawable, nestingLevel, AztecAttributes(attributes), onVideoTappedListener,
                onMediaDeletedListener, editor)
        if (inline) {
            insertMediaInline(span)
        } else {
            insertMediaAfterBlock(span)
        }
    }

    fun insertImage(inline: Boolean, drawable: Drawable?, attributes: Attributes, onImageTappedListener: AztecText.OnImageTappedListener?,
                    onMediaDeletedListener: AztecText.OnMediaDeletedListener?) {
        val nestingLevel = if (inline) IAztecNestable.getNestingLevelAt(editableText, selectionStart) else 0
        val span = AztecImageSpan(editor.context, drawable, nestingLevel, AztecAttributes(attributes), onImageTappedListener,
                onMediaDeletedListener, editor)
        if (inline) {
            insertMediaInline(span)
        } else {
            insertMediaAfterBlock(span)
        }
    }

    private fun insertMediaInline(span: AztecMediaSpan) {
        editor.removeInlineStylesFromRange(selectionStart, selectionEnd)

        val ssb = SpannableStringBuilder(Constants.IMG_STRING)

        buildClickableMediaSpan(ssb, span)

        // We need to be sure the cursor is placed correctly after media insertion
        // Note that media has '\n' around them when needed
        val isLastItem = selectionEnd == EndOfBufferMarkerAdder.safeLength(editor)
        editableText.replace(selectionStart, selectionEnd, ssb)

        setSelection(isLastItem, selectionEnd)
    }

    private fun insertMediaAfterBlock(span: AztecMediaSpan) {
        val ssb = SpannableStringBuilder(Constants.IMG_STRING)
        ssb.append("\n")
        buildClickableMediaSpan(ssb, span)
        insertSpanAfterBlock(ssb)
    }

    private fun insertSpanAfterBlock(ssb: SpannableStringBuilder) {
        val position = getEndOfBlock()
        // We need to be sure the cursor is placed correctly after media insertion
        // Note that media has '\n' around them when needed
        val isLastItem = position == EndOfBufferMarkerAdder.safeLength(editor)
        val insertedLength = ssb.length
        editableText.insert(position, ssb)
        val spans = editableText.getSpans(position, position + insertedLength, IAztecBlockSpan::class.java).filter {
            it !is AztecMediaSpan && editableText.getSpanStart(it) == position
        }
        spans.forEach {
            val spanStart = editableText.getSpanStart(it)
            val spanEnd = editableText.getSpanEnd(it)
            val spanFlags = editableText.getSpanFlags(it)
            editableText.removeSpan(it)
            if (spanStart + insertedLength < spanEnd) {
                editableText.setSpan(it, spanStart + insertedLength, spanEnd, spanFlags)
            }
        }
        setSelection(isLastItem, position)
    }

    private fun setSelection(isLastItem: Boolean, position: Int) {
        val newSelection = if (isLastItem) {
            EndOfBufferMarkerAdder.safeLength(editor)
        } else {
            if (position < EndOfBufferMarkerAdder.safeLength(editor)) position + 1 else position
        }
        editor.setSelection(newSelection)
        editor.isMediaAdded = true
    }

    private fun buildClickableMediaSpan(ssb: SpannableStringBuilder, span: AztecMediaSpan) {
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
    }

    private fun getEndOfBlock(): Int {
        var position = 0
        editableText.getSpans(selectionStart, selectionEnd, IAztecBlockSpan::class.java).forEach {
            val spanEnd = editableText.getSpanEnd(it)
            if (spanEnd > position) {
                position = spanEnd
            }
        }
        return position
    }
}

