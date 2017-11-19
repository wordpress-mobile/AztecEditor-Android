package org.wordpress.aztec.plugins.shortcodes.watchers

import android.text.Spanned
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import org.wordpress.aztec.plugins.shortcodes.spans.CaptionShortcodeSpan
import org.wordpress.aztec.util.SpanWrapper
import org.wordpress.aztec.watchers.BlockElementWatcher

class CaptionWatcher(private val aztecText: AztecText) : BlockElementWatcher(aztecText) {

    var blocks = arrayListOf<SpanWrapper<CaptionShortcodeSpan>>()
    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
        super.beforeTextChanged(text, start, count, after)

        // prevent moving the start of the caption beyond an image when a backspace is entered right before the
        // image by making it non-paragraph
        if (count > 0 && start + count < text.length && aztecText.text[start + count] == Constants.IMG_CHAR) {
            val spans = SpanWrapper.getSpans<CaptionShortcodeSpan>(aztecText.text, start + count, start + count, CaptionShortcodeSpan::class.java)
            spans.forEach { block ->
                aztecText.text.setSpan(block.span, block.start, block.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                blocks.add(block)
            }
        }
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        super.onTextChanged(s, start, before, count)

        // we can make it a paragraph again now that everything is done
        blocks.forEach { block ->
            aztecText.text.setSpan(block.span, block.start, block.end, Spanned.SPAN_PARAGRAPH)
        }
        blocks.clear()
    }
}