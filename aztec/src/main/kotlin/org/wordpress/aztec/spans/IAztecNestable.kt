package org.wordpress.aztec.spans

import android.text.Spannable
import android.text.Spanned
import org.wordpress.aztec.Constants
import org.wordpress.aztec.util.SpanWrapper

interface IAztecNestable {
    var nestingLevel: Int

    companion object {
        fun getNestingLevelAt(spanned: Spanned, index: Int, nextIndex: Int = index): Int {
            return spanned.getSpans(index, nextIndex, IAztecNestable::class.java)
                    .filter { spanned.getSpanEnd(it) != index || index == 0 || spanned[index - 1] != Constants.NEWLINE }
                    .maxBy { it.nestingLevel }?.nestingLevel ?: 0
        }

        fun getMinNestingLevelAt(spanned: Spanned, index: Int, nextIndex: Int = index): Int {
            return spanned.getSpans(index, nextIndex, IAztecNestable::class.java)
                    .filter { spanned.getSpanEnd(it) != index || index == 0 || spanned[index - 1] != Constants.NEWLINE }
                    .filter { spanned.getSpanStart(it) <= index && spanned.getSpanEnd(it) >= nextIndex &&
                            (spanned.getSpanStart(it) != index || spanned.getSpanEnd(it) != nextIndex) }
                    .filter { index != nextIndex || spanned.getSpanStart(it) != index && spanned.getSpanEnd(it) != index }
                    .minBy { it.nestingLevel }?.nestingLevel ?: 0
        }

        fun pushDeeper(spannable: Spannable, start: Int, end: Int, fromLevel: Int = 0, pushBy: Int = 1): List<SpanWrapper<IAztecNestable>> {
            val spans = SpanWrapper.getSpans(spannable, start, end, IAztecNestable::class.java)
                    .filter { spannable.getSpanStart(it.span) >= start && spannable.getSpanEnd(it.span) <= end }
                    .filter { it.span.nestingLevel >= fromLevel }

            spans.forEach {
                it.span.nestingLevel += pushBy
            }

            return spans
        }

        fun pullUp(spannable: Spannable, start: Int, end: Int, fromLevel: Int, pullBy: Int = 1): List<SpanWrapper<IAztecNestable>> {
            val spans = SpanWrapper.getSpans(spannable, start, end, IAztecNestable::class.java)
                    .filter { spannable.getSpanStart(it.span) >= start && spannable.getSpanEnd(it.span) <= end }
                    .filter { it.span.nestingLevel >= fromLevel }

            spans.forEach {
                it.span.nestingLevel -= pullBy
            }

            return spans
        }

        fun getParent(spannable: Spannable, child: SpanWrapper<out IAztecNestable>): SpanWrapper<out IAztecNestable>? {
            return SpanWrapper.getSpans<IAztecNestable>(spannable, child.start, child.start + 1)
                    .sortedBy { it.span.nestingLevel }
                    .lastOrNull { it.span.nestingLevel < child.span.nestingLevel }
        }
    }
}
