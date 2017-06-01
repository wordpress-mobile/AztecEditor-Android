package org.wordpress.aztec.glideloader

import android.content.Context
import android.graphics.drawable.Drawable

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target

import org.wordpress.aztec.Html


class GlideImageLoader(private val context: Context) : Html.ImageGetter, Html.VideoThumbnailGetter {

    override fun loadVideoThumbnail(source: String, callbacks: Html.VideoThumbnailGetter.Callbacks, maxWidth: Int) {

        Glide.with(context)
                .using(VideoThumbnailLoader())
                .load(source)
                .fitCenter()
                .into(object : Target<GlideDrawable> {
                    override fun onLoadStarted(placeholder: Drawable?) {
                        callbacks.onThumbnailLoading(placeholder)
                    }

                    override fun onLoadFailed(e: Exception?, errorDrawable: Drawable?) {
                        callbacks.onThumbnailFailed()
                    }

                    override fun onResourceReady(resource: GlideDrawable?, glideAnimation: GlideAnimation<in GlideDrawable>?) {
                        callbacks.onThumbnailLoaded(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}

                    override fun getSize(cb: SizeReadyCallback?) {
                        cb?.onSizeReady(maxWidth, maxWidth)
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

    override fun loadImage(source: String, callbacks: Html.ImageGetter.Callbacks, maxWidth: Int) {
        Glide.with(context).load(source).fitCenter().into(object : Target<GlideDrawable> {
            override fun onLoadStarted(placeholder: Drawable?) {
                callbacks.onImageLoading(placeholder)
            }

            override fun onLoadFailed(e: Exception?, errorDrawable: Drawable?) {
                callbacks.onImageFailed()
            }

            override fun onResourceReady(resource: GlideDrawable?, glideAnimation: GlideAnimation<in GlideDrawable>?) {
                callbacks.onImageLoaded(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {}

            override fun getSize(cb: SizeReadyCallback?) {
                cb?.onSizeReady(maxWidth, Target.SIZE_ORIGINAL)
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
