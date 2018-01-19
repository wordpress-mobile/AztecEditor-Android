package org.wordpress.aztec.watchers.event.sequence.known.space.steps

import org.wordpress.aztec.watchers.EndOfBufferMarkerAdder
import org.wordpress.aztec.watchers.event.text.AfterTextChangedEventData
import org.wordpress.aztec.watchers.event.text.BeforeTextChangedEventData
import org.wordpress.aztec.watchers.event.text.OnTextChangedEventData
import org.wordpress.aztec.watchers.event.text.TextWatcherEvent

class TextWatcherEventInsertText(beforeEventData: BeforeTextChangedEventData, onEventData: OnTextChangedEventData, afterEventData: AfterTextChangedEventData) : TextWatcherEvent(beforeEventData, onEventData, afterEventData) {

    private var beforeText: CharSequence? = null
    var insertionStart: Int = 0
    var insertionLength: Int = 0

    private fun testBeforeTextChangedEventData(data: BeforeTextChangedEventData): Boolean {
        beforeText = data.textBefore
        return data.count == 0 && data.after > 0
    }

    private fun testOnTextChangedEventData(data: OnTextChangedEventData): Boolean {
        return data.start >= 0 && data.count > 0 && data.textOn!!.length > 0
    }

    private fun testAfterTextChangedEventData(data: AfterTextChangedEventData): Boolean {
        return EndOfBufferMarkerAdder.safeLength(beforeText!!) < EndOfBufferMarkerAdder.safeLength(data.textAfter!!)
    }

    override fun testFitsBeforeOnAndAfter(): Boolean {
        return (testBeforeTextChangedEventData(beforeEventData)
                && testOnTextChangedEventData(onEventData)
                && testAfterTextChangedEventData(afterEventData))
    }

    class Builder : TextWatcherEvent.Builder() {
        override fun build(): TextWatcherEventInsertText {
            super.setGenericEventDataIfNotInit()
            return TextWatcherEventInsertText(beforeEventData, onEventData, afterEventData)
        }
    }
}
