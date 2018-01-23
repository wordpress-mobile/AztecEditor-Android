package org.wordpress.aztec.spans

import org.wordpress.aztec.AztecAttributes

class ParagraphSpan(
        override var nestingLevel: Int,
        override var attributes: AztecAttributes = AztecAttributes()
    ) : IAztecBlockSpan {

    override var TAG: String = "p"

    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1
}