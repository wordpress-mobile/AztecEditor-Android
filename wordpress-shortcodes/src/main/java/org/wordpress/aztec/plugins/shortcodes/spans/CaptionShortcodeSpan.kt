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
                if (start <= end) {
                    return aztecText.text.subSequence(start, end).toString()
                }
            }
            return ""
        }
        set(value) {
            aztecText?.let {
                val wrapper = SpanWrapper<CaptionShortcodeSpan>(aztecText.text, this)
                val start = getStart(wrapper)
                val end = getEnd(wrapper, aztecText)

                // a condition start > end is true if the caption is empty
                if (start > end) {
                    val newValue = Constants.NEWLINE_STRING + value
                    if (end < aztecText.length() && aztecText.text[end] != Constants.NEWLINE) {
                        aztecText.text.insert(end, Constants.NEWLINE_STRING)
                    }
                    aztecText.text.insert(end, newValue)

                    val newEnd = Math.min(end + newValue.length + 1, aztecText.length())
                    aztecText.text.setSpan(this, wrapper.start, newEnd, Spanned.SPAN_PARAGRAPH)
                } else {
                    aztecText.text.replace(start, end, value)
                }
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

    private fun getStart(wrapper: SpanWrapper<CaptionShortcodeSpan>): Int {
        // skip the image char and the newline
        return wrapper.spannable.indexOf(Constants.IMG_CHAR, wrapper.start) + 2
    }

    private fun getEnd(wrapper: SpanWrapper<CaptionShortcodeSpan>, aztecText: AztecText): Int {
        // return the true end without the newline
        var end = wrapper.end
        if (end > 0 && aztecText.text[end - 1] == Constants.NEWLINE) {
            end--
        }
        return end
    }
}
