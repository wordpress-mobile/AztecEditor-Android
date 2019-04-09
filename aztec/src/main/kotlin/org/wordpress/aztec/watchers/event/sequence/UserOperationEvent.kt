package org.wordpress.aztec.watchers.event.sequence

import org.wordpress.aztec.spans.AztecCodeSpan
import org.wordpress.aztec.spans.AztecHeadingSpan
import org.wordpress.aztec.spans.AztecListItemSpan
import org.wordpress.aztec.spans.AztecPreformatSpan
import org.wordpress.aztec.watchers.event.text.BeforeTextChangedEventData
import org.wordpress.aztec.watchers.event.text.TextWatcherEvent

abstract class UserOperationEvent(var sequence: EventSequence<TextWatcherEvent> = EventSequence()) {

    enum class ObservedOperationResultType {
        SEQUENCE_FOUND,
        SEQUENCE_NOT_FOUND,
        SEQUENCE_FOUND_CLEAR_QUEUE
    }

    fun isFound(resultType: ObservedOperationResultType) : Boolean {
        return resultType == ObservedOperationResultType.SEQUENCE_FOUND
    }

    fun needsClear(resultType: ObservedOperationResultType) : Boolean {
        return resultType == ObservedOperationResultType.SEQUENCE_FOUND_CLEAR_QUEUE
    }

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

    fun isEventFoundWithinABlock(data: BeforeTextChangedEventData) : Boolean {
        // ok finally let's make sure  we are not within a Block element
        val inputStart = data.start + data.count
        val inputEnd = data.start + data.count + 1

        val text = data.textBefore!!
        val isInsideList = text.getSpans(inputStart, inputEnd, AztecListItemSpan::class.java).isNotEmpty()
        val isInsidePre = text.getSpans(inputStart, inputEnd, AztecPreformatSpan::class.java).isNotEmpty()
        val isInsideCode = text.getSpans(inputStart, inputEnd, AztecCodeSpan::class.java).isNotEmpty()
        var insideHeading = text.getSpans(inputStart, inputEnd, AztecHeadingSpan::class.java).isNotEmpty()

        if (insideHeading && (text.length > inputEnd && text[inputEnd] == '\n')) {
            insideHeading = false
        }

        return isInsideList || insideHeading || isInsidePre || isInsideCode
    }

    abstract fun isUserOperationObservedInSequence(sequence: EventSequence<TextWatcherEvent>) : ObservedOperationResultType
    abstract fun buildReplacementEventWithSequenceData(sequence: EventSequence<TextWatcherEvent>) : TextWatcherEvent
}

