package org.wordpress.aztec.watchers.event.buckets;

import org.wordpress.aztec.watchers.event.sequence.known.space.API26InWordSpaceInsertionEvent;

public class API26Bucket extends Bucket{

    public API26Bucket() {
        // constructor - here add all identified sequences for this bucket
        mUserOperations.add(new API26InWordSpaceInsertionEvent());
        //mUserOperations.add(new ...);
        //mUserOperations.add(new ...);
        //mUserOperations.add(new ...);
    }
}
