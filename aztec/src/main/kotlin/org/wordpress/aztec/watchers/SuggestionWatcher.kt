package org.wordpress.aztec.watchers


import android.text.Editable
import android.text.TextWatcher
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.formatting.InlineFormatter
import java.lang.ref.WeakReference

/**
 * Auto correct/suggestion in android often strip all the inline spans from the target words.
 *This watcher monitors detects this behavior and reapplies style to the target words.
 */
class SuggestionWatcher(var inlineFormatter: InlineFormatter, aztecText: AztecText) : TextWatcher {
    private val aztecTextRef: WeakReference<AztecText?> = WeakReference(aztecText)

    private var textChangedEventDetails = TextChangedEvent("", 0, 0, 0)

    var isRestoringSuggestedText = false
    var multipleCharactersWereDeleted = false

    var previousInputWasSuggestion = false
    var previousInputEventWasRegular = false

    //cached values of beforeTextChanged event
    var previousStart = -1
    var previousCount = -1
    var beforeAfter = -1

    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
        if (aztecTextRef.get()?.isTextChangedListenerDisabled() ?: true) {
            return
        }

        textChangedEventDetails = TextChangedEvent(text.toString())

        val isMultiSelection = aztecTextRef.get()?.selectionStart != aztecTextRef.get()?.selectionEnd

        //possibly suggestion framework event
        multipleCharactersWereDeleted = (count > 1 && after == 0 && !isMultiSelection)

        isRestoringSuggestedText = previousStart == start && previousCount == after && previousInputWasSuggestion


        if (!multipleCharactersWereDeleted && !isRestoringSuggestedText && !isMultiSelection) {
            aztecTextRef.get()?.enableOnSelectionListener()
            inlineFormatter.clearCarriedOverSpans()
            if (after > 0) {
                inlineFormatter.carryOverInlineSpans(start, count, after, multipleCharactersWereDeleted)
            }
            previousInputEventWasRegular = true
        } else if (multipleCharactersWereDeleted && previousInputEventWasRegular) {
            //disable selection because moving cursor will cause selected style to reset
            aztecTextRef.get()?.disableOnSelectionListener()

            inlineFormatter.clearCarriedOverSpans()
            inlineFormatter.carryOverInlineSpans(start, count, after, multipleCharactersWereDeleted)
            previousInputEventWasRegular = false
        } else if (isRestoringSuggestedText) {
            aztecTextRef.get()?.disableOnSelectionListener()
            previousInputEventWasRegular = false
        }

        previousStart = start
        previousCount = count
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
            inlineFormatter.reapplyCarriedOverInlineSpans()
        }

        if (isRestoringSuggestedText) {
            inlineFormatter.clearCarriedOverSpans()
        }
    }

    override fun afterTextChanged(text: Editable) {
        if (aztecTextRef.get()?.isTextChangedListenerDisabled() ?: true) {
            return
        }

        if (isRestoringSuggestedText) {
            isRestoringSuggestedText = false
            aztecTextRef.get()?.enableOnSelectionListener()
        }

        previousInputWasSuggestion = multipleCharactersWereDeleted
    }


    companion object {
        fun install(inlineFormatter: InlineFormatter, text: AztecText) {
            text.addTextChangedListener(SuggestionWatcher(inlineFormatter, text))
        }
    }
}
