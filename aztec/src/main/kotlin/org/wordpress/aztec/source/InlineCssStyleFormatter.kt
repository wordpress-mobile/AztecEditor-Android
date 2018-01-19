package org.wordpress.aztec.source

import android.text.Editable
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.spans.IAztecAttributedSpan
import org.wordpress.aztec.util.ColorConverter
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Utility for parsing and processing the HTML *style* attribute to create a styled [Spannable].
 * The CSS style properties current supported are:
 * + *color*<br/>Example: style="color:blue"
 */
class InlineCssStyleFormatter {

    companion object {

        val STYLE_ATTRIBUTE = "style"
        val CSS_TEXT_DECORATION_ATTRIBUTE = "text-decoration"
        val CSS_COLOR_ATTRIBUTE = "color"

        /**
         * Check the provided [attributes] for the *style* attribute. If found, parse out the
         * supported CSS style properties and use the results to create a [ForegroundColorSpan],
         * then add it to the provided [text].
         *
         * Must be called immediately after the base [IAztecAttributedSpan] has been processed.
         *
         * @param [text] An [Editable] containing an [IAztecAttributedSpan] for processing.
         * @param [attributes] The attributes of the Html tag used to search for the *style* attributes.
         * @param [start] The index where the [IAztecAttributedSpan] starts inside the [text].
         */
        fun applyInlineStyleAttributes(text: Editable, attributes: AztecAttributes, start: Int, end: Int) {
            if (attributes.hasAttribute(STYLE_ATTRIBUTE) && start != end) {
                processColor(attributes, text, start, end)
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
                    newStyle = newStyle.replace(";(.?)".toRegex(), "; ($1)")
                    newStyle = newStyle.replace(":".toRegex(), ": ")
                    attributes.setValue(STYLE_ATTRIBUTE, newStyle)
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
                style += "; "
            }

            style += "$styleAttributeName: $styleAttributeValue"
            attributes.setValue(STYLE_ATTRIBUTE, style)
        }

        fun mergeStyleAttributes(firstStyle: String, secondStyle: String): String {
            var style = firstStyle.trim()

            if (!style.isEmpty() && !style.endsWith(";")) {
                style += "; "
            }

            return style + secondStyle
        }
    }
}
