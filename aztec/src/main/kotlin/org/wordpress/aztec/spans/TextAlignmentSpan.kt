package org.wordpress.aztec.spans

import android.text.Layout
import android.text.style.AlignmentSpan

class TextAlignmentSpan(var align: Layout.Alignment) : AlignmentSpan, IAztecParagraphStyle {
    override fun getAlignment(): Layout.Alignment {
        return align
    }
}