package org.wordpress.aztec.spans

import org.wordpress.aztec.AztecAttributes

class HiddenHtmlSpan(tag: String, override var attributes: AztecAttributes = AztecAttributes(),
                     override var nestingLevel: Int) : IAztecSpan, IAztecNestable, IAztecAttributedSpan {

    override val TAG: String = tag
}
