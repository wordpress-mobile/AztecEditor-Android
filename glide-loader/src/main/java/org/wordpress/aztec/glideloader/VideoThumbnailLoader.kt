package org.wordpress.aztec.glideloader

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import com.bumptech.glide.Priority
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.GenericLoaderFactory
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.stream.StreamModelLoader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

// Based on a Gist from Stepan Goncharov
// URL: https://gist.github.com/stepango/5edcbdb408b0ba87f8383f868961c257
class VideoThumbnailLoader : StreamModelLoader<String> {

    override fun getResourceFetcher(src: String, width: Int, height: Int) = VideoThumbnailFetcher(src)

    internal class Factory : ModelLoaderFactory<String, InputStream> {
        override fun build(context: Context, factories: GenericLoaderFactory) = VideoThumbnailLoader()

        override fun teardown() = Unit
    }

    class VideoThumbnailFetcher(val source: String) : DataFetcher<InputStream> {
        var stream: InputStream? = null
        @Volatile var cancelled = false

        override fun getId(): String = source

        override fun loadData(priority: Priority): InputStream? {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(source, emptyMap())
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