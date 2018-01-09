package org.wordpress.aztec.source

import android.text.Editable
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.spans.AztecUnderlineSpan
import org.wordpress.aztec.util.ColorConverter
import java.util.regex.Pattern
import org.wordpress.aztec.spans.IAztecAttributedSpan

/**
 * Utility for parsing and processing the HTML *style* attribute to create a styled [Spannable].
 * The CSS style properties current supported are:
 * + *color*<br/>Example: style="color:blue"
 */
class InlineCssStyleFormatter {

    companion object {
        /**
         * Regex pattern for pulling the *color* property from the style string.
         */
        private val foregroundColorPattern by lazy {
            Pattern.compile(
                    "(?:;|\\A)color:(.+?)(?:;|$)", Pattern.CASE_INSENSITIVE or Pattern.MULTILINE)
        }

        private val textDecorationPattern by lazy {
            Pattern.compile(
                    "(?:;|\\A)text-decoration:(.+?)(?:;|$)", Pattern.CASE_INSENSITIVE or Pattern.MULTILINE)
        }

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
            if (attributes.hasAttribute("style")) {

                if (start != end) {
                    // Process the CSS 'color' property, remove any whitespace or newline characters
                    val style = attributes.getValue("", "style").replace("\\s".toRegex(), "")

                    processColor(style, text, start, end)
                    processTextDecoration(style, text, start, end, attributes)
                }
            }
        }

        private fun processColor(style: String, text: Editable, start: Int, end: Int) {
            val m = foregroundColorPattern.matcher(style)
            if (m.find()) {
                val colorString = m.group(1)
                val colorInt = ColorConverter.getColorInt(colorString)
                if (colorInt != ColorConverter.COLOR_NOT_FOUND) {
                    text.setSpan(ForegroundColorSpan(colorInt), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }

        private fun processTextDecoration(style: String, text: Editable, start: Int, end: Int, attrs: AztecAttributes) {
            val m = textDecorationPattern.matcher(style)
            if (m.find()) {
                val decoration = m.group(1)
                if (decoration == "underline") {
                    text.setSpan(UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }
    }
}
