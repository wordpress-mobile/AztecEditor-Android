package org.wordpress.aztec.watchers


import android.text.Editable
import android.text.Spanned
import android.text.TextWatcher
import android.util.Log
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.spans.IAztecInlineSpan
import java.lang.ref.WeakReference
import java.util.*

/**
 * Auto correct/suggestion in android often strip all the inline spans from the target words.
 *This watcher monitors detects this behavior and reapplies style to the target words.
 */
class SuggestionWatcher(aztecText: AztecText) : TextWatcher {
    private val aztecTextRef: WeakReference<AztecText?> = WeakReference(aztecText)

    data class CarryOverSpan(val span: IAztecInlineSpan, val start: Int, val end: Int)
    private var textChangedEventDetails = TextChangedEvent("", 0, 0, 0)
    val carryOverSpans = ArrayList<CarryOverSpan>()


    var isRestoringSuggestedText = false
    var frameworkEvent = false

    var previousInputWasSuggestion = false
    var previousInputEventWasRegular = false

    //cached values of beforeTextChanged event
    var previousStart = -1
    var previousCount = -1
    var beforeAfter = -1

    var deletedAutoDot = false


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

//      possibly suggestion framework event
        frameworkEvent = (selectionStart != start + 1 && after == 0 && !isMultiSelection)

        isRestoringSuggestedText = previousStart == start && (previousCount == after || deletedAutoDot) && previousInputWasSuggestion


        if (!frameworkEvent && !isRestoringSuggestedText && !isMultiSelection) {
            Log.v("SuggestionWatcher", "Normal Typing")
            aztecTextRef.get()?.enableOnSelectionListener()
            clearCarriedOverSpans()
            if (after > 0) {
                Log.v("SuggestionWatcher", "Carrying over spans")
                carryOverInlineSpans(text as Editable,start, count, after, frameworkEvent)
            }
            previousInputEventWasRegular = true
        } else if (frameworkEvent && previousInputEventWasRegular) {
            Log.v("SuggestionWatcher", "Multiple characters were deleted.")
            //disable selection because moving cursor will cause selected style to reset
            aztecTextRef.get()?.disableOnSelectionListener()

            clearCarriedOverSpans()
            carryOverInlineSpans(text as Editable,start, count, after, frameworkEvent,text.length > start && text[start] == '.' && count == 2 && !isMultiSelection && selectionStart == start + 2)
            Log.v("SuggestionWatcher", "Carrying over spans : " + carryOverSpans.size)
            previousInputEventWasRegular = false
        } else if (isRestoringSuggestedText) {
            Log.v("SuggestionWatcher", "Restoring text")
            aztecTextRef.get()?.disableInlineTextHandling()
            previousInputEventWasRegular = false
            deletedAutoDot = false
        }

        previousStart = start
        previousCount = count
        beforeAfter = after
        deletedAutoDot = text.length > start && text[start] == '.' && count == 2 && !isMultiSelection && selectionStart == start + 2
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

        if (!frameworkEvent && carryOverSpans.size > 0) {
            Log.v("SuggestionWatcher", "Reapplying carried over span. Formatting is applied:" + aztecTextRef.get()?.formattingIsApplied())
            Log.v("SuggestionWatcher", "Reapplying carried over span. Carried over " + carryOverSpans.size + " spans")
            reapplyCarriedOverInlineSpans(text as Editable)
        }

        if (isRestoringSuggestedText) {
            clearCarriedOverSpans()
        }
    }


    fun clearCarriedOverSpans() {
        carryOverSpans.clear()
    }

    fun carryOverInlineSpans(editableText: Editable, start: Int, count: Int, after: Int, multipleCharactersWereDeleted: Boolean, deletedAutodot: Boolean = false) {


        val charsAdded = after - count
        val isAddingCharacters = charsAdded >= 0 && count > 0

        if (isAddingCharacters) {
            editableText.getSpans(start, start + count, IAztecInlineSpan::class.java).forEach {
                val spanStart = editableText.getSpanStart(it)
                val spanEnd = editableText.getSpanEnd(it)

                editableText.removeSpan(it)
                carryOverSpans.add(CarryOverSpan(it, spanStart, spanEnd))
            }
        } else if (charsAdded < 0 && count > 0) {
            if (!multipleCharactersWereDeleted) {
                editableText.getSpans(start, start + after, IAztecInlineSpan::class.java).forEach {
                    val spanStart = editableText.getSpanStart(it)
                    val spanEnd = if (editableText.getSpanEnd(it) > start + after) start + after else editableText.getSpanEnd(it)
                    editableText.removeSpan(it)
                    carryOverSpans.add(CarryOverSpan(it, spanStart, spanEnd))
                }
            } else {
                editableText.getSpans(start, start + count, IAztecInlineSpan::class.java).forEach {
                    val spanStart = editableText.getSpanStart(it)
                    val spanEnd = if (editableText.getSpanEnd(it) > start + count) start + count else editableText.getSpanEnd(it)
                    editableText.removeSpan(it)
                    carryOverSpans.add(CarryOverSpan(it, spanStart, spanEnd - if (deletedAutodot) 1 else 0))
                }
            }

        }
    }

    fun reapplyCarriedOverInlineSpans(editableText: Editable) {
        carryOverSpans.forEach {
            editableText.setSpan(it.span, it.start, it.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
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
        fun install(text: AztecText) {
            text.addTextChangedListener(SuggestionWatcher(text))
        }
    }
}
