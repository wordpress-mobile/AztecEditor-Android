package org.wordpress.aztec.watchers.event.buckets

import org.wordpress.aztec.watchers.event.sequence.UserOperationEvent

import java.util.ArrayList

/*
    extend from this class to construct a specific bucket
 */
abstract class Bucket {
    val userOperations = ArrayList<UserOperationEvent>()
}
