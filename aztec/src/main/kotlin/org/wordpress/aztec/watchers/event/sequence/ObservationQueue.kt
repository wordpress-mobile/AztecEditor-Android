package org.wordpress.aztec.watchers.event.sequence

import android.os.Build
import org.wordpress.aztec.watchers.event.IEventInjector
import org.wordpress.aztec.watchers.event.buckets.API25Bucket
import org.wordpress.aztec.watchers.event.buckets.API26Bucket
import org.wordpress.aztec.watchers.event.buckets.Bucket
import org.wordpress.aztec.watchers.event.text.TextWatcherEvent

class ObservationQueue(val injector: IEventInjector) : EventSequence<TextWatcherEvent>() {
    val buckets = ArrayList<Bucket>()

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            buckets.add(API26Bucket())
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            buckets.add(API25Bucket())
        }
        /*
            remember to add here any other buckets and init logic as suitable, depending on the context
         */
    }

    fun hasActiveBuckets() : Boolean {
        return buckets.size > 0
    }

    override fun add(element: TextWatcherEvent): Boolean {
        synchronized(this@ObservationQueue) {
            val added: Boolean = super.add(element)
            if (buckets.size == 0) {
                return added
            }
            if (added) {
                processQueue()
            }
            return added
        }
    }

    private fun processQueue() {
        // here let's check whether our current queue matches / fits any of the installed buckets
        var foundOnePartialMatch = false

        // if we only have 2 events and the first one is older than xxx milliseconds,
        // that means that event is certainly not worth observing so let's discard that one
        // we never pile up events we are not interested in this way
        if (size == 2) {
            val timeDistance = this.get(1).timestamp - this.get(0).timestamp
            if (timeDistance > ObservationQueue.MAXIMUM_TIME_BETWEEN_EVENTS_IN_PATTERN_MS) {
                removeAt(0)
            }
        }

        // now let's continue processing
        for (bucket in buckets) {
            for (operation in bucket.userOperations) {
                if (size < operation.sequence.size) {
                    if (operation.isUserOperationPartiallyObservedInSequence(this)) {
                        foundOnePartialMatch = true
                    }
                } else {
                    // does this particular event look like a part of any of the user operations as defined in this bucket?
                    val result = operation.isUserOperationObservedInSequence(this)
                    if (operation.isFound(result)) {
                        // replace user operation with ONE TextWatcherEvent and inject this one in the actual
                        // textwatchers
                        val replacementEvent = operation.buildReplacementEventWithSequenceData(this)
                        injector.executeEvent(replacementEvent)
                        clear()
                    }

                    // regardless of the operation being found, let's check if it needs the queue to be cleared
                    if (operation.needsClear(result)) {
                        clear()
                    }
                }
            }
        }

        // we didn't find neither a partial match nor a total match, let's just clear the queue
        if (size > 0 && !foundOnePartialMatch) {
            // immediately discard the queue
            clear()
        }
    }

    companion object {
        val MAXIMUM_TIME_BETWEEN_EVENTS_IN_PATTERN_MS = 100
    }
}

