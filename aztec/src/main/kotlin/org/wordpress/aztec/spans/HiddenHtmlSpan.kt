package org.wordpress.aztec.spans

import android.text.Layout
import org.wordpress.aztec.AlignmentApproach
import org.wordpress.aztec.AztecAttributes

fun createHiddenHtmlSpan(tag: String,
                         attributes: AztecAttributes = AztecAttributes(),
                         nestingLevel: Int,
                         alignmentApproach: AlignmentApproach
) = when (alignmentApproach) {
    AlignmentApproach.SPAN_LEVEL -> HiddenHtmlSpanAligned(tag, attributes, nestingLevel)
    AlignmentApproach.VIEW_LEVEL -> HiddenHtmlSpan(tag, attributes, nestingLevel)
}

class HiddenHtmlSpanAligned(tag: String,
                            attributes: AztecAttributes,
                            nestingLevel: Int
) : HiddenHtmlSpan(tag, attributes, nestingLevel), IAztecAlignmentSpan {
    override var align: Layout.Alignment? = null
}

open class HiddenHtmlSpan(tag: String,
                          override var attributes: AztecAttributes,
                          override var nestingLevel: Int) : IAztecParagraphStyle {
    override val TAG: String = tag
}
