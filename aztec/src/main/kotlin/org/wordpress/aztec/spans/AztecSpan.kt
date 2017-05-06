package org.wordpress.aztec.spans

import android.annotation.SuppressLint

@SuppressLint("NewApi")
interface AztecSpan : AztecAttributedSpan {

    val TAG: String
        get

    val startTag: String
        get() {
            if (attributes.isEmpty()) {
                return TAG
            }
            return TAG + " " + attributes
        }

    val endTag: String
        get() {
            return TAG
        }
}
