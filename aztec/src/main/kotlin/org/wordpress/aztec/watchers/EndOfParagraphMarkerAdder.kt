package org.wordpress.aztec.watchers

import android.text.Editable
import android.text.Spanned
import android.text.TextWatcher
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.spans.AztecHeadingSpan
import org.wordpress.aztec.spans.AztecListItemSpan
import org.wordpress.aztec.spans.EndOfParagraphMarker
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
    }

    override fun afterTextChanged(text: Editable) {
        if (textChangedEventDetails.isNewLineButNotAtTheBeginning()) {
            if (isTargetForParagraphMarker(textChangedEventDetails)) {
                text.setSpan(EndOfParagraphMarker(verticalParagraphMargin), textChangedEventDetails.inputStart, textChangedEventDetails.inputEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            return
        }
    }

    fun isTargetForParagraphMarker(textChangedEvent: TextChangedEvent): Boolean {
        val aztecText = aztecTextRef.get()
        if (aztecText != null && !aztecText.isTextChangedListenerDisabled() && aztecText.isInCalypsoMode) {
            val isInsideList = aztecText.text.getSpans(textChangedEvent.inputStart, textChangedEvent.inputEnd, AztecListItemSpan::class.java).isNotEmpty()

            var insideHeading = aztecText.text.getSpans(textChangedEvent.inputStart, textChangedEvent.inputEnd, AztecHeadingSpan::class.java).isNotEmpty()

            if (insideHeading && (aztecText.text.length > textChangedEvent.inputEnd && aztecText.text[textChangedEvent.inputEnd] == '\n')) {
                insideHeading = false
            }
            return !isInsideList && !insideHeading
        }

        return false
    }

    companion object {
        fun install(editText: AztecText, verticalParagraphMargin: Int) {
            editText.addTextChangedListener(EndOfParagraphMarkerAdder(editText, verticalParagraphMargin))
        }

    }
}
