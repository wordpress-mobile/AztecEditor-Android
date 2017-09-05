package org.wordpress.aztec.watchers


import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.formatting.InlineFormatter
import java.lang.ref.WeakReference

class SuggestionWatcher(var inlineFormatter: InlineFormatter, aztecText: AztecText) : TextWatcher {
    private val aztecTextRef: WeakReference<AztecText?> = WeakReference(aztecText)

    private var textChangedEventDetails = TextChangedEvent("", 0, 0, 0)

    var restoringSuggestedText = false

    var multipleCharactersWereDeleted = false
    var multipleCharactersWereDeletedBefore = false
    var wasNormalTyping = false

    var beforeStart = -1
    var beforeCount = -1
    var beforeAfter = -1

    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
        if (aztecTextRef.get()?.isTextChangedListenerDisabled() ?: true) {
            return
        }

        textChangedEventDetails = TextChangedEvent(text.toString())


        Log.v("SuggestionWatch", "SuggestionWatcher: beforeTextChanged, text:$text start:$start count:$count after:$after")


        multipleCharactersWereDeleted = (count > 1 && after == 0 && aztecTextRef.get()?.selectionStart == aztecTextRef.get()?.selectionEnd)
        restoringSuggestedText = beforeStart == start && beforeCount == after && multipleCharactersWereDeletedBefore


        if (!multipleCharactersWereDeleted && !restoringSuggestedText) {
            aztecTextRef.get()?.enableOnSelectionListener()
            Log.v("SuggestionWatch", "SuggestionWatcher: Normal Typing. Formatting is applied:" + aztecTextRef.get()?.formattingIsApplied())
            inlineFormatter.clearCarriedOverSpans()
            if (after > 0) {
                inlineFormatter.carryOverInlineSpans(start, count, after, multipleCharactersWereDeleted)
            }
            wasNormalTyping = true
        } else if (multipleCharactersWereDeleted) {
            Log.v("SuggestionWatch", "SuggestionWatcher: Multiple Characters were deleted. Formatting is applied:" + aztecTextRef.get()?.formattingIsApplied())
            aztecTextRef.get()?.disableOnSelectionListener()
            inlineFormatter.clearCarriedOverSpans()
            inlineFormatter.carryOverInlineSpans(start, count, after, multipleCharactersWereDeleted)
            wasNormalTyping = false
        } else if (restoringSuggestedText) {
            Log.v("SuggestionWatch", "SuggestionWatcher: Restoring suggested text. Formatting is applied:" + aztecTextRef.get()?.formattingIsApplied())
            aztecTextRef.get()?.disableOnSelectionListener()
            wasNormalTyping = false
        }


        beforeStart = start
        beforeCount = count
        beforeAfter = after
    }

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
        if (aztecTextRef.get()?.isTextChangedListenerDisabled() ?: true) {
            return
        }

        textChangedEventDetails.before = before
        textChangedEventDetails.text = text
        textChangedEventDetails.countOfCharacters = count
        textChangedEventDetails.start = start
        textChangedEventDetails.initialize()

        if (!multipleCharactersWereDeleted) {
            Log.v("SuggestionWatch", "SuggestionWatcher: Reapplying carried over span. Formatting is applied:" + aztecTextRef.get()?.formattingIsApplied())
            Log.v("SuggestionWatch", "SuggestionWatcher: Reapplying carried over span. Carried over " + inlineFormatter.carryOverSpans.size + " spans")
            inlineFormatter.reapplyCarriedOverInlineSpans()
        }

        if (restoringSuggestedText) {
            Log.v("SuggestionWatch", "SuggestionWatcher: Clearing Carried over span")
            inlineFormatter.clearCarriedOverSpans()
        }

        Log.v("SuggestionWatch", "InlineTextWatcher: onTextChanged")
    }

    override fun afterTextChanged(text: Editable) {
        if (aztecTextRef.get()?.isTextChangedListenerDisabled() ?: true) {
            return
        }

        if (!multipleCharactersWereDeleted && !restoringSuggestedText) {
            Log.v("SuggestionWatch", "SuggestionWatcher: handleInlineStyling Formatting is applied:" + aztecTextRef.get()?.formattingIsApplied())
            inlineFormatter.handleInlineStyling(textChangedEventDetails)
        }


        if (restoringSuggestedText) {
            Log.v("SuggestionWatch", "SuggestionWatcher: Restored text. Formatting is applied:" + aztecTextRef.get()?.formattingIsApplied())
            restoringSuggestedText = false
            aztecTextRef.get()?.enableOnSelectionListener()
        }

        multipleCharactersWereDeletedBefore = multipleCharactersWereDeleted
    }


    companion object {
        fun install(inlineFormatter: InlineFormatter, text: AztecText) {
            text.addTextChangedListener(SuggestionWatcher(inlineFormatter, text))
        }
    }
}
