package org.wordpress.aztec.spans

import android.graphics.Paint
import android.text.Layout
import android.text.Spanned
import android.text.TextPaint
import android.text.style.LineHeightSpan
import android.text.style.MetricAffectingSpan
import android.text.style.UpdateLayout
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecTextFormat
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.formatting.BlockFormatter

class AztecHeadingSpan @JvmOverloads constructor(
        override var nestingLevel: Int,
        textFormat: ITextFormat,
        override var attributes: AztecAttributes,
        var headerStyle: BlockFormatter.HeaderStyle = BlockFormatter.HeaderStyle(0),
        override var align: Layout.Alignment? = null
    ) : MetricAffectingSpan(), IAztecLineBlockSpan, LineHeightSpan, UpdateLayout {
    override val TAG: String
        get() = heading.tag

    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1

    var textFormat: ITextFormat = AztecTextFormat.FORMAT_HEADING_1
        set(value) {
            field = value
            heading = textFormatToHeading(value)
        }

    lateinit var heading: Heading

    var previousFontMetrics: Paint.FontMetricsInt? = null
    var previousTextScale: Float = 1.0f

    enum class Heading constructor(internal val scale: Float, internal val tag: String) {
        H1(SCALE_H1, "h1"),
        H2(SCALE_H2, "h2"),
        H3(SCALE_H3, "h3"),
        H4(SCALE_H4, "h4"),
        H5(SCALE_H5, "h5"),
        H6(SCALE_H6, "h6")
    }

    companion object {
        private val SCALE_H1: Float = 1.73f
        private val SCALE_H2: Float = 1.32f
        private val SCALE_H3: Float = 1.02f
        private val SCALE_H4: Float = 0.87f
        private val SCALE_H5: Float = 0.72f
        private val SCALE_H6: Float = 0.60f

        fun tagToTextFormat(tag: String): ITextFormat {
            when (tag.toLowerCase()) {
                "h1" -> return AztecTextFormat.FORMAT_HEADING_1
                "h2" -> return AztecTextFormat.FORMAT_HEADING_2
                "h3" -> return AztecTextFormat.FORMAT_HEADING_3
                "h4" -> return AztecTextFormat.FORMAT_HEADING_4
                "h5" -> return AztecTextFormat.FORMAT_HEADING_5
                "h6" -> return AztecTextFormat.FORMAT_HEADING_6
                else -> return AztecTextFormat.FORMAT_HEADING_1
            }
        }

        fun textFormatToHeading(textFormat: ITextFormat): Heading {
            when (textFormat) {
                AztecTextFormat.FORMAT_HEADING_1 -> return AztecHeadingSpan.Heading.H1
                AztecTextFormat.FORMAT_HEADING_2 -> return AztecHeadingSpan.Heading.H2
                AztecTextFormat.FORMAT_HEADING_3 -> return AztecHeadingSpan.Heading.H3
                AztecTextFormat.FORMAT_HEADING_4 -> return AztecHeadingSpan.Heading.H4
                AztecTextFormat.FORMAT_HEADING_5 -> return AztecHeadingSpan.Heading.H5
                AztecTextFormat.FORMAT_HEADING_6 -> return AztecHeadingSpan.Heading.H6
                else -> { return AztecHeadingSpan.Heading.H1 }
            }
        }
    }

    init {
        this.textFormat = textFormat
    }

    constructor(nestingLevel: Int, tag: String, attrs: AztecAttributes = AztecAttributes(),
            headerStyle: BlockFormatter.HeaderStyle = BlockFormatter.HeaderStyle(0))
            : this(nestingLevel, tagToTextFormat(tag), attrs, headerStyle)

    override fun chooseHeight(text: CharSequence, start: Int, end: Int, spanstartv: Int, v: Int, fm: Paint.FontMetricsInt) {
        val spanned = text as Spanned
        val spanStart = spanned.getSpanStart(this)
        val spanEnd = spanned.getSpanEnd(this)

        // save original font metrics
        if (previousFontMetrics == null) {
            previousFontMetrics = Paint.FontMetricsInt()
            previousFontMetrics!!.top = fm.top
            previousFontMetrics!!.ascent = fm.ascent
            previousFontMetrics!!.bottom = fm.bottom
            previousFontMetrics!!.descent = fm.descent
        }

        var addedTopPadding = false
        var addedBottomPadding = false

        if (start == spanStart || start < spanStart) {
            fm.ascent -= headerStyle.verticalPadding
            fm.top -= headerStyle.verticalPadding
            addedTopPadding = true
        }
        if (end == spanEnd || spanEnd < end) {
            fm.descent += headerStyle.verticalPadding
            fm.bottom += headerStyle.verticalPadding
            addedBottomPadding = true
        }

        // apply original font metrics to lines that should not have vertical padding
        if (!addedTopPadding) {
            fm.ascent = previousFontMetrics!!.ascent
            fm.top = previousFontMetrics!!.top
        }

        if (!addedBottomPadding) {
            fm.descent = previousFontMetrics!!.descent
            fm.bottom = previousFontMetrics!!.bottom
        }
    }

    override fun updateDrawState(textPaint: TextPaint) {
        textPaint.textSize *= heading.scale
        textPaint.isFakeBoldText = true
    }

    override fun updateMeasureState(textPaint: TextPaint) {
        // when font size changes - reset cached font metrics to reapply vertical padding
        if (previousTextScale != heading.scale) {
            previousFontMetrics = null
        }
        previousTextScale = heading.scale

        textPaint.textSize *= heading.scale
    }

    override fun toString() = "AztecHeadingSpan : $TAG"
}
