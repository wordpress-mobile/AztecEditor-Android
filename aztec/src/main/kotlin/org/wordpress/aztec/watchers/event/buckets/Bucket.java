package org.wordpress.aztec.watchers.event.buckets;

import org.wordpress.aztec.watchers.event.sequence.UserOperationEvent;

import java.util.ArrayList;

/*
    extend from this class to construct a specific bucket
 */
public abstract class Bucket {
    protected ArrayList<UserOperationEvent> mUserOperations = new ArrayList<>();
    public ArrayList<UserOperationEvent> getUserOperations() {
        return mUserOperations;
    }
}
