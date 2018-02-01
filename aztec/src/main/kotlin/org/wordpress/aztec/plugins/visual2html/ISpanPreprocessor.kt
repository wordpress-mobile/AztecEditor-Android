package org.wordpress.aztec.plugins.visual2html

import android.text.SpannableStringBuilder
import org.wordpress.aztec.plugins.IAztecPlugin

interface ISpanPreprocessor : IAztecPlugin {
    /**
     * Called before the spannable is processed by [org.wordpress.aztec.AztecParser] during span-to-HTML parsing.
     *
     * Warning: This callback should be read-only. The content should not be modified as it can be called by
     * the edit history handler, which would alter the content before the actual parsing.
     */
    fun beforeSpansProcessed(spannable: SpannableStringBuilder)
}