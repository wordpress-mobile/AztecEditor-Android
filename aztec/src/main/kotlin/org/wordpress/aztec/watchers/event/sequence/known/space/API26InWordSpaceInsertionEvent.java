package org.wordpress.aztec.watchers.event.sequence.known.space;

import android.text.SpannableStringBuilder;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.wordpress.aztec.watchers.event.sequence.EventSequence;
import org.wordpress.aztec.watchers.event.sequence.UserOperationEvent;
import org.wordpress.aztec.watchers.event.sequence.known.space.steps.TextWatcherEventDeleteText;
import org.wordpress.aztec.watchers.event.sequence.known.space.steps.TextWatcherEventInsertText;
import org.wordpress.aztec.watchers.event.text.AfterTextChangedEventData;
import org.wordpress.aztec.watchers.event.text.BeforeTextChangedEventData;
import org.wordpress.aztec.watchers.event.text.TextWatcherEvent;

/*
 This case implements the behavior observed in https://github.com/wordpress-mobile/AztecEditor-Android/issues/555
 */
public class API26InWordSpaceInsertionEvent extends UserOperationEvent {
    private static final char SPACE = ' ';
    private static final String SPACE_STRING = "" + SPACE;
    private static final int MAXIMUM_TIME_BETWEEN_EVENTS_IN_PATTERN_MS = 50;

    public API26InWordSpaceInsertionEvent() {
        // here we populate our model of reference (which is the sequence of events we expect to find)
        // note we don' populate the TextWatcherEvents with actual data, but rather we just want
        // to instantiate them so we can populate them later and test whether data holds true to their
        // validation.

        // 2 generic deletes, followed by 2 generic inserts
        TextWatcherEventDeleteText.Builder builder = new TextWatcherEventDeleteText.Builder();
        TextWatcherEventDeleteText step1 = builder.build();

        TextWatcherEventDeleteText.Builder builderStep2 = new TextWatcherEventDeleteText.Builder();
        TextWatcherEventDeleteText step2 = builderStep2.build();

        TextWatcherEventInsertText.Builder builderStep3 = new TextWatcherEventInsertText.Builder();
        TextWatcherEventInsertText step3 = builderStep3.build();

        TextWatcherEventInsertText.Builder builderStep4 = new TextWatcherEventInsertText.Builder();
        TextWatcherEventInsertText step4 = builderStep4.build();

        // add each of the steps that make up for the identified API26InWordSpaceInsertionEvent here
        clear();
        addSequenceStep(step1);
        addSequenceStep(step2);
        addSequenceStep(step3);
        addSequenceStep(step4);
    }

    @Override
    public boolean isUserOperationObservedInSequence(EventSequence<TextWatcherEvent> sequence) {
        /* here check:

        If we have 2 deletes followed by 2 inserts AND:
        1) checking the first BEFORETEXTCHANGED and
        2) checking the LAST AFTERTEXTCHANGED
        text length is longer by 1, and the item that is now located at the first BEFORETEXTCHANGED is a SPACE character.

         */
        if (this.getSequence().size() == sequence.size()) {

            // populate data in our own sequence to be able to run the comparator checks
            if (!isUserOperationPartiallyObservedInSequence(sequence)) {
                return false;
            }

            // ok all events are good individually and match the sequence we want to compare against.
            // now let's make sure the BEFORE / AFTER situation is what we are trying to identify
            TextWatcherEvent firstEvent = sequence.get(0);
            TextWatcherEvent lastEvent = sequence.get(sequence.size()-1);

            // if new text length is longer than original text by 1
            if (firstEvent.getBeforeEventData().getTextBefore().length() ==
                    lastEvent.getAfterEventData().getTextAfter().length() - 1) {
                // now check that the inserted character is actually a space
                BeforeTextChangedEventData data = firstEvent.getBeforeEventData();
                if (lastEvent.getAfterEventData().getTextAfter().charAt(data.getStart() + data.getCount()) == SPACE) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean isUserOperationPartiallyObservedInSequence(@NotNull EventSequence<TextWatcherEvent> sequence) {
        for (int i = 0; i < sequence.size(); i++) {

            TextWatcherEvent eventHolder = getSequence().get(i);
            TextWatcherEvent observableEvent = sequence.get(i);

            // if time distance between any of the events is longer than 50 millis, discard this as this pattern is
            // likely not produced by the platform, but rather the user.
            // WARNING! When debugging with breakpoints, you should disable this check as time can exceed the 50 MS limit and
            // create undesired behavior.
            if (i > 0) { // only try to compare when we have at least 2 events, so we can compare with the previous one
                long timestampForPreviousEvent = sequence.get(i-1).getTimestamp();
                long timeDistance = observableEvent.getTimestamp() - timestampForPreviousEvent;
                if (timeDistance > MAXIMUM_TIME_BETWEEN_EVENTS_IN_PATTERN_MS) {
                    return false;
                }
            }

            eventHolder.setBeforeTextChangedEvent(observableEvent.getBeforeEventData());
            eventHolder.setOnTextChangedEvent(observableEvent.getOnEventData());
            eventHolder.setAfterTextChangedEvent(observableEvent.getAfterEventData());

            // return immediately as soon as we realize the pattern diverges
            if (!eventHolder.testFitsBeforeOnAndAfter()) {
                return false;
            }
        }

        return true;
    }

    @NotNull
    @Override
    public TextWatcherEvent buildReplacementEventWithSequenceData(EventSequence<TextWatcherEvent> sequence) {
        TextWatcherEventInsertText replacementEvent = new TextWatcherEventInsertText();
        // here make it all up as a unique event that does the insert as usual, as we'd get it on older APIs
        TextWatcherEvent firstEvent = sequence.get(0);
        TextWatcherEvent lastEvent = sequence.get(sequence.size()-1);

        BeforeTextChangedEventData beforeData = firstEvent.getBeforeEventData();

        int differenceIndex = StringUtils.indexOfDifference(beforeData.getTextBefore(), lastEvent.getAfterEventData().getTextAfter());
        replacementEvent.setInsertionStart(differenceIndex);
        replacementEvent.setInsertionLength(1);

        SpannableStringBuilder oldText = beforeData.getTextBefore();
        oldText.insert(differenceIndex, SPACE_STRING);

        replacementEvent.setAfterTextChangedEvent(new AfterTextChangedEventData(oldText));

        return replacementEvent;
    }

}
