package org.wordpress.aztec.watchers.event.buckets

import org.wordpress.aztec.watchers.event.sequence.known.space.API25InWordSpaceInsertionEvent

class API25Bucket : Bucket() {
    init {
        // constructor - here add all identified sequences for this bucket
        userOperations.add(API25InWordSpaceInsertionEvent())
        //mUserOperations.add(new ...);
        //mUserOperations.add(new ...);
        //mUserOperations.add(new ...);
    }
}
