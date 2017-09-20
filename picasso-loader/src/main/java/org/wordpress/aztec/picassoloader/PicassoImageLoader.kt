package org.wordpress.aztec.picassoloader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v4.util.ArrayMap
import android.support.v4.util.LruCache
import android.util.DisplayMetrics
import com.squareup.picasso.MemoryPolicy

import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.squareup.picasso.Transformation

import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Html

class PicassoImageLoader(private val context: Context, aztec: AztecText) : Html.ImageGetter {
    private val targets: MutableMap<String, com.squareup.picasso.Target>
    private val transformations: ArrayList<Transformation> = ArrayList()

    companion object {
        // Images cache shared in all instances of editors
        val cacheSize = 4 * 1024 * 1024 // 4MiB
        @JvmStatic val bitmapCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String?, value: Bitmap?): Int {
                return value?.byteCount ?: 1
            }
        }
    }

    init {
        this.targets = ArrayMap<String, Target>()

        // Picasso keeps a weak reference to targets so we need to attach them to AztecText
        aztec.tag = targets
        transformations.add(CompressTransformation())
    }

    override fun loadImage(source: String, callbacks: Html.ImageGetter.Callbacks, maxWidth: Int) {
        val cacheKey = source + maxWidth
        synchronized (bitmapCache) {
            if (bitmapCache.get(cacheKey) != null) {
                callbacks.onImageLoaded(BitmapDrawable(context.resources, bitmapCache.get(cacheKey)))
                return
            }
        }


        val picasso = Picasso.with(context)
        picasso.isLoggingEnabled = true

        val target = object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                bitmap?.density = DisplayMetrics.DENSITY_DEFAULT
                callbacks.onImageLoaded(BitmapDrawable(context.resources, bitmap))
                targets.remove(source)
                synchronized (bitmapCache) {
                    bitmapCache.put(cacheKey, bitmap)
                }
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

        picasso.load(source).resize(maxWidth, maxWidth).transform(transformations).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).centerInside().onlyScaleDown().into(target)
    }
}
