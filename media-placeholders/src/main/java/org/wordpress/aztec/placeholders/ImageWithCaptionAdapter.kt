package org.wordpress.aztec.placeholders

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.placeholders.PlaceholderManager.PlaceholderAdapter.Proportion

/**
 * A sample adapter which creates a custom layout over the placeholder. Treat this as an example of what can be done.
 * This adapter creates an image with a caption under it
 */
class ImageWithCaptionAdapter(
        override val type: String = "image_with_caption"
) : PlaceholderManager.PlaceholderAdapter {
    private val media = mutableMapOf<String, StateFlow<ImageWithCaptionObject>>()
    private val scope = CoroutineScope(Dispatchers.Main)
    suspend override fun getHeight(attrs: AztecAttributes): Proportion {
        return Proportion.Ratio(attrs.getValue(HEIGHT).toFloatOrNull() ?: 0.5f)
    }

    suspend override fun createView(context: Context, placeholderUuid: String, viewParamsUpdate: StateFlow<PlaceholderManager.Placeholder.ViewParams>): View {
        val attrs = viewParamsUpdate.value.attrs
        val imageViewId = View.generateViewId()
        val stateFlow = viewParamsUpdate.map {
            ImageWithCaptionObject(placeholderUuid, it.attrs.getValue(SRC_ATTRIBUTE), imageViewId, it.width, it.height, it.initial)
        }.stateIn(scope)
        media[placeholderUuid] = stateFlow
        val imageWithCaptionObject = stateFlow.value
        Log.d("vojta2", "Drawing image with caption ${imageWithCaptionObject.src}")
        val captionLayoutId = View.generateViewId()
        val imageLayoutId = imageWithCaptionObject.layoutId
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.VERTICAL

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

    override fun animateLayoutChanges(): Boolean {
        return true
    }

    suspend override fun onViewCreated(view: View, placeholderUuid: String) {
        val image = media[placeholderUuid]!!
        scope.launch {
            image.collect {
                val imageView = view.findViewById<ImageView>(it.layoutId)
                ResizeAnimation(view, it.width, it.height).apply {
                    duration = if (it.initialLoad) {
                        0
                    } else {
                        200
                    }
                    view.startAnimation(this)
                }
                Glide.with(view).load(it.src).transition(DrawableTransitionOptions.withCrossFade()).into(imageView)
            }
        }
        super.onViewCreated(view, placeholderUuid)
    }

    class ResizeAnimation(private val view: View, private val newWidth: Int, private val newHeight: Int) : Animation() {
        private val startWidth: Int = view.width
        private val startHeight: Int = view.height

        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            view.updateLayoutParams {
                width = startWidth + ((newWidth - startWidth) * interpolatedTime).toInt()
                height = startHeight + ((newHeight - startHeight) * interpolatedTime).toInt()
            }
            Log.d("vojta", "Changing height to ${view.layoutParams.height} and width to ${view.layoutParams.width}")
            view.requestLayout()
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    override fun onPlaceholderDeleted(placeholderUuid: String) {
        media.remove(placeholderUuid)
    }

    data class ImageWithCaptionObject(val id: String, val src: String, val layoutId: Int, val width: Int, val height: Int, val initialLoad: Boolean)

    companion object {
        private const val ADAPTER_TYPE = "image_with_caption"
        private const val CAPTION_ATTRIBUTE = "caption"
        private const val SRC_ATTRIBUTE = "src"
        private const val HEIGHT = "height"

        suspend fun insertImageWithCaption(placeholderManager: PlaceholderManager, src: String, caption: String, height: Float = 0.5f, shouldMergePlaceholders: Boolean = true) {
            placeholderManager.insertOrUpdateItem(ADAPTER_TYPE, {
                shouldMergePlaceholders
            }) { currentAttributes, type, placeAtStart ->
                if (currentAttributes == null || type != ADAPTER_TYPE) {
                    mapOf(SRC_ATTRIBUTE to src, CAPTION_ATTRIBUTE to caption, HEIGHT to height.toString())
                } else {
                    val currentCaption = currentAttributes[CAPTION_ATTRIBUTE]
                    val newCaption = if (placeAtStart) {
                        "$caption - $currentCaption"
                    } else {
                        "$currentCaption - $caption"
                    }
                    mapOf(SRC_ATTRIBUTE to src, CAPTION_ATTRIBUTE to newCaption, HEIGHT to height.toString())
                }
            }
        }
    }
}

