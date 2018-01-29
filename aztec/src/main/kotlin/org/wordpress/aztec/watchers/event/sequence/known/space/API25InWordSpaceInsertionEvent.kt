package org.wordpress.aztec.watchers.event.sequence.known.space

import org.apache.commons.lang3.StringUtils
import org.wordpress.aztec.watchers.event.sequence.EventSequence
import org.wordpress.aztec.watchers.event.sequence.UserOperationEvent
import org.wordpress.aztec.watchers.event.sequence.known.space.steps.TextWatcherEventDeleteText
import org.wordpress.aztec.watchers.event.sequence.known.space.steps.TextWatcherEventInsertText
import org.wordpress.aztec.watchers.event.text.AfterTextChangedEventData
import org.wordpress.aztec.watchers.event.text.TextWatcherEvent

/*
 This case implements the behavior observed in https://github.com/wordpress-mobile/AztecEditor-Android/issues/555
 */
class API25InWordSpaceInsertionEvent : UserOperationEvent() {
    private val SPACE = ' '
    private val SPACE_STRING = "" + SPACE
    private val MAXIMUM_TIME_BETWEEN_EVENTS_IN_PATTERN_MS = 50

    init {
        // here we populate our model of reference (which is the sequence of events we expect to find)
        // note we don' populate the TextWatcherEvents with actual data, but rather we just want
        // to instantiate them so we can populate them later and test whether data holds true to their
        // validation.

        // 2 generic deletes, followed by 2 generic inserts
        val builder = TextWatcherEventDeleteText.Builder()
        val step1 = builder.build()

        val builderStep2 = TextWatcherEventDeleteText.Builder()
        val step2 = builderStep2.build()

        val builderStep3 = TextWatcherEventInsertText.Builder()
        val step3 = builderStep3.build()

        val builderStep4 = TextWatcherEventInsertText.Builder()
        val step4 = builderStep4.build()

        // add each of the steps that make up for the identified API25InWordSpaceInsertionEvent here
        clear()
        addSequenceStep(step1)
        addSequenceStep(step2)
        addSequenceStep(step3)
        addSequenceStep(step4)
    }

    override fun isUserOperationObservedInSequence(sequence: EventSequence<TextWatcherEvent>): Boolean {
        /* here check:

        If we have 2 deletes followed by 2 inserts AND:
        1) checking the first BEFORETEXTCHANGED and
        2) checking the LAST AFTERTEXTCHANGED
        text length is longer by 1, and the item that is now located at the first BEFORETEXTCHANGED is a SPACE character.

         */
        if (this.sequence.size == sequence.size) {

            // populate data in our own sequence to be able to run the comparator checks
            if (!isUserOperationPartiallyObservedInSequence(sequence)) {
                return false
            }

            // ok all events are good individually and match the sequence we want to compare against.
            // now let's make sure the BEFORE / AFTER situation is what we are trying to identify
            val firstEvent = sequence.first()
            val lastEvent = sequence[sequence.size - 1]

            // if new text length is longer than original text by 1
            if (firstEvent.beforeEventData.textBefore?.length == lastEvent.afterEventData.textAfter!!.length - 1) {
                // now check that the inserted character is actually a space
                //val (_, start, count) = firstEvent.beforeEventData
                val data = firstEvent.beforeEventData
                if (lastEvent.afterEventData.textAfter!![data.start + data.count] == SPACE) {
                    return true
                }
            }
        }

        return false
    }

    override fun isUserOperationPartiallyObservedInSequence(sequence: EventSequence<TextWatcherEvent>): Boolean {
        for (i in sequence.indices) {

            val eventHolder = this.sequence[i]
            val observableEvent = sequence[i]

            // if time distance between any of the events is longer than 50 millis, discard this as this pattern is
            // likely not produced by the platform, but rather the user.
            // WARNING! When debugging with breakpoints, you should disable this check as time can exceed the 50 MS limit and
            // create undesired behavior.
            if (i > 0) { // only try to compare when we have at least 2 events, so we can compare with the previous one
                val timestampForPreviousEvent = sequence[i - 1].timestamp
                val timeDistance = observableEvent.timestamp - timestampForPreviousEvent
                if (timeDistance > MAXIMUM_TIME_BETWEEN_EVENTS_IN_PATTERN_MS) {
                    return false
                }
            }

            eventHolder.beforeEventData = observableEvent.beforeEventData
            eventHolder.onEventData = observableEvent.onEventData
            eventHolder.afterEventData = observableEvent.afterEventData

            // return immediately as soon as we realize the pattern diverges
            if (!eventHolder.testFitsBeforeOnAndAfter()) {
                return false
            }
        }

        return true
    }

    override fun buildReplacementEventWithSequenceData(sequence: EventSequence<TextWatcherEvent>): TextWatcherEvent {
        val builder = TextWatcherEventInsertText.Builder()
        // here make it all up as a unique event that does the insert as usual, as we'd get it on older APIs
        val firstEvent = sequence.first()
        val lastEvent = sequence[sequence.size - 1]

        val (oldText) = firstEvent.beforeEventData

        val differenceIndex = StringUtils.indexOfDifference(oldText, lastEvent.afterEventData.textAfter)
        oldText?.insert(differenceIndex, SPACE_STRING)

        builder.afterEventData = AfterTextChangedEventData(oldText)
        val replacementEvent = builder.build()
        replacementEvent.insertionStart = differenceIndex
        replacementEvent.insertionLength = 1

        return replacementEvent
    }
}
