package org.wordpress.aztec.spans

import android.graphics.Paint
import android.text.Layout
import android.text.Spanned
import android.text.TextPaint
import android.text.style.LineHeightSpan
import android.text.style.MetricAffectingSpan
import android.text.style.UpdateLayout
import android.util.Log
import org.wordpress.aztec.AlignmentRendering
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecTextFormat
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.formatting.BlockFormatter
import java.util.Locale

fun createHeadingSpan(nestingLevel: Int,
                      tag: String,
                      attributes: AztecAttributes,
                      alignmentRendering: AlignmentRendering,
                      headerStyle: BlockFormatter.HeaderStyles = BlockFormatter.HeaderStyles(0, emptyMap())
): AztecHeadingSpan {
    val textFormat = when (tag.lowercase(Locale.getDefault())) {
        "h1" -> AztecTextFormat.FORMAT_HEADING_1
        "h2" -> AztecTextFormat.FORMAT_HEADING_2
        "h3" -> AztecTextFormat.FORMAT_HEADING_3
        "h4" -> AztecTextFormat.FORMAT_HEADING_4
        "h5" -> AztecTextFormat.FORMAT_HEADING_5
        "h6" -> AztecTextFormat.FORMAT_HEADING_6
        else -> AztecTextFormat.FORMAT_HEADING_1
    }
    return createHeadingSpan(nestingLevel, textFormat, attributes, alignmentRendering, headerStyle)
}

fun createHeadingSpan(nestingLevel: Int,
                      textFormat: ITextFormat,
                      attributes: AztecAttributes,
                      alignmentRendering: AlignmentRendering,
                      headerStyle: BlockFormatter.HeaderStyles = BlockFormatter.HeaderStyles(0, emptyMap())
): AztecHeadingSpan =
        when (alignmentRendering) {
            AlignmentRendering.SPAN_LEVEL -> AztecHeadingSpanAligned(nestingLevel, textFormat, attributes, headerStyle)
            AlignmentRendering.VIEW_LEVEL -> AztecHeadingSpan(nestingLevel, textFormat, attributes, headerStyle)
        }

/**
 * We need to have two classes for handling alignment at either the Span-level (AlignedAztecHeadingSpan)
 * or the View-level (AztecHeadingSpan). IAztecAlignment implements AlignmentSpan, which has a
 * getAlignment method that returns a non-null Layout.Alignment. The Android system checks for
 * AlignmentSpans and, if present, overrides the view's gravity with their value. Having a class
 * that does not implement AlignmentSpan allows the view's gravity to control. These classes should
 * be created using the createHeadingSpan(...) methods.
 */
class AztecHeadingSpanAligned(
        override var nestingLevel: Int,
        textFormat: ITextFormat,
        override var attributes: AztecAttributes,
        override var headerStyle: BlockFormatter.HeaderStyles,
        override var align: Layout.Alignment? = null
) : AztecHeadingSpan(nestingLevel, textFormat, attributes, headerStyle), IAztecAlignmentSpan

open class AztecHeadingSpan(
        override var nestingLevel: Int,
        textFormat: ITextFormat,
        override var attributes: AztecAttributes,
        open var headerStyle: BlockFormatter.HeaderStyles
) : MetricAffectingSpan(), IAztecLineBlockSpan, LineHeightSpan, UpdateLayout {
    override val TAG: String
        get() = heading.tag

    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1

    override var textFormat: ITextFormat = AztecTextFormat.FORMAT_HEADING_1
        set(value) {
            field = value
            heading = textFormatToHeading(value)
        }

    lateinit var heading: Heading

    var previousFontMetrics: Paint.FontMetricsInt? = null
    private var previousHeadingSize: HeadingSize = HeadingSize.Scale(1.0f)
    var previousSpacing: Float? = null

    enum class Heading constructor(internal val scale: Float, internal val tag: String) {
        H1(SCALE_H1, "h1"),
        H2(SCALE_H2, "h2"),
        H3(SCALE_H3, "h3"),
        H4(SCALE_H4, "h4"),
        H5(SCALE_H5, "h5"),
        H6(SCALE_H6, "h6")
    }

    companion object {
        private const val SCALE_H1: Float = 1.73f
        private const val SCALE_H2: Float = 1.32f
        private const val SCALE_H3: Float = 1.02f
        private const val SCALE_H4: Float = 0.87f
        private const val SCALE_H5: Float = 0.72f
        private const val SCALE_H6: Float = 0.60f

        fun textFormatToHeading(textFormat: ITextFormat): Heading {
            return when (textFormat) {
                AztecTextFormat.FORMAT_HEADING_1 -> Heading.H1
                AztecTextFormat.FORMAT_HEADING_2 -> Heading.H2
                AztecTextFormat.FORMAT_HEADING_3 -> Heading.H3
                AztecTextFormat.FORMAT_HEADING_4 -> Heading.H4
                AztecTextFormat.FORMAT_HEADING_5 -> Heading.H5
                AztecTextFormat.FORMAT_HEADING_6 -> Heading.H6
                else -> {
                    Heading.H1
                }
            }
        }
    }

    init {
        this.textFormat = textFormat
    }

    override fun chooseHeight(text: CharSequence, start: Int, end: Int, spanstartv: Int, v: Int, fm: Paint.FontMetricsInt) {
        val spanned = text as Spanned
        val spanStart = spanned.getSpanStart(this)
        val spanEnd = spanned.getSpanEnd(this)

        Log.d("TESTING 101", "chooseHeight is called")
        // save original font metrics
        if (previousFontMetrics == null) {
            Log.d("TESTING 101", "chooseHeight is called, inside previousFontMetrics null check")
            previousFontMetrics = Paint.FontMetricsInt()
            previousFontMetrics!!.top = fm.top
            previousFontMetrics!!.ascent = fm.ascent
            previousFontMetrics!!.bottom = fm.bottom
            previousFontMetrics!!.descent = fm.descent
        }

        previousFontMetrics = null

        var addedTopPadding = false
        var addedBottomPadding = false

        val verticalPadding = headerStyle.verticalPadding

        if (start == spanStart || start < spanStart) {
            fm.ascent -= verticalPadding
            fm.top -= verticalPadding
            addedTopPadding = true
        }
        if (end == spanEnd || spanEnd < end) {
            fm.descent += verticalPadding
            fm.bottom += verticalPadding
            addedBottomPadding = true
        }

        // apply original font metrics to lines that should not have vertical padding
        if (!addedTopPadding) {
            fm.ascent = previousFontMetrics!!.ascent
            fm.top = previousFontMetrics!!.top
            Log.d("TESTING 101", "chooseHeight is called, addedTopPadding")
        }

        if (!addedBottomPadding) {
            fm.descent = previousFontMetrics!!.descent
            fm.bottom = previousFontMetrics!!.bottom
            Log.d("TESTING 101", "chooseHeight is called, addedBottomPadding")
        }
    }

    override fun updateDrawState(textPaint: TextPaint) {
        when (val headingSize = getHeadingSize()) {
            is HeadingSize.Scale -> {
                textPaint.textSize *= heading.scale
                if (textPaint.textSize + getSizeModifier() >= 0) {
                    textPaint.textSize += getSizeModifier()
                } else {
                    textPaint.textSize = 0f
                }
            }
            is HeadingSize.Size -> {
                textPaint.textSize = headingSize.value.toFloat()
            }
        }
        textPaint.isFakeBoldText = true
        getHeadingColor()?.let {
            textPaint.color = it
        }
    }

    override fun updateMeasureState(paint: TextPaint) {
        val headingSize = getHeadingSize()
        // when font size changes - reset cached font metrics to reapply vertical padding
        /* if (headingSize != previousHeadingSize || previousSpacing != paint.fontSpacing) {
            previousFontMetrics = null
        }
        */
        previousFontMetrics = null
        Log.d("TESTING 101", "updateMeasureState is called and previousFontMetrics set to null")
        previousHeadingSize = headingSize
        previousSpacing = paint.fontSpacing
        when (headingSize) {
            is HeadingSize.Scale -> {
                paint.textSize *= heading.scale
                if (paint.textSize + getSizeModifier() >= 0) {
                    paint.textSize += getSizeModifier()
                } else {
                    paint.textSize = 0f
                }
            }
            is HeadingSize.Size -> {
                paint.textSize = headingSize.value.toFloat()
            }
        }
        getHeadingColor()?.let {
            paint.color = it
        }
    }

    private fun getHeadingSize(): HeadingSize {
        return headerStyle.styles[heading]?.fontSize?.takeIf { it > 0 }?.let { HeadingSize.Size(it + getSizeModifier()) }
                ?: HeadingSize.Scale(heading.scale)
    }

    private fun getSizeModifier(): Int {
        return headerStyle.styles[heading]?.fontSizeModifier ?: 0
    }

    private fun getHeadingColor(): Int? {
        return headerStyle.styles[heading]?.fontColor?.takeIf { it != 0 }
    }

    sealed class HeadingSize {
        data class Scale(val value: Float) : HeadingSize()
        data class Size(val value: Int) : HeadingSize()
    }

    override fun toString() = "AztecHeadingSpan : $TAG"
}
