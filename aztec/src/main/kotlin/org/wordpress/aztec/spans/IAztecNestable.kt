package org.wordpress.aztec.spans

import android.text.Spannable
import android.text.Spanned
import org.wordpress.aztec.util.SpanWrapper

interface IAztecNestable {
    var nestingLevel: Int

    companion object {
        fun getNestingLevelAt(spanned: Spanned, index: Int, nextIndex: Int = index): Int {
            return spanned.getSpans(index, nextIndex, IAztecNestable::class.java).maxBy { it.nestingLevel }?.nestingLevel ?: 0
        }

        fun getParent(spannable: Spannable, child: SpanWrapper<out IAztecNestable>): SpanWrapper<out IAztecNestable>? {
            return SpanWrapper.getSpans<IAztecNestable>(spannable, child.start, child.start + 1)
                    .sortedBy { it.span.nestingLevel }
                    .lastOrNull { it.span.nestingLevel < child.span.nestingLevel }
        }
    }
}
