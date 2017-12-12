package org.wordpress.aztec.watchers.event.sequence.known.space.steps;

import org.wordpress.aztec.watchers.event.text.AfterTextChangedEventData;
import org.wordpress.aztec.watchers.event.text.BeforeTextChangedEventData;
import org.wordpress.aztec.watchers.event.text.ITextWatcherEventComparator;
import org.wordpress.aztec.watchers.event.text.OnTextChangedEventData;
import org.wordpress.aztec.watchers.event.text.TextWatcherEvent;


public class TextWatcherEventDeleteText extends TextWatcherEvent implements ITextWatcherEventComparator {

    private CharSequence beforeText;
    private boolean beforeHasRun = false;
    private boolean onHasRun = false;
    private boolean afterHasRun = false;

    private boolean beforeFits = false;
    private boolean onFits = false;
    private boolean afterFits = false;

    public TextWatcherEventDeleteText(BeforeTextChangedEventData beforeEventData, OnTextChangedEventData onEventData,
                                      AfterTextChangedEventData afterEventData) {
        this.beforeEventData = beforeEventData;
        this.onEventData = onEventData;
        this.afterEventData = afterEventData;
    }

    @Override
    public boolean testBeforeTextChangedEventData(BeforeTextChangedEventData data) {
        beforeText = data.getTextBefore();
        beforeHasRun = true;
        beforeFits = (data.getCount() > 0 &&  data.getAfter() == 0) && ((data.getStart() + data.getCount()) < data.getTextBefore().length());
        return beforeFits;
    }

    @Override
    public boolean testOnTextChangedEventData(OnTextChangedEventData data) {
        onHasRun = true;
        onFits = (data.getStart() >= 0 && data.getCount() == 0 && data.getTextOn().length() > 0);
        return onFits;
    }

    @Override
    public boolean testAfterTextChangedEventData(AfterTextChangedEventData data) {
        afterHasRun = true;
        afterFits = beforeText.length() > data.getTextAfter().length();
        return afterFits;
    }

    @Override
    public boolean testFitsBeforeOnAndAfter() {
        return allMomentsHaveRun() && beforeFits && onFits && afterFits;
    }

    @Override
    public boolean allMomentsHaveRun() {
        return beforeHasRun && onHasRun && afterHasRun;
    }

    public static class Builder extends TextWatcherEvent.Builder {
        public TextWatcherEventDeleteText build(){
            return new TextWatcherEventDeleteText(beforeEventData, onEventData, afterEventData);
        }
    }
}
