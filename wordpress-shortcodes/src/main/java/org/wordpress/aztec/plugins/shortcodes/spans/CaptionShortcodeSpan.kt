package org.wordpress.aztec.plugins.shortcodes.spans

import android.graphics.Typeface
import android.text.Editable
import android.text.style.StyleSpan
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.spans.IAztecBlockSpan
import org.wordpress.aztec.util.SpanWrapper

class CaptionShortcodeSpan(override var attributes: AztecAttributes,
                           override val TAG: String,
                           override var nestingLevel: Int,
                           private val editable: Editable)
    : StyleSpan(Typeface.ITALIC), IAztecBlockSpan {

    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1

    var caption: String
        get() {
            val span = SpanWrapper<CaptionShortcodeSpan>(editable, this)
            return editable.subSequence(span.start + 1, span.end).toString()
        }
        set(value) {
            val span = SpanWrapper<CaptionShortcodeSpan>(editable, this)
            editable.delete(span.start + 1, span.end)
            editable.insert(span.start + 1, value)
        }
}
