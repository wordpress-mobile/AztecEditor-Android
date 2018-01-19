package org.wordpress.aztec.watchers.event.sequence

import org.wordpress.aztec.watchers.event.text.TextWatcherEvent

abstract class UserOperationEvent(var sequence: EventSequence<TextWatcherEvent> = EventSequence()) {

    fun addSequenceStep(event: TextWatcherEvent) {
        sequence.add(event)
    }

    fun clear() {
        sequence.clear()
    }

    fun isUserOperationPartiallyObservedInSequence(sequence: EventSequence<TextWatcherEvent>): Boolean {
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
                if (timeDistance > ObservationQueue.MAXIMUM_TIME_BETWEEN_EVENTS_IN_PATTERN_MS) {
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

    abstract fun isUserOperationObservedInSequence(sequence: EventSequence<TextWatcherEvent>) : Boolean
    abstract fun buildReplacementEventWithSequenceData(sequence: EventSequence<TextWatcherEvent>) : TextWatcherEvent
}

