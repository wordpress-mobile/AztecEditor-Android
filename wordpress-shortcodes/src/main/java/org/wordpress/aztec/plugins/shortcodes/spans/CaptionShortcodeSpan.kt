package org.wordpress.aztec.plugins.shortcodes.spans

import android.graphics.Typeface
import android.text.Layout
import android.text.Spanned
import android.text.style.StyleSpan
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import org.wordpress.aztec.spans.IAztecAlignmentSpan
import org.wordpress.aztec.spans.IAztecBlockSpan
import org.wordpress.aztec.util.SpanWrapper

class CaptionShortcodeSpan @JvmOverloads constructor(override var attributes: AztecAttributes,
                                                     override val TAG: String,
                                                     override var nestingLevel: Int,
                                                     private val aztecText: AztecText? = null,
                                                     override var align: Layout.Alignment? = null)
    : StyleSpan(Typeface.ITALIC), IAztecAlignmentSpan, IAztecBlockSpan {

    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1

    var caption: String
        get() {
            aztecText?.let {
                val wrapper = SpanWrapper<CaptionShortcodeSpan>(aztecText.text, this)
                val start = getCaptionStart(wrapper)
                val end = getCaptionEnd(wrapper, aztecText)
                return aztecText.text.subSequence(start, end).toString()
            }
            return ""
        }
        set(value) {
            aztecText?.let {
                val wrapper = SpanWrapper<CaptionShortcodeSpan>(aztecText.text, this)
                val start = getCaptionStart(wrapper)
                val end = getCaptionEnd(wrapper, aztecText)

                // a condition start == end is true if the caption is empty
                if (start == end) {
                    val newValue = Constants.NEWLINE_STRING + value
                    if (end < aztecText.length() && aztecText.text[end] != Constants.NEWLINE) {
                        aztecText.text.insert(end, Constants.NEWLINE_STRING)
                    }
                    aztecText.text.insert(end, newValue)

                    val newEnd = Math.min(end + newValue.length + 1, aztecText.length())
                    aztecText.text.setSpan(this, wrapper.start, newEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                } else {
                    aztecText.text.replace(start, end, value)
                }
            }
        }

    fun remove() {
        aztecText?.let {
            val wrapper = SpanWrapper<CaptionShortcodeSpan>(aztecText.text, this)
            val start = getCaptionStart(wrapper) - 1 // there is always a newline, we want to remove it
            val end = getCaptionEnd(wrapper, aztecText)

            aztecText.text.delete(start, end)
            aztecText.text.removeSpan(this)
        }
    }

    // returns the start of the caption string without newlines
    private fun getCaptionStart(wrapper: SpanWrapper<CaptionShortcodeSpan>): Int {
        // skip the image char and the newline
        val imgEnd = wrapper.spannable.indexOf(Constants.IMG_CHAR, wrapper.start) + 1
        var start = imgEnd
        // unless the caption's empty, there the first character is always a newline
        if (imgEnd != wrapper.end && wrapper.spannable[imgEnd] == Constants.NEWLINE) {
            start++
        }
        return start
    }

    // returns the end of the caption string without newlines
    private fun getCaptionEnd(wrapper: SpanWrapper<CaptionShortcodeSpan>, aztecText: AztecText): Int {
        // return the true end without the newline
        val start = getCaptionStart(wrapper)
        var end = Math.max(start, wrapper.end)
        if (end > start && aztecText.text[end - 1] == Constants.NEWLINE) {
            end--
        }
        return end
    }

    override fun shouldParseAlignmentToHtml(): Boolean {
        return false
    }
}
