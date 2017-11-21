package org.wordpress.aztec.plugins.html2visual

import android.text.SpannableStringBuilder
import org.wordpress.aztec.plugins.IAztecPlugin

interface ISpanPostprocessor : IAztecPlugin {
    fun onSpansProcessed(spannable: SpannableStringBuilder)
}