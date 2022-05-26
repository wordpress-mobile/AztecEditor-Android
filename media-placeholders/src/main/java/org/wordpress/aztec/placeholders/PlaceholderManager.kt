package org.wordpress.aztec.placeholders

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.Layout
import android.text.Spanned
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecContentChangeWatcher
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import org.wordpress.aztec.Html
import org.wordpress.aztec.plugins.html2visual.IHtmlTagHandler
import org.wordpress.aztec.spans.AztecMediaClickableSpan
import org.xml.sax.Attributes
import java.util.UUID

/**
 * This class handles the "Placeholders". Placeholders are custom spans which are drawn in the Aztec text and the user
 * can interact with them in a similar way as with other media. These spans are invisible and are used by this class
 * as a place we can draw over with custom views. The custom views are placed in the `FrameLayout` which contains the
 * Aztec text item and are shifted up and down if anything above them changes (for example if the user adds a new line
 * before the placeholder).
 */
class PlaceholderManager(
        private val aztecText: AztecText,
        private val container: FrameLayout
) : AztecContentChangeWatcher.AztecTextChangeObserver,
        IHtmlTagHandler,
        Html.MediaCallback,
        AztecText.OnMediaDeletedListener,
        AztecText.OnVisibilityChangeListener {
    private val adapters = mutableMapOf<String, PlaceholderAdapter>()
    private val positionToId = mutableSetOf<Placeholder>()

    init {
        aztecText.setOnVisibilityChangeListener(this)
        aztecText.mediaCallback = this
        aztecText.contentChangeWatcher.registerObserver(this)
    }

    fun onDestroy() {
        aztecText.contentChangeWatcher.unregisterObserver(this)
        adapters.clear()
    }

    /**
     * Register a custom adapter to draw a custom view over a placeholder.
     */
    fun registerAdapter(placeholderAdapter: PlaceholderAdapter) {
        adapters[placeholderAdapter.type] = placeholderAdapter
    }

    /**
     * Call this method to manually insert a new item into the aztec text. There has to be a adapter associated with the
     * item type.
     * @param type placeholder type
     * @param attributes other attributes passed to the view. For example a `src` for an image.
     */
    fun insertItem(type: String, vararg attributes: Pair<String, String>) {
        val adapter = adapters[type]
                ?: throw IllegalArgumentException("Adapter for inserted type not found. Register it with `registerDrawer` method")
        val attrs = getAttributesForMedia(type, attributes)
        val drawable = buildPlaceholderDrawable(adapter)
        aztecText.insertMediaSpan(AztecPlaceholderSpan(aztecText.context, drawable, 0, attrs,
                this, aztecText))
        insertContentOverSpanWithId(attrs.getValue(UUID_ATTRIBUTE), null)
    }

    private fun buildPlaceholderDrawable(adapter: PlaceholderAdapter): Drawable {
        val drawable = ContextCompat.getDrawable(aztecText.context, android.R.color.transparent)!!
        drawable.setBounds(0, 0, aztecText.maxImagesWidth, adapter.getHeight(aztecText.maxImagesWidth))
        return drawable
    }

    private fun updateAllBelowSelection(selectionStart: Int) {
        positionToId.filter {
            it.elementPosition >= selectionStart - 1
        }.forEach {
            insertContentOverSpanWithId(it.uuid, it.elementPosition)
        }
    }

    private fun insertContentOverSpanWithId(uuid: String, currentPosition: Int? = null) {
        var aztecAttributes: AztecAttributes? = null
        val predicate = object : AztecText.AttributePredicate {
            override fun matches(attrs: Attributes): Boolean {
                val match = attrs.getValue(UUID_ATTRIBUTE) == uuid
                if (match) {
                    aztecAttributes = attrs as AztecAttributes
                }
                return match
            }
        }
        val targetPosition = aztecText.getElementPosition(predicate) ?: return

        insertInPosition(aztecAttributes ?: return, targetPosition, currentPosition)
    }

    private fun insertInPosition(attrs: AztecAttributes, targetPosition: Int, currentPosition: Int? = null) {
        validateAttributes(attrs)
        val uuid = attrs.getValue(UUID_ATTRIBUTE)
        val type = attrs.getValue(TYPE_ATTRIBUTE)
        val textViewLayout: Layout = aztecText.layout
        val parentTextViewRect = Rect()
        val targetLineOffset = getLineForOffset(targetPosition)
        if (currentPosition != null) {
            if (targetLineOffset != 0 && currentPosition == targetPosition) {
                return
            } else {
                positionToId.removeAll {
                    it.uuid == uuid
                }
            }
        }
        textViewLayout.getLineBounds(targetLineOffset, parentTextViewRect)

        val parentTextViewLocation = intArrayOf(0, 0)
        aztecText.getLocationOnScreen(parentTextViewLocation)
        val parentTextViewTopAndBottomOffset = aztecText.scrollY + aztecText.compoundPaddingTop

        parentTextViewRect.top += parentTextViewTopAndBottomOffset
        parentTextViewRect.bottom += parentTextViewTopAndBottomOffset

        var box = container.findViewWithTag<View>(uuid)
        val exists = box != null
        val adapter = adapters[type]!!
        if (!exists) {
            box = adapter.createView(container.context, uuid, attrs)
        }
        val params = FrameLayout.LayoutParams(
                parentTextViewRect.right - parentTextViewRect.left - 20,
                parentTextViewRect.bottom - parentTextViewRect.top - 20
        )
        val padding = 10
        params.setMargins(parentTextViewRect.left + padding, parentTextViewRect.top + padding, parentTextViewRect.right - padding, parentTextViewRect.bottom - padding)
        box.layoutParams = params
        box.tag = uuid
        box.setBackgroundColor(Color.TRANSPARENT)
        box.setOnTouchListener(adapter)
        positionToId.add(Placeholder(targetPosition, uuid))
        if (!exists && box.parent == null) {
            container.addView(box)
            adapter.onViewCreated(box, uuid)
        }
    }

    private fun getLineForOffset(offset: Int): Int {
        var counter = 0
        var index = 0
        for (line in aztecText.text.split("\n")) {
            counter += line.length + 1
            if (counter > offset) {
                break
            }
            index += 1
        }
        return index
    }

    private fun validateAttributes(attributes: AztecAttributes): Boolean {
        return attributes.hasAttribute(UUID_ATTRIBUTE) &&
                attributes.hasAttribute(TYPE_ATTRIBUTE) &&
                adapters[attributes.getValue(TYPE_ATTRIBUTE)] != null
    }

    private fun getAttributesForMedia(type: String, attributes: Array<out Pair<String, String>>): AztecAttributes {
        val attrs = AztecAttributes()
        attrs.setValue(UUID_ATTRIBUTE, UUID.randomUUID().toString())
        attrs.setValue(TYPE_ATTRIBUTE, type)
        attributes.forEach {
            attrs.setValue(it.first, it.second)
        }
        return attrs
    }

    /**
     * Called when the aztec text content changes.
     */
    override fun onContentChanged() {
        updateAllBelowSelection(aztecText.selectionStart)
    }

    /**
     * Called when any media is deleted. We use this method to remove the custom views if the placeholder is deleted.
     */
    override fun onMediaDeleted(attrs: AztecAttributes) {
        if (validateAttributes(attrs)) {
            val uuid = attrs.getValue(UUID_ATTRIBUTE)
            val adapter = adapters[attrs.getValue(TYPE_ATTRIBUTE)]
            adapter?.onPlaceholderDeleted(uuid)
            positionToId.removeAll { it.uuid == uuid }
            container.findViewWithTag<View>(uuid)?.let {
                it.visibility = View.GONE
                container.removeView(it)
            }
        }
    }

    /**
     * Called before media is deleted. There is a delay between user deleting a media and when the media is actually is
     * confirmed. That's why we first hide the media and we delete it when `onMediaDeleted` is actually called.
     */
    override fun beforeMediaDeleted(attrs: AztecAttributes) {
        if (validateAttributes(attrs)) {
            val uuid = attrs.getValue(UUID_ATTRIBUTE)
            container.findViewWithTag<View>(uuid)?.let {
                it.visibility = View.GONE
            }
        }
    }

    override fun canHandleTag(tag: String): Boolean {
        return tag == HTML_TAG
    }

    /**
     * This method handled a `placeholder` tag found in the HTML. It creates a placeholder and inserts a view over it.
     */
    override fun handleTag(opening: Boolean, tag: String, output: Editable, attributes: Attributes, nestingLevel: Int): Boolean {
        if (opening) {
            val type = attributes.getValue(TYPE_ATTRIBUTE)
            val adapter = adapters[type] ?: return false
            val drawable = buildPlaceholderDrawable(adapter)
            val aztecAttributes = AztecAttributes(attributes)
            aztecAttributes.setValue(UUID_ATTRIBUTE, UUID.randomUUID().toString())
            val span = AztecPlaceholderSpan(
                    context = aztecText.context,
                    drawable = drawable,
                    nestingLevel = nestingLevel,
                    attributes = aztecAttributes,
                    onMediaDeletedListener = this
            )
            val clickableSpan = AztecMediaClickableSpan(span)
            val position = output.length
            output.setSpan(span, position, position, Spanned.SPAN_MARK_MARK)
            output.setSpan(clickableSpan, position, position, Spanned.SPAN_MARK_MARK)
            output.append(Constants.IMG_CHAR)
            output.setSpan(clickableSpan, position, output.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            output.setSpan(span, position, output.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            span.applyInlineStyleAttributes(output, position, output.length)
        }
        return tag == HTML_TAG
    }

    override fun mediaLoadingStarted() {
        clearAllViews()
        val spans = aztecText.editableText.getSpans(0, aztecText.editableText.length, AztecPlaceholderSpan::class.java)

        if (spans == null || spans.isEmpty()) {
            return
        }

        aztecText.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                aztecText.viewTreeObserver.removeOnGlobalLayoutListener(this)
                spans.forEach {
                    val type = it.attributes.getValue(TYPE_ATTRIBUTE)
                    val adapter = adapters[type] ?: return
                    val editorWidth = aztecText.width
                    if (it.drawable?.bounds?.right == editorWidth) return
                    it.drawable?.setBounds(0, 0, editorWidth, adapter.getHeight(editorWidth))
                    aztecText.post {
                        aztecText.refreshText(false)
                        insertInPosition(it.attributes, aztecText.editableText.getSpanStart(it))
                    }
                }
            }
        })
    }

    private fun clearAllViews() {
        for (placeholder in positionToId) {
            container.findViewWithTag<View>(placeholder.uuid)?.let {
                it.visibility = View.GONE
                container.removeView(it)
            }
        }
        positionToId.clear()
    }

    override fun onVisibility(visibility: Int) {
        for (placeholder in positionToId) {
            container.findViewWithTag<View>(placeholder.uuid)?.visibility = visibility
        }
    }

    /**
     * A adapter for a custom view drawn over the placeholder in the Aztec text.
     */
    interface PlaceholderAdapter: View.OnTouchListener {
        /**
         * Creates the view but it's called before the view is measured. If you need the actual width and height. Use
         * the `onViewCreated` method where the view is already present in its correct size.
         * @param context necessary to build custom views
         * @param placeholderUuid the placeholder UUID
         * @param attrs aztec attributes of the view
         */
        fun createView(context: Context, placeholderUuid: String, attrs: AztecAttributes): View

        /**
         * Called after the view is measured. Use this method if you need the actual width and height of the view to
         * draw your media.
         * @param view the frame layout wrapping the custom view
         * @param placeholderUuid the placeholder ID
         */
        fun onViewCreated(view: View, placeholderUuid: String) {}

        /**
         * Called when the placeholder is deleted by the user. Use this method if you need to clear your data when the
         * item is deleted (for example delete an image in your DB).
         * @param placeholderUuid placeholder UUID
         */
        fun onPlaceholderDeleted(placeholderUuid: String) {}

        /**
         * Override this method if you want to handle view touches. To handle clicks on subviews just use
         * `setOnClickListener` on the view that you want to handle the click.
         */
        override fun onTouch(v: View, event: MotionEvent): Boolean { return false }

        /**
         * Override this field to set the height of the placeholder. It could be either a ratio of width to height or
         * a fixed size.
         */
        val placeholderHeight: PlaceholderHeight

        /**
         * Define unique string type here in order to differentiate between the adapters drawing the custom views.
         */
        val type: String

        /**
         * Returns height of the view based on the width and the placeholder height.
         */
        fun getHeight(width: Int): Int {
            return placeholderHeight.let {
                when (it) {
                    is PlaceholderHeight.Fixed -> it.height
                    is PlaceholderHeight.Ratio -> (it.ratio * width).toInt()
                }
            }
        }

        sealed class PlaceholderHeight {
            data class Fixed(val height: Int) : PlaceholderHeight()
            data class Ratio(val ratio: Float) : PlaceholderHeight()
        }
    }

    data class Placeholder(val elementPosition: Int, val uuid: String)

    companion object {
        private const val HTML_TAG = "placeholder"
        private const val UUID_ATTRIBUTE = "uuid"
        private const val TYPE_ATTRIBUTE = "type"
    }
}
