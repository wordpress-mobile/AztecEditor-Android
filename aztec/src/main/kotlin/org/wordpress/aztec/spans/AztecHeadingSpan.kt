package org.wordpress.aztec.spans

import android.graphics.Paint
import android.text.Spanned
import android.text.TextPaint
import android.text.style.LineHeightSpan
import android.text.style.MetricAffectingSpan
import android.text.style.UpdateLayout
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.TextFormat
import org.wordpress.aztec.formatting.BlockFormatter

class AztecHeadingSpan @JvmOverloads constructor(
        override var nestingLevel: Int,
        textFormat: TextFormat,
        override var attributes: AztecAttributes,
        var headerStyle: BlockFormatter.HeaderStyle = BlockFormatter.HeaderStyle(0)
    ) : MetricAffectingSpan(), AztecBlockSpan, LineHeightSpan, UpdateLayout {

    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1

    var textFormat: TextFormat = TextFormat.FORMAT_HEADING_1
        get() = field
        set(value) {
            field = value
            heading = textFormatToHeading(value)
        }

    lateinit var heading: Heading

    var previousFontMetrics: Paint.FontMetricsInt? = null
    var previousTextScale: Float = 1.0f

    companion object {
        private val SCALE_H1: Float = 1.73f
        private val SCALE_H2: Float = 1.32f
        private val SCALE_H3: Float = 1.02f
        private val SCALE_H4: Float = 0.87f
        private val SCALE_H5: Float = 0.72f
        private val SCALE_H6: Float = 0.60f

        fun getTextFormat(tag: String): TextFormat {
            when (tag.toLowerCase()) {
                "h1" -> return TextFormat.FORMAT_HEADING_1
                "h2" -> return TextFormat.FORMAT_HEADING_2
                "h3" -> return TextFormat.FORMAT_HEADING_3
                "h4" -> return TextFormat.FORMAT_HEADING_4
                "h5" -> return TextFormat.FORMAT_HEADING_5
                "h6" -> return TextFormat.FORMAT_HEADING_6
                else -> return TextFormat.FORMAT_HEADING_1
            }
        }

        fun textFormatToHeading(textFormat: TextFormat): Heading {
            when (textFormat) {
                TextFormat.FORMAT_HEADING_1 -> return AztecHeadingSpan.Heading.H1
                TextFormat.FORMAT_HEADING_2 -> return AztecHeadingSpan.Heading.H2
                TextFormat.FORMAT_HEADING_3 -> return AztecHeadingSpan.Heading.H3
                TextFormat.FORMAT_HEADING_4 -> return AztecHeadingSpan.Heading.H4
                TextFormat.FORMAT_HEADING_5 -> return AztecHeadingSpan.Heading.H5
                TextFormat.FORMAT_HEADING_6 -> return AztecHeadingSpan.Heading.H6
                else -> { return AztecHeadingSpan.Heading.H1 }
            }
        }
    }

    init {
        this.textFormat = textFormat
    }

    constructor(nestingLevel: Int, tag: String, attrs: AztecAttributes = AztecAttributes(),
            headerStyle: BlockFormatter.HeaderStyle = BlockFormatter.HeaderStyle(0))
            : this(nestingLevel, getTextFormat(tag), attrs, headerStyle)

    override fun chooseHeight(text: CharSequence, start: Int, end: Int, spanstartv: Int, v: Int, fm: Paint.FontMetricsInt) {
        val spanned = text as Spanned
        val spanStart = spanned.getSpanStart(this)
        val spanEnd = spanned.getSpanEnd(this)

        //save original font metrics
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

        //apply original font metrics to lines that should not have vertical padding
        if (!addedTopPadding) {
            fm.ascent = previousFontMetrics!!.ascent
            fm.top = previousFontMetrics!!.top
        }

        if (!addedBottomPadding) {
            fm.descent = previousFontMetrics!!.descent
            fm.bottom = previousFontMetrics!!.bottom
        }

    }

    enum class Heading constructor(internal val scale: Float) {
        H1(SCALE_H1),
        H2(SCALE_H2),
        H3(SCALE_H3),
        H4(SCALE_H4),
        H5(SCALE_H5),
        H6(SCALE_H6)
    }

    override fun getStartTag(): String {
        if (attributes.isEmpty()) {
            return getTag()
        }
        return getTag() + " " + attributes
    }

    override fun getEndTag(): String {
        return getTag()
    }

    override fun updateDrawState(textPaint: TextPaint) {
        textPaint.textSize *= heading.scale
        textPaint.isFakeBoldText = true
    }

    override fun updateMeasureState(textPaint: TextPaint) {
        //when font size changes - reset cached font metrics to reapply vertical padding
        if (previousTextScale != heading.scale) {
            previousFontMetrics = null
        }
        previousTextScale = heading.scale

        textPaint.textSize *= heading.scale
    }

    private fun getTag(): String {
        when (heading.scale) {
            SCALE_H1 -> return "h1"
            SCALE_H2 -> return "h2"
            SCALE_H3 -> return "h3"
            SCALE_H4 -> return "h4"
            SCALE_H5 -> return "h5"
            SCALE_H6 -> return "h6"
            else -> return "h1"
        }
    }
}
