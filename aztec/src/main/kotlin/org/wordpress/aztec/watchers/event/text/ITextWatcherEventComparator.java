package org.wordpress.aztec.watchers.event.text;

public interface ITextWatcherEventComparator {
    boolean testBeforeTextChangedEventData (BeforeTextChangedEventData data);
    boolean testOnTextChangedEventData (OnTextChangedEventData data);
    boolean testAfterTextChangedEventData (AfterTextChangedEventData data);
    boolean testFitsBeforeOnAndAfter();
    boolean allMomentsHaveRun();
}
