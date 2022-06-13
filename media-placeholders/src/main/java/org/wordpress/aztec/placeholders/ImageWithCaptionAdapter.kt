package org.wordpress.aztec.placeholders

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.placeholders.PlaceholderManager.PlaceholderAdapter.Proportion

/**
 * A sample adapter which creates a custom layout over the placeholder. Treat this as an example of what can be done.
 * This adapter creates an image with a caption under it
 */
class ImageWithCaptionAdapter(
        override val type: String = "image_with_caption"
) : PlaceholderManager.PlaceholderAdapter {
    private val media = mutableMapOf<String, ImageWithCaptionObject>()
    suspend override fun getHeight(attrs: AztecAttributes): Proportion {
        return Proportion.Ratio(0.5f)
    }

    suspend override fun createView(context: Context, placeholderUuid: String, attrs: AztecAttributes): View {
        val imageWithCaptionObject = media[placeholderUuid]
                ?: ImageWithCaptionObject(placeholderUuid, attrs.getValue(SRC_ATTRIBUTE), View.generateViewId()).apply {
                    media[placeholderUuid] = this
                }
        val captionLayoutId = View.generateViewId()
        val imageLayoutId = imageWithCaptionObject.layoutId
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.VERTICAL

        val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        linearLayout.layoutParams = layoutParams
        val image = ImageView(context)
        image.id = imageLayoutId
        val imageParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f)
        imageParams.setMargins(5, 5, 5, 5)

        image.layoutParams = imageParams

        val caption = TextView(context)
        caption.id = captionLayoutId
        caption.text = attrs.getValue(CAPTION_ATTRIBUTE)
        val captionParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)

        captionParams.setMargins(5, 5, 5, 5)
        captionParams.gravity = Gravity.CENTER_HORIZONTAL

        caption.layoutParams = captionParams

        linearLayout.addView(image)
        linearLayout.addView(caption)
        return linearLayout
    }

    suspend override fun onViewCreated(view: View, placeholderUuid: String) {
        val image = media[placeholderUuid]!!
        val imageView = view.findViewById<ImageView>(image.layoutId)
        Glide.with(view).load(image.src).into(imageView)
        super.onViewCreated(view, placeholderUuid)
    }

    override fun onPlaceholderDeleted(placeholderUuid: String) {
        media.remove(placeholderUuid)
    }

    data class ImageWithCaptionObject(val id: String, val src: String, val layoutId: Int)

    companion object {
        private const val ADAPTER_TYPE = "image_with_caption"
        private const val CAPTION_ATTRIBUTE = "caption"
        private const val SRC_ATTRIBUTE = "src"

        suspend fun insertImageWithCaption(placeholderManager: PlaceholderManager, src: String, caption: String) {
            placeholderManager.insertItem(ADAPTER_TYPE, SRC_ATTRIBUTE to src, CAPTION_ATTRIBUTE to caption)
        }
    }
}

