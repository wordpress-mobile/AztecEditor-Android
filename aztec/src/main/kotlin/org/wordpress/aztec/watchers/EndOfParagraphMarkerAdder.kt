package org.wordpress.aztec.watchers

import android.text.Editable
import android.text.Spanned
import android.text.TextWatcher
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.spans.AztecCodeSpan
import org.wordpress.aztec.spans.AztecHeadingSpan
import org.wordpress.aztec.spans.AztecListItemSpan
import org.wordpress.aztec.spans.AztecPreformatSpan
import org.wordpress.aztec.spans.EndOfParagraphMarker
import org.wordpress.aztec.spans.ParagraphSpan
import java.lang.ref.WeakReference

class EndOfParagraphMarkerAdder(aztecText: AztecText, val verticalParagraphMargin: Int) : TextWatcher {
    private val aztecTextRef: WeakReference<AztecText?> = WeakReference(aztecText)
    private var textChangedEventDetails = TextChangedEvent("", 0, 0, 0)

    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
        textChangedEventDetails = TextChangedEvent(text.toString())
    }

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
        textChangedEventDetails.before = before
        textChangedEventDetails.text = text
        textChangedEventDetails.countOfCharacters = count
        textChangedEventDetails.start = start
        textChangedEventDetails.initialize()

        if (!textChangedEventDetails.isNewLine()) return

        val aztecText = aztecTextRef.get()
        if (aztecText != null && !aztecText.isTextChangedListenerDisabled() && aztecText.isInCalypsoMode) {
            val inputStart = textChangedEventDetails.inputStart
            val inputEnd = textChangedEventDetails.inputEnd

            if (paragraphMarkerCanBeApplied(aztecText.text)) {
                aztecText.text.setSpan(EndOfParagraphMarker(verticalParagraphMargin), inputStart, inputEnd,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                // if we added a newline inside a paragraph, we should split it up into 2
                val paragraphs = aztecText.text.getSpans(inputStart, inputEnd, ParagraphSpan::class.java)
                if (paragraphs.isNotEmpty()) {
                    val paragraph = paragraphs.first()
                    val paragraphEnd = aztecText.text.getSpanEnd(paragraph)
                    if (paragraphEnd > inputEnd) {
                        val paragraphStart = aztecText.text.getSpanStart(paragraph)
                        val paragraphFlags = aztecText.text.getSpanFlags(paragraph)
                        aztecText.text.setSpan(paragraph, paragraphStart, inputEnd, paragraphFlags)
                    }
                }
            }
        }
    }

    fun paragraphMarkerCanBeApplied(text: Editable): Boolean {
        val inputStart = textChangedEventDetails.inputStart
        val inputEnd = textChangedEventDetails.inputEnd

        val isInsideList = text.getSpans(inputStart, inputEnd, AztecListItemSpan::class.java).isNotEmpty()
        val isInsidePre = text.getSpans(inputStart, inputEnd, AztecPreformatSpan::class.java).isNotEmpty()
        val isInsideCode = text.getSpans(inputStart, inputEnd, AztecCodeSpan::class.java).isNotEmpty()
        var insideHeading = text.getSpans(inputStart, inputEnd, AztecHeadingSpan::class.java).isNotEmpty()

        if (insideHeading && (text.length > inputEnd && text[inputEnd] == '\n')) {
            insideHeading = false
        }

        return !isInsideList && !insideHeading && !isInsidePre && !isInsideCode
    }

    override fun afterTextChanged(text: Editable) {
        text.getSpans(0, text.length, EndOfParagraphMarker::class.java).forEach {
            text.setSpan(it, text.getSpanStart(it), text.getSpanEnd(it), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    companion object {
        fun install(editText: AztecText, verticalParagraphMargin: Int) {
            editText.addTextChangedListener(EndOfParagraphMarkerAdder(editText, verticalParagraphMargin))
        }
    }
}
