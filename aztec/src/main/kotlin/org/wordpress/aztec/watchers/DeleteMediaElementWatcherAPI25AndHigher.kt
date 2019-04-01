package org.wordpress.aztec.watchers

import android.text.Editable
import android.text.TextWatcher
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.spans.AztecMediaSpan
import java.lang.ref.WeakReference

class DeleteMediaElementWatcherAPI25AndHigher(aztecText: AztecText) : TextWatcher {
    private val aztecTextRef: WeakReference<AztecText?> = WeakReference(aztecText)
    private var deleted = false
    private var queueHasBeenPopulatedInThisTimeframe = false
    private var deletedSpans = ArrayList<AztecMediaSpan>()

    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
        if (aztecTextRef.get()?.isTextChangedListenerDisabled() ?: true) {
            return
        }

        if (aztecTextRef.get()?.isMediaDeletedListenerDisabled() ?: true) {
            return
        }

        if (count > 0) {
            deleted = true

            // we need to save the spans for later reference, as these spans are going to not exist anymore by the time
            // the afterTextChanged event arrives. So, adding them to a local deletedSpans array.
            aztecTextRef.get()?.text?.getSpans(start, start + count, AztecMediaSpan::class.java)
                    ?.forEach {
                        deletedSpans.add(it)
                    }

            // only call the onMediaDeleted callback if we are sure the ObservationQueue has not been filled with
            // platform-only events in a short time. These platform-originated events shall not be confused with
            // real user deletions.
            aztecTextRef.get()?.postDelayed( object : Runnable {
                override fun run() {
                    if (!queueHasBeenPopulatedInThisTimeframe) {
                        deletedSpans.forEach { it.onMediaDeleted() }
                    }
                    // reset flag
                    deletedSpans.clear()
                    queueHasBeenPopulatedInThisTimeframe = false
                }
            }, 500)
        }
    }

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
        // no op
    }

    override fun afterTextChanged(text: Editable) {
        if (deleted && aztecTextRef.get()?.isObservationQueueBeingPopulated() ?: true) {
            queueHasBeenPopulatedInThisTimeframe = true
        }
    }

    companion object {
        fun install(text: AztecText) {
            text.addTextChangedListener(DeleteMediaElementWatcherAPI25AndHigher(text))
        }
    }
}
