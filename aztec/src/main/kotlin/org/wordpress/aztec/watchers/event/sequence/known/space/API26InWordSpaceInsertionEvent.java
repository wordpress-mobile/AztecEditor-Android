package org.wordpress.aztec.watchers.event.sequence.known.space;

import org.wordpress.aztec.watchers.event.sequence.UserOperationEvent;
import org.wordpress.aztec.watchers.event.sequence.known.space.steps.TextWatcherEventDeleteText;
import org.wordpress.aztec.watchers.event.sequence.known.space.steps.TextWatcherEventInsertText;

/*
 This case implements the behavior observed in https://github.com/wordpress-mobile/AztecEditor-Android/issues/555
 */
public class API26InWordSpaceInsertionEvent extends UserOperationEvent {
    public API26InWordSpaceInsertionEvent() {


        TextWatcherEventDeleteText.Builder builder = new TextWatcherEventDeleteText.Builder();
//        builder.setBeforeTextChangedEvent();
//        builder.setOnTextChangedEvent();
//        builder.setAfterTextChangedEvent();
        TextWatcherEventDeleteText step1 = builder.build();

        TextWatcherEventDeleteText.Builder builderStep2 = new TextWatcherEventDeleteText.Builder();
//        builder.setBeforeTextChangedEvent();
//        builder.setOnTextChangedEvent();
//        builder.setAfterTextChangedEvent();
        TextWatcherEventDeleteText step2 = builderStep2.build();

        TextWatcherEventInsertText.Builder builderStep3 = new TextWatcherEventInsertText.Builder();
//        builder.setBeforeTextChangedEvent();
//        builder.setOnTextChangedEvent();
//        builder.setAfterTextChangedEvent();
        TextWatcherEventInsertText step3 = builderStep3.build();

        TextWatcherEventInsertText.Builder builderStep4 = new TextWatcherEventInsertText.Builder();
//        builder.setBeforeTextChangedEvent();
//        builder.setOnTextChangedEvent();
//        builder.setAfterTextChangedEvent();
        TextWatcherEventInsertText step4 = builderStep4.build();

        // TODO add each of the steps that make up for the identified API26InWordSpaceInsertionEvent here
        addSequenceStep(step1);
        addSequenceStep(step2);
        addSequenceStep(step3);
        addSequenceStep(step4);
    }

    @Override
    public boolean isUserOperationObservedInSequence() {
        // TODO here check:
        /*
        If we have 2 deletes followed by 2 inserts AND:
        1) checking the first BEFORETEXTCHANGED and
        2) checking the LAST AFTERTEXTCHANGED
        text length is longer by 1, and the item that is now located at the first BEFORETEXTCHANGED is a SPACE character.

        With this, we avoid the wait and any time-sensitive based controls, and only rely on sequence of changes.
         */

        return false;
    }
}
