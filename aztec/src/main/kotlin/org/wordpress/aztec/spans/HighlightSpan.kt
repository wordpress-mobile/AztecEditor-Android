package org.wordpress.aztec.spans

import android.content.Context
import android.text.style.BackgroundColorSpan
import androidx.core.content.ContextCompat
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.R
import org.wordpress.aztec.formatting.InlineFormatter

class HighlightSpan(
        override var attributes: AztecAttributes = AztecAttributes(),
        highlightStyle: InlineFormatter.HighlightStyle = InlineFormatter.HighlightStyle(R.color.grey_lighten_10),
        context: Context) : BackgroundColorSpan(ContextCompat.getColor(context, highlightStyle.color)), IAztecInlineSpan {

    override var TAG = "highlight"
    companion object {
        @JvmStatic
        fun create(attributes: AztecAttributes, context: Context) = HighlightSpan(attributes = attributes, context = context)
    }
}
