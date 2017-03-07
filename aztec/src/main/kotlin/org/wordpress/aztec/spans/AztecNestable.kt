package org.wordpress.aztec.spans

import android.text.Spannable
import android.text.Spanned
import org.wordpress.aztec.util.SpanWrapper

interface AztecNestable {
    var nestingLevel: Int

    companion object {
        fun getNestingLevelAt(spanned: Spanned, index: Int, nextIndex: Int = index): Int {
            return spanned.getSpans(index, nextIndex, AztecNestable::class.java).maxBy { it.nestingLevel }?.nestingLevel ?: 0
        }

        fun getParent(spannable: Spannable, child: SpanWrapper<out AztecNestable>): SpanWrapper<out AztecNestable>? {
            return SpanWrapper.getSpans<AztecNestable>(spannable, child.start, child.start + 1)
                    .sortedBy { it.span.nestingLevel }
                    .lastOrNull { it.span.nestingLevel < child.span.nestingLevel }
        }
    }
}
