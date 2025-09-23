@file:Suppress("ktlint")

package org.wordpress.aztec.placeholders

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.Layout
import android.text.Spanned
import android.view.View
import android.view.ViewTreeObserver
import android.os.Looper
import android.view.ViewGroup
import android.view.View.MeasureSpec
import androidx.compose.ui.platform.ComposeView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecContentChangeWatcher
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import org.wordpress.aztec.Html
import org.wordpress.aztec.placeholders.PlaceholderManager.Companion.DEFAULT_HTML_TAG
import org.wordpress.aztec.placeholders.PlaceholderManager.Companion.EDITOR_INNER_PADDING
import org.wordpress.aztec.placeholders.PlaceholderManager.Companion.TYPE_ATTRIBUTE
import org.wordpress.aztec.placeholders.PlaceholderManager.Companion.UUID_ATTRIBUTE
import org.wordpress.aztec.plugins.html2visual.IHtmlPreprocessor
import org.wordpress.aztec.plugins.html2visual.IHtmlTagHandler
import org.wordpress.aztec.spans.AztecMediaClickableSpan
import org.xml.sax.Attributes
import java.lang.ref.WeakReference
import java.util.UUID
import kotlin.coroutines.CoroutineContext

/**
 * This class handles the "Placeholders". Placeholders are custom spans which are drawn in the Aztec text and the user
 * can interact with them in a similar way as with other media. These spans are invisible and are used by this class
 * as a place we can draw over with custom views. The custom views are placed in the `FrameLayout` which contains the
 * Aztec text item and are shifted up and down if anything above them changes (for example if the user adds a new line
 * before the placeholder).
 */
class ComposePlaceholderManager(
    private val aztecText: AztecText,
    private val htmlTag: String = DEFAULT_HTML_TAG,
    private val generateUuid: () -> String = {
        UUID.randomUUID().toString()
    }
) : PlaceholderManager,
    AztecContentChangeWatcher.AztecTextChangeObserver,
    IHtmlTagHandler,
    Html.MediaCallback,
    AztecText.OnMediaDeletedListener,
    AztecText.OnVisibilityChangeListener,
    CoroutineScope,
    IHtmlPreprocessor {
    private val adapters = mutableMapOf<String, ComposePlaceholderAdapter>()
    private val positionToIdMutex = Mutex()
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    private val _composeViewState = MutableStateFlow(emptyMap<String, ComposeView>())
    private val composeViewState = _composeViewState.map { map ->
        map.values.filter { it.visible }.sortedBy { it.topMargin }
    }

    init {
        aztecText.setOnVisibilityChangeListener(this)
        aztecText.mediaCallback = this
        aztecText.contentChangeWatcher.registerObserver(this)
    }

    data class ComposeView(
        val uuid: String,
        val width: Int,
        val height: Int,
        val topMargin: Int,
        val leftMargin: Int,
        val visible: Boolean,
        val adapterKey: String,
        val attrs: AztecAttributes
    )

    @Composable
    fun Draw() {
        val density = LocalDensity.current

        val values = composeViewState.collectAsState(emptyList()).value

        values.forEach { composeView ->
            Box(
                Modifier
                    .zIndex(9f)
                    .padding(
                        top = with(density) { composeView.topMargin.toDp() },
                        start = with(density) { composeView.leftMargin.toDp() }
                    )
                    .width(
                        with(density) { composeView.width.toDp() }
                    )
                    .height(
                        with(density) { composeView.height.toDp() }
                    )
            ) {
                key(composeView.uuid, composeView.width, composeView.height) {
                    adapters[composeView.adapterKey]?.Placeholder(
                        composeView.uuid,
                        composeView.attrs
                    )
                }
            }
        }
    }

    fun onDestroy() {
        _composeViewState.value = emptyMap()
        aztecText.contentChangeWatcher.unregisterObserver(this)
        adapters.values.forEach { it.onDestroy() }
        adapters.clear()
        job.cancel()
    }

    /**
     * Register a custom adapter to draw a custom view over a placeholder.
     */
    fun registerAdapter(placeholderAdapter: ComposePlaceholderAdapter) {
        adapters[placeholderAdapter.type] = placeholderAdapter
    }

    /**
     * Call this method to manually insert a new item into the aztec text. There has to be a adapter associated with the
     * item type.
     * @param type placeholder type
     * @param attributes other attributes passed to the view. For example a `src` for an image.
     */
    suspend override fun insertItem(type: String, vararg attributes: Pair<String, String>) {
        val adapter = adapters[type]
            ?: throw IllegalArgumentException("Adapter for inserted type not found. Register it with `registerAdapter` method")
        val attrs = getAttributesForMedia(type, attributes)
        val drawable = buildPlaceholderDrawable(adapter, attrs)
        aztecText.insertMediaSpan(
            AztecPlaceholderSpan(
                drawable, 0, attrs,
                this, aztecText, WeakReference(adapter), TAG = htmlTag
            )
        )
        insertContentOverSpanWithId(attrs.getValue(UUID_ATTRIBUTE))
    }

    /**
     * Call this method to insert an item with an option to merge it with the previous item. This could be used to
     * build a gallery of images on adding a new image.
     * @param type placeholder type
     * @param shouldMergeItem this method should return true when the previous type is compatible and should be updated
     * @param updateItem function to update current parameters with new params
     */
    suspend override fun insertOrUpdateItem(
        type: String,
        shouldMergeItem: (currentItemType: String) -> Boolean,
        updateItem: (
            currentAttributes: Map<String, String>?,
            currentType: String?,
            placeAtStart: Boolean
        ) -> Map<String, String>
    ) {
        val targetItem = getTargetItem()
        val targetSpan = targetItem?.span
        val currentType = targetSpan?.attributes?.getValue(TYPE_ATTRIBUTE)
        if (currentType != null) {
            if (shouldMergeItem(currentType)) {
                updateSpan(type, targetItem.span, targetItem.placeAtStart, updateItem, currentType)
            } else {
                val (newLinePosition, targetSelection) = if (targetItem.placeAtStart) {
                    targetItem.spanStart to targetItem.spanStart
                } else {
                    targetItem.spanEnd to targetItem.spanEnd + 2
                }
                aztecText.text.insert(newLinePosition, Constants.NEWLINE_STRING)
                aztecText.setSelection(targetSelection)
                insertItem(type, *updateItem(null, null, false).toList().toTypedArray())
            }
        } else {
            insertItem(type, *updateItem(null, null, false).toList().toTypedArray())
        }
    }

    private suspend fun updateSpan(
        type: String,
        targetSpan: AztecPlaceholderSpan,
        placeAtStart: Boolean,
        updateItem: (currentAttributes: Map<String, String>, currentType: String, placeAtStart: Boolean) -> Map<String, String>,
        currentType: String
    ) {
        val adapter = adapters[type]
            ?: throw IllegalArgumentException("Adapter for inserted type not found. Register it with `registerAdapter` method")
        val currentAttributes = mutableMapOf<String, String>()
        val uuid = targetSpan.attributes.getValue(UUID_ATTRIBUTE)
        for (i in 0 until targetSpan.attributes.length) {
            val name = targetSpan.attributes.getQName(i)
            val value = targetSpan.attributes.getValue(name)
            currentAttributes[name] = value
        }
        val updatedAttributes = updateItem(currentAttributes, currentType, placeAtStart)
        val attrs = AztecAttributes().apply {
            updatedAttributes.forEach { (key, value) ->
                setValue(key, value)
            }
        }
        attrs.setValue(UUID_ATTRIBUTE, uuid)
        attrs.setValue(TYPE_ATTRIBUTE, type)
        val drawable = buildPlaceholderDrawable(adapter, attrs)
        val span = AztecPlaceholderSpan(
            drawable, 0, attrs,
            this, aztecText, WeakReference(adapter), TAG = htmlTag
        )
        aztecText.replaceMediaSpan(span) { attributes ->
            attributes.getValue(UUID_ATTRIBUTE) == uuid
        }
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
    suspend override fun removeOrUpdate(
        uuid: String,
        shouldUpdateItem: (Attributes) -> Boolean,
        updateItem: (currentAttributes: Map<String, String>) -> Map<String, String>
    ): Boolean {
        val currentItem =
            aztecText.editableText.getSpans(0, aztecText.length(), AztecPlaceholderSpan::class.java)
                .find {
                    it.attributes.getValue(UUID_ATTRIBUTE) == uuid
                } ?: return false
        if (shouldUpdateItem(currentItem.attributes)) {
            val type = currentItem.attributes.getValue(TYPE_ATTRIBUTE)
            val selectionStart = aztecText.selectionStart
            val selectionEnd = aztecText.selectionEnd
            aztecText.setSelection(aztecText.editableText.getSpanStart(currentItem))
            updateSpan(type, currentItem, updateItem = { attributes, _, _ ->
                updateItem(attributes)
            }, placeAtStart = false, currentType = type)
            aztecText.setSelection(selectionStart, selectionEnd)
        } else {
            removeItem(uuid)
        }
        return true
    }

    private data class TargetItem(
        val span: AztecPlaceholderSpan,
        val placeAtStart: Boolean,
        val spanStart: Int,
        val spanEnd: Int
    )

    private fun getTargetItem(): TargetItem? {
        if (aztecText.length() == 0) {
            return null
        }
        val limitLength = aztecText.length() - 1
        val selectionStart = aztecText.selectionStart
        val selectionStartMinusOne = (selectionStart - 1).coerceIn(0, limitLength)
        val selectionStartMinusTwo = (selectionStart - 2).coerceIn(0, limitLength)
        val selectionEnd = aztecText.selectionEnd
        val selectionEndPlusOne = (selectionStart + 1).coerceIn(0, limitLength)
        val selectionEndPlusTwo = (selectionStart + 2).coerceIn(0, limitLength)
        val editableText = aztecText.editableText
        var placeAtStart = false
        val (from, to) = if (editableText[selectionStartMinusOne] == Constants.IMG_CHAR) {
            selectionStartMinusOne to selectionStart
        } else if (editableText[selectionStartMinusOne] == '\n' && editableText[selectionStartMinusTwo] == Constants.IMG_CHAR) {
            selectionStartMinusTwo to selectionStart
        } else if (editableText[selectionEndPlusOne] == Constants.IMG_CHAR) {
            placeAtStart = true
            selectionEndPlusOne to (selectionEndPlusOne + 1).coerceIn(0, limitLength)
        } else if (editableText[selectionEndPlusOne] == '\n' && editableText[selectionEndPlusTwo] == Constants.IMG_CHAR) {
            placeAtStart = true
            selectionEndPlusTwo to (selectionEndPlusTwo + 1).coerceIn(0, limitLength)
        } else {
            selectionStart to selectionEnd
        }
        return editableText.getSpans(
            from,
            to,
            AztecPlaceholderSpan::class.java
        ).map {
            TargetItem(
                it,
                placeAtStart,
                editableText.getSpanStart(it),
                editableText.getSpanEnd(it)
            )
        }.lastOrNull()
    }

    /**
     * Call this method to remove a placeholder from both the AztecText and the overlaying layer programmatically.
     * @param predicate determines whether a span should be removed
     */
    override fun removeItem(predicate: (Attributes) -> Boolean) {
        aztecText.removeMedia { predicate(it) }
    }

    /**
     * Call this method to remove a placeholder from both the AztecText and the overlaying layer programmatically.
     * @param uuid of the removed item
     */
    override fun removeItem(uuid: String) {
        aztecText.removeMedia { it.getValue(UUID_ATTRIBUTE) == uuid }
    }

    private suspend fun buildPlaceholderDrawable(
        adapter: ComposePlaceholderAdapter,
        attrs: AztecAttributes
    ): Drawable {
        val drawable = ContextCompat.getDrawable(aztecText.context, android.R.color.transparent)!!
        val editorWidth = if (aztecText.width > 0) {
            aztecText.width - aztecText.paddingStart - aztecText.paddingEnd
        } else aztecText.maxImagesWidth
        val widthPx = adapter.calculateWidth(attrs, editorWidth)
        val heightPx = computeHeightPx(adapter, attrs, editorWidth, widthPx)
        // Reserve additional flow space after the placeholder to visually separate following blocks
        val flowHeight = heightPx + (adapter.bottomSpacingPx(attrs))
        drawable.setBounds(0, 0, widthPx, flowHeight)
        return drawable
    }

    /**
     * Call this method to reload all the placeholders
     */
    suspend fun reloadAllPlaceholders() {
        val tempPositionToId = _composeViewState.value
        tempPositionToId.forEach { placeholder ->
            val isValid = positionToIdMutex.withLock {
                _composeViewState.value.containsKey(placeholder.key)
            }
            if (isValid) {
                insertContentOverSpanWithId(placeholder.value.uuid)
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
        val targetWidth = adapter.calculateWidth(attrs, windowWidth)
        val measuredHeight = computeHeightPx(adapter, attrs, windowWidth, targetWidth)
        val extraBottom = adapter.bottomSpacingPx(attrs)
        val height = measuredHeight + extraBottom
        parentTextViewRect.top += parentTextViewTopAndBottomOffset
        parentTextViewRect.bottom = parentTextViewRect.top + height

        val box = _composeViewState.value[uuid]
        val newWidth = targetWidth
        val newHeight = measuredHeight
        val overlayPad = adapter.overlayPaddingPx(attrs)
        val newLeftPadding = parentTextViewRect.left + overlayPad.left + aztecText.paddingStart
        val newTopPadding = parentTextViewRect.top + overlayPad.top
        box?.let { existingView ->
            val widthSame = existingView.width == newWidth
            val heightSame = existingView.height == newHeight
            val topMarginSame = existingView.topMargin == newTopPadding
            val leftMarginSame = existingView.leftMargin == newLeftPadding
            val attrsSame = existingView.attrs == attrs
            if (widthSame && heightSame && topMarginSame && leftMarginSame && attrsSame) {
                return
            }
        }
        _composeViewState.value = _composeViewState.value.let { state ->
            val mutableState = state.toMutableMap()
            val adjustedHeight = newHeight + (adapter.contentHeightAdjustmentPx(attrs))
            mutableState[uuid] = ComposeView(
                uuid = uuid,
                width = newWidth,
                height = adjustedHeight,
                topMargin = newTopPadding,
                leftMargin = newLeftPadding,
                visible = true,
                adapterKey = adapter.type,
                attrs = attrs
            )
            mutableState
        }
    }

    private suspend fun computeHeightPx(
        adapter: ComposePlaceholderAdapter,
        attrs: AztecAttributes,
        windowWidth: Int,
        contentWidthPx: Int
    ): Int =
        when (val policy = adapter.sizingPolicy(attrs)) {
            is ComposePlaceholderAdapter.SizingPolicy.FixedHeightPx -> policy.heightPx

            is ComposePlaceholderAdapter.SizingPolicy.AspectRatio -> (policy.ratio * contentWidthPx).toInt()

            ComposePlaceholderAdapter.SizingPolicy.MatchWidthWrapContentHeight ->
                preMeasureHeight(adapter, attrs, contentWidthPx) ?: adapter.calculateHeight(attrs, windowWidth)

            ComposePlaceholderAdapter.SizingPolicy.Unknown -> adapter.calculateHeight(attrs, windowWidth)
        }

    private suspend fun preMeasureHeight(
        adapter: ComposePlaceholderAdapter,
        attrs: AztecAttributes,
        widthPx: Int
    ): Int? {
        // Pre-measure only on main thread. If not on main, fall back to legacy path
        if (Looper.myLooper() != Looper.getMainLooper()) return null
        val measurer = object : ComposePlaceholderAdapter.PlaceholderMeasurer {
            override suspend fun measure(content: @Composable () -> Unit, widthPx: Int): Int {
                if (!aztecText.isAttachedToWindow) return -1
                val parent = aztecText.parent as? ViewGroup ?: return -1
                val composeView = ComposeView(aztecText.context)
                composeView.visibility = View.GONE
                composeView.layoutParams = ViewGroup.LayoutParams(0, 0)
                try {
                    parent.addView(composeView)
                    composeView.setContent {
                        Box(
                            Modifier
                                .width(with(LocalDensity.current) { widthPx.toDp() })
                        ) {
                            content()
                        }
                    }
                    val wSpec = MeasureSpec.makeMeasureSpec(widthPx, MeasureSpec.EXACTLY)
                    val hSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                    composeView.measure(wSpec, hSpec)
                    return composeView.measuredHeight
                } catch (_: IllegalStateException) {
                    return -1
                } finally {
                    parent.removeView(composeView)
                }
            }
        }
        // Let adapter compute/measure if it wants to
        val fromAdapter = adapter.preComposeMeasureHeight(attrs, widthPx, measurer)
        if (fromAdapter != null && fromAdapter >= 0) return fromAdapter
        // If adapter did not implement it but hinted wrap content policy, measure the actual content once
        if (adapter.sizingPolicy(attrs) == ComposePlaceholderAdapter.SizingPolicy.MatchWidthWrapContentHeight) {
            val uuid = attrs.getValue(UUID_ATTRIBUTE)
            val h = measurer.measure(content = { adapter.Placeholder(uuid, attrs) }, widthPx = widthPx)
            return if (h >= 0) h else null
        }
        return null
    }

    private fun validateAttributes(attributes: AztecAttributes): Boolean {
        return attributes.hasAttribute(UUID_ATTRIBUTE) &&
            attributes.hasAttribute(TYPE_ATTRIBUTE) &&
            adapters[attributes.getValue(TYPE_ATTRIBUTE)] != null
    }

    private fun getAttributesForMedia(
        type: String,
        attributes: Array<out Pair<String, String>>
    ): AztecAttributes {
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
            _composeViewState.value = _composeViewState.value.let { state ->
                val mutableState = state.toMutableMap()
                mutableState.remove(uuid)
                mutableState
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
            _composeViewState.value = _composeViewState.value.let { state ->
                val mutableState = state.toMutableMap()
                mutableState.remove(uuid)
                mutableState
            }
        }
    }

    override fun canHandleTag(tag: String): Boolean {
        return tag == htmlTag
    }

    /**
     * This method handled a `placeholder` tag found in the HTML. It creates a placeholder and inserts a view over it.
     */
    override fun handleTag(
        opening: Boolean,
        tag: String,
        output: Editable,
        attributes: Attributes,
        nestingLevel: Int
    ): Boolean {
        if (opening) {
            val type = attributes.getValue(TYPE_ATTRIBUTE)
            attributes.getValue(UUID_ATTRIBUTE)?.also { uuid ->
                _composeViewState.value = _composeViewState.value.let { state ->
                    val mutableState = state.toMutableMap()
                    mutableState.remove(uuid)
                    mutableState
                }
            }
            val adapter = adapters[type] ?: return false
            val aztecAttributes = AztecAttributes(attributes)
            aztecAttributes.setValue(UUID_ATTRIBUTE, generateUuid())
            val drawable = runBlocking { buildPlaceholderDrawable(adapter, aztecAttributes) }
            val span = AztecPlaceholderSpan(
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
        val spans = aztecText.editableText.getSpans(
            0,
            aztecText.editableText.length,
            AztecPlaceholderSpan::class.java
        )

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
            job = redrawViews()
        }
    }

    fun redrawViews(): Job? {
        val spans = aztecText.editableText.getSpans(
            0,
            aztecText.editableText.length,
            AztecPlaceholderSpan::class.java
        )

        if (spans == null || spans.isEmpty()) {
            return null
        }
        return launch {
            clearAllViews()
            spans.forEach {
                val type = it.attributes.getValue(TYPE_ATTRIBUTE)
                val adapter = adapters[type] ?: return@forEach
                it.drawable = buildPlaceholderDrawable(adapter, it.attributes)
                aztecText.refreshText(false)
                insertInPosition(it.attributes, aztecText.editableText.getSpanStart(it))
            }
        }
    }

    private suspend fun clearAllViews() {
        positionToIdMutex.withLock {
            _composeViewState.value = emptyMap()
        }
    }

    override fun onVisibility(visibility: Int) {
        launch {
            positionToIdMutex.withLock {
                _composeViewState.value = _composeViewState.value.let { state ->
                    state.mapValues { (_, value) ->
                        value.copy(
                            visible = View.VISIBLE == visibility
                        )
                    }
                }
            }
        }
    }

    override fun beforeHtmlProcessed(source: String): String {
        runBlocking {
            clearAllViews()
        }
        return source
    }

    fun getViewInPosition(x: Float, y: Float): ComposeView? {
        return _composeViewState.value.values.firstOrNull {
            (it.topMargin < y && (it.topMargin + it.height) > y) && (it.leftMargin < x && (it.leftMargin + it.width) > x)
        }
    }
}
