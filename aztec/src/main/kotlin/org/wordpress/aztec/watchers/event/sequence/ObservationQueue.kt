package org.wordpress.aztec.watchers.event.sequence

import android.os.Build
import org.wordpress.aztec.watchers.event.IEventInjector
import org.wordpress.aztec.watchers.event.buckets.API26Bucket
import org.wordpress.aztec.watchers.event.text.TextWatcherEvent
import org.wordpress.aztec.watchers.event.buckets.Bucket

class ObservationQueue(injector: IEventInjector) : EventSequence<TextWatcherEvent>() {
    val buckets = ArrayList<Bucket>()
    val injector = injector

    init {
        init()
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
        for (bucket in buckets) {
            for (operation in bucket.userOperations) {
                if (size < operation.sequence.size) {
                    if (operation.isUserOperationPartiallyObservedInSequence(this)) {
                        foundOnePartialMatch = true
                    }
                } else {
                    // does this particular event look like a part of any of the user operations as defined in this bucket?
                    if (operation.isUserOperationObservedInSequence(this)) {
                        // replace user operation with ONE TextWatcherEvent and inject this one in the actual
                        // textwatchers
                        val replacementEvent = operation.buildReplacementEventWithSequenceData(this)
                        injector.executeEvent(replacementEvent)
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

    private fun init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            buckets.add(API26Bucket())
        }
        /*
            remember to add here any other buckets and init logic as suitable, depending on the context
         */
    }
}

