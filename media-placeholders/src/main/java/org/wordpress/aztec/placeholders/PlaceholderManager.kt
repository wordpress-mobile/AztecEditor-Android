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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecContentChangeWatcher
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import org.wordpress.aztec.Html
import org.wordpress.aztec.plugins.html2visual.IHtmlTagHandler
import org.wordpress.aztec.spans.AztecMediaClickableSpan
import org.xml.sax.Attributes
import java.lang.ref.WeakReference
import java.util.UUID
import kotlin.coroutines.CoroutineContext
import kotlin.math.min

/**
 * This class handles the "Placeholders". Placeholders are custom spans which are drawn in the Aztec text and the user
 * can interact with them in a similar way as with other media. These spans are invisible and are used by this class
 * as a place we can draw over with custom views. The custom views are placed in the `FrameLayout` which contains the
 * Aztec text item and are shifted up and down if anything above them changes (for example if the user adds a new line
 * before the placeholder).
 */
class PlaceholderManager(
        private val aztecText: AztecText,
        private val container: FrameLayout,
        private val htmlTag: String = DEFAULT_HTML_TAG,
        private val generateUuid: () -> String = {
            UUID.randomUUID().toString()
        }
) : AztecContentChangeWatcher.AztecTextChangeObserver,
        IHtmlTagHandler,
        Html.MediaCallback,
        AztecText.OnMediaDeletedListener,
        AztecText.OnVisibilityChangeListener,
        CoroutineScope {
    private val adapters = mutableMapOf<String, PlaceholderAdapter>()
    private val positionToIdMutex = Mutex()
    private val positionToId = mutableSetOf<Placeholder>()
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    init {
        aztecText.setOnVisibilityChangeListener(this)
        aztecText.mediaCallback = this
        aztecText.contentChangeWatcher.registerObserver(this)
    }

    fun onDestroy() {
        positionToId.forEach {
            container.findViewWithTag<View>(it.uuid)?.let { placeholder ->
                container.removeView(placeholder)
            }
        }
        positionToId.clear()
        aztecText.contentChangeWatcher.unregisterObserver(this)
        adapters.values.forEach { it.onDestroy() }
        adapters.clear()
        job.cancel()
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
    suspend fun insertItem(type: String, vararg attributes: Pair<String, String>) {
        val adapter = adapters[type]
                ?: throw IllegalArgumentException("Adapter for inserted type not found. Register it with `registerAdapter` method")
        val attrs = getAttributesForMedia(type, attributes)
        val drawable = buildPlaceholderDrawable(adapter, attrs)
        aztecText.insertMediaSpan(AztecPlaceholderSpan(aztecText.context, drawable, 0, attrs,
                this, aztecText, WeakReference(adapter), TAG = htmlTag))
        insertContentOverSpanWithId(attrs.getValue(UUID_ATTRIBUTE))
    }

    /**
     * Call this method to insert an item with an option to merge it with the previous item. This could be used to
     * build a gallery of images on adding a new image.
     * @param type placeholder type
     * @param shouldMergeItem this method should return true when the previous type is compatible and should be updated
     * @param updateItem function to update current parameters with new params
     */
    suspend fun insertOrUpdateItem(type: String, shouldMergeItem: (currentItemType: String) -> Boolean = { true }, updateItem: (currentAttributes: Map<String, String>?, currentType: String?) -> Map<String, String>) {
        val previousIndex = (aztecText.selectionStart - 1).coerceAtLeast(0)
        val indexBeforePrevious = (aztecText.selectionStart - 2).coerceAtLeast(0)
        val from = if (aztecText.editableText.length > previousIndex && aztecText.editableText[previousIndex] == Constants.IMG_CHAR) {
            previousIndex
        } else if (aztecText.editableText.length > previousIndex && aztecText.editableText[previousIndex] == '\n') {
            indexBeforePrevious
        } else {
            aztecText.selectionStart
        }
        val editableText = aztecText.editableText
        val currentItem = editableText.getSpans(
                from,
                aztecText.selectionStart,
                AztecPlaceholderSpan::class.java
        ).lastOrNull()
        val currentType = currentItem?.attributes?.getValue(TYPE_ATTRIBUTE)
        if (currentType != null && shouldMergeItem(currentType)) {
            updateSpan(type, currentItem, updateItem, currentType)
        } else {
            insertItem(type, *updateItem(null, null).toList().toTypedArray())
        }
    }

    private suspend fun updateSpan(
            type: String,
            currentItem: AztecPlaceholderSpan,
            updateItem: (currentAttributes: Map<String, String>, currentType: String) -> Map<String, String>,
            currentType: String
    ) {
        val adapter = adapters[type]
                ?: throw IllegalArgumentException("Adapter for inserted type not found. Register it with `registerAdapter` method")
        val currentAttributes = mutableMapOf<String, String>()
        val uuid = currentItem.attributes.getValue(UUID_ATTRIBUTE)
        for (i in 0 until currentItem.attributes.length) {
            val name = currentItem.attributes.getQName(i)
            val value = currentItem.attributes.getValue(name)
            currentAttributes[name] = value
        }
        val updatedAttributes = updateItem(currentAttributes, currentType)
        removeItem(false) { aztecAttributes ->
            aztecAttributes.getValue(UUID_ATTRIBUTE) == uuid
        }
        val attrs = AztecAttributes().apply {
            updatedAttributes.forEach { (key, value) ->
                setValue(key, value)
            }
        }
        attrs.setValue(UUID_ATTRIBUTE, uuid)
        attrs.setValue(TYPE_ATTRIBUTE, type)
        val drawable = buildPlaceholderDrawable(adapter, attrs)
        aztecText.insertMediaSpan(AztecPlaceholderSpan(aztecText.context, drawable, 0, attrs,
                this, aztecText, WeakReference(adapter), TAG = htmlTag))
        insertContentOverSpanWithId(uuid)
    }

    /**
     * Use this function to either update or remove an item. The decision whether to remove or update will be made
     * based upon the results of the parameter functions. An example of usage is a gallery of images. If the user wants
     * to remove one image in the gallery, they would call this method. If the removed image is one of many, they might
     * want to update the current parameters instead of removing the entire gallery. However, if the removed image is
     * the last one in the gallery, they will probably want to remove the entire gallery.
     * @param uuid UUID of the span we want to remove or update
     * @param shouldUpdateItem This function should return true if the span can be updated, false if it should be removed
     * @param updateItem Function that updates the selected item
     */
    suspend fun removeOrUpdate(uuid: String, shouldUpdateItem: (Attributes) -> Boolean, updateItem: (currentAttributes: Map<String, String>) -> Map<String, String>): Boolean {
        val currentItem = aztecText.editableText.getSpans(0, aztecText.length(), AztecPlaceholderSpan::class.java).find {
            it.attributes.getValue(UUID_ATTRIBUTE) == uuid
        } ?: return false
        if (shouldUpdateItem(currentItem.attributes)) {
            val type = currentItem.attributes.getValue(TYPE_ATTRIBUTE)
            val selectionStart = aztecText.selectionStart
            val selectionEnd = aztecText.selectionEnd
            aztecText.setSelection(aztecText.editableText.getSpanStart(currentItem))
            updateSpan(type, currentItem, updateItem = { attributes, _ ->
                updateItem(attributes)
            }, type)
            aztecText.setSelection(selectionStart, selectionEnd)
        } else {
            removeItem(uuid)
        }
        return true
    }

    /**
     * Call this method to remove a placeholder from both the AztecText and the overlaying layer programmatically.
     * @param predicate determines whether a span should be removed
     */
    fun removeItem(notifyContentChange: Boolean = true, predicate: (Attributes) -> Boolean) {
        aztecText.removeMedia(notifyContentChange) { predicate(it) }
    }

    /**
     * Call this method to remove a placeholder from both the AztecText and the overlaying layer programmatically.
     * @param uuid of the removed item
     */
    fun removeItem(uuid: String) {
        aztecText.removeMedia { it.getValue(UUID_ATTRIBUTE) == uuid }
    }

    private suspend fun buildPlaceholderDrawable(adapter: PlaceholderAdapter, attrs: AztecAttributes): Drawable {
        val drawable = ContextCompat.getDrawable(aztecText.context, android.R.color.transparent)!!
        updateDrawableBounds(adapter, attrs, drawable)
        return drawable
    }

    /**
     * Call this method to reload all the placeholders
     */
    suspend fun reloadAllPlaceholders() {
        val tempPositionToId = positionToId.toList()
        tempPositionToId.forEach { placeholder ->
            val isValid = positionToIdMutex.withLock {
                positionToId.contains(placeholder)
            }
            if (isValid) {
                insertContentOverSpanWithId(placeholder.uuid)
            }
        }
    }

    /**
     * Call this method to relaod a placeholder with UUID
     */
    suspend fun refreshWithUuid(uuid: String) {
        insertContentOverSpanWithId(uuid)
    }

    private suspend fun insertContentOverSpanWithId(uuid: String) {
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

        insertInPosition(aztecAttributes ?: return, targetPosition)
    }

    private suspend fun insertInPosition(attrs: AztecAttributes, targetPosition: Int) {
        if (!validateAttributes(attrs)) {
            return
        }
        val uuid = attrs.getValue(UUID_ATTRIBUTE)
        val type = attrs.getValue(TYPE_ATTRIBUTE)
        // At this point we can get to a race condition where the aztec text layout is not yet initialized.
        // We want to wait a bit and make sure it's properly loaded.
        var counter = 0
        while (aztecText.layout == null && counter < 10) {
            delay(50)
            counter += 1
        }
        val textViewLayout: Layout = aztecText.layout ?: return
        val parentTextViewRect = Rect()
        val targetLineOffset = textViewLayout.getLineForOffset(targetPosition)
        textViewLayout.getLineBounds(targetLineOffset, parentTextViewRect)

        val parentTextViewLocation = intArrayOf(0, 0)
        aztecText.getLocationOnScreen(parentTextViewLocation)
        val parentTextViewTopAndBottomOffset = aztecText.scrollY + aztecText.compoundPaddingTop

        val adapter = adapters[type]!!
        val windowWidth = parentTextViewRect.right - parentTextViewRect.left - EDITOR_INNER_PADDING
        val height = adapter.calculateHeight(attrs, windowWidth)
        parentTextViewRect.top += parentTextViewTopAndBottomOffset
        parentTextViewRect.bottom = parentTextViewRect.top + height

        positionToIdMutex.withLock {
            positionToId.removeAll {
                it.uuid == uuid
            }
        }

        var box = container.findViewWithTag<View>(uuid)
        val exists = box != null
        if (!exists) {
            box = adapter.createView(container.context, uuid, attrs)
        }
        val params = FrameLayout.LayoutParams(
                adapter.calculateWidth(attrs, windowWidth) - EDITOR_INNER_PADDING,
                height - EDITOR_INNER_PADDING
        )
        val padding = 10
        params.setMargins(
                parentTextViewRect.left + padding + aztecText.paddingStart,
                parentTextViewRect.top + padding,
                0,
                0
        )
        box.layoutParams = params
        box.tag = uuid
        box.setBackgroundColor(Color.TRANSPARENT)
        box.setOnTouchListener(adapter)
        positionToIdMutex.withLock {
            positionToId.add(Placeholder(targetPosition, uuid))
        }
        if (!exists && box.parent == null) {
            container.addView(box)
            adapter.onViewCreated(box, uuid)
        }
    }

    private fun validateAttributes(attributes: AztecAttributes): Boolean {
        return attributes.hasAttribute(UUID_ATTRIBUTE) &&
                attributes.hasAttribute(TYPE_ATTRIBUTE) &&
                adapters[attributes.getValue(TYPE_ATTRIBUTE)] != null
    }

    private fun getAttributesForMedia(type: String, attributes: Array<out Pair<String, String>>): AztecAttributes {
        val attrs = AztecAttributes()
        attrs.setValue(UUID_ATTRIBUTE, generateUuid())
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
        launch {
            reloadAllPlaceholders()
        }
    }

    /**
     * Called when any media is deleted. We use this method to remove the custom views if the placeholder is deleted.
     */
    override fun onMediaDeleted(attrs: AztecAttributes) {
        if (validateAttributes(attrs)) {
            val uuid = attrs.getValue(UUID_ATTRIBUTE)
            val adapter = adapters[attrs.getValue(TYPE_ATTRIBUTE)]
            adapter?.onPlaceholderDeleted(uuid)
            launch {
                positionToIdMutex.withLock {
                    positionToId.removeAll { it.uuid == uuid }
                }
            }
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
        return tag == htmlTag
    }

    /**
     * This method handled a `placeholder` tag found in the HTML. It creates a placeholder and inserts a view over it.
     */
    override fun handleTag(opening: Boolean, tag: String, output: Editable, attributes: Attributes, nestingLevel: Int): Boolean {
        if (opening) {
            val type = attributes.getValue(TYPE_ATTRIBUTE)
            val adapter = adapters[type] ?: return false
            val aztecAttributes = AztecAttributes(attributes)
            aztecAttributes.setValue(UUID_ATTRIBUTE, generateUuid())
            val drawable = runBlocking { buildPlaceholderDrawable(adapter, aztecAttributes) }
            val span = AztecPlaceholderSpan(
                    context = aztecText.context,
                    drawable = drawable,
                    nestingLevel = nestingLevel,
                    attributes = aztecAttributes,
                    onMediaDeletedListener = this,
                    adapter = WeakReference(adapter),
                    TAG = htmlTag
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
        return tag == htmlTag
    }

    override fun mediaLoadingStarted() {
        val spans = aztecText.editableText.getSpans(0, aztecText.editableText.length, AztecPlaceholderSpan::class.java)

        if (spans == null || spans.isEmpty()) {
            return
        }
        aztecText.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
        aztecText.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }

    private val globalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        private var job: Job? = null
        override fun onGlobalLayout() {
            if (job?.isActive == true) {
                return
            }
            aztecText.viewTreeObserver.removeOnGlobalLayoutListener(this)
            val spans = aztecText.editableText.getSpans(
                    0,
                    aztecText.editableText.length,
                    AztecPlaceholderSpan::class.java
            )

            if (spans == null || spans.isEmpty()) {
                return
            }
            job = launch {
                clearAllViews()
                spans.forEach {
                    val type = it.attributes.getValue(TYPE_ATTRIBUTE)
                    val adapter = adapters[type] ?: return@forEach
                    updateDrawableBounds(adapter, it.attributes, it.drawable)
                    aztecText.refreshText(false)
                    insertInPosition(it.attributes, aztecText.editableText.getSpanStart(it))
                }
            }
        }
    }

    private suspend fun updateDrawableBounds(adapter: PlaceholderAdapter, attrs: AztecAttributes, drawable: Drawable?) {
        val editorWidth = if (aztecText.width > 0) {
            aztecText.width - aztecText.paddingStart - aztecText.paddingEnd - EDITOR_INNER_PADDING
        } else aztecText.maxImagesWidth
        if (drawable?.bounds?.right != editorWidth) {
            drawable?.setBounds(0, 0, adapter.calculateWidth(attrs, editorWidth), adapter.calculateHeight(attrs, editorWidth))
        }
    }

    private suspend fun clearAllViews() {
        positionToIdMutex.withLock {
            for (placeholder in positionToId) {
                container.findViewWithTag<View>(placeholder.uuid)?.let {
                    it.visibility = View.GONE
                    container.removeView(it)
                }
            }
            positionToId.clear()
        }
    }

    override fun onVisibility(visibility: Int) {
        launch {
            positionToIdMutex.withLock {
                for (placeholder in positionToId) {
                    container.findViewWithTag<View>(placeholder.uuid)?.visibility = visibility
                }
            }
        }
    }

    /**
     * A adapter for a custom view drawn over the placeholder in the Aztec text.
     */
    interface PlaceholderAdapter : View.OnTouchListener {
        /**
         * Creates the view but it's called before the view is measured. If you need the actual width and height. Use
         * the `onViewCreated` method where the view is already present in its correct size.
         * @param context necessary to build custom views
         * @param placeholderUuid the placeholder UUID
         * @param attrs aztec attributes of the view
         */
        suspend fun createView(context: Context, placeholderUuid: String, attrs: AztecAttributes): View

        /**
         * Called after the view is measured. Use this method if you need the actual width and height of the view to
         * draw your media.
         * @param view the frame layout wrapping the custom view
         * @param placeholderUuid the placeholder ID
         */
        suspend fun onViewCreated(view: View, placeholderUuid: String) {}

        /**
         * Called when the placeholder is deleted by the user. Use this method if you need to clear your data when the
         * item is deleted (for example delete an image in your DB).
         * @param placeholderUuid placeholder UUID
         */
        fun onPlaceholderDeleted(placeholderUuid: String) {}

        /**
         * This method is called when the placeholders are destroyed
         */
        fun onDestroy() {}

        /**
         * Override this method if you want to handle view touches. To handle clicks on subviews just use
         * `setOnClickListener` on the view that you want to handle the click.
         */
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            return false
        }

        /**
         * Define unique string type here in order to differentiate between the adapters drawing the custom views.
         */
        val type: String

        /**
         * Returns width of the view based on the HTML attributes. Use this method to either set fixed width or to
         * calculate width based on the view.
         */
        suspend fun getWidth(attrs: AztecAttributes): Proportion = Proportion.Ratio(1.0f)

        /**
         * Returns height of the view based on the HTML attributes. Use this method to either set fixed height or to
         * calculate width based on the view.
         */
        suspend fun getHeight(attrs: AztecAttributes): Proportion

        /**
         * Returns height of the view based on the width and the placeholder height.
         */
        suspend fun calculateHeight(attrs: AztecAttributes, windowWidth: Int): Int {
            return getHeight(attrs).let { height ->
                when (height) {
                    is Proportion.Fixed -> height.value
                    is Proportion.Ratio -> {
                        val ratio = if (height.ratio < 0.1) {
                            0.1f
                        } else {
                            height.ratio
                        }
                        val result = (ratio * calculateWidth(attrs, windowWidth)).toInt()
                        if (height.limit != null && height.limit < result) {
                            height.limit
                        } else {
                            result
                        }
                    }
                }
            }
        }

        /**
         * Returns height of the view based on the width and the placeholder height.
         */
        suspend fun calculateWidth(attrs: AztecAttributes, windowWidth: Int): Int {
            return getWidth(attrs).let { width ->
                when (width) {
                    is Proportion.Fixed -> min(windowWidth, width.value)
                    is Proportion.Ratio -> {
                        val safeRatio: Float = when {
                            width.ratio < 0.1 -> 0.1f
                            width.ratio > 1.0 -> 1.0f
                            else -> width.ratio
                        }
                        val result = (safeRatio * windowWidth).toInt()
                        if (width.limit != null && result > width.limit) {
                            width.limit
                        } else {
                            result
                        }
                    }
                }
            }
        }

        sealed class Proportion {
            data class Fixed(val value: Int, val limit: Int? = null) : Proportion()
            data class Ratio(val ratio: Float, val limit: Int? = null) : Proportion()
        }
    }

    data class Placeholder(val elementPosition: Int, val uuid: String)

    companion object {
        private const val DEFAULT_HTML_TAG = "placeholder"
        private const val UUID_ATTRIBUTE = "uuid"
        private const val TYPE_ATTRIBUTE = "type"
        private const val EDITOR_INNER_PADDING = 20
    }
}
