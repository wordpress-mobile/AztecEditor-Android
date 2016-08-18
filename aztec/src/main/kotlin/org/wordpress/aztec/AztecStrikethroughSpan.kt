package org.wordpress.aztec

import android.text.style.StrikethroughSpan

class AztecStrikethroughSpan(tag: String) : StrikethroughSpan() {

    private var mTag: String = tag

    //default tag
    constructor() : this("del")

    fun getTag(): String {
        return mTag
    }
}
