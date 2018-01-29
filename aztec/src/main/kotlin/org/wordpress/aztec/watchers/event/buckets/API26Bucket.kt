package org.wordpress.aztec.watchers.event.buckets

import org.wordpress.aztec.watchers.event.sequence.known.space.API25InWordSpaceInsertionEvent
import org.wordpress.aztec.watchers.event.sequence.known.space.API26PrependNewLineOnStyledSpecialTextEvent
import org.wordpress.aztec.watchers.event.sequence.known.space.API26PrependNewLineOnStyledTextEvent

class API26Bucket : Bucket() {
    init {
        // constructor - here add all identified sequences for this bucket
        userOperations.add(API25InWordSpaceInsertionEvent())
        userOperations.add(API26PrependNewLineOnStyledTextEvent())
        userOperations.add(API26PrependNewLineOnStyledSpecialTextEvent())
        //mUserOperations.add(new ...);
        //mUserOperations.add(new ...);
        //mUserOperations.add(new ...);
    }
}
