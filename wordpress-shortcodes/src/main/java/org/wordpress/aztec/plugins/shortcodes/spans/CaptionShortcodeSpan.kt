package org.wordpress.aztec.plugins.shortcodes.spans

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.StyleSpan
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import org.wordpress.aztec.spans.IAztecBlockSpan
import org.wordpress.aztec.util.SpanWrapper

class CaptionShortcodeSpan @JvmOverloads constructor(override var attributes: AztecAttributes,
                            override val TAG: String,
                            override var nestingLevel: Int,
                            private val aztecText: AztecText? = null)
    : StyleSpan(Typeface.ITALIC), IAztecBlockSpan {

    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1

    var caption: String
        get() {
            aztecText?.let {
                val wrapper = SpanWrapper<CaptionShortcodeSpan>(aztecText.text, this)
                val start = getStart(wrapper)
                val end = getEnd(wrapper, aztecText)
                return aztecText.text.subSequence(start, end).toString()
            }
            return ""
        }
        set(value) {
            aztecText?.let {
                val wrapper = SpanWrapper<CaptionShortcodeSpan>(aztecText.text, this)
                var start = getStart(wrapper)
                val end = getEnd(wrapper, aztecText)

                // a possible condition if caption is empty
                if (end < start)
                    start = end

                aztecText.text.replace(start, end, value)
                aztecText.text.setSpan(wrapper.span, wrapper.start, start + value.length + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

    fun remove() {
        aztecText?.let {
            val wrapper = SpanWrapper<CaptionShortcodeSpan>(aztecText.text, this)
            var start = getStart(wrapper)
            val end = getEnd(wrapper, aztecText)

            if (start > 0 && aztecText.text[start - 1] == Constants.NEWLINE &&
                    end < aztecText.text.length && aztecText.text[end] == Constants.NEWLINE) {
                start--
            }
            aztecText.text.delete(start, end)
            aztecText.text.removeSpan(this)
        }
    }

    // skip the image char and the newline
    private fun getStart(wrapper: SpanWrapper<CaptionShortcodeSpan>): Int {
        return wrapper.spannable.indexOf(Constants.IMG_CHAR, wrapper.start) + 2
    }

    private fun getEnd(wrapper: SpanWrapper<CaptionShortcodeSpan>, aztecText: AztecText): Int {
        var end = wrapper.end
        if (end > 0 && aztecText.text[end - 1] == Constants.NEWLINE) {
            end--
        }
        return end
    }
}
