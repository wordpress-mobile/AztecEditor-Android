package org.wordpress.aztec.watchers

import android.text.Editable
import android.text.Spannable
import android.text.Spanned
import android.text.TextWatcher
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import org.wordpress.aztec.spans.IParagraphFlagged
import org.wordpress.aztec.util.SpanWrapper
import java.lang.ref.WeakReference

class ParagraphBleedAdjuster private constructor(aztecText: AztecText) : TextWatcher {

    private val aztecTextRef: WeakReference<AztecText?> = WeakReference(aztecText)

    override fun beforeTextChanged(s: CharSequence, start: Int, before: Int, after: Int) {
        if (aztecTextRef.get()?.isTextChangedListenerDisabled() ?: true) {
            return
        }

        if (start == 0) {
            // change will be at the beginning of text so, nothing to worry about. Bail.
            return
        }

        if (before == 0) {
            // change is only adding characters so, will not start an inadvertent paragraph bleed.
            return
        }

        if (start + before < s.length) {
            // change will not reach the end-of-text so, nothing to worry about. Bail.
            return
        }

        if (s[start - 1] != Constants.NEWLINE) {
            // no newline will touch the end-of-text during the replace so, nothing to worry about. Bail.
            return
        }

        // OK, the change will cause an end-of-text paragraph bleed so, mark the paragraphs
        //  with their current anchor position

        val newline = s.subSequence(start - 1, start) as Spanned
        SpanWrapper.getSpans(s as Spannable, newline.getSpans<IParagraphFlagged>(0, 1, IParagraphFlagged::class.java))
                .filter { it.start < start && it.end == start }
                .forEach { it.span.endBeforeBleed = start }
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, after: Int) {
        if (aztecTextRef.get()?.isTextChangedListenerDisabled() ?: true) {
            return
        }

        if (after == 0) {
            // no addition of characters so, any bleeding cannot bleed. We'll wait for when chars are added. Bail.
            return
        }

        // OK, chars where added so, let's check for bleeding and adjust
        SpanWrapper.getSpans(s as Spannable, start, start, IParagraphFlagged::class.java)
                .filter { it.span.hasBled() }
                .forEach {
                    it.end = it.span.endBeforeBleed
                    it.span.clearEndBeforeBleed()
                }
    }

    override fun afterTextChanged(s: Editable) {}

    companion object {
        fun install(text: AztecText) {
            text.addTextChangedListener(ParagraphBleedAdjuster(text))
        }
    }
}
