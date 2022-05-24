package org.wordpress.aztec.placeholders

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.*
import android.widget.TextView
import com.bumptech.glide.Glide
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.placeholders.PlaceholderManager.PlaceholderAdapter.PlaceholderHeight

/**
 * A sample drawer which draws a custom layout over the placeholder. Treat this as an example of what can be done.
 * This drawer draws an image with a caption under it
 */
class ImageWithCaptionAdapter(override val placeholderHeight: PlaceholderHeight = PlaceholderHeight.Ratio(0.5f), override val type: String = "image_with_caption") : PlaceholderManager.PlaceholderAdapter {
    private val media = mutableMapOf<String, ImageWithCaptionObject>()
    override fun createView(context: Context, placeholderId: String, attrs: AztecAttributes): View {
        val imageLayoutId = View.generateViewId()
        val captionLayoutId = View.generateViewId()
        if (media[placeholderId] == null) {
            media[placeholderId] = ImageWithCaptionObject(placeholderId, attrs.getValue(SRC_ATTRIBUTE), imageLayoutId)
        }
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = VERTICAL

        val layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT)
        linearLayout.layoutParams = layoutParams
        val image = ImageView(context)
        image.id = imageLayoutId
        val imageParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                0,
        1.0f)
        imageParams.setMargins(5, 5, 5, 5)

        image.layoutParams = imageParams

        val caption = TextView(context)
        caption.id = captionLayoutId
        caption.text = attrs.getValue(CAPTION_ATTRIBUTE)
        val captionParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT)

        captionParams.setMargins(5, 5, 5, 5)
        captionParams.gravity = Gravity.CENTER_HORIZONTAL

        caption.layoutParams = captionParams

        linearLayout.addView(image)
        linearLayout.addView(caption)
        return linearLayout
    }

    override fun onViewCreated(view: View, placeholderId: String) {
        val image = media[placeholderId]!!
        val width = view.width
        val imageView = view.findViewById<ImageView>(image.layoutId)
        val height = getHeight(width)
//        imageView.layoutParams = ViewGroup.LayoutParams(width, height)

        Glide.with(view).load(image.src).into(imageView)
        super.onViewCreated(view, placeholderId)
    }

    override fun onPlaceholderDeleted(placeholderId: String) {
        media.remove(placeholderId)
    }

    data class ImageWithCaptionObject(val id: String, val src: String, val layoutId: Int)

    companion object {
        private const val DRAWER_TYPE = "image_with_caption"
        private const val CAPTION_ATTRIBUTE = "caption"
        private const val SRC_ATTRIBUTE = "src"

        fun insertImageWithCaption(placeholderManager: PlaceholderManager, id: String, src: String, caption: String) {
            placeholderManager.insertItem(id, DRAWER_TYPE, SRC_ATTRIBUTE to src, CAPTION_ATTRIBUTE to caption)
        }
    }
}

