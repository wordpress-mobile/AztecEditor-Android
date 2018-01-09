package org.wordpress.aztec.watchers.event.buckets

import org.wordpress.aztec.watchers.event.sequence.known.space.API26InWordSpaceInsertionEvent

class API26Bucket : Bucket() {
    init {
        // constructor - here add all identified sequences for this bucket
        userOperations.add(API26InWordSpaceInsertionEvent())
        //mUserOperations.add(new ...);
        //mUserOperations.add(new ...);
        //mUserOperations.add(new ...);
    }
}
