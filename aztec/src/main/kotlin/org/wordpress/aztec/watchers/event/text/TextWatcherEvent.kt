package org.wordpress.aztec.watchers.event.text

open class TextWatcherEvent (var beforeEventData: BeforeTextChangedEventData,
                             var onEventData: OnTextChangedEventData,
                             var afterEventData: AfterTextChangedEventData) {
    val timestamp: Long = System.currentTimeMillis()

    open fun testFitsBeforeOnAndAfter(): Boolean {
        // always returns false, subclasses should make an assessment and properly return true/false depending
        // on their specific data contents
        return false
    }

    open class Builder {
        lateinit var beforeEventData: BeforeTextChangedEventData
        lateinit var onEventData: OnTextChangedEventData
        lateinit var afterEventData: AfterTextChangedEventData

        fun setGenericEventDataIfNotInit() {
            if (!this::beforeEventData.isInitialized) {
                beforeEventData = BeforeTextChangedEventData(null)
            }

            if (!this::onEventData.isInitialized) {
                onEventData = OnTextChangedEventData(null)
            }

            if (!this::afterEventData.isInitialized) {
                afterEventData = AfterTextChangedEventData(null)
            }
        }

        open fun build(): TextWatcherEvent {
            return TextWatcherEvent(beforeEventData, onEventData, afterEventData)
        }
    }

}
