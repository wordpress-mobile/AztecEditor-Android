package org.wordpress.aztec.picassoloader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v4.util.ArrayMap
import android.util.DisplayMetrics
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Html

class PicassoImageLoader(private val context: Context, aztec: AztecText) : Html.ImageGetter {
    private val targets: MutableMap<String, com.squareup.picasso.Target>

    init {
        this.targets = ArrayMap<String, Target>()

        // Picasso keeps a weak reference to targets so we need to attach them to AztecText
        aztec.tag = targets
    }

    override fun loadImage(source: String, callbacks: Html.ImageGetter.Callbacks, maxWidth: Int) {
        val picasso = Picasso.with(context)
        picasso.isLoggingEnabled = true

        val target = object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                bitmap?.density = DisplayMetrics.DENSITY_DEFAULT
                callbacks.onImageLoaded(BitmapDrawable(context.resources, bitmap))
                targets.remove(source)
            }

            override fun onBitmapFailed(errorDrawable: Drawable?) {
                callbacks.onImageFailed()
                targets.remove(source)
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                callbacks.onImageLoading(placeHolderDrawable)
            }
        }

        // add a strong reference to the target until it's called or the view gets destroyed
        targets.put(source, target)

        picasso.load(source).resize(maxWidth, maxWidth).centerInside().onlyScaleDown().into(target)
    }
}
