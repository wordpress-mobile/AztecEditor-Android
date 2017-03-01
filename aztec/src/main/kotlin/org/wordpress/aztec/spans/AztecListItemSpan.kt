package org.wordpress.aztec.spans

import android.text.TextUtils
import org.wordpress.aztec.ParagraphFlagged

class AztecListItemSpan(override var nestingLevel: Int, override var attributes: String = "") : AztecBlockSpan,
        ParagraphFlagged {

    private val TAG = "li"

    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1

    override fun getStartTag(): String {
        if (TextUtils.isEmpty(attributes)) {
            return TAG
        }
        return TAG + attributes
    }

    override fun getEndTag(): String {
        return TAG
    }

}
