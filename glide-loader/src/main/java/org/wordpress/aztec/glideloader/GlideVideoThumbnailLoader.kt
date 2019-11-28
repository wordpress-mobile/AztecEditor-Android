package org.wordpress.aztec.glideloader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import com.bumptech.glide.Glide
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import org.wordpress.aztec.Html
import org.wordpress.aztec.glideloader.extensions.upscaleTo

class GlideVideoThumbnailLoader(private val context: Context) : Html.VideoThumbnailGetter {

    override fun loadVideoThumbnail(source: String, callbacks: Html.VideoThumbnailGetter.Callbacks, maxWidth: Int) {
        loadVideoThumbnail(source, callbacks, maxWidth, 0)
    }

    override fun loadVideoThumbnail(
            source: String,
            callbacks: Html.VideoThumbnailGetter.Callbacks,
            maxWidth: Int,
            minWidth: Int
    ) {
        Glide.with(context)
                .asBitmap()
                .load(source)
                .fitCenter()
                .into(object : Target<Bitmap> {
                    override fun onLoadStarted(placeholder: Drawable?) {
                        callbacks.onThumbnailLoading(placeholder)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        callbacks.onThumbnailFailed()
                    }

                    override fun onResourceReady(resource: Bitmap, glideAnimation: Transition<in Bitmap>?) {
                        //Upscaling bitmap only for demonstration purposes.
                        //This should probably be done somewhere more appropriate for Glide (?).
                        if (resource.width < minWidth) {
                            return callbacks.onThumbnailLoaded(
                                    BitmapDrawable(context.resources, resource.upscaleTo(minWidth))
                            )
                        }

                        // By default, BitmapFactory.decodeFile sets the bitmap's density to the device default so,
                        // we need to correctly set the input density to 160 ourselves.
                        resource.density = DisplayMetrics.DENSITY_DEFAULT
                        callbacks.onThumbnailLoaded(BitmapDrawable(context.resources, resource))
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}

                    override fun getSize(cb: SizeReadyCallback) {
                        cb.onSizeReady(maxWidth, maxWidth)
                    }

                    override fun removeCallback(cb: SizeReadyCallback) {
                    }

                    override fun setRequest(request: Request?) {
                    }

                    override fun getRequest(): Request? {
                        return null
                    }

                    override fun onStart() {
                    }

                    override fun onStop() {
                    }

                    override fun onDestroy() {
                    }
                })
    }
}