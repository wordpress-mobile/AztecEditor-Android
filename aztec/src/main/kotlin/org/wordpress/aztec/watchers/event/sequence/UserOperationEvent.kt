package org.wordpress.aztec.watchers.event.sequence

import org.wordpress.aztec.watchers.event.text.TextWatcherEvent

abstract class UserOperationEvent(var sequence: EventSequence<TextWatcherEvent> = EventSequence()) {

    fun addSequenceStep(event: TextWatcherEvent) {
        sequence.add(event)
    }

    fun equals(anotherSequence: UserOperationEvent) {
        return this.equals(anotherSequence)
    }
}

