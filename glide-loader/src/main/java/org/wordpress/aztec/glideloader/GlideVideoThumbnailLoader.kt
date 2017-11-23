package org.wordpress.aztec.glideloader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.DisplayMetrics
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.GenericLoaderFactory
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.stream.StreamModelLoader
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import org.wordpress.aztec.Html
import org.wordpress.aztec.glideloader.extensions.upscaleTo
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

class GlideVideoThumbnailLoader(private val context: Context) : Html.VideoThumbnailGetter {

    override fun loadVideoThumbnail(source: String, callbacks: Html.VideoThumbnailGetter.Callbacks, maxWidth: Int) {
        loadVideoThumbnail(source, callbacks, maxWidth, 0)
    }

    override fun loadVideoThumbnail(source: String, callbacks: Html.VideoThumbnailGetter.Callbacks, maxWidth: Int, minWidth: Int) {

        Glide.with(context)
                .using(ThumbnailLoader(context))
                .load(source)
                .asBitmap()
                .fitCenter()
                .into(object : Target<Bitmap> {
                    override fun onLoadStarted(placeholder: Drawable?) {
                        callbacks.onThumbnailLoading(placeholder)
                    }

                    override fun onLoadFailed(e: Exception?, errorDrawable: Drawable?) {
                        callbacks.onThumbnailFailed()
                    }

                    override fun onResourceReady(resource: Bitmap?, glideAnimation: GlideAnimation<in Bitmap>?) {
                        //Upscaling bitmap only for demonstration purposes.
                        //This should probably be done somewhere more appropriate for Glide (?).
                        if (resource != null && resource.width < minWidth) {
                            return callbacks.onThumbnailLoaded(BitmapDrawable(context.resources, resource.upscaleTo(minWidth)))
                        }

                        // By default, BitmapFactory.decodeFile sets the bitmap's density to the device default so, we need
                        // to correctly set the input density to 160 ourselves.
                        resource?.density = DisplayMetrics.DENSITY_DEFAULT
                        callbacks.onThumbnailLoaded(BitmapDrawable(context.resources, resource))
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

    // Based on a Gist from Stepan Goncharov (https://gist.github.com/stepango/5edcbdb408b0ba87f8383f868961c257)
    internal class ThumbnailLoader(val context: Context) : StreamModelLoader<String> {

        override fun getResourceFetcher(src: String, width: Int, height: Int) = VideoThumbnailFetcher(src, context)

        internal class Factory : ModelLoaderFactory<String, InputStream> {
            override fun build(context: Context, factories: GenericLoaderFactory) = ThumbnailLoader(context)

            override fun teardown() = Unit
        }

        class VideoThumbnailFetcher(val source: String, val context: Context) : DataFetcher<InputStream> {
            var stream: InputStream? = null
            @Volatile
            var cancelled = false

            override fun getId(): String = source

            override fun loadData(priority: Priority): InputStream? {
                val retriever = MediaMetadataRetriever()
                try {

                    val uri = Uri.parse(source)
                    val isRemote = uri?.scheme?.startsWith("http", true) ?: false
                    if (isRemote) {
                        retriever.setDataSource(source, emptyMap())
                    } else {
                        retriever.setDataSource(context, uri)
                    }

                    if (cancelled) return null
                    val picture = retriever.frameAtTime
                    if (cancelled) return null
                    if (picture != null) {
                        val bitmapData = ByteArrayOutputStream().use { bos ->
                            picture.compress(Bitmap.CompressFormat.JPEG, 90, bos)
                            bos.toByteArray()
                        }
                        if (cancelled) return null
                        stream = ByteArrayInputStream(bitmapData)
                        return stream
                    }
                } finally {
                    retriever.release()
                }
                return null
            }

            override fun cleanup() = try {
                stream?.close()
            } catch (e: IOException) {
                // Just Ignore it
            } ?: Unit

            override fun cancel() {
                cancelled = true
            }
        }
    }
}