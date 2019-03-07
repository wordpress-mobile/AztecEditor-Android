package org.wordpress.aztec.plugins.html2visual

import android.text.Spannable
import org.wordpress.aztec.plugins.IAztecPlugin

interface ISpanPostprocessor : IAztecPlugin {
    fun afterSpansProcessed(spannable: Spannable)
}
