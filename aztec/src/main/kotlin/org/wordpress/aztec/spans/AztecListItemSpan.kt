package org.wordpress.aztec.spans

import org.wordpress.aztec.AztecAttributes

class AztecListItemSpan(override var nestingLevel: Int, override var attributes: AztecAttributes = AztecAttributes()) : AztecBlockSpan {

    private val TAG = "li"

    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1

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
