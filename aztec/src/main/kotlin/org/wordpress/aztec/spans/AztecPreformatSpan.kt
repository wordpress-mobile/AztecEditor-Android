package org.wordpress.aztec.spans

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import android.text.style.LineBackgroundSpan
import android.text.style.LineHeightSpan
import android.text.style.TypefaceSpan
import org.wordpress.aztec.AlignmentRendering
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.formatting.BlockFormatter

fun createPreformatSpan(
        nestingLevel: Int,
        alignmentRendering: AlignmentRendering,
        attributes: AztecAttributes = AztecAttributes(),
        preformatStyle: BlockFormatter.PreformatStyle = BlockFormatter.PreformatStyle(0, 0f, 0, 0)
) : AztecPreformatSpan =
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
        TypefaceSpan("monospace")
    {
    override val TAG: String = "pre"

    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1

    val rect = Rect()

    private val MARGIN = 16

    override fun chooseHeight(text: CharSequence, start: Int, end: Int, spanstartv: Int, v: Int, fm: Paint.FontMetricsInt) {
        val spanned = text as Spanned
        val spanStart = spanned.getSpanStart(this)
        val spanEnd = spanned.getSpanEnd(this)

        if (start == spanStart || start < spanStart) {
            fm.ascent -= preformatStyle.verticalPadding
            fm.top -= preformatStyle.verticalPadding
        }

        if (end == spanEnd || spanEnd < end) {
            fm.descent += preformatStyle.verticalPadding
            fm.bottom += preformatStyle.verticalPadding
        }
    }

    override fun drawBackground(canvas: Canvas, paint: Paint, left: Int, right: Int, top: Int, baseline: Int, bottom: Int, text: CharSequence?, start: Int, end: Int, lnum: Int) {
        val color = paint.color
        val alpha: Int = (preformatStyle.preformatBackgroundAlpha * 255).toInt()
        paint.color = Color.argb(
                alpha,
                Color.red(preformatStyle.preformatBackground),
                Color.green(preformatStyle.preformatBackground),
                Color.blue(preformatStyle.preformatBackground)
        )
        rect.set(left, top, right, bottom)
        canvas.drawRect(rect, paint)
        paint.color = color
    }

    override fun drawLeadingMargin(canvas: Canvas, paint: Paint, x: Int, dir: Int, top: Int, baseline: Int, bottom: Int, text: CharSequence, start: Int, end: Int, first: Boolean, layout: Layout) {
        val style = paint.style
        val color = paint.color

        paint.style = Paint.Style.FILL
        paint.color = preformatStyle.preformatColor

        canvas.drawRect(x.toFloat() + MARGIN, top.toFloat(), (x + MARGIN).toFloat(), bottom.toFloat(), paint)

        paint.style = style
        paint.color = color
    }

    override fun getLeadingMargin(first: Boolean): Int {
        return MARGIN
    }
}
