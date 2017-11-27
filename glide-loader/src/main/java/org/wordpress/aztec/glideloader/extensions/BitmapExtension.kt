package org.wordpress.aztec.glideloader.extensions

import android.graphics.Bitmap
import android.graphics.Bitmap.createScaledBitmap

internal fun Bitmap.upscaleTo(desiredWidth: Int): Bitmap {
    val ratio = this.height.toFloat() / this.width.toFloat()
    val proportionateHeight = ratio * desiredWidth
    val finalHeight = Math.rint(proportionateHeight.toDouble()).toInt()

    return createScaledBitmap(this, desiredWidth, finalHeight, true)
}