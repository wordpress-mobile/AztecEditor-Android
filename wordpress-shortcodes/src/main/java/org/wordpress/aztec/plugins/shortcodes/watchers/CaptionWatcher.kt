package org.wordpress.aztec.plugins.shortcodes.watchers

import android.text.Spanned
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import org.wordpress.aztec.plugins.shortcodes.spans.CaptionShortcodeSpan
import org.wordpress.aztec.util.SpanWrapper
import org.wordpress.aztec.watchers.BlockElementWatcher

class CaptionWatcher(private val aztecText: AztecText) : BlockElementWatcher(aztecText) {

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        super.onTextChanged(s, start, before, count)

        if (count > 0 && start + count < s.length && s[start + count] == Constants.IMG_CHAR) {
            val spans = SpanWrapper.getSpans<CaptionShortcodeSpan>(aztecText.text, start + count, start + count,
                    CaptionShortcodeSpan::class.java)
            spans.forEach {

                // if text is added right before an image, move it out of the caption
                if (it.start < start + count && it.end > start + count) {
                    it.flags = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    it.start = start + count
                }
            }
        } else if (count > 0) {
            val spans = SpanWrapper.getSpans<CaptionShortcodeSpan>(aztecText.text, start, start, CaptionShortcodeSpan::class.java)
            spans.forEach {

                // if text is added right after an image, move it below the caption
                if (start > 0 && s[start - 1] == Constants.IMG_CHAR && s[start] != Constants.NEWLINE) {
                    val newText = "" + s.subSequence(start, start + count)
                    aztecText.disableTextChangedListener()
                    aztecText.text.insert(it.end, Constants.NEWLINE_STRING)
                    aztecText.text.delete(start, start + count)
                    aztecText.text.insert(it.end, newText)
                    aztecText.enableTextChangedListener()
                    aztecText.setSelection(it.end + newText.length)
                }
            }
        }

        val spans = SpanWrapper.getSpans<CaptionShortcodeSpan>(aztecText.text, 0, aztecText.length(),
                CaptionShortcodeSpan::class.java)
        spans.forEach {

            // if a caption's beginning is behind an image, align it with the image beginning
            if (it.start < aztecText.length() && aztecText.text[it.start] != Constants.IMG_CHAR) {
                if (it.start > 1 && aztecText.text[it.start - 1] == Constants.NEWLINE &&
                        aztecText.text[it.start - 2] == Constants.IMG_CHAR) {
                    it.flags = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    it.start -= 2
                }
            }

            // remove captions that are not attached to any image
            if (it.start < aztecText.length() && aztecText.text[it.start] != Constants.IMG_CHAR) {
                val spanStart = it.start
                var spanEnd = it.end
                if (spanEnd < aztecText.length()) {
                    spanEnd = it.end - 1
                }
                it.remove()
                aztecText.text.delete(spanStart, spanEnd)
                return@forEach
            }

            // remove captions that are blank
            if (it.start < aztecText.length() && count == 0 && it.span.caption.isBlank() && !aztecText.isTextChangedListenerDisabled()) {
                it.remove()
                return@forEach
            }

            // if a caption's ending doesn't align with an ending of a line immediately following an image, align them
            // if the last line is the end of the text, make the caption end there
            val imgCharPosition = aztecText.text.indexOf(Constants.IMG_CHAR, it.start)
            val secondNewline = aztecText.text.indexOf(Constants.NEWLINE, imgCharPosition + 2)
            val correctEnding = if (secondNewline != -1) secondNewline + 1 else aztecText.length()

            if (imgCharPosition != -1 && it.end != correctEnding &&
                    it.start < aztecText.length() && aztecText.text[it.start] == Constants.IMG_CHAR) {
                it.flags = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                it.end = correctEnding
            }
        }
    }
}