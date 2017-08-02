package org.wordpress.aztec.plugins.shortcodes.spans

import android.graphics.Typeface
import android.text.style.StyleSpan
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.spans.*

class CaptionShortcodeSpan(override var attributes: AztecAttributes,
                           override val TAG: String, override var nestingLevel: Int)
    : StyleSpan(Typeface.ITALIC), IAztecBlockSpan {

    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1
}