package org.wordpress.aztec.watchers.event

import org.wordpress.aztec.watchers.TextChangedEvent

data class UserOperationEvent(val eventSequence: EventSequence) {

    var sequence: EventSequence = EventSequence()

    fun addSequenceStep(event: TextChangedEvent) {
        sequence.add(event)
    }

}

