package org.wordpress.aztec.watchers.event.sequence.known.space.steps

import org.wordpress.aztec.watchers.event.text.AfterTextChangedEventData
import org.wordpress.aztec.watchers.event.text.BeforeTextChangedEventData
import org.wordpress.aztec.watchers.event.text.OnTextChangedEventData
import org.wordpress.aztec.watchers.event.text.TextWatcherEvent

class TextWatcherEventDeleteText(beforeEventData: BeforeTextChangedEventData, onEventData: OnTextChangedEventData, afterEventData: AfterTextChangedEventData) : TextWatcherEvent(beforeEventData, onEventData, afterEventData) {

    private var beforeText: CharSequence? = null

    private fun testBeforeTextChangedEventData(data: BeforeTextChangedEventData): Boolean {
        beforeText = data.textBefore
        return data.count > 0 && data.after == 0 && data.start + data.count <= data.textBefore!!.length
    }

    private fun testOnTextChangedEventData(data: OnTextChangedEventData): Boolean {
        return data.start >= 0 && data.count == 0 && data.textOn!!.length < beforeText!!.length
    }

    private fun testAfterTextChangedEventData(data: AfterTextChangedEventData): Boolean {
        return beforeText!!.length > data.textAfter!!.length
    }

    override fun testFitsBeforeOnAndAfter(): Boolean {
        return (testBeforeTextChangedEventData(beforeEventData)
                && testOnTextChangedEventData(onEventData)
                && testAfterTextChangedEventData(afterEventData))
    }

    class Builder : TextWatcherEvent.Builder() {
        override fun build(): TextWatcherEventDeleteText {
            return TextWatcherEventDeleteText(beforeEventData, onEventData, afterEventData)
        }

        fun buildGeneric(): TextWatcherEventDeleteText {
            beforeEventData = BeforeTextChangedEventData(null)
            onEventData = OnTextChangedEventData(null)
            afterEventData = AfterTextChangedEventData(null)
            return TextWatcherEventDeleteText(beforeEventData, onEventData, afterEventData)
        }
    }
}
