package org.wordpress.aztec.watchers.event

import org.wordpress.aztec.watchers.TextChangedEvent

data class UserOperationEvent(val eventSequence: ArrayList<TextChangedEvent>) {

    var sequence: ArrayList<TextChangedEvent> = ArrayList<TextChangedEvent>()

    fun addSequenceStep(event: TextChangedEvent) {
        sequence.add(event)
    }

}

