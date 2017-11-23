package org.wordpress.aztec.glideloader.extensions

import android.graphics.Bitmap
import android.graphics.Bitmap.createScaledBitmap

internal fun Bitmap.upscaleTo(desiredWidth: Int): Bitmap {
    val ratio = this.height / this.width
    val height = (ratio * desiredWidth)
    return createScaledBitmap(this, desiredWidth, height, true)
}