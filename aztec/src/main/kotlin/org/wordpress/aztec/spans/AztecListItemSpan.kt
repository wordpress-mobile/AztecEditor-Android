package org.wordpress.aztec.spans

import android.text.Layout
import org.wordpress.aztec.AztecAttributes

class AztecListItemSpan(override var nestingLevel: Int,
                        override var attributes: AztecAttributes = AztecAttributes(),
                        override var align: Layout.Alignment? = null) : IAztecCompositeBlockSpan {
    override val TAG = "li"

    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1
}
