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

class ParagraphCollapseRemover private constructor(aztecText: AztecText) : TextWatcher {

    private val aztecTextRef: WeakReference<AztecText?> = WeakReference(aztecText)

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        if (aztecTextRef.get()?.isTextChangedListenerDisabled() ?: true) {
            return
        }

        val charsOld = s.subSequence(start, start + count) as Spanned

        val paragraphs = SpanWrapper.getSpans(s as Spannable,
                start, start + count, IParagraphFlagged::class.java)
        if (paragraphs.isEmpty() && start + count >= s.length) {
            // no paragraphs in the text to be removed and no other text beyond the change so, nothing to do here. Bail.
            return
        }

        var firstNewlineBeyondChangeIndex = s.toString().indexOf(Constants.NEWLINE, start + count)
        if (firstNewlineBeyondChangeIndex == -1) {
            // no newline beyond the change so, let's set it to the text end.
            firstNewlineBeyondChangeIndex = s.length
        }

        val charsOldString = charsOld.toString()

        var lastNewlineIndex = charsOldString.length
        do {
            lastNewlineIndex = charsOldString.lastIndexOf(Constants.NEWLINE, lastNewlineIndex - 1)

            if (lastNewlineIndex == -1) {
                break
            }

            if (start + lastNewlineIndex + 2 > s.length) {

                continue
            }

            var paragraphsToCheck: Array<IParagraphFlagged>

            if (start + lastNewlineIndex + 1 < s.length) {
                val postNewline = s.subSequence(start + lastNewlineIndex + 1, start + lastNewlineIndex + 2) as Spanned
                paragraphsToCheck = postNewline.getSpans<IParagraphFlagged>(0, 1, IParagraphFlagged::class.java)
            } else {
                paragraphsToCheck = charsOld.getSpans<IParagraphFlagged>(lastNewlineIndex + 1, lastNewlineIndex + 1, IParagraphFlagged::class.java)
            }

            SpanWrapper.getSpans(s, paragraphsToCheck)
                    .filter { it.start == start + lastNewlineIndex + 1 }
                    .forEach {
                        // this paragraph is anchored to the newline in question

                        if (it.end > firstNewlineBeyondChangeIndex + 1) {
                            // paragraph end is beyond the newline that will be picked up. That means the paragraph
                            //  will manage to get to the new anchor without totally collapsing. Let's move on.
                            return@forEach
                        }

                        // paragraph end is closer or at the newline that will be picked up. That means the paragraph
                        //  will effectively collapse since its start will reach its end before the newline. Retire it.
                        it.remove()
                    }
        } while (lastNewlineIndex > -1)
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable) {}

    companion object {
        fun install(text: AztecText) {
            text.addTextChangedListener(ParagraphCollapseRemover(text))
        }
    }
}
