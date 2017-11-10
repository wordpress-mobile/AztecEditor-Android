package org.wordpress.aztec.formatting

import android.text.Editable
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import org.wordpress.aztec.AztecAttributes
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
                    "(?:\\s+|\\A)color\\s*:\\s*(\\S*)\\b")
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
        fun applyInlineStyleAttributes(text: Editable, attributes: AztecAttributes, start: Int) {
            if (attributes != null && attributes.hasAttribute("style")) {
                val len = text.length

                if (start != len) {
                    val style = attributes.getValue("style")
                    //
                    // Process the CSS 'color' property
                    val m = foregroundColorPattern.matcher(style)
                    if (m.find()) {
                        val colorString = m.group(1)
                        val colorInt  = ColorConverter.getColorInt(colorString)
                        if (colorInt != -1) {
                            text.setSpan(ForegroundColorSpan(colorInt), start, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                }
            }
        }
    }
}
