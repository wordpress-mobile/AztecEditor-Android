package org.wordpress.aztec.spans

import android.text.style.ParagraphStyle
import org.wordpress.aztec.AztecAttributes

class ParagraphSpan(
        override var nestingLevel: Int,
        override var attributes: AztecAttributes = AztecAttributes()
    ) : ParagraphStyle, AztecBlockSpan {

    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1

    private var TAG: String = "p"

    override fun getStartTag(): String {
        if (attributes.isEmpty()) {
            return TAG
        }
        return TAG + " " + attributes
    }

    override fun getEndTag(): String {
        return TAG
    }
}