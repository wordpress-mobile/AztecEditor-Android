package org.wordpress.aztec.spans

import org.wordpress.aztec.AztecAttributes

class AztecListItemSpan(override var nestingLevel: Int, override var attributes: AztecAttributes = AztecAttributes()) : IAztecBlockSpan {

    override val TAG = "li"

    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1
}
