package org.wordpress.aztec.picassoloader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.squareup.picasso.Transformation
import android.graphics.Matrix
import android.util.Log
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream


class CompressTransformation : Transformation {

    override fun key(): String {
        return "CompressTransformation()"
    }

    override fun transform(source: Bitmap): Bitmap {

        val matrix = Matrix()
        matrix.postScale(0.99f, 0.99f)

        val fmt: Bitmap.CompressFormat =  Bitmap.CompressFormat.JPEG
        val bmpRotated = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true)
        val streamoOut = ByteArrayOutputStream()
        bmpRotated.compress(fmt, 80, streamoOut)
        val inputStream = ByteArrayInputStream(streamoOut.toByteArray())
        val newBitmap = BitmapFactory.decodeStream(inputStream)

        Log.d("danilo", "Saved " + (newBitmap.byteCount - source.byteCount) + " bytes")
        source.recycle()
        return newBitmap
    }
}