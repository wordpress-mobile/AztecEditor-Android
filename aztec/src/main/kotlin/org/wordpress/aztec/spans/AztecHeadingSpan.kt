package org.wordpress.aztec.spans

import android.text.TextPaint
import android.text.TextUtils
import android.text.style.MetricAffectingSpan
import org.wordpress.aztec.TextFormat

class AztecHeadingSpan @JvmOverloads constructor(var textFormat: TextFormat, attrs: String? = null) : MetricAffectingSpan(), AztecLineBlockSpan, AztecContentSpan, AztecInlineSpan {

    lateinit var heading: Heading
    override var attributes: String? = attrs


    companion object {
        private val SCALE_H1: Float = 2.0f
        private val SCALE_H2: Float = 1.8f
        private val SCALE_H3: Float = 1.6f
        private val SCALE_H4: Float = 1.4f
        private val SCALE_H5: Float = 1.2f
        private val SCALE_H6: Float = 1.0f


        fun getTextFormat(tag: String): TextFormat {
            when (tag.toLowerCase()) {
                "h1" -> return TextFormat.FORMAT_HEADING_1
                "h2" -> return TextFormat.FORMAT_HEADING_2
                "h3" -> return TextFormat.FORMAT_HEADING_3
                "h4" -> return TextFormat.FORMAT_HEADING_4
                "h5" -> return TextFormat.FORMAT_HEADING_5
                "h6" -> return TextFormat.FORMAT_HEADING_6
                else -> return TextFormat.FORMAT_PARAGRAPH
            }
        }
    }

    constructor(tag: String, attrs: String? = null) : this(getTextFormat(tag), attrs) {

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
        if (TextUtils.isEmpty(attributes)) {
            return getTag()
        }
        return getTag() + attributes
    }

    override fun getEndTag(): String {
        return getTag()
    }

    override fun updateDrawState(textPaint: TextPaint) {
        textPaint.textSize *= heading.scale
        textPaint.isFakeBoldText = true
    }

    override fun updateMeasureState(textPaint: TextPaint) {
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
            else -> return "p"
        }
    }

    init {
        when (textFormat) {
            TextFormat.FORMAT_HEADING_1 ->
                heading = AztecHeadingSpan.Heading.H1
            TextFormat.FORMAT_HEADING_2 ->
                heading = AztecHeadingSpan.Heading.H2
            TextFormat.FORMAT_HEADING_3 ->
                heading = AztecHeadingSpan.Heading.H3
            TextFormat.FORMAT_HEADING_4 ->
                heading = AztecHeadingSpan.Heading.H4
            TextFormat.FORMAT_HEADING_5 ->
                heading = AztecHeadingSpan.Heading.H5
            TextFormat.FORMAT_HEADING_6 ->
                heading = AztecHeadingSpan.Heading.H6
            else -> {
            }
        }
    }
}