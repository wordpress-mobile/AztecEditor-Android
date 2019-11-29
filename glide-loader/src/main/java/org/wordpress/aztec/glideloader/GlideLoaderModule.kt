package org.wordpress.aztec.glideloader

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.module.LibraryGlideModule
import com.bumptech.glide.signature.ObjectKey
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

@GlideModule
class GlideLoaderModule : LibraryGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.append(String::class.java, InputStream::class.java, ThumbnailLoader.Factory(context))
        super.registerComponents(context, glide, registry)
    }

    internal class ThumbnailLoader(private val context: Context) : ModelLoader<String, InputStream> {
        override fun buildLoadData(
                model: String,
                width: Int,
                height: Int,
                options: Options
        ): ModelLoader.LoadData<InputStream>? {
            return ModelLoader.LoadData<InputStream>(ObjectKey(model), VideoThumbnailFetcher(model, context))
        }

        override fun handles(model: String): Boolean {
            return true
        }

        internal class Factory(private val context: Context) : ModelLoaderFactory<String, InputStream> {
            override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<String, InputStream> {
                return ThumbnailLoader(context)
            }

            override fun teardown() = Unit
        }

        class VideoThumbnailFetcher(val source: String, private val context: Context) : DataFetcher<InputStream> {
            override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
                val retriever = MediaMetadataRetriever()
                try {

                    val uri = Uri.parse(source)
                    val isRemote = uri?.scheme?.startsWith("http", true) ?: false
                    if (isRemote) {
                        retriever.setDataSource(source, emptyMap())
                    } else {
                        retriever.setDataSource(context, uri)
                    }

                    if (cancelled) return
                    val picture = retriever.frameAtTime
                    if (cancelled) return
                    if (picture != null) {
                        val bitmapData = ByteArrayOutputStream().use { bos ->
                            picture.compress(Bitmap.CompressFormat.JPEG, 90, bos)
                            bos.toByteArray()
                        }
                        if (cancelled) return
                        stream = ByteArrayInputStream(bitmapData)
                        return callback.onDataReady(stream)
                    }
                } finally {
                    retriever.release()
                }
            }

            override fun getDataClass(): Class<InputStream> {
                return InputStream::class.java
            }

            override fun getDataSource(): DataSource {
                return DataSource.REMOTE
            }

            var stream: InputStream? = null
            @Volatile
            var cancelled = false

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