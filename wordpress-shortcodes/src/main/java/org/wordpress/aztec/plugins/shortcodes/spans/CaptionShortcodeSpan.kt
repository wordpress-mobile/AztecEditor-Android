package org.wordpress.aztec.plugins.shortcodes.spans

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.StyleSpan
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import org.wordpress.aztec.spans.IAztecBlockSpan
import org.wordpress.aztec.util.SpanWrapper

class CaptionShortcodeSpan(override var attributes: AztecAttributes,
                           override val TAG: String,
                           override var nestingLevel: Int,
                           private val aztecText: AztecText)
    : StyleSpan(Typeface.ITALIC), IAztecBlockSpan {

    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1

    var caption: String
        get() {
            val wrapper = SpanWrapper<CaptionShortcodeSpan>(aztecText.text, this)
            return aztecText.text.subSequence(wrapper.start + 1, wrapper.end).toString()
        }
        set(value) {
            val wrapper = SpanWrapper<CaptionShortcodeSpan>(aztecText.text, this)
            var end = wrapper.end
            if (end - 1 >= 0 && aztecText.text[end - 1] == Constants.NEWLINE) {
                end--
            }
            aztecText.text.replace(wrapper.start + 1, end, value)
            aztecText.text.setSpan(wrapper.span, wrapper.start, wrapper.start + 1 + value.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
}
