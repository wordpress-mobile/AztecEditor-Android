package org.wordpress.aztec.spans

import android.text.Layout
import org.wordpress.aztec.AztecAttributes

class HiddenHtmlSpan(tag: String,
                     override var attributes: AztecAttributes = AztecAttributes(),
                     override var nestingLevel: Int) : IAztecAlignmentSpan, IAztecParagraphStyle {
    override var align: Layout.Alignment? = null

    override val TAG: String = tag
}
