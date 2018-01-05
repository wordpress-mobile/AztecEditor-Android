package org.wordpress.aztec.watchers.event.sequence.known.space.steps;

import org.wordpress.aztec.watchers.event.text.AfterTextChangedEventData;
import org.wordpress.aztec.watchers.event.text.BeforeTextChangedEventData;
import org.wordpress.aztec.watchers.event.text.OnTextChangedEventData;
import org.wordpress.aztec.watchers.event.text.TextWatcherEvent;


public class TextWatcherEventDeleteText extends TextWatcherEvent {

    private CharSequence beforeText;

    private TextWatcherEventDeleteText(BeforeTextChangedEventData beforeEventData, OnTextChangedEventData onEventData,
                                      AfterTextChangedEventData afterEventData) {
        this.beforeEventData = beforeEventData;
        this.onEventData = onEventData;
        this.afterEventData = afterEventData;
    }

    private boolean testBeforeTextChangedEventData(BeforeTextChangedEventData data) {
        beforeText = data.getTextBefore();
        return (data.getCount() > 0 &&  data.getAfter() == 0) && ((data.getStart() + data.getCount()) <= data.getTextBefore().length());
    }

    private boolean testOnTextChangedEventData(OnTextChangedEventData data) {
        return (data.getStart() >= 0 && data.getCount() == 0 && data.getTextOn().length() < beforeText.length());
    }

    private boolean testAfterTextChangedEventData(AfterTextChangedEventData data) {
        return beforeText.length() > data.getTextAfter().length();
    }

    @Override
    public boolean testFitsBeforeOnAndAfter() {
        return testBeforeTextChangedEventData(beforeEventData)
                && testOnTextChangedEventData(onEventData)
                && testAfterTextChangedEventData(afterEventData);
    }

    public static class Builder extends TextWatcherEvent.Builder {
        public TextWatcherEventDeleteText build(){
            return new TextWatcherEventDeleteText(beforeEventData, onEventData, afterEventData);
        }
    }
}
