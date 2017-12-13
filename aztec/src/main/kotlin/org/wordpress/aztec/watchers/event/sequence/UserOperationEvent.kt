package org.wordpress.aztec.watchers.event.sequence

import org.wordpress.aztec.watchers.event.text.TextWatcherEvent

abstract class UserOperationEvent(var sequence: EventSequence<TextWatcherEvent> = EventSequence()) {

    fun addSequenceStep(event: TextWatcherEvent) {
        sequence.add(event)
    }

    fun clear() {
        sequence.clear()
    }

    abstract fun isUserOperationObservedInSequence(sequence: EventSequence<TextWatcherEvent>) : Boolean
    abstract fun isUserOperationPartiallyObservedInSequence(sequence: EventSequence<TextWatcherEvent>) : Boolean
    abstract fun buildReplacementEventWithSequenceData(sequence: EventSequence<TextWatcherEvent>) : TextWatcherEvent
}

