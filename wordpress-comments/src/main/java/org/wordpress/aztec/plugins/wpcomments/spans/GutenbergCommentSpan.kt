package org.wordpress.aztec.plugins.wpcomments.spans

import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.spans.IAztecBlockSpan

class GutenbergCommentSpan(
        override val startTag: String,
        override var nestingLevel: Int,
        override var attributes: AztecAttributes = AztecAttributes()
) : IAztecBlockSpan {
    override val TAG: String = "wp:"
    override var startBeforeCollapse: Int = -1
    override var endBeforeBleed: Int = -1

    private var _endTag: String = super.endTag
    override var endTag: String
        get() = _endTag
        set(value) {
            _endTag = value
        }
}