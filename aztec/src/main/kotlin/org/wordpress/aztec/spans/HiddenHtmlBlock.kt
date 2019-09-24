package org.wordpress.aztec.spans

import android.text.Layout
import org.wordpress.aztec.AztecAttributes

class HiddenHtmlBlock(tag: String,
                      override var attributes: AztecAttributes = AztecAttributes(),
                      override var nestingLevel: Int) : IAztecAlignmentSpan, IAztecBlockSpan {
    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1

    override var align: Layout.Alignment? = null

    override val TAG: String = tag
}
