package org.wordpress.aztec.plugins.visual2html

import android.text.SpannableStringBuilder
import org.wordpress.aztec.plugins.IAztecPlugin

interface ISpanPreprocessor : IAztecPlugin {
    fun beforeSpansProcessed(spannable: SpannableStringBuilder)
}