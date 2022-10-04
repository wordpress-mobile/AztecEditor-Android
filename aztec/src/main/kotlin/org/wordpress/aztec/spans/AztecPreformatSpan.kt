package org.wordpress.aztec.spans

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.text.Layout
import android.text.Spanned
import android.text.TextPaint
import android.text.style.LeadingMarginSpan
import android.text.style.LineBackgroundSpan
import android.text.style.LineHeightSpan
import android.text.style.TypefaceSpan
import org.wordpress.aztec.AlignmentRendering
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecTextFormat
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.formatting.BlockFormatter

fun createPreformatSpan(
        nestingLevel: Int,
        alignmentRendering: AlignmentRendering,
        attributes: AztecAttributes = AztecAttributes(),
        preformatStyle: BlockFormatter.PreformatStyle = BlockFormatter.PreformatStyle(0, 0f, 0, 0, 0, 0, 0, 0, 0)
): AztecPreformatSpan =
        when (alignmentRendering) {
            AlignmentRendering.SPAN_LEVEL -> AztecPreformatSpanAligned(nestingLevel, attributes, preformatStyle)
            AlignmentRendering.VIEW_LEVEL -> AztecPreformatSpan(nestingLevel, attributes, preformatStyle)
        }

/**
 * We need to have two classes for handling alignment at either the Span-level (AztecPreformatSpanAligned)
 * or the View-level (AztecPreformatSpan). IAztecAlignment implements AlignmentSpan, which has a
 * getAlignment method that returns a non-null Layout.Alignment. The Android system checks for
 * AlignmentSpans and, if present, overrides the view's gravity with their value. Having a class
 * that does not implement AlignmentSpan allows the view's gravity to control. These classes should
 * be created using the createPreformatSpan(...) method.
 */
class AztecPreformatSpanAligned(
        override var nestingLevel: Int,
        override var attributes: AztecAttributes,
        override var preformatStyle: BlockFormatter.PreformatStyle,
        override var align: Layout.Alignment? = null
) : AztecPreformatSpan(nestingLevel, attributes, preformatStyle), IAztecAlignmentSpan

open class AztecPreformatSpan(
        override var nestingLevel: Int,
        override var attributes: AztecAttributes,
        open var preformatStyle: BlockFormatter.PreformatStyle
) : IAztecBlockSpan,
        LeadingMarginSpan,
        LineBackgroundSpan,
        LineHeightSpan,
        TypefaceSpan("monospace") {
    override val TAG: String = "pre"

    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1

    private var originalAscent: Int = 0
    private var originalTop: Int = 0
    private var originalDescent: Int = 0
    private var originalBottom: Int = 0

    // this method adds extra padding to the top and bottom lines of the text while removing it from middle lines
    override fun chooseHeight(text: CharSequence, start: Int, end: Int, spanstartv: Int, v: Int,
                              fm: Paint.FontMetricsInt) {
        val spanned = text as Spanned
        val spanStart = spanned.getSpanStart(this)
        val spanEnd = spanned.getSpanEnd(this)
        val isFirstLine = start <= spanStart
        val isLastLine = spanEnd <= end

        if (isFirstLine) {
            originalAscent = fm.ascent
            originalTop = fm.top
            originalDescent = fm.descent
            originalBottom = fm.bottom

            fm.ascent -= preformatStyle.verticalPadding
            fm.top -= preformatStyle.verticalPadding

            if (!isLastLine) {
                fm.descent = originalDescent
                fm.bottom = originalBottom
            }
        }
        if (isLastLine) {
            fm.descent += preformatStyle.verticalPadding
            fm.bottom += preformatStyle.verticalPadding

            if (!isFirstLine) {
                fm.ascent = originalAscent
                fm.top = originalTop
            }
        }

        if (!isFirstLine && !isLastLine) {
            fm.ascent = originalAscent
            fm.top = originalTop
            fm.descent = originalDescent
            fm.bottom = originalBottom
        }
    }

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.textSize = preformatStyle.preformatTextSize.toFloat()
    }

    override fun updateMeasureState(paint: TextPaint) {
        super.updateMeasureState(paint)
        paint.textSize = preformatStyle.preformatTextSize.toFloat()
    }

    private val strokePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private val fillPaint = Paint().apply {
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }

    override fun drawBackground(canvas: Canvas, paint: Paint, left: Int, right: Int, top: Int, baseline: Int,
                                bottom: Int, text: CharSequence?, start: Int, end: Int, lnum: Int) {
        val spanned = text as Spanned
        val spanStart = spanned.getSpanStart(this)
        val spanEnd = spanned.getSpanEnd(this)

        val isFirstLine = spanStart == start
        val isLastLine = spanEnd == end

        val alpha: Int = (preformatStyle.preformatBackgroundAlpha * 255).toInt()
        fillPaint.color = Color.argb(
                alpha,
                Color.red(preformatStyle.preformatBackground),
                Color.green(preformatStyle.preformatBackground),
                Color.blue(preformatStyle.preformatBackground)
        )

        fillPaint.pathEffect = CornerPathEffect(preformatStyle.preformatBorderRadius.toFloat())
        strokePaint.pathEffect = CornerPathEffect(preformatStyle.preformatBorderRadius.toFloat())

        strokePaint.color = preformatStyle.preformatBorderColor
        strokePaint.strokeWidth = preformatStyle.preformatBorderThickness.toFloat()

        val fillPath = Path().apply {
            if (isFirstLine) {
                moveTo(left.toFloat(), bottom.toFloat())
                lineTo(left.toFloat(), top.toFloat())
                lineTo(right.toFloat(), top.toFloat())
                lineTo(right.toFloat(), bottom.toFloat())
            } else if (isLastLine) {
                moveTo(left.toFloat(), top.toFloat())
                lineTo(left.toFloat(), bottom.toFloat())
                lineTo(right.toFloat(), bottom.toFloat())
                lineTo(right.toFloat(), top.toFloat())
            } else {
                fillPaint.pathEffect = null
                moveTo(left.toFloat(), top.toFloat())
                lineTo(right.toFloat(), top.toFloat())
                lineTo(right.toFloat(), bottom.toFloat())
                lineTo(left.toFloat(), bottom.toFloat())
                lineTo(left.toFloat(), top.toFloat())

            }
        }

        canvas.drawPath(fillPath, fillPaint)

        val borderPath = Path().apply {
            if (isFirstLine) {
                moveTo(left.toFloat(), bottom.toFloat())
                lineTo(left.toFloat(), top.toFloat())
                lineTo(right.toFloat(), top.toFloat())
                lineTo(right.toFloat(), bottom.toFloat())
                if (isLastLine) {
                    lineTo(left.toFloat(), bottom.toFloat())
                }
            } else if (isLastLine) {
                moveTo(left.toFloat(), top.toFloat())
                lineTo(left.toFloat(), bottom.toFloat())
                lineTo(right.toFloat(), bottom.toFloat())
                lineTo(right.toFloat(), top.toFloat())
            } else {
                moveTo(left.toFloat(), top.toFloat())
                lineTo(left.toFloat(), bottom.toFloat())
                moveTo(right.toFloat(), top.toFloat())
                lineTo(right.toFloat(), bottom.toFloat())
            }
        }

        canvas.drawPath(borderPath, strokePaint)
    }

    override fun drawLeadingMargin(canvas: Canvas, paint: Paint, x: Int, dir: Int, top: Int, baseline: Int,
                                   bottom: Int, text: CharSequence, start: Int, end: Int, first: Boolean,
                                   layout: Layout) = Unit

    override fun getLeadingMargin(first: Boolean): Int {
        return preformatStyle.leadingMargin
    }

    override val textFormat: ITextFormat = AztecTextFormat.FORMAT_PREFORMAT
}
