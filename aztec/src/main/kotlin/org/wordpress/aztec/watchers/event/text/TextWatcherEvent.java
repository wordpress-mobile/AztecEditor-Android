package org.wordpress.aztec.watchers.event.text;

public class TextWatcherEvent {
    protected BeforeTextChangedEventData beforeEventData;
    protected OnTextChangedEventData onEventData;
    protected AfterTextChangedEventData afterEventData;
    private long timestamp;

    public TextWatcherEvent() {

    }

    public TextWatcherEvent(BeforeTextChangedEventData beforeEventData, OnTextChangedEventData onEventData,
                            AfterTextChangedEventData afterEventData) {
        this.beforeEventData = beforeEventData;
        this.onEventData = onEventData;
        this.afterEventData = afterEventData;
        this.timestamp = System.currentTimeMillis();
    }

    public void setBeforeTextChangedEvent(BeforeTextChangedEventData beforeEventDataToAdd) {
        beforeEventData = beforeEventDataToAdd;
    }

    public void setOnTextChangedEvent(OnTextChangedEventData onEventDataToAdd) {
        onEventData = onEventDataToAdd;
    }

    public void setAfterTextChangedEvent(AfterTextChangedEventData afterEventDataToAdd) {
        afterEventData = afterEventDataToAdd;
    }

    public BeforeTextChangedEventData getBeforeEventData() {
        return beforeEventData;
    }

    public OnTextChangedEventData getOnEventData() {
        return onEventData;
    }

    public AfterTextChangedEventData getAfterEventData() {
        return afterEventData;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean testFitsBeforeOnAndAfter() {
        // always returns false, subclassess should make an assessment and properly return true/false depending
        // on their specific data contents
        return false;
    }

    public static class Builder {
        protected BeforeTextChangedEventData beforeEventData;
        protected OnTextChangedEventData onEventData;
        protected AfterTextChangedEventData afterEventData;

        public Builder setBeforeTextChangedEvent(BeforeTextChangedEventData beforeEventDataToAdd) {
            beforeEventData = beforeEventDataToAdd;
            return this;
        }

        public Builder setOnTextChangedEvent(OnTextChangedEventData onEventDataToAdd) {
            onEventData = onEventDataToAdd;
            return this;
        }

        public Builder setAfterTextChangedEvent(AfterTextChangedEventData afterEventDataToAdd) {
            afterEventData = afterEventDataToAdd;
            return this;
        }

        public TextWatcherEvent build(){
            return new TextWatcherEvent(beforeEventData, onEventData, afterEventData);
        }
    }

}
