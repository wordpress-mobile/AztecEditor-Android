package org.wordpress.aztec.extensions

import android.text.Layout

fun Layout.Alignment.toCssString(isRtl: Boolean): String {
    return when (this) {
        Layout.Alignment.ALIGN_NORMAL -> if (!isRtl) "left" else "right"
        Layout.Alignment.ALIGN_CENTER -> "center"
        else -> if (isRtl) "left" else "right"
    }
}