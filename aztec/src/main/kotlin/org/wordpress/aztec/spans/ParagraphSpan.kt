package org.wordpress.aztec.spans

import android.graphics.Paint
import android.text.Layout
import android.text.Spanned
import android.text.style.LineHeightSpan
import org.wordpress.aztec.AlignmentRendering
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecTextFormat
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.formatting.BlockFormatter

fun createParagraphSpan(nestingLevel: Int,
                        alignmentRendering: AlignmentRendering,
                        attributes: AztecAttributes = AztecAttributes(),
                        paragraphStyle: BlockFormatter.ParagraphStyle = BlockFormatter.ParagraphStyle(0)): IAztecBlockSpan =
        when (alignmentRendering) {
            AlignmentRendering.SPAN_LEVEL -> ParagraphSpanAligned(nestingLevel, attributes, null, paragraphStyle)
            AlignmentRendering.VIEW_LEVEL -> ParagraphSpan(nestingLevel, attributes, paragraphStyle)
        }

fun createParagraphSpan(nestingLevel: Int,
                        align: Layout.Alignment?,
                        attributes: AztecAttributes = AztecAttributes(),
                        paragraphStyle: BlockFormatter.ParagraphStyle = BlockFormatter.ParagraphStyle(0)): IAztecBlockSpan =
        ParagraphSpanAligned(nestingLevel, attributes, align, paragraphStyle)

/**
 * We need to have two classes for handling alignment at either the Span-level (ParagraphSpanAligned)
 * or the View-level (ParagraphSpan). IAztecAlignment implements AlignmentSpan, which has a
 * getAlignment method that returns a non-null Layout.Alignment. The Android system checks for
 * AlignmentSpans and, if present, overrides the view's gravity with their value. Having a class
 * that does not implement AlignmentSpan allows the view's gravity to control. These classes should
 * be created using the createParagraphSpan(...) methods.
 */
open class ParagraphSpan(
        override var nestingLevel: Int,
        override var attributes: AztecAttributes,
        var paragraphStyle: BlockFormatter.ParagraphStyle = BlockFormatter.ParagraphStyle(0))
    : IAztecBlockSpan, LineHeightSpan {

    private var removeTopPadding = false

    override fun chooseHeight(text: CharSequence, start: Int, end: Int, spanstartv: Int, lineHeight: Int, fm: Paint.FontMetricsInt) {
        val spanned = text as Spanned
        val spanStart = spanned.getSpanStart(this)
        val spanEnd = spanned.getSpanEnd(this)
        val previousLineBreak = if (start > 1) {
            text.substring(start-1, start) == "\n"
        } else {
            false
        }
        val followingLineBreak = if (end < text.length) {
            text.substring(end, end + 1) == "\n"
        } else {
            false
        }
        val isFirstLine = start <= spanStart || previousLineBreak
        val isLastLine = spanEnd <= end || followingLineBreak
        if (isFirstLine) {
            removeTopPadding = true
            fm.ascent -= paragraphStyle.verticalMargin
            fm.top -= paragraphStyle.verticalMargin
        }
        if (isLastLine) {
            fm.descent += paragraphStyle.verticalMargin
            fm.bottom += paragraphStyle.verticalMargin
            removeTopPadding = false
        }
        if (!isFirstLine && !isLastLine && removeTopPadding) {
            removeTopPadding = false
            if (fm.ascent + paragraphStyle.verticalMargin < 0) {
                fm.ascent += paragraphStyle.verticalMargin
            }
            if (fm.top + paragraphStyle.verticalMargin < 0) {
                fm.top += paragraphStyle.verticalMargin
            }
        }
    }

    override var TAG: String = "p"

    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1
    override val textFormat: ITextFormat = AztecTextFormat.FORMAT_PARAGRAPH
}

class ParagraphSpanAligned(
        nestingLevel: Int,
        attributes: AztecAttributes,
        override var align: Layout.Alignment?,
        paragraphStyle: BlockFormatter.ParagraphStyle) : ParagraphSpan(nestingLevel, attributes, paragraphStyle), IAztecAlignmentSpan
