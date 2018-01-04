package org.wordpress.aztec.watchers

import android.text.Editable
import android.text.Spannable
import android.text.Spanned
import android.text.TextWatcher
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.spans.IAztecInlineSpan
import java.lang.ref.WeakReference
import java.util.ArrayList

/**
 * Auto correct/suggestion in android often strip all the inline spans from the target words.
 * This watcher monitors detects this behavior and reapplies style to the target words.
 */
class SuggestionWatcher(aztecText: AztecText) : TextWatcher {
    private val aztecTextRef: WeakReference<AztecText?> = WeakReference(aztecText)

    data class CarryOverSpan(val span: IAztecInlineSpan, val start: Int, val end: Int)

    private var textChangedEventDetails = TextChangedEvent("", 0, 0, 0)
    private val carryOverSpans = ArrayList<CarryOverSpan>()

    private var isRestoringSuggestedText = false
    private var frameworkEvent = false

    private var previousInputWasSuggestion = false
    private var previousInputEventWasRegular = false

    private var previousStart = -1
    private var previousCount = -1
    private var beforeAfter = -1

    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
        if (aztecTextRef.get()?.isTextChangedListenerDisabled() != false || text !is Spannable) {
            return
        }
        textChangedEventDetails = TextChangedEvent(text.toString())

        val selectionStart = aztecTextRef.get()?.selectionStart
        val selectionEnd = aztecTextRef.get()?.selectionEnd

        val isMultiSelection = selectionStart != selectionEnd

        //possibly autocorrect framework event
        frameworkEvent = (selectionStart != start + 1 && after == 0 && !isMultiSelection && count > 1)

        isRestoringSuggestedText = previousStart == start && (previousCount == after) && previousInputWasSuggestion

        if (!frameworkEvent && !isRestoringSuggestedText && !isMultiSelection) {
            aztecTextRef.get()?.enableOnSelectionListener()
            clearCarriedOverSpans()
            carryOverInlineSpans(text, start, count, after)
            previousInputEventWasRegular = true
        } else if (frameworkEvent && previousInputEventWasRegular) {
            //disable selection because moving cursor will cause selected style to reset
            aztecTextRef.get()?.disableOnSelectionListener()
            carryOverInlineSpans(text, start, count, after)
            previousInputEventWasRegular = false
        } else if (isRestoringSuggestedText) {
            aztecTextRef.get()?.disableInlineTextHandling()
            previousInputEventWasRegular = false
        }

        previousStart = start
        previousCount = count
        beforeAfter = after
    }

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
        if (aztecTextRef.get()?.isTextChangedListenerDisabled() != false || text !is Spannable) {
            return
        }

        textChangedEventDetails.before = before
        textChangedEventDetails.text = text
        textChangedEventDetails.countOfCharacters = count
        textChangedEventDetails.start = start
        textChangedEventDetails.initialize()

        if (!frameworkEvent && carryOverSpans.size > 0) {
            reapplyCarriedOverInlineSpans(text)
        }

        if (isRestoringSuggestedText) {
            clearCarriedOverSpans()
        }
    }

    override fun afterTextChanged(text: Editable) {
        if (aztecTextRef.get()?.isTextChangedListenerDisabled() != false) {
            return
        }

        if (isRestoringSuggestedText) {
            isRestoringSuggestedText = false
            aztecTextRef.get()?.enableOnSelectionListener()
            aztecTextRef.get()?.disableInlineTextHandling()
        }

        previousInputWasSuggestion = frameworkEvent
    }

    private fun clearCarriedOverSpans() {
        carryOverSpans.clear()
    }

    private fun carryOverInlineSpans(editableText: Spannable, start: Int, count: Int, after: Int) {
        val charsAdded = after - count
        val isAddingCharacters = charsAdded >= 0 && count > 0

        if (isAddingCharacters) {
            editableText.getSpans(start, start + count, IAztecInlineSpan::class.java).forEach {
                val spanStart = editableText.getSpanStart(it)
                val spanEnd = editableText.getSpanEnd(it)
                carryOverSpans.add(CarryOverSpan(it, spanStart, spanEnd))
            }
        } else if (charsAdded < 0 && count > 0) {
            if (count - after <= 1) {
                editableText.getSpans(start, start + after, IAztecInlineSpan::class.java).forEach {
                    val spanStart = editableText.getSpanStart(it)
                    var spanEnd = editableText.getSpanEnd(it)

                    if ((start == spanEnd && editableText[start] == ' ') || start + after >= spanEnd) {
                        //do nothing
                    } else if (start < spanEnd && count - after == 1) {
                        spanEnd--
                    }
                    carryOverSpans.add(CarryOverSpan(it, spanStart, spanEnd))
                }
            } else {
                editableText.getSpans(start, start + count, IAztecInlineSpan::class.java).forEach {
                    val replacingDoubleSpaceDot = count == 2 && editableText[start] == ' ' && editableText[start + 1] == ' '

                    val spanStart = editableText.getSpanStart(it)
                    val spanEnd = if (editableText.getSpanEnd(it) >= start + count && !replacingDoubleSpaceDot) editableText.getSpanEnd(it) - (count - after) else editableText.getSpanEnd(it)
                    carryOverSpans.add(CarryOverSpan(it, spanStart, spanEnd))
                }
            }
        }
    }

    private fun reapplyCarriedOverInlineSpans(editableText: Spannable) {
        carryOverSpans.forEach {
            if (it.start >= 0 && it.end <= editableText.length && it.start < it.end) {
                editableText.setSpan(it.span, it.start, it.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    companion object {
        fun install(text: AztecText) {
            text.addTextChangedListener(SuggestionWatcher(text))
        }
    }
}
