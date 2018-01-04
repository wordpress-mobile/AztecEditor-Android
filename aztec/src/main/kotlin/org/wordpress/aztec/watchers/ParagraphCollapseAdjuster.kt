package org.wordpress.aztec.watchers

import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.spans.IParagraphFlagged
import org.wordpress.aztec.util.SpanWrapper

class ParagraphCollapseAdjuster : TextWatcher {

    override fun beforeTextChanged(s: CharSequence, start: Int, before: Int, after: Int) {
        if (start + before < s.length) {
            // change will not reach the end-of-text so, nothing to worry about. Bail.
            return
        }

        if (before == 0) {
            // change will not remove any text so, no start of an inadvertent paragraph collapse. Bail.
            return
        }

        // OK, the change will cause an end-of-text paragraph collapse so, mark the paragraphs
        //  with their current anchor position

        SpanWrapper.getSpans(s as Spannable, start, start, IParagraphFlagged::class.java)
                .filter { it.start == start && it.end > start }
                .forEach { it.span.startBeforeCollapse = start }
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, after: Int) {
        if (after == 0) {
            // no addition of characters so, any collapse cannot be reverted yet. We'll wait for when chars are added.
            return
        }

        // OK, chars where added so, let's check for collapses and adjust
        SpanWrapper.getSpans(s as Spannable, s.length, s.length, IParagraphFlagged::class.java)
                .filter { it.span.hasCollapsed() }
                .forEach {
                    it.start = it.span.startBeforeCollapse
                    it.span.clearStartBeforeCollapse()
                }
    }

    override fun afterTextChanged(s: Editable) {}

    companion object {
        fun install(text: AztecText) {
            text.addTextChangedListener(ParagraphCollapseAdjuster())
        }
    }
}
