package org.wordpress.aztec.watchers.event.sequence

import android.os.Build
import org.wordpress.aztec.watchers.event.IEventInjector
import org.wordpress.aztec.watchers.event.buckets.API26Bucket
import org.wordpress.aztec.watchers.event.text.TextWatcherEvent
import org.wordpress.aztec.watchers.event.buckets.Bucket
import java.util.Timer
import kotlin.concurrent.schedule

class ObservationQueue(injector: IEventInjector) : EventSequence<TextWatcherEvent>() {
    val buckets = ArrayList<Bucket>()
    val injector = injector

    init {
        init()
    }

    @Synchronized override fun add(event: TextWatcherEvent): Boolean {
        val added: Boolean = super.add(event)
        if (added) {
            processQueue(event)
        }

        // There trigger a timer if queue length is > 0, and within the timer process when timeout is triggered do this
        // check: if it stays the same for more than 100 ms, execute and empty the queue
        if (size > 0) {
            val timer = Timer()
            timer.schedule(50000) {
                // if it stays the same for more than 100 ms, execute and empty the queue
                emptyQueue()
            }
        }
        return added
    }

    private fun emptyQueue() {
        if (size > 0) {
            for (queuedEvent in super.iterator()) {
                injector.executeEvent(queuedEvent)
                remove(queuedEvent)
            }
        }
    }

    private fun processQueue(event: TextWatcherEvent) {
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
                        val replacementEvent = operation.buildReplacementEventWithSequenceData(this);
                        injector.executeEvent(replacementEvent)
                        clear()
                    } else {
                        // delay execution and wait for more events to come, or clean the queue
                    }
                }
            }
        }

        // we didn't find neither a partial match nor a total match, let's just process the event normally
        if (size > 0 && !foundOnePartialMatch) {
            // immediately discard and execute the event
            remove(event)
            injector.executeEvent(event)
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

