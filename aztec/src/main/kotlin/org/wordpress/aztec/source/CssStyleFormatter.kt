package org.wordpress.aztec.source

import androidx.core.text.TextDirectionHeuristicsCompat
import android.text.Editable
import android.text.Layout
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.spans.IAztecAlignmentSpan
import org.wordpress.aztec.spans.IAztecAttributedSpan
import org.wordpress.aztec.spans.IAztecParagraphStyle
import org.wordpress.aztec.util.ColorConverter
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Utility for parsing and processing the HTML *style* attribute to create a styled [Spannable].
 * The CSS style properties current supported are:
 * + *color*<br/>Example: style="color:blue"
 */
class CssStyleFormatter {

    companion object {

        val STYLE_ATTRIBUTE = "style"
        val CSS_TEXT_DECORATION_ATTRIBUTE = "text-decoration"
        val CSS_TEXT_ALIGN_ATTRIBUTE = "text-align"
        val CSS_COLOR_ATTRIBUTE = "color"

        /**
         * Check the provided [attributedSpan] for the *style* attribute. If found, parse out the
         * supported CSS style properties and use the results to create a [ForegroundColorSpan],
         * then add it to the provided [text].
         *
         * Must be called immediately after the base [IAztecAttributedSpan] has been processed.
         *
         * @param [text] An [Editable] containing an [IAztecAttributedSpan] for processing.
         * @param [attributedSpan] The attributed span of the Html tag used to search for the *style* attributes.
         * @param [start] The index where the [IAztecAttributedSpan] starts inside the [text].
         */
        fun applyInlineStyleAttributes(text: Editable, attributedSpan: IAztecAttributedSpan, start: Int, end: Int) {
            if (attributedSpan.attributes.hasAttribute(STYLE_ATTRIBUTE) && start != end) {
                processColor(attributedSpan.attributes, text, start, end)
                if (attributedSpan is IAztecParagraphStyle) {
                    processAlignment(attributedSpan, text, start, end)
                }
            }
        }

        private fun processAlignment(blockSpan: IAztecParagraphStyle, text: Editable, start: Int, end: Int) {
            if (blockSpan is IAztecAlignmentSpan) {
                val alignment = getStyleAttribute(blockSpan.attributes, CSS_TEXT_ALIGN_ATTRIBUTE)
                if (!alignment.isBlank()) {
                    val direction = TextDirectionHeuristicsCompat.FIRSTSTRONG_LTR
                    val isRtl = direction.isRtl(text, start, end - start)

                    val align = when (alignment) {
                        "right" -> if (isRtl) Layout.Alignment.ALIGN_NORMAL else Layout.Alignment.ALIGN_OPPOSITE
                        "center" -> Layout.Alignment.ALIGN_CENTER
                        else -> if (!isRtl) Layout.Alignment.ALIGN_NORMAL else Layout.Alignment.ALIGN_OPPOSITE
                    }

                    blockSpan.align = align
                }
            }
        }

        private fun getPattern(styleAttr: String): Pattern {
            return Pattern.compile(
                    "(?:;|\\A)$styleAttr:(.+?)(?:;|$)", Pattern.CASE_INSENSITIVE or Pattern.MULTILINE)
        }

        private fun getMatcher(attributes: AztecAttributes, styleAttributeName: String): Matcher {
            val style = (attributes.getValue(STYLE_ATTRIBUTE) ?: "").replace("\\s".toRegex(), "")
            return getPattern(styleAttributeName).matcher(style)
        }

        private fun processColor(attributes: AztecAttributes, text: Editable, start: Int, end: Int) {
            val colorAttrValue = getStyleAttribute(attributes, CSS_COLOR_ATTRIBUTE)
            if (!colorAttrValue.isBlank()) {
                val colorInt = ColorConverter.getColorInt(colorAttrValue)
                if (colorInt != ColorConverter.COLOR_NOT_FOUND) {
                    text.setSpan(ForegroundColorSpan(colorInt), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }

        fun containsStyleAttribute(attributes: AztecAttributes, styleAttributeName: String): Boolean {
            return attributes.hasAttribute(STYLE_ATTRIBUTE) && getMatcher(attributes, styleAttributeName).find()
        }

        fun removeStyleAttribute(attributes: AztecAttributes, styleAttributeName: String) {
            if (attributes.hasAttribute(STYLE_ATTRIBUTE)) {
                val m = getMatcher(attributes, styleAttributeName)
                var newStyle = m.replaceAll("")

                if (newStyle.isBlank()) {
                    attributes.removeAttribute(STYLE_ATTRIBUTE)
                } else {
                    newStyle = newStyle.replace(";".toRegex(), "; ")
                    attributes.setValue(STYLE_ATTRIBUTE, newStyle.trim())
                }
            }
        }

        fun getStyleAttribute(attributes: AztecAttributes, styleAttributeName: String): String {
            val m = getMatcher(attributes, styleAttributeName)

            var styleAttributeValue = ""
            if (m.find()) {
                styleAttributeValue = m.group(1)
            }
            return styleAttributeValue
        }

        fun addStyleAttribute(attributes: AztecAttributes, styleAttributeName: String, styleAttributeValue: String) {
            var style = attributes.getValue(STYLE_ATTRIBUTE) ?: ""
            style = style.trim()

            if (!style.isEmpty() && !style.endsWith(";")) {
                style += ";"
            }

            style += " $styleAttributeName:$styleAttributeValue;"
            attributes.setValue(STYLE_ATTRIBUTE, style.trim())
        }

        fun mergeStyleAttributes(firstStyle: String, secondStyle: String): String {
            val firstStyles = firstStyle.trim().split(";").map { it.trim().replace(" ", "") }
            var secondStyles = secondStyle.trim().split(";").map { it.trim().replace(" ", "") }

            val mergedArray = firstStyles.union(secondStyles).filterNot { it.trim().isEmpty() }

            var style = ""
            mergedArray.forEach({
                style = style + it + ";"
            })
            return style.trimEnd()
        }
    }
}
