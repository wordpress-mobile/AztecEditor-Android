package org.wordpress.aztec.plugins.shortcodes.extensions

import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Html
import org.wordpress.aztec.spans.AztecVideoSpan

val ATTRIBUTE_VIDEOPRESS_HIDDEN_ID = "videopress_hidden_id"
val ATTRIBUTE_VIDEOPRESS_HIDDEN_SRC = "videopress_hidden_src"

fun AztecText.updateVideoPressThumb(thumbURL: String, videoURL: String, videoPressID: String) {
    val loadingDrawable = ContextCompat.getDrawable(context, this.drawableLoading)
    val callbacks = object : Html.ImageGetter.Callbacks {
        private fun replaceImage(drawable: Drawable?) {
            val spans = text.getSpans(0, text.length, AztecVideoSpan::class.java)
            spans.forEach {
                if (it.attributes.hasAttribute(ATTRIBUTE_VIDEOPRESS_HIDDEN_ID) &&
                        it.attributes.getValue(ATTRIBUTE_VIDEOPRESS_HIDDEN_ID) == videoPressID) {

                    // Set the hidden videopress source. Used when the video is tapped
                    it.attributes.setValue(ATTRIBUTE_VIDEOPRESS_HIDDEN_SRC, videoURL)
                    it.drawable = drawable
                }
            }
            post {
                refreshText(false)
            }
        }

        override fun onImageFailed() {
            replaceImage(ContextCompat.getDrawable(context, drawableFailed))
        }

        override fun onImageLoaded(drawable: Drawable?) {
            replaceImage(drawable)
        }

        override fun onImageLoading(drawable: Drawable?) {
            replaceImage(drawable ?: loadingDrawable)
        }
    }
    imageGetter?.loadImage(thumbURL, callbacks, this.maxImagesWidth, this.minImagesWidth)
}
