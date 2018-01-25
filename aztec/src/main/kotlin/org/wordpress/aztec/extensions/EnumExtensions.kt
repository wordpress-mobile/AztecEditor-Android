package org.wordpress.aztec.extensions

import android.text.Layout

fun Layout.Alignment.toCssString(): String {
    return when (this) {
        Layout.Alignment.ALIGN_NORMAL -> "left"
        Layout.Alignment.ALIGN_CENTER -> "center"
        else -> "right"
    }
}