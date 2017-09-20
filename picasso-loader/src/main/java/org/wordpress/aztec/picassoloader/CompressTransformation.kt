package org.wordpress.aztec.picassoloader

import android.graphics.Bitmap
import com.squareup.picasso.Transformation
import org.wordpress.android.util.ImageUtils


class CompressTransformation : Transformation {

    override fun key(): String {
        return "CompressTransformation()"
    }

    override fun transform(source: Bitmap): Bitmap {
        val newBitmap = ImageUtils.getScaledBitmapAtLongestSide(source, 500)
        if (newBitmap != null && newBitmap != source) {
            source.recycle()
            return newBitmap
        }
        return  source
    }
}