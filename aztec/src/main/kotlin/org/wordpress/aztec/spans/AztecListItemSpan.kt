package org.wordpress.aztec.spans

import android.text.TextUtils
import org.wordpress.aztec.ParagraphFlagged

class AztecListItemSpan : AztecBlockSpan, ParagraphFlagged {

    private val TAG = "li"

    override var attributes: String

    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1

    constructor(attributes: String = "") : super() {
        this.attributes = attributes
    }

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
