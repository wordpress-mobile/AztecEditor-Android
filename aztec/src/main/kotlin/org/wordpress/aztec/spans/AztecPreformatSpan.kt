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
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.formatting.BlockFormatter

class AztecPreformatSpan(
        override var nestingLevel: Int,
        override var attributes: AztecAttributes = AztecAttributes(),
        var preformatStyle: BlockFormatter.PreformatStyle = BlockFormatter.PreformatStyle(0, 0f, 0, 0),
        override var align: Layout.Alignment? = null
    ) : IAztecBlockSpan, LeadingMarginSpan, LineBackgroundSpan, LineHeightSpan, TypefaceSpan("monospace") {
    override val TAG: String = "pre"

    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1

    val rect = Rect()

    private val MARGIN = 16

    override fun chooseHeight(text: CharSequence, start: Int, end: Int, spanstartv: Int, v: Int, fm: Paint.FontMetricsInt) {
        val spanned = text as Spanned
        val spanStart = spanned.getSpanStart(this)
        val spanEnd = spanned.getSpanEnd(this)

        // Edge lines are made longer during the drawing phase
        val topDelta = getTopMarginDelta(text, start)
        if (start == spanStart || start < spanStart) {
            fm.ascent -= (preformatStyle.verticalPadding + topDelta)
            fm.top -= (preformatStyle.verticalPadding + topDelta)
        }

        val bottomDelta = getBottomMarginDelta(text, end)
        if (end == spanEnd || spanEnd < end) {
            fm.descent += (preformatStyle.verticalPadding + bottomDelta)
            fm.bottom += (preformatStyle.verticalPadding + bottomDelta)
        }
    }

    private fun getTopMarginDelta(text: CharSequence?, start: Int) : Int {
        if (text == null) return 0
        val spanned = text as Spanned
        val spanStart = spanned.getSpanStart(this)
        if (start == spanStart || start < spanStart) {
            return 10
        }
        return 0
    }

    private fun getBottomMarginDelta(text: CharSequence?, end: Int) : Int {
        if (text == null) return 0
        val spanned = text as Spanned
        val spanEnd = spanned.getSpanEnd(this)
        if (end == spanEnd || spanEnd < end) {
            return 10
        }
        return 0
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
        rect.set(left, top + getTopMarginDelta(text, start), right, bottom - getBottomMarginDelta(text, end))
        canvas.drawRect(rect, paint)
        paint.color = color
    }

    override fun drawLeadingMargin(canvas: Canvas, paint: Paint, x: Int, dir: Int, top: Int, baseline: Int, bottom: Int, text: CharSequence, start: Int, end: Int, first: Boolean, layout: Layout) {
        val style = paint.style
        val color = paint.color

        paint.style = Paint.Style.FILL
        paint.color = preformatStyle.preformatColor

        canvas.drawRect(x.toFloat() + MARGIN, top.toFloat() + + getTopMarginDelta(text, start),
                (x + MARGIN).toFloat(), bottom.toFloat() - getBottomMarginDelta(text, end), paint)

        paint.style = style
        paint.color = color
    }

    override fun getLeadingMargin(first: Boolean): Int {
        return MARGIN
    }
}
