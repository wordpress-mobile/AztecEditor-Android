package org.wordpress.aztec.watchers.event.sequence.known.space;

import org.wordpress.aztec.watchers.event.sequence.UserOperationEvent;
import org.wordpress.aztec.watchers.event.sequence.known.space.steps.API26I555TextWatcherEventStep1;

/*
 This case implements the behavior observed in https://github.com/wordpress-mobile/AztecEditor-Android/issues/555
 */
public class API26InWordSpaceInsertionEvent extends UserOperationEvent {
    public API26InWordSpaceInsertionEvent() {


        API26I555TextWatcherEventStep1.Builder builder = new API26I555TextWatcherEventStep1.Builder();
//        builder.setBeforeTextChangedEvent();
//        builder.setOnTextChangedEvent();
//        builder.setAfterTextChangedEvent();
        API26I555TextWatcherEventStep1 step1 = builder.build();

        // TODO add each of the steps that make up for the identified API26InWordSpaceInsertionEvent here
        addSequenceStep(step1);
    }
}
