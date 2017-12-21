package org.wordpress.aztec.watchers.event.sequence.known.space.steps;

import org.wordpress.aztec.watchers.event.text.AfterTextChangedEventData;
import org.wordpress.aztec.watchers.event.text.BeforeTextChangedEventData;
import org.wordpress.aztec.watchers.event.text.ITextWatcherEventComparator;
import org.wordpress.aztec.watchers.event.text.OnTextChangedEventData;
import org.wordpress.aztec.watchers.event.text.TextWatcherEvent;


public class TextWatcherEventInsertText extends TextWatcherEvent implements ITextWatcherEventComparator {

    private CharSequence beforeText;
    private int insertionStart;
    private int insertionLength;

    public TextWatcherEventInsertText() {

    }

    public TextWatcherEventInsertText(BeforeTextChangedEventData beforeEventData, OnTextChangedEventData onEventData,
                                      AfterTextChangedEventData afterEventData) {
        this.beforeEventData = beforeEventData;
        this.onEventData = onEventData;
        this.afterEventData = afterEventData;
    }

    public void setInsertionStart(int insertionStart) {
        this.insertionStart = insertionStart;
    }

    public int getInsertionStart() {
        return insertionStart;
    }

    public int getInsertionLength() {
        return insertionLength;
    }

    public void setInsertionLength(int insertionLength) {
        this.insertionLength = insertionLength;
    }

    @Override
    public boolean testBeforeTextChangedEventData(BeforeTextChangedEventData data) {
        beforeText = data.getTextBefore();
        return (data.getCount() == 0 &&  data.getAfter() > 0);
    }

    @Override
    public boolean testOnTextChangedEventData(OnTextChangedEventData data) {
        return (data.getStart() >= 0 && data.getCount() > 0 && data.getTextOn().length() > 0);
    }

    @Override
    public boolean testAfterTextChangedEventData(AfterTextChangedEventData data) {
        return beforeText.length() < data.getTextAfter().length();
    }

    @Override
    public boolean testFitsBeforeOnAndAfter() {
        return testBeforeTextChangedEventData(beforeEventData)
                && testOnTextChangedEventData(onEventData)
                && testAfterTextChangedEventData(afterEventData);
    }

    public static class Builder extends TextWatcherEvent.Builder {
        public TextWatcherEventInsertText build(){
            return new TextWatcherEventInsertText(beforeEventData, onEventData, afterEventData);
        }
    }
}
