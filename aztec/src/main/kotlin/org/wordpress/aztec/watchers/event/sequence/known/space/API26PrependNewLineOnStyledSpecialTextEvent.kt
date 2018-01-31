package org.wordpress.aztec.watchers.event.sequence.known.space

import org.wordpress.aztec.Constants
import org.wordpress.aztec.watchers.event.sequence.EventSequence
import org.wordpress.aztec.watchers.event.sequence.UserOperationEvent
import org.wordpress.aztec.watchers.event.sequence.known.space.steps.TextWatcherEventDeleteText
import org.wordpress.aztec.watchers.event.sequence.known.space.steps.TextWatcherEventInsertText
import org.wordpress.aztec.watchers.event.sequence.known.space.steps.TextWatcherEventInsertTextDelAfter
import org.wordpress.aztec.watchers.event.text.AfterTextChangedEventData
import org.wordpress.aztec.watchers.event.text.TextWatcherEvent

/*
 This case implements the behavior observed in https://github.com/wordpress-mobile/AztecEditor-Android/issues/610
 special case for block formated text like HEADING, LIST, etc.
 */
class API26PrependNewLineOnStyledSpecialTextEvent : UserOperationEvent() {

    init {
        // here we populate our model of reference (which is the sequence of events we expect to find)
        // note we don' populate the TextWatcherEvents with actual data, but rather we just want
        // to instantiate them so we can populate them later and test whether data holds true to their
        // validation.

        // 1 generic delete, followed by 1 special insert, then 1 generic insert
        val builder = TextWatcherEventDeleteText.Builder()
        val step1 = builder.build()

        val builderStep2 = TextWatcherEventInsertTextDelAfter.Builder()
        val step2 = builderStep2.build()

        val builderStep3 = TextWatcherEventInsertText.Builder()
        val step3 = builderStep3.build()

        // add each of the steps that make up for the identified API26InWordSpaceInsertionEvent here
        clear()
        addSequenceStep(step1)
        addSequenceStep(step2)
        addSequenceStep(step3)
    }

    override fun isUserOperationObservedInSequence(sequence: EventSequence<TextWatcherEvent>): ObservedOperationResultType {
        /* here check:

        If we have 1 delete followed by 2 inserts AND:
        1) checking the first BEFORETEXTCHANGED and
        2) checking the LAST AFTERTEXTCHANGED
        text length is longer by 1, and the item that is now located start of AFTERTEXTCHANGED is a NEWLINE character.

         */
        if (this.sequence.size == sequence.size) {

            // populate data in our own sequence to be able to run the comparator checks
            if (!isUserOperationPartiallyObservedInSequence(sequence)) {
                return ObservedOperationResultType.SEQUENCE_NOT_FOUND
            }

            // ok all events are good individually and match the sequence we want to compare against.
            // now let's make sure the BEFORE / AFTER situation is what we are trying to identify
            val firstEvent = sequence.first()
            val lastEvent = sequence.last()
            val midEvent = sequence[1]

            // if new text length is equal as original text length
            if (firstEvent.beforeEventData.textBefore?.length == lastEvent.afterEventData.textAfter!!.length) {
                //but, middle event has a new line at the start index of change
                if (midEvent.onEventData.textOn!![midEvent.onEventData.start] == Constants.NEWLINE) {
                    return ObservedOperationResultType.SEQUENCE_FOUND
                }
            }
        }

        return ObservedOperationResultType.SEQUENCE_NOT_FOUND
    }

    override fun buildReplacementEventWithSequenceData(sequence: EventSequence<TextWatcherEvent>): TextWatcherEvent {
        val builder = TextWatcherEventInsertText.Builder()
        // here make it all up as a unique event that does the insert as usual, as we'd get it on older APIs
        val firstEvent = sequence.first()

        val (oldText) = firstEvent.beforeEventData

        val indexWhereToInsertNewLine = firstEvent.beforeEventData.start
        oldText?.insert(indexWhereToInsertNewLine, Constants.NEWLINE_STRING)

        builder.afterEventData = AfterTextChangedEventData(oldText)
        val replacementEvent = builder.build()
        replacementEvent.insertionStart = indexWhereToInsertNewLine
        replacementEvent.insertionLength = 1

        return replacementEvent
    }
}
