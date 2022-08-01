package org.wordpress.aztec.watchers

import android.text.Editable
import android.text.TextWatcher
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.spans.AztecMediaSpan
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentLinkedQueue

class DeleteMediaElementWatcherAPI25AndHigher(aztecText: AztecText) : TextWatcher {
    private val aztecTextRef: WeakReference<AztecText?> = WeakReference(aztecText)
    private var deleted = false
    private var queueHasBeenPopulatedInThisTimeframe = false
    private val deletedSpans = ConcurrentLinkedQueue<AztecMediaSpan>()

    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
        if (aztecTextRef.get()?.isTextChangedListenerDisabled() != false) {
            return
        }

        if (aztecTextRef.get()?.isMediaDeletedListenerDisabled() != false) {
            return
        }

        if (count > 0) {
            deleted = true

            // we need to save the spans for later reference, as these spans are going to not exist anymore by the time
            // the afterTextChanged event arrives. So, adding them to a local deletedSpans array.
            aztecTextRef.get()?.text?.getSpans(start, start + count, AztecMediaSpan::class.java)
                    ?.forEach {
                        deletedSpans.add(it)
                        if (!queueHasBeenPopulatedInThisTimeframe) {
                            it.beforeMediaDeleted()
                        }
                    }
            // only call the onMediaDeleted callback if we are sure the ObservationQueue has not been filled with
            // platform-only events in a short time. These platform-originated events shall not be confused with
            // real user deletions.
            aztecTextRef.get()?.postDelayed({
                while (!queueHasBeenPopulatedInThisTimeframe && deletedSpans.isNotEmpty()) {
                    deletedSpans.poll().onMediaDeleted()
                }
                queueHasBeenPopulatedInThisTimeframe = false
            }, 500)
        }
    }

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
        // no op
    }

    override fun afterTextChanged(text: Editable) {
        if (deleted && aztecTextRef.get()?.isObservationQueueBeingPopulated() != false) {
            queueHasBeenPopulatedInThisTimeframe = true
        }
    }

    companion object {
        fun install(text: AztecText) {
            text.addTextChangedListener(DeleteMediaElementWatcherAPI25AndHigher(text))
        }
    }
}
