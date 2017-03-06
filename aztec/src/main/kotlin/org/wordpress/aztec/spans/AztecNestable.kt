package org.wordpress.aztec.spans

import android.text.Spanned

interface AztecNestable {
    var nestingLevel: Int

    companion object {
        fun getNestingLevelAt(spanned: Spanned, index: Int, nextIndex: Int = index): Int {
            return spanned.getSpans(index, nextIndex, AztecNestable::class.java).maxBy { it.nestingLevel }?.nestingLevel ?: 0
        }
    }
}
