package org.wordpress.aztec.watchers


import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
    var frameworkEvent = false

    var previousInputWasSuggestion = false
    var previousInputEventWasRegular = false

    //cached values of beforeTextChanged event
    var previousStart = -1
    var previousCount = -1
    var beforeAfter = -1

    var spaceWasDeleted = false

    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
        if (aztecTextRef.get()?.isTextChangedListenerDisabled() ?: true) {
            return
        }
        Log.v("SuggestionWatcher", "selectionStart: ${aztecTextRef.get()?.selectionStart} selectionEnd: ${aztecTextRef.get()?.selectionEnd}")
        Log.v("SuggestionWatcher", "start: $start count:$count after: $after")

        textChangedEventDetails = TextChangedEvent(text.toString())

        val selectionStart = aztecTextRef.get()?.selectionStart
        val selectionEnd = aztecTextRef.get()?.selectionEnd

        val isMultiSelection = selectionStart != selectionEnd

//        val spaceIsDeleted = false

//        possibly suggestion framework event
        frameworkEvent = (selectionStart != start+1 && after == 0 && !isMultiSelection)
//        frameworkEvent = (count > 1 && after == 0 && !isMultiSelection)

        isRestoringSuggestedText = previousStart == start && previousCount == after && previousInputWasSuggestion


        if (!frameworkEvent && !isRestoringSuggestedText && !isMultiSelection) {
            Log.v("SuggestionWatcher", "Normal Typing")
            aztecTextRef.get()?.enableOnSelectionListener()
            inlineFormatter.clearCarriedOverSpans()
            if (after > 0) {
                Log.v("SuggestionWatcher", "Carrying over spans")
                inlineFormatter.carryOverInlineSpans(start, count, after, frameworkEvent)
            }
            previousInputEventWasRegular = true
        } else if (frameworkEvent && previousInputEventWasRegular) {
            Log.v("SuggestionWatcher", "Multiple characters were deleted.")
            //disable selection because moving cursor will cause selected style to reset
            aztecTextRef.get()?.disableOnSelectionListener()

            inlineFormatter.clearCarriedOverSpans()
            inlineFormatter.carryOverInlineSpans(start, count, after, frameworkEvent)
            Log.v("SuggestionWatcher", "Carrying over spans : " + inlineFormatter.carryOverSpans.size)
            previousInputEventWasRegular = false
        } else if (isRestoringSuggestedText) {
            Log.v("SuggestionWatcher", "Restoring text")
            aztecTextRef.get()?.disableInlineTextHandling()
            previousInputEventWasRegular = false
        }

        previousStart = start
        previousCount = count
        beforeAfter = after

//        spaceWasDeleted = spaceIsDeleted
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

        if (!frameworkEvent && inlineFormatter.carryOverSpans.size > 0) {
            Log.v("SuggestionWatcher", "Reapplying carried over span. Formatting is applied:" + aztecTextRef.get()?.formattingIsApplied())
            Log.v("SuggestionWatcher", "Reapplying carried over span. Carried over " + inlineFormatter.carryOverSpans.size + " spans")
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
            aztecTextRef.get()?.disableInlineTextHandling()
        }

        previousInputWasSuggestion = frameworkEvent
    }

    companion object {
        fun install(inlineFormatter: InlineFormatter, text: AztecText) {
            text.addTextChangedListener(SuggestionWatcher(inlineFormatter, text))
        }
    }
}
