package org.wordpress.aztec.spans

import android.content.Context
import android.text.style.BackgroundColorSpan
import androidx.core.content.ContextCompat
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.R
import org.wordpress.aztec.formatting.InlineFormatter
import org.wordpress.aztec.source.CssStyleFormatter
import org.wordpress.aztec.util.ColorConverter

class HighlightSpan(
        override var attributes: AztecAttributes = AztecAttributes(),
        val colorHex: Int
) : BackgroundColorSpan(colorHex), IAztecInlineSpan {
    var alpha: Int = 220

    override var TAG = HIGHLIGHT_TAG

    companion object {
        const val HIGHLIGHT_TAG = "highlight"

        @JvmStatic
        @JvmOverloads
        fun create(attributes: AztecAttributes = AztecAttributes(),
                   context: Context,
                   defaultStyle: InlineFormatter.HighlightStyle? = null
        ) = HighlightSpan(attributes = attributes,
                colorHex = buildColor(context, attributes, defaultStyle))

        private fun buildColor(context: Context, attrs: AztecAttributes, defaultStyle: InlineFormatter.HighlightStyle?): Int {
            return if (CssStyleFormatter.containsStyleAttribute(
                            attrs,
                            CssStyleFormatter.CSS_BACKGROUND_COLOR_ATTRIBUTE
                    )
            ) {
                val att = CssStyleFormatter.getStyleAttribute(attrs,
                        CssStyleFormatter.CSS_BACKGROUND_COLOR_ATTRIBUTE)
                return ColorConverter.getColorInt(att)
            } else if (defaultStyle != null) {
                ContextCompat.getColor(context, defaultStyle.color)
            } else {
                ContextCompat.getColor(context, R.color.grey_lighten_10)
            }
        }
    }
}
