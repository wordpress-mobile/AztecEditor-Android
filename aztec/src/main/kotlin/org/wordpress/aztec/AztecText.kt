/*
 * Copyright (C) 2016 Automattic
 * Copyright (C) 2015 Matthew Lee
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wordpress.aztec

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.Parcel
import android.os.Parcelable
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.SuggestionSpan
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnLongClickListener
import android.view.WindowManager
import android.view.inputmethod.BaseInputConnection
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.wordpress.android.util.AppLog
import org.wordpress.android.util.ImageUtils
import org.wordpress.aztec.formatting.BlockFormatter
import org.wordpress.aztec.formatting.InlineFormatter
import org.wordpress.aztec.formatting.LineBlockFormatter
import org.wordpress.aztec.formatting.LinkFormatter
import org.wordpress.aztec.handlers.HeadingHandler
import org.wordpress.aztec.handlers.ListHandler
import org.wordpress.aztec.handlers.ListItemHandler
import org.wordpress.aztec.handlers.PreformatHandler
import org.wordpress.aztec.handlers.QuoteHandler
import org.wordpress.aztec.plugins.IAztecPlugin
import org.wordpress.aztec.plugins.IToolbarButton
import org.wordpress.aztec.source.Format
import org.wordpress.aztec.source.SourceViewEditText
import org.wordpress.aztec.spans.AztecAudioSpan
import org.wordpress.aztec.spans.AztecCodeSpan
import org.wordpress.aztec.spans.AztecCursorSpan
import org.wordpress.aztec.spans.AztecDynamicImageSpan
import org.wordpress.aztec.spans.AztecImageSpan
import org.wordpress.aztec.spans.AztecListItemSpan
import org.wordpress.aztec.spans.AztecMediaClickableSpan
import org.wordpress.aztec.spans.AztecMediaSpan
import org.wordpress.aztec.spans.AztecURLSpan
import org.wordpress.aztec.spans.AztecVideoSpan
import org.wordpress.aztec.spans.AztecVisualLinebreak
import org.wordpress.aztec.spans.CommentSpan
import org.wordpress.aztec.spans.EndOfParagraphMarker
import org.wordpress.aztec.spans.IAztecAttributedSpan
import org.wordpress.aztec.spans.IAztecBlockSpan
import org.wordpress.aztec.spans.UnknownClickableSpan
import org.wordpress.aztec.spans.UnknownHtmlSpan
import org.wordpress.aztec.toolbar.IAztecToolbar
import org.wordpress.aztec.toolbar.ToolbarAction
import org.wordpress.aztec.util.AztecLog
import org.wordpress.aztec.util.CleaningUtils
import org.wordpress.aztec.util.InstanceStateUtils
import org.wordpress.aztec.util.SpanWrapper
import org.wordpress.aztec.util.coerceToHtmlText
import org.wordpress.aztec.watchers.BlockElementWatcher
import org.wordpress.aztec.watchers.DeleteMediaElementWatcherAPI25AndHigher
import org.wordpress.aztec.watchers.DeleteMediaElementWatcherPreAPI25
import org.wordpress.aztec.watchers.EndOfBufferMarkerAdder
import org.wordpress.aztec.watchers.EndOfParagraphMarkerAdder
import org.wordpress.aztec.watchers.FullWidthImageElementWatcher
import org.wordpress.aztec.watchers.InlineTextWatcher
import org.wordpress.aztec.watchers.ParagraphBleedAdjuster
import org.wordpress.aztec.watchers.ParagraphCollapseAdjuster
import org.wordpress.aztec.watchers.ParagraphCollapseRemover
import org.wordpress.aztec.watchers.SuggestionWatcher
import org.wordpress.aztec.watchers.TextDeleter
import org.wordpress.aztec.watchers.ZeroIndexContentWatcher
import org.wordpress.aztec.watchers.event.IEventInjector
import org.wordpress.aztec.watchers.event.sequence.ObservationQueue
import org.wordpress.aztec.watchers.event.sequence.known.space.steps.TextWatcherEventInsertText
import org.wordpress.aztec.watchers.event.text.AfterTextChangedEventData
import org.wordpress.aztec.watchers.event.text.BeforeTextChangedEventData
import org.wordpress.aztec.watchers.event.text.OnTextChangedEventData
import org.wordpress.aztec.watchers.event.text.TextWatcherEvent
import org.xml.sax.Attributes
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.ArrayList
import java.util.Arrays
import java.util.LinkedList

@Suppress("UNUSED_PARAMETER")
open class AztecText : AppCompatEditText, TextWatcher, UnknownHtmlSpan.OnUnknownHtmlTappedListener, IEventInjector {
    companion object {
        val BLOCK_EDITOR_HTML_KEY = "RETAINED_BLOCK_HTML_KEY"
        val BLOCK_EDITOR_START_INDEX_KEY = "BLOCK_EDITOR_START_INDEX_KEY"
        val BLOCK_DIALOG_VISIBLE_KEY = "BLOCK_DIALOG_VISIBLE_KEY"

        val LINK_DIALOG_VISIBLE_KEY = "LINK_DIALOG_VISIBLE_KEY"
        val LINK_DIALOG_URL_KEY = "LINK_DIALOG_URL_KEY"
        val LINK_DIALOG_ANCHOR_KEY = "LINK_DIALOG_ANCHOR_KEY"
        val LINK_DIALOG_OPEN_NEW_WINDOW_KEY = "LINK_DIALOG_OPEN_NEW_WINDOW_KEY"

        val HISTORY_LIST_KEY = "HISTORY_LIST_KEY"
        val HISTORY_CURSOR_KEY = "HISTORY_CURSOR_KEY"

        val SELECTION_START_KEY = "SELECTION_START_KEY"
        val SELECTION_END_KEY = "SELECTION_END_KEY"

        val INPUT_LAST_KEY = "INPUT_LAST_KEY"
        val VISIBILITY_KEY = "VISIBILITY_KEY"
        val IS_MEDIA_ADDED_KEY = "IS_MEDIA_ADDED_KEY"
        val RETAINED_HTML_KEY = "RETAINED_HTML_KEY"
        val RETAINED_INITIAL_HTML_PARSED_SHA256_KEY = "RETAINED_INITIAL_HTML_PARSED_SHA256_KEY"

        val DEFAULT_IMAGE_WIDTH = 800

        val DEFAULT_ALIGNMENT_RENDERING = AlignmentRendering.SPAN_LEVEL

        var watchersNestingLevel: Int = 0

        private fun getPlaceholderDrawableFromResID(context: Context, @DrawableRes drawableId: Int, maxImageWidthForVisualEditor: Int): BitmapDrawable {
            val drawable = AppCompatResources.getDrawable(context, drawableId)
            var bitmap: Bitmap
            if (drawable is BitmapDrawable) {
                bitmap = drawable.bitmap
                bitmap = ImageUtils.getScaledBitmapAtLongestSide(bitmap, maxImageWidthForVisualEditor)
            } else if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && (drawable is VectorDrawableCompat || drawable is VectorDrawable) ) ||
                    drawable is VectorDrawableCompat) {
                bitmap = Bitmap.createBitmap(maxImageWidthForVisualEditor, maxImageWidthForVisualEditor, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
            } else {
                throw IllegalArgumentException("Unsupported Drawable Type")
            }
            bitmap.density = DisplayMetrics.DENSITY_DEFAULT
            return BitmapDrawable(context.resources, bitmap)
        }

        @Throws(NoSuchAlgorithmException::class)
        fun calculateSHA256(s: String): ByteArray {
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(s.toByteArray())
            return digest.digest()
        }

        fun calculateInitialHTMLSHA(initialHTMLParsed: String, initialEditorContentParsedSHA256: ByteArray): ByteArray {
            try {
                // Do not recalculate the hash if it's not the first call to `fromHTML`.
                if (initialEditorContentParsedSHA256.isEmpty() || Arrays.equals(initialEditorContentParsedSHA256, calculateSHA256(""))) {
                    return calculateSHA256(initialHTMLParsed)
                } else {
                    return initialEditorContentParsedSHA256
                }
            } catch (e: Throwable) {
                // Do nothing here. `toPlainHtml` can throw exceptions, also calculateSHA256 -> NoSuchAlgorithmException
            }

            return ByteArray(0)
        }

        fun hasChanges(initialEditorContentParsedSHA256: ByteArray, newContent: String): EditorHasChanges {
            try {
                if (Arrays.equals(initialEditorContentParsedSHA256, calculateSHA256(newContent))) {
                    return EditorHasChanges.NO_CHANGES
                }
                return EditorHasChanges.CHANGES
            } catch (e: Throwable) {
                // Do nothing here. `toPlainHtml` can throw exceptions, also calculateSHA256 -> NoSuchAlgorithmException
                return EditorHasChanges.UNKNOWN
            }
        }
    }

    private val REGEXP_EMAIL = Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+.[A-Z]{2,}$",
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
    private val REGEXP_STANDALONE_URL = Regex("^(?:[a-z]+:|#|\\?|\\.|/)", RegexOption.DOT_MATCHES_ALL)

    enum class EditorHasChanges {
        CHANGES, NO_CHANGES, UNKNOWN
    }

    private var historyEnable = resources.getBoolean(R.bool.history_enable)
    private var historySize = resources.getInteger(R.integer.history_size)

    private var addLinkDialog: AlertDialog? = null
    private var blockEditorDialog: AlertDialog? = null
    private var consumeEditEvent: Boolean = false
    private var consumeSelectionChangedEvent: Boolean = false
    private var isInlineTextHandlerEnabled: Boolean = true
    private var bypassObservationQueue: Boolean = false
    private var bypassMediaDeletedListener: Boolean = false
    private var bypassCrashPreventerInputFilter: Boolean = false

    var initialEditorContentParsedSHA256: ByteArray = ByteArray(0)

    private var onSelectionChangedListener: OnSelectionChangedListener? = null
    private var onImeBackListener: OnImeBackListener? = null
    private var onImageTappedListener: OnImageTappedListener? = null
    private var onVideoTappedListener: OnVideoTappedListener? = null
    private var onAudioTappedListener: OnAudioTappedListener? = null
    private var onMediaDeletedListener: OnMediaDeletedListener? = null
    private var onVideoInfoRequestedListener: OnVideoInfoRequestedListener? = null
    private var onAztecKeyListener: OnAztecKeyListener? = null
    var externalLogger: AztecLog.ExternalLogger? = null

    private var isViewInitialized = false
    private var isLeadingStyleRemoved = false

    private var isHandlingBackspaceEvent = false

    var commentsVisible = resources.getBoolean(R.bool.comments_visible)

    var isInCalypsoMode = true
    var isInGutenbergMode: Boolean = false
    val alignmentRendering: AlignmentRendering

    var consumeHistoryEvent: Boolean = false

    private var unknownBlockSpanStart = -1

    private var formatToolbar: IAztecToolbar? = null

    val selectedStyles = ArrayList<ITextFormat>()

    private var isNewStyleSelected = false

    var drawableFailed: Int = 0
    var drawableLoading: Int = 0

    var isMediaAdded = false

    lateinit var history: History

    lateinit var inlineFormatter: InlineFormatter
    lateinit var blockFormatter: BlockFormatter
    lateinit var lineBlockFormatter: LineBlockFormatter
    lateinit var linkFormatter: LinkFormatter

    var imageGetter: Html.ImageGetter? = null
    var videoThumbnailGetter: Html.VideoThumbnailGetter? = null

    var plugins: ArrayList<IAztecPlugin> = ArrayList()

    var widthMeasureSpec: Int = 0

    var verticalParagraphMargin: Int = 0
    var verticalHeadingMargin: Int = 0

    var maxImagesWidth: Int = 0
    var minImagesWidth: Int = 0

    var observationQueue: ObservationQueue = ObservationQueue(this)
    var textWatcherEventBuilder: TextWatcherEvent.Builder = TextWatcherEvent.Builder()

    private var accessibilityDelegate = AztecTextAccessibilityDelegate(this)

    private var uncaughtExceptionHandler: AztecExceptionHandler? = null

    private var focusOnVisible = true

    val contentChangeWatcher = AztecContentChangeWatcher()

    var lastPressedXCoord: Int = 0
    var lastPressedYCoord: Int = 0

    interface OnSelectionChangedListener {
        fun onSelectionChanged(selStart: Int, selEnd: Int)
    }

    interface OnImeBackListener {
        fun onImeBack()
    }

    interface OnImageTappedListener {
        fun onImageTapped(attrs: AztecAttributes, naturalWidth: Int, naturalHeight: Int)
    }

    interface OnVideoTappedListener {
        fun onVideoTapped(attrs: AztecAttributes)
    }

    interface OnAudioTappedListener {
        fun onAudioTapped(attrs: AztecAttributes)
    }

    interface OnMediaDeletedListener {
        fun onMediaDeleted(attrs: AztecAttributes)
    }

    interface OnVideoInfoRequestedListener {
        fun onVideoInfoRequested(attrs: AztecAttributes)
    }

    interface OnAztecKeyListener {
        fun onEnterKey(text: Spannable, firedAfterTextChanged: Boolean, selStart: Int, selEnd: Int) : Boolean
        fun onBackspaceKey() : Boolean
    }

    interface OnLinkTappedListener {
        fun onLinkTapped(widget: View, url: String)
    }

    constructor(context: Context) : super(context) {
        alignmentRendering = DEFAULT_ALIGNMENT_RENDERING
        init(null)
    }

    constructor(context: Context, alignmentRendering: AlignmentRendering) : super(context) {
        this.alignmentRendering = alignmentRendering
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        alignmentRendering = DEFAULT_ALIGNMENT_RENDERING
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        alignmentRendering = DEFAULT_ALIGNMENT_RENDERING
        init(attrs)
    }

    fun setCalypsoMode(isCompatibleWithCalypso: Boolean) {
        isInCalypsoMode = isCompatibleWithCalypso
    }

    fun setGutenbergMode(isCompatibleWithGutenberg: Boolean) {
        isInGutenbergMode = isCompatibleWithGutenberg
    }

    // Newer AppCompatEditText returns Editable?, and using that would require changing all of Aztec to not use `text.`
    override fun getText(): Editable {
        return super.getText()!!
    }

    @SuppressLint("ResourceType")
    private fun init(attrs: AttributeSet?) {
        disableTextChangedListener()

        val styles = context.obtainStyledAttributes(attrs, R.styleable.AztecText, 0, R.style.AztecTextStyle)
        setLineSpacing(
                styles.getDimension(
                        R.styleable.AztecText_lineSpacingExtra,
                        resources.getDimension(R.dimen.spacing_extra)
                ),
                styles.getFloat(
                        R.styleable.AztecText_lineSpacingMultiplier,
                        resources.getString(R.dimen.spacing_multiplier).toFloat()
                )
        )
        setBackgroundColor(styles.getColor(R.styleable.AztecText_backgroundColor, ContextCompat.getColor(context, R.color.background)))
        setTextColor(styles.getColor(R.styleable.AztecText_textColor, ContextCompat.getColor(context, R.color.text)))
        setHintTextColor(styles.getColor(R.styleable.AztecText_textColorHint, ContextCompat.getColor(context, R.color.text_hint)))

        drawableLoading = styles.getResourceId(R.styleable.AztecText_drawableLoading, R.drawable.ic_image_loading)
        drawableFailed = styles.getResourceId(R.styleable.AztecText_drawableFailed, R.drawable.ic_image_failed)

        historyEnable = styles.getBoolean(R.styleable.AztecText_historyEnable, historyEnable)
        historySize = styles.getInt(R.styleable.AztecText_historySize, historySize)

        commentsVisible = styles.getBoolean(R.styleable.AztecText_commentsVisible, commentsVisible)

        verticalParagraphMargin = styles.getDimensionPixelSize(R.styleable.AztecText_blockVerticalPadding,
                        resources.getDimensionPixelSize(R.dimen.block_vertical_padding))
        verticalHeadingMargin = styles.getDimensionPixelSize(R.styleable.AztecText_headingVerticalPadding,
                        resources.getDimensionPixelSize(R.dimen.heading_vertical_padding))

        inlineFormatter = InlineFormatter(this,
                InlineFormatter.CodeStyle(
                        styles.getColor(R.styleable.AztecText_codeBackground, 0),
                        styles.getFraction(R.styleable.AztecText_codeBackgroundAlpha, 1, 1, 0f),
                        styles.getColor(R.styleable.AztecText_codeColor, 0)))

        blockFormatter = BlockFormatter(this,
                BlockFormatter.ListStyle(
                        styles.getColor(R.styleable.AztecText_bulletColor, 0),
                        styles.getDimensionPixelSize(R.styleable.AztecText_bulletMargin, 0),
                        styles.getDimensionPixelSize(R.styleable.AztecText_bulletPadding, 0),
                        styles.getDimensionPixelSize(R.styleable.AztecText_bulletWidth, 0),
                        verticalParagraphMargin),
                BlockFormatter.QuoteStyle(
                        styles.getColor(R.styleable.AztecText_quoteBackground, 0),
                        styles.getColor(R.styleable.AztecText_quoteColor, 0),
                        styles.getFraction(R.styleable.AztecText_quoteBackgroundAlpha, 1, 1, 0f),
                        styles.getDimensionPixelSize(R.styleable.AztecText_quoteMargin, 0),
                        styles.getDimensionPixelSize(R.styleable.AztecText_quotePadding, 0),
                        styles.getDimensionPixelSize(R.styleable.AztecText_quoteWidth, 0),
                        verticalParagraphMargin),
                BlockFormatter.HeaderStyle(verticalHeadingMargin),
                BlockFormatter.PreformatStyle(
                        styles.getColor(R.styleable.AztecText_preformatBackground, 0),
                        getPreformatBackgroundAlpha(styles),
                        styles.getColor(R.styleable.AztecText_preformatColor, 0),
                        verticalParagraphMargin),
                alignmentRendering
        )

        linkFormatter = LinkFormatter(this, LinkFormatter.LinkStyle(styles.getColor(
                R.styleable.AztecText_linkColor, 0),
                styles.getBoolean(R.styleable.AztecText_linkUnderline, true)))

        lineBlockFormatter = LineBlockFormatter(this)

        styles.recycle()

        // set the pictures max size to the min of screen width/height and DEFAULT_IMAGE_WIDTH
        val minScreenSize = Math.min(context.resources.displayMetrics.widthPixels,
                context.resources.displayMetrics.heightPixels)
        maxImagesWidth = Math.min(minScreenSize, DEFAULT_IMAGE_WIDTH)
        minImagesWidth = lineHeight

        if (historyEnable && historySize <= 0) {
            throw IllegalArgumentException("historySize must > 0")
        }

        history = History(historyEnable, historySize)

        // triggers ClickableSpan onClick() events
        movementMethod = EnhancedMovementMethod

        setupKeyListenersAndInputFilters()

        //disable auto suggestions/correct for older devices
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            inputType = inputType or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        }

        install()

        // Needed to properly initialize the cursor position
        setSelection(0)

        // only override startDrag for Android 9 or later to workaround the following issue:
        // "IllegalStateException: Drag shadow dimensions must be positive"
        // - see https://issuetracker.google.com/issues/113347222
        // - also https://github.com/wordpress-mobile/WordPress-Android/issues/10492
        // rationale: the LongClick gesture takes precedence over the startDrag operation
        // so, listening to it first gives us the possibility to discard processing the event
        // when the crash conditions would be otherwise met. Conditions follow.
        // In the case of a zero width character being the sole selection, the shadow dimensions
        // would be zero, incurring in the actual crash. Given it doesn't really make sense to
        // select a newline and try dragging it around, we're just completely capturing the event
        // and signaling the OS that it was handled, so it doesn't propagate to the TextView's
        // longClickListener actually implementing dragging.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            setOnLongClickListener(OnLongClickListener { // if we have a selection
                val start: Int = getSelectionStart()
                val end: Int = getSelectionEnd()
                // the selection is exactly 1 character long, so let's check
                if (end - start == 1) {
                    // check if the user long-clicked on the selection to start a drag movement
                    val selectionRect: Rect = getBoxContainingSelectionCoordinates()
                    if (selectionRect.left < lastPressedXCoord
                            && selectionRect.top < lastPressedYCoord
                            && selectionRect.right > lastPressedXCoord
                            && selectionRect.bottom > lastPressedYCoord) {

                        if (selectionHasExactlyOneMarker(
                                        start,
                                        end,
                                        EndOfParagraphMarker::class.java)) {
                            // signal this event as handled so dragging does not go forward
                            return@OnLongClickListener true
                        }

                        if (selectionHasExactlyOneMarker(
                                        start,
                                        end,
                                        AztecVisualLinebreak::class.java)) {
                            // signal this event as handled so dragging does not go forward
                            return@OnLongClickListener true
                        }
                    }
                }
                false
            })
        }

        enableTextChangedListener()

        isViewInitialized = true
    }

    private fun <T>selectionHasExactlyOneMarker(start: Int, end: Int, type: Class<T>): Boolean {
        val spanFound: Array<T> = editableText.getSpans(
                start,
                end,
                type
        )
        return spanFound.size == 1
    }

    private fun getBoxContainingSelectionCoordinates(): Rect {
        // obtain the location on the screen, we'll use it later to adjust x/y
        val location = IntArray(2)
        getLocationOnScreen(location)
        val startLine = layout.getLineForOffset(selectionStart)
        val endLine = layout.getLineForOffset(selectionEnd)
        val startLineBounds = Rect()
        getLineBounds(startLine, startLineBounds)
        val containingBoxBounds: Rect
        // if both lines aren't the same, the selection expands accross multiple lines
        containingBoxBounds = if (endLine != startLine) {
            // in such case, let's simplify things and obtain the bigger box
            // (first line top/left, last line bottom/right)
            val lastLineBounds = Rect()
            getLineBounds(endLine, lastLineBounds)
            Rect(
                    startLineBounds.left + location[0] - scrollX,
                    startLineBounds.top + location[1] - scrollY,
                    lastLineBounds.right + location[0] - scrollX,
                    lastLineBounds.bottom + location[1] - scrollY
            )
        } else {
            // if the selection doesn't go through lines, then make the containing box adjusted to actual
            // selection start / end
            // now I need the X to be the actual start cursor X
            val left = (layout.getPrimaryHorizontal(selectionStart).toInt() + location[0]
                    - scrollX + startLineBounds.left)
            val right = (layout.getPrimaryHorizontal(selectionEnd).toInt() + location[0]
                    - scrollX + startLineBounds.left)
            val top = startLineBounds.top + location[1] - scrollY
            val bottom = startLineBounds.bottom + location[1] - scrollY
            Rect(left, top, right, bottom)
        }
        return containingBoxBounds
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                && event.action == MotionEvent.ACTION_DOWN) {
            // we'll use these values in OnLongClickListener
            lastPressedXCoord = event.rawX.toInt()
            lastPressedYCoord = event.rawY.toInt()
        }
        return super.onTouchEvent(event)
    }

    // Setup the keyListener(s) for Backspace and Enter key.
    // Backspace: If listener does return false we remove the style here
    // Enter: Ask the listener if we need to insert or not the char
    private fun setupKeyListenersAndInputFilters() {
        //hardware keyboard
        setOnKeyListener { _, _, event ->
            handleBackspaceAndEnter(event)
        }

        // This InputFilter created only for the purpose of avoiding crash described here:
        // https://android-review.googlesource.com/c/platform/frameworks/base/+/634929
        // https://github.com/wordpress-mobile/AztecEditor-Android/issues/729
        // the rationale behind this workaround is that the specific crash happens only when adding/deleting text right
        // before an AztecImageSpan, so we detect the specific case and re-create the contents only when that happens.
        // This is indeed tackling the symptom rather than the actual problem, and should be removed once the real
        // problem is fixed at the Android OS level as described in the following url
        // https://android-review.googlesource.com/c/platform/frameworks/base/+/634929
        val dynamicLayoutCrashPreventer = InputFilter { source, start, end, dest, dstart, dend ->
            var temp : CharSequence? = null
            if (!bypassCrashPreventerInputFilter && dend < dest.length && source != Constants.NEWLINE_STRING) {

                // if there are any images right after the destination position, hack the text
                val spans = dest.getSpans(dend, dend+1, AztecImageSpan::class.java)
                if (spans.isNotEmpty()) {

                    // prevent this filter from running recursively
                    disableCrashPreventerInputFilter()
                    // disable MediaDeleted listener before operating on content
                    disableMediaDeletedListener()

                    // create a new Spannable to perform the text change here
                    var newText = SpannableStringBuilder(dest.subSequence(0, dstart))
                            .append(source.subSequence(start, end))
                            .append(dest.subSequence(dend, dest.length))

                    // force a history update to ensure the change is recorded
                    history.beforeTextChanged(this@AztecText)

                    // use HTML from the new text to set the state of the editText directly
                    fromHtml(toFormattedHtml(newText), false)

                    contentChangeWatcher.notifyContentChanged()

                    // re-enable MediaDeleted listener
                    enableMediaDeletedListener()
                    // re-enable this very filter
                    enableCrashPreventerInputFilter()
                }
            }
            temp
        }

        val emptyEditTextBackspaceDetector = InputFilter { source, start, end, dest, dstart, dend ->
            if (selectionStart == 0 && selectionEnd == 0
                    && start == 0
                    && dstart == 0 && dend == 0
                    && isCleanStringEmpty(source)
                    && !isHandlingBackspaceEvent) {
                isHandlingBackspaceEvent = true

                // Prevent the forced backspace from being added to the history stack
                consumeHistoryEvent = true

                handleBackspaceAndEnter(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                isHandlingBackspaceEvent = false
            }
            source
        }

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {
            // dynamicLayoutCrashPreventer needs to be first in array as these are going to be chained when processed
            filters = arrayOf(dynamicLayoutCrashPreventer, emptyEditTextBackspaceDetector)
        } else {
            filters = arrayOf(emptyEditTextBackspaceDetector)
        }
    }

    private fun isCleanStringEmpty(text: CharSequence): Boolean {
        if ( isInGutenbergMode ) {
            return (text.count() == 1 && text[0] == Constants.END_OF_BUFFER_MARKER)
        } else {
            return text.count() == 0
        }
    }

    private fun handleBackspaceAndEnter(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
            // Check if the external listener has consumed the enter pressed event
            // In that case stop the execution
            if (onAztecKeyListener?.onEnterKey(text, false, 0, 0) == true) {
                return true
            }
        }

        if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_DEL) {
            // Check if the external listener has consumed the backspace pressed event
            // In that case stop the execution and do not delete styles later
            if (onAztecKeyListener?.onBackspaceKey() == true) {
                // There listener has consumed the event
                return true
            }
        }

        var wasStyleRemoved = false
        if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_DEL) {
            if (!consumeHistoryEvent) {
                history.beforeTextChanged(this@AztecText)
            }
            wasStyleRemoved = blockFormatter.tryRemoveBlockStyleFromFirstLine()

            if (selectionStart == 0 || selectionEnd == 0) {
                deleteInlineStyleFromTheBeginning()
            }

            // required to clear the toolbar style when using hardware keyboard
            if (text.isEmpty()) {
                disableTextChangedListener()
                setText("")
                enableTextChangedListener()
            }
            contentChangeWatcher.notifyContentChanged()
        }
        return wasStyleRemoved
    }

    private fun install() {
        ParagraphBleedAdjuster.install(this)
        ParagraphCollapseAdjuster.install(this)

        EndOfParagraphMarkerAdder.install(this, verticalParagraphMargin)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SuggestionWatcher.install(this)
        }

        InlineTextWatcher.install(inlineFormatter, this)

        // NB: text change handler should not alter text before "afterTextChanged" is called otherwise not all watchers
        // will have the chance to run their "beforeTextChanged" and "onTextChanged" with the same string!

        BlockElementWatcher(this)
                .add(HeadingHandler(alignmentRendering))
                .add(ListHandler())
                .add(ListItemHandler(alignmentRendering))
                .add(QuoteHandler())
                .add(PreformatHandler())
                .install(this)

        TextDeleter.install(this)

        FullWidthImageElementWatcher.install(this)

        EndOfBufferMarkerAdder.install(this)
        ZeroIndexContentWatcher.install(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            DeleteMediaElementWatcherAPI25AndHigher.install(this)
        } else {
            DeleteMediaElementWatcherPreAPI25.install(this)
        }

        // History related logging has to happen before the changes in [ParagraphCollapseRemover]
        addHistoryLoggingWatcher()
        ParagraphCollapseRemover.install(this)

        // finally add the TextChangedListener
        addTextChangedListener(this)
    }

    private fun addHistoryLoggingWatcher() {
        val historyLoggingWatcher = object : TextWatcher {
            override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
                if (!isViewInitialized) return
                if (!isTextChangedListenerDisabled() && !consumeHistoryEvent) {
                    history.beforeTextChanged(this@AztecText)
                }
            }

            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                if (!isViewInitialized) return
            }

            override fun afterTextChanged(text: Editable) {
                contentChangeWatcher.notifyContentChanged()
                if (isTextChangedListenerDisabled()) {
                    return
                }

                isMediaAdded = text.getSpans(0, text.length, AztecMediaSpan::class.java).isNotEmpty()

                if (consumeHistoryEvent) {
                    consumeHistoryEvent = false
                }
            }
        }
        addTextChangedListener(historyLoggingWatcher)
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        val selStart = selectionStart
        val selEnd = selectionEnd

        super.onWindowFocusChanged(hasWindowFocus)
        if (!hasWindowFocus) {
            // on older android versions selection is lost when window loses focus, so we are making sure to keep it
            setSelection(selStart, selEnd)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeTextChangedListener(this)
        if (addLinkDialog != null && addLinkDialog!!.isShowing) {
            addLinkDialog!!.dismiss()
        }

        if (blockEditorDialog != null && blockEditorDialog!!.isShowing) {
            blockEditorDialog!!.dismiss()
        }
    }

    // We are exposing this method in order to allow subclasses to set their own alpha value
    // for preformatted background
    open fun getPreformatBackgroundAlpha(styles: TypedArray): Float {
        return styles.getFraction(R.styleable.AztecText_preformatBackgroundAlpha, 1, 1, 0f)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        disableTextChangedListener()

        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        val customState = savedState.state
        val array = InstanceStateUtils.readAndPurgeTempInstance<ArrayList<String>>(HISTORY_LIST_KEY, ArrayList<String>(), savedState.state)
        val list = LinkedList<String>()

        list += array

        history.historyList = list
        history.historyCursor = customState.getInt(HISTORY_CURSOR_KEY)
        history.inputLast = InstanceStateUtils.readAndPurgeTempInstance<String>(INPUT_LAST_KEY, "", savedState.state)
        visibility = customState.getInt(VISIBILITY_KEY)

        initialEditorContentParsedSHA256 = customState.getByteArray(RETAINED_INITIAL_HTML_PARSED_SHA256_KEY)
        val retainedHtml = InstanceStateUtils.readAndPurgeTempInstance<String>(RETAINED_HTML_KEY, "", savedState.state)
        fromHtml(retainedHtml)

        val retainedSelectionStart = customState.getInt(SELECTION_START_KEY)
        val retainedSelectionEnd = customState.getInt(SELECTION_END_KEY)

        if (retainedSelectionEnd < editableText.length) {
            setSelection(retainedSelectionStart, retainedSelectionEnd)
        }

        val isLinkDialogVisible = customState.getBoolean(LINK_DIALOG_VISIBLE_KEY, false)
        if (isLinkDialogVisible) {
            val retainedUrl = customState.getString(LINK_DIALOG_URL_KEY, "")
            val retainedAnchor = customState.getString(LINK_DIALOG_ANCHOR_KEY, "")
            val retainedOpenInNewWindow = customState.getString(LINK_DIALOG_OPEN_NEW_WINDOW_KEY, "")
            showLinkDialog(retainedUrl, retainedAnchor, retainedOpenInNewWindow)
        }

        val isBlockEditorDialogVisible = customState.getBoolean(BLOCK_DIALOG_VISIBLE_KEY, false)
        if (isBlockEditorDialogVisible) {
            val retainedBlockHtmlIndex = customState.getInt(BLOCK_EDITOR_START_INDEX_KEY, -1)
            if (retainedBlockHtmlIndex != -1) {
                val unknownSpan = text.getSpans(retainedBlockHtmlIndex, retainedBlockHtmlIndex + 1, UnknownHtmlSpan::class.java).firstOrNull()
                if (unknownSpan != null) {
                    val retainedBlockHtml = InstanceStateUtils.readAndPurgeTempInstance<String>(BLOCK_EDITOR_HTML_KEY, "",
                            savedState.state)
                    showBlockEditorDialog(unknownSpan, retainedBlockHtml)
                }
            }
        }

        isMediaAdded = customState.getBoolean(IS_MEDIA_ADDED_KEY)

        enableTextChangedListener()
    }

    // Do not include the content of the editor when saving state to bundle.
    // EditText has it `true` by default, and then the content was saved in bundle making the app crashing
    // due to the TransactionTooLargeException Exception.
    // The content is saved in tmp files in `onSaveInstanceState`. See: https://github.com/wordpress-mobile/AztecEditor-Android/pull/641
    override fun getFreezesText(): Boolean {
        return false
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        val bundle = Bundle()
        InstanceStateUtils.writeTempInstance(context, externalLogger, HISTORY_LIST_KEY, ArrayList<String>(history.historyList), bundle)
        bundle.putInt(HISTORY_CURSOR_KEY, history.historyCursor)
        InstanceStateUtils.writeTempInstance(context, externalLogger, INPUT_LAST_KEY, history.inputLast, bundle)
        bundle.putInt(VISIBILITY_KEY, visibility)
        bundle.putByteArray(RETAINED_INITIAL_HTML_PARSED_SHA256_KEY, initialEditorContentParsedSHA256)
        InstanceStateUtils.writeTempInstance(context, externalLogger, RETAINED_HTML_KEY, toHtml(false), bundle)
        bundle.putInt(SELECTION_START_KEY, selectionStart)
        bundle.putInt(SELECTION_END_KEY, selectionEnd)

        if (addLinkDialog != null && addLinkDialog!!.isShowing) {
            bundle.putBoolean(LINK_DIALOG_VISIBLE_KEY, true)

            val urlInput = addLinkDialog!!.findViewById<EditText>(R.id.linkURL)
            val anchorInput = addLinkDialog!!.findViewById<EditText>(R.id.linkText)
            val openInNewWindowCheckbox = addLinkDialog!!.findViewById<CheckBox>(R.id.openInNewWindow)

            bundle.putString(LINK_DIALOG_URL_KEY, urlInput?.text?.toString())
            bundle.putString(LINK_DIALOG_ANCHOR_KEY, anchorInput?.text?.toString())
            bundle.putString(LINK_DIALOG_OPEN_NEW_WINDOW_KEY, if (openInNewWindowCheckbox != null && openInNewWindowCheckbox.isChecked) "checked=true" else "checked=false")
        }

        if (blockEditorDialog != null && blockEditorDialog!!.isShowing) {
            val source = blockEditorDialog!!.findViewById<SourceViewEditText>(R.id.source)

            bundle.putBoolean(BLOCK_DIALOG_VISIBLE_KEY, true)
            bundle.putInt(BLOCK_EDITOR_START_INDEX_KEY, unknownBlockSpanStart)
            InstanceStateUtils.writeTempInstance(context, externalLogger, BLOCK_EDITOR_HTML_KEY, source?.getPureHtml(false), bundle)
        }

        bundle.putBoolean(IS_MEDIA_ADDED_KEY, isMediaAdded)

        savedState.state = bundle
        return savedState
    }

    internal class SavedState : BaseSavedState {
        var state: Bundle = Bundle()

        constructor(superState: Parcelable) : super(superState)

        constructor(parcel: Parcel) : super(parcel) {
            state = parcel.readBundle(javaClass.classLoader)
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeBundle(state)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState {
                    return SavedState(source)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // capture the width spec to be used when pre-layingout AztecMediaSpans
        this.widthMeasureSpec = widthMeasureSpec

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    fun setSelectedStyles(styles: ArrayList<ITextFormat>) {
        isNewStyleSelected = true
        selectedStyles.clear()
        selectedStyles.addAll(styles)
    }

    fun setOnSelectionChangedListener(onSelectionChangedListener: OnSelectionChangedListener) {
        this.onSelectionChangedListener = onSelectionChangedListener
    }

    fun getAztecKeyListener() : OnAztecKeyListener? {
        return this.onAztecKeyListener
    }

    /**
     * Sets the Aztec key listener to be used with this AztecText.
     * Please note that this listener does hold a copy of the whole text in the editor
     * each time a key is pressed.
     */
    fun setAztecKeyListener(listenerAztec: OnAztecKeyListener) {
        this.onAztecKeyListener = listenerAztec
    }

    fun setOnImeBackListener(listener: OnImeBackListener) {
        this.onImeBackListener = listener
    }

    fun setOnImageTappedListener(listener: OnImageTappedListener) {
        this.onImageTappedListener = listener
    }

    fun setOnVideoTappedListener(listener: OnVideoTappedListener) {
        this.onVideoTappedListener = listener
    }

    fun setOnAudioTappedListener(listener: OnAudioTappedListener) {
        this.onAudioTappedListener = listener
    }

    fun setOnMediaDeletedListener(listener: OnMediaDeletedListener) {
        this.onMediaDeletedListener = listener
    }

    fun setOnVideoInfoRequestedListener(listener: OnVideoInfoRequestedListener) {
        this.onVideoInfoRequestedListener = listener
    }

    fun setOnLinkTappedListener(listener: OnLinkTappedListener) {
        EnhancedMovementMethod.linkTappedListener = listener
    }

    fun setLinkTapEnabled(isLinkTapEnabled: Boolean) {
        EnhancedMovementMethod.isLinkTapEnabled = isLinkTapEnabled
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            onImeBackListener?.onImeBack()
        }
        return super.onKeyPreIme(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, keyEvent: KeyEvent): Boolean {
        if (formatToolbar?.onKeyUp(keyCode, keyEvent) ?: false) {
            return true
        } else {
            return super.onKeyUp(keyCode, keyEvent)
        }
    }

    public override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        if (!isViewInitialized) return

        if (isOnSelectionListenerDisabled()) {
            if (isInGutenbergMode) {
                return
            }

            enableOnSelectionListener()
            return
        }

        if (length() != 0) {
            // if the text end has the marker, let's make sure the cursor never includes it or surpasses it
            if ((selStart == length() || selEnd == length()) && text[length() - 1] == Constants.END_OF_BUFFER_MARKER) {
                var start = selStart
                var end = selEnd

                if (start == length()) {
                    start--
                }

                if (end == length()) {
                    end--
                }

                setSelection(start, end)
                return
            }
        }

        // do not update toolbar or selected styles when we removed the last character in editor
        if (!isLeadingStyleRemoved && length() == 1 && text[0] == Constants.END_OF_BUFFER_MARKER) {
            return
        }

        onSelectionChangedListener?.onSelectionChanged(selStart, selEnd)
        setSelectedStyles(getAppliedStyles(selStart, selEnd))

        isLeadingStyleRemoved = false
    }

    override fun getSelectionStart(): Int {
        return Math.min(super.getSelectionStart(), super.getSelectionEnd())
    }

    override fun getSelectionEnd(): Int {
        return Math.max(super.getSelectionStart(), super.getSelectionEnd())
    }

    fun getSelectedText(): String {
        if (selectionStart == -1 || selectionEnd == -1
                || editableText.length < selectionEnd || editableText.length < selectionStart) return ""
        return editableText.substring(selectionStart, selectionEnd)
    }

    fun getAppliedStyles(selectionStart: Int, selectionEnd: Int): ArrayList<ITextFormat> {
        val styles = ArrayList<ITextFormat>()

        if (selectionStart < 0 || selectionEnd < 0) {
            // view is focused, but there is no cursor
            return styles
        }
        var newSelStart = if (selectionStart > selectionEnd) selectionEnd else selectionStart
        var newSelEnd = selectionEnd

        if (editableText.isEmpty()) {
            return styles
        }

        if (newSelStart == 0 && newSelEnd == 0) {
            newSelEnd++
        } else if (newSelStart == newSelEnd && editableText.length > selectionStart && editableText[selectionStart - 1] == Constants.NEWLINE) {
            newSelEnd++
        } else if (newSelStart > 0 && !isTextSelected()) {
            newSelStart--
        }

        AztecTextFormat.values().forEach {
            if (contains(it, newSelStart, newSelEnd)) {
                styles.add(it)
            }
        }

        plugins.filter { it is IToolbarButton }
                .flatMap { (it as IToolbarButton).action.textFormats }
                .forEach {
                    if (contains(it, newSelStart, newSelEnd)) {
                        styles.add(it)
                    }
                }

        return styles
    }

    fun isEmpty(): Boolean {
        return text.isEmpty()
    }

    fun formattingIsApplied(): Boolean {
        return !selectedStyles.isEmpty()
    }

    fun formattingHasChanged(): Boolean {
        return isNewStyleSelected
    }

    fun setFormattingChangesApplied() {
        isNewStyleSelected = false
    }

    fun isTextSelected(): Boolean {
        return selectionStart != selectionEnd
    }

    fun toggleFormatting(textFormat: ITextFormat) {
        history.beforeTextChanged(this@AztecText)

        when (textFormat) {
            AztecTextFormat.FORMAT_PARAGRAPH,
            AztecTextFormat.FORMAT_HEADING_1,
            AztecTextFormat.FORMAT_HEADING_2,
            AztecTextFormat.FORMAT_HEADING_3,
            AztecTextFormat.FORMAT_HEADING_4,
            AztecTextFormat.FORMAT_HEADING_5,
            AztecTextFormat.FORMAT_HEADING_6,
            AztecTextFormat.FORMAT_PREFORMAT -> blockFormatter.toggleHeading(textFormat)
            AztecTextFormat.FORMAT_ITALIC,
            AztecTextFormat.FORMAT_EMPHASIS,
            AztecTextFormat.FORMAT_CITE,
            AztecTextFormat.FORMAT_UNDERLINE,
            AztecTextFormat.FORMAT_STRIKETHROUGH,
            AztecTextFormat.FORMAT_CODE -> inlineFormatter.toggle(textFormat)
            AztecTextFormat.FORMAT_BOLD,
            AztecTextFormat.FORMAT_STRONG -> inlineFormatter.toggleAny(ToolbarAction.BOLD.textFormats)
            AztecTextFormat.FORMAT_UNORDERED_LIST -> blockFormatter.toggleUnorderedList()
            AztecTextFormat.FORMAT_ORDERED_LIST -> blockFormatter.toggleOrderedList()
            AztecTextFormat.FORMAT_ALIGN_LEFT,
            AztecTextFormat.FORMAT_ALIGN_CENTER,
            AztecTextFormat.FORMAT_ALIGN_RIGHT -> return blockFormatter.toggleTextAlignment(textFormat)
            AztecTextFormat.FORMAT_QUOTE -> blockFormatter.toggleQuote()
            AztecTextFormat.FORMAT_HORIZONTAL_RULE -> lineBlockFormatter.applyHorizontalRule()
            else -> {
                plugins.filter { it is IToolbarButton && it.action.textFormats.contains(textFormat) }
                        .map { it as IToolbarButton }
                        .forEach { it.toggle() }
            }
        }

        contentChangeWatcher.notifyContentChanged()
    }

    fun contains(format: ITextFormat, selStart: Int = selectionStart, selEnd: Int = selectionEnd): Boolean {
        when (format) {
            AztecTextFormat.FORMAT_HEADING_1,
            AztecTextFormat.FORMAT_HEADING_2,
            AztecTextFormat.FORMAT_HEADING_3,
            AztecTextFormat.FORMAT_HEADING_4,
            AztecTextFormat.FORMAT_HEADING_5,
            AztecTextFormat.FORMAT_HEADING_6 -> return lineBlockFormatter.containsHeading(format, selStart, selEnd)
            AztecTextFormat.FORMAT_BOLD,
            AztecTextFormat.FORMAT_STRONG,
            AztecTextFormat.FORMAT_ITALIC,
            AztecTextFormat.FORMAT_EMPHASIS,
            AztecTextFormat.FORMAT_CITE,
            AztecTextFormat.FORMAT_UNDERLINE,
            AztecTextFormat.FORMAT_STRIKETHROUGH,
            AztecTextFormat.FORMAT_MARK,
            AztecTextFormat.FORMAT_CODE -> return inlineFormatter.containsInlineStyle(format, selStart, selEnd)
            AztecTextFormat.FORMAT_UNORDERED_LIST,
            AztecTextFormat.FORMAT_ORDERED_LIST -> return blockFormatter.containsList(format, selStart, selEnd)
            AztecTextFormat.FORMAT_ALIGN_LEFT,
            AztecTextFormat.FORMAT_ALIGN_CENTER,
            AztecTextFormat.FORMAT_ALIGN_RIGHT -> return blockFormatter.containsAlignment(format, selStart, selEnd)
            AztecTextFormat.FORMAT_QUOTE -> return blockFormatter.containsQuote(selectionStart, selectionEnd)
            AztecTextFormat.FORMAT_PREFORMAT -> return blockFormatter.containsPreformat(selectionStart, selectionEnd)
            AztecTextFormat.FORMAT_LINK -> return linkFormatter.containLink(selStart, selEnd)
            else -> return false
        }
    }

    fun setToolbar(toolbar: IAztecToolbar) {
        formatToolbar = toolbar
    }

    fun getToolbar(): IAztecToolbar? {
        return formatToolbar
    }

    private fun addWatcherNestingLevel(): Int {
        watchersNestingLevel++
        return watchersNestingLevel
    }

    private fun subWatcherNestingLevel(): Int {
        watchersNestingLevel--
        return watchersNestingLevel
    }

    private fun isEventObservableCandidate(): Boolean {
        return (observationQueue.hasActiveBuckets() && !bypassObservationQueue && (watchersNestingLevel == 1))
    }

    fun isObservationQueueBeingPopulated(): Boolean {
        // TODO: use the value that is going to be published from ObservationQueue.MAXIMUM_TIME_BETWEEN_EVENTS_IN_PATTERN_MS
        val MAXIMUM_TIME_BETWEEN_EVENTS_IN_PATTERN_MS = 100
        return !observationQueue.isEmpty() &&
                ((System.currentTimeMillis() - observationQueue.last().timestamp) < MAXIMUM_TIME_BETWEEN_EVENTS_IN_PATTERN_MS)
    }

    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
        addWatcherNestingLevel()
        if (!isViewInitialized) return

        if (isEventObservableCandidate()) {
            // we need to make a copy to preserve the contents as they were before the change
            val textCopy = SpannableStringBuilder(text)
            val data = BeforeTextChangedEventData(textCopy, start, count, after)
            textWatcherEventBuilder.beforeEventData = data
        }
    }

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
        if (!isViewInitialized) return

        if (isEventObservableCandidate()) {
            val textCopy = SpannableStringBuilder(text)
            val data = OnTextChangedEventData(textCopy, start, before, count)
            textWatcherEventBuilder.onEventData = data
        }
    }

    open fun shouldSkipTidying(): Boolean {
        return false
    }

    open fun shouldIgnoreWhitespace(): Boolean {
        return true
    }

    override fun afterTextChanged(text: Editable) {
        if (isTextChangedListenerDisabled()) {
            subWatcherNestingLevel()
            return
        }

        if (isEventObservableCandidate()) {
            val textCopy = Editable.Factory.getInstance().newEditable(editableText)
            val data = AfterTextChangedEventData(textCopy)
            textWatcherEventBuilder.afterEventData = data

            // now that we have a full event cycle (before, on, and after) we can add the event to the observation queue
            observationQueue.add(textWatcherEventBuilder.build())
        }
        subWatcherNestingLevel()
    }

    fun redo() {
        history.redo(this)
        contentChangeWatcher.notifyContentChanged()
    }

    fun undo() {
        history.undo(this)
        contentChangeWatcher.notifyContentChanged()
    }

    // Helper ======================================================================================

    fun consumeCursorPosition(text: SpannableStringBuilder): Int {
        var cursorPosition = Math.min(selectionStart, text.length)

        text.getSpans(0, text.length, AztecCursorSpan::class.java).forEach {
            cursorPosition = text.getSpanStart(it)
            text.removeSpan(it)
        }

        // Make sure the cursor position is a valid one
        cursorPosition = Math.min(cursorPosition, text.length)
        cursorPosition = Math.max(0, cursorPosition)

        return cursorPosition
    }

    open fun fromHtml(source: String, isInit: Boolean = true) {
        val builder = SpannableStringBuilder()
        val parser = AztecParser(alignmentRendering, plugins)

        var cleanSource = CleaningUtils.cleanNestedBoldTags(source)
        cleanSource = Format.removeSourceEditorFormatting(cleanSource, isInCalypsoMode, isInGutenbergMode)
        builder.append(parser.fromHtml(cleanSource, context, shouldSkipTidying(), shouldIgnoreWhitespace()))

        Format.preProcessSpannedText(builder, isInCalypsoMode)

        switchToAztecStyle(builder, 0, builder.length)
        disableTextChangedListener()

        builder.getSpans(0, builder.length, AztecDynamicImageSpan::class.java).forEach {
            it.textView = this
        }

        val cursorPosition = consumeCursorPosition(builder)
        setSelection(0)

        setTextKeepState(builder)
        enableTextChangedListener()

        setSelection(cursorPosition)

        if (isInit) {
            initialEditorContentParsedSHA256 = calculateInitialHTMLSHA(toPlainHtml(false), initialEditorContentParsedSHA256)
        }

        loadImages()
        loadVideos()
    }

    private fun loadImages() {
        val spans = this.text.getSpans(0, text.length, AztecImageSpan::class.java)
        val loadingDrawable = AztecText.getPlaceholderDrawableFromResID(context, drawableLoading, maxImagesWidth)

        // Make sure to keep a reference to the maxWidth, otherwise in the Callbacks there is
        // the wrong value when used in 3rd party app
        val maxDimension = maxImagesWidth
        spans.forEach {
            val callbacks = object : Html.ImageGetter.Callbacks {
                override fun onImageFailed() {
                    replaceImage(
                            AztecText.getPlaceholderDrawableFromResID(context, drawableFailed, maxImagesWidth)
                    )
                }

                override fun onImageLoaded(drawable: Drawable?) {
                    replaceImage(drawable)
                }

                override fun onImageLoading(drawable: Drawable?) {
                    replaceImage(drawable ?: loadingDrawable)
                }

                private fun replaceImage(drawable: Drawable?) {
                    it.drawable = drawable
                    post {
                        refreshText(false)
                    }
                }
            }
            imageGetter?.loadImage(it.getSource(), callbacks, maxDimension, minImagesWidth)
        }
    }

    private fun loadVideos() {
        val spans = this.text.getSpans(0, text.length, AztecVideoSpan::class.java)
        val loadingDrawable = AztecText.getPlaceholderDrawableFromResID(context, drawableLoading, maxImagesWidth)
        val videoListenerRef = this.onVideoInfoRequestedListener

        // Make sure to keep a reference to the maxWidth, otherwise in the Callbacks there is
        // the wrong value when used in 3rd party app
        val maxDimension = maxImagesWidth
        spans.forEach {
            val callbacks = object : Html.VideoThumbnailGetter.Callbacks {
                override fun onThumbnailFailed() {
                    AztecText.getPlaceholderDrawableFromResID(context, drawableFailed, maxDimension)
                }

                override fun onThumbnailLoaded(drawable: Drawable?) {
                    replaceImage(drawable)
                }

                override fun onThumbnailLoading(drawable: Drawable?) {
                    replaceImage(drawable ?: loadingDrawable)
                }

                private fun replaceImage(drawable: Drawable?) {
                    it.drawable = drawable
                    post {
                        refreshText(false)
                    }
                }
            }
            videoThumbnailGetter?.loadVideoThumbnail(it.getSource(), callbacks, maxImagesWidth, minImagesWidth)

            // Call the Video listener and ask for more info about the current video
            videoListenerRef?.onVideoInfoRequested(it.attributes)
        }
    }

    open fun hasChanges(): EditorHasChanges {
        return hasChanges(initialEditorContentParsedSHA256, toPlainHtml(false))
    }

    // returns regular or "calypso" html depending on the mode
    // default behavior returns HTML from this text
    fun toHtml(withCursorTag: Boolean = false): String {
        return toHtml(text, withCursorTag)
    }

    // general function accepts any Spannable and converts it to regular or "calypso" html
    // depending on the mode
    fun toHtml(content: Spannable, withCursorTag: Boolean = false): String {
        val html = toPlainHtml(content, withCursorTag)

        if (isInCalypsoMode) {
            // calypso format is a mix of newline characters and html
            // paragraphs and line breaks are added on server, from newline characters
            return Format.addSourceEditorFormatting(html, true)
        } else {
            return html
        }
    }

    // platform agnostic HTML
    // default behavior returns HTML from this text
    fun toPlainHtml(withCursorTag: Boolean = false): String {
        return toPlainHtml(text, withCursorTag)
    }

    // general function accepts any Spannable and converts it to platform agnostic HTML
    fun toPlainHtml(content: Spannable, withCursorTag: Boolean = false): String {
        return if (Looper.myLooper() != Looper.getMainLooper()) {
            runBlocking {
                withContext(Dispatchers.Main) {
                    parseHtml(content, withCursorTag)
                }
            }
        } else {
            parseHtml(content, withCursorTag)
        }
    }

    private fun parseHtml(content: Spannable, withCursorTag: Boolean): String {
        val parser = AztecParser(alignmentRendering, plugins)
        val output: SpannableStringBuilder
        try {
            output = SpannableStringBuilder(content)
        } catch (e: Exception) {
            // FIXME: Remove this log once we've data to replicate the issue, and fix it in some way.
            AppLog.e(AppLog.T.EDITOR, "There was an error creating SpannableStringBuilder. See #452 and #582 for details.")
            // No need to log details here. The default AztecExceptionHandler does this for us.
            throw e
        }

        clearMetaSpans(output)

        for (span in output.getSpans(0, output.length, AztecCursorSpan::class.java)) {
            output.removeSpan(span)
        }
        if (withCursorTag && !isInCalypsoMode) {
            output.setSpan(AztecCursorSpan(), selectionEnd, selectionEnd, Spanned.SPAN_MARK_MARK)
        }

        parser.syncVisualNewlinesOfBlockElements(output)

        Format.postProcessSpannedText(output, isInCalypsoMode)

        return EndOfBufferMarkerAdder.removeEndOfTextMarker(parser.toHtml(output, withCursorTag, shouldSkipTidying()))
    }

    // default behavior returns formatted HTML from this text
    fun toFormattedHtml(): String {
        return toFormattedHtml(text)
    }

    // general function accepts any Spannable and converts it to formatted HTML
    fun toFormattedHtml(content: Spannable): String {
        return Format.addSourceEditorFormatting(toHtml(content), isInCalypsoMode)
    }

    private fun switchToAztecStyle(editable: Editable, start: Int, end: Int) {
        editable.getSpans(start, end, IAztecBlockSpan::class.java).forEach { blockFormatter.setBlockStyle(it) }
        editable.getSpans(start, end, EndOfParagraphMarker::class.java).forEach { it.verticalPadding = verticalParagraphMargin }
        editable.getSpans(start, end, AztecURLSpan::class.java).forEach { it.linkStyle = linkFormatter.linkStyle }
        editable.getSpans(start, end, AztecCodeSpan::class.java).forEach { it.codeStyle = inlineFormatter.codeStyle }

        val imageSpans = editable.getSpans(start, end, AztecImageSpan::class.java)
        imageSpans.forEach {
            it.onImageTappedListener = onImageTappedListener
            it.onMediaDeletedListener = onMediaDeletedListener
        }

        val videoSpans = editable.getSpans(start, end, AztecVideoSpan::class.java)
        videoSpans.forEach {
            it.onVideoTappedListener = onVideoTappedListener
            it.onMediaDeletedListener = onMediaDeletedListener
        }

        val audioSpans = editable.getSpans(start, end, AztecAudioSpan::class.java)
        audioSpans.forEach {
            it.onAudioTappedListener = onAudioTappedListener
            it.onMediaDeletedListener = onMediaDeletedListener
        }

        val unknownHtmlSpans = editable.getSpans(start, end, UnknownHtmlSpan::class.java)
        unknownHtmlSpans.forEach {
            it.onUnknownHtmlTappedListener = this
        }

        if (!commentsVisible) {
            val commentSpans = editable.getSpans(start, end, CommentSpan::class.java)
            commentSpans.forEach {
                val wrapper = SpanWrapper(editable, it)
                wrapper.span.isHidden = true
                editable.replace(wrapper.start, wrapper.end, Constants.MAGIC_STRING)
            }
        }
    }

    fun disableTextChangedListener() {
        consumeEditEvent = true
    }

    fun enableTextChangedListener() {
        consumeEditEvent = false
    }

    fun disableObservationQueue() {
        bypassObservationQueue = true
    }

    fun enableObservationQueue() {
        bypassObservationQueue = false
    }

    fun disableCrashPreventerInputFilter() {
        bypassCrashPreventerInputFilter = true
    }

    fun enableCrashPreventerInputFilter() {
        bypassCrashPreventerInputFilter = false
    }

    fun disableMediaDeletedListener() {
        bypassMediaDeletedListener = true
    }

    fun enableMediaDeletedListener() {
        bypassMediaDeletedListener = false
    }

    fun isMediaDeletedListenerDisabled(): Boolean {
        return bypassMediaDeletedListener
    }

    fun isTextChangedListenerDisabled(): Boolean {
        return consumeEditEvent
    }

    fun disableOnSelectionListener() {
        consumeSelectionChangedEvent = true
    }

    fun enableOnSelectionListener() {
        consumeSelectionChangedEvent = false
    }

    fun disableInlineTextHandling() {
        isInlineTextHandlerEnabled = false
    }

    fun enableInlineTextHandling() {
        isInlineTextHandlerEnabled = true
    }

    fun isInlineTextHandlerEnabled(): Boolean {
        return isInlineTextHandlerEnabled
    }

    fun isOnSelectionListenerDisabled(): Boolean {
        return consumeSelectionChangedEvent
    }

    fun setFocusOnVisible(focus: Boolean) {
        focusOnVisible = focus
    }

    open fun refreshText() {
        refreshText(true)
    }

    open fun refreshText(stealEditorFocus: Boolean) {
        disableTextChangedListener()
        val selStart = selectionStart
        val selEnd = selectionEnd
        if (stealEditorFocus) {
            setFocusOnParentView()
        }
        text = editableText
        setSelection(selStart, selEnd)
        enableTextChangedListener()
    }

    fun setFocusOnParentView() {
        if (parent is View) {
            val parentView = parent as View
            parentView.isFocusable = true
            parentView.isFocusableInTouchMode = true
            parentView.requestFocus()
        }
    }

    fun removeInlineStylesFromRange(start: Int, end: Int) {
        inlineFormatter.removeInlineStyle(AztecTextFormat.FORMAT_BOLD, start, end)
        inlineFormatter.removeInlineStyle(AztecTextFormat.FORMAT_STRONG, start, end)
        inlineFormatter.removeInlineStyle(AztecTextFormat.FORMAT_ITALIC, start, end)
        inlineFormatter.removeInlineStyle(AztecTextFormat.FORMAT_EMPHASIS, start, end)
        inlineFormatter.removeInlineStyle(AztecTextFormat.FORMAT_CITE, start, end)
        inlineFormatter.removeInlineStyle(AztecTextFormat.FORMAT_STRIKETHROUGH, start, end)
        inlineFormatter.removeInlineStyle(AztecTextFormat.FORMAT_UNDERLINE, start, end)
        inlineFormatter.removeInlineStyle(AztecTextFormat.FORMAT_CODE, start, end)
        inlineFormatter.removeInlineStyle(AztecTextFormat.FORMAT_MARK, start, end)
    }

    fun removeBlockStylesFromRange(start: Int, end: Int, ignoreLineBounds: Boolean = false) {
        blockFormatter.removeBlockStyle(AztecTextFormat.FORMAT_PARAGRAPH, start, end, Arrays.asList(IAztecBlockSpan::class.java), ignoreLineBounds)
    }

    // logic party copied from TextView
    override fun onTextContextMenuItem(id: Int): Boolean {
        var min = 0
        var max = text.length

        if (isFocused) {
            min = Math.max(0, Math.min(selectionStart, selectionEnd))
            max = Math.max(0, Math.max(selectionStart, selectionEnd))
        }

        var clipboardIdentifier = resources.getIdentifier("android:id/clipboard", "id", context.packageName)

        when (id) {
            android.R.id.paste -> paste(text, min, max)
            android.R.id.pasteAsPlainText -> paste(text, min, max, true)
            android.R.id.copy -> {
                copy(text, min, max)
                setSelection(max) // dismiss the selection to make the action menu hide
            }
            android.R.id.cut -> {
                copy(text, min, max)
                text.delete(min, max) // this will hide text action menu

                // if we are cutting text from the beginning of editor, remove leading inline style
                if (min == 0) {
                    deleteInlineStyleFromTheBeginning()
                }
            }
            // Fix for crash when pasting text on Samsung Devices running Android 7 & 8.
            // Android 7 Ref: https://github.com/wordpress-mobile/WordPress-Android/issues/10872
            // Android 8 Ref: https://github.com/wordpress-mobile/WordPress-Android/issues/8827
            clipboardIdentifier -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && Build.VERSION.SDK_INT < Build.VERSION_CODES.P
                        && Build.MANUFACTURER.toLowerCase().equals("samsung")) {
                    // Nope return true
                    Toast.makeText(context, context.getString(R.string.samsung_disabled_custom_clipboard, Build.VERSION.RELEASE), Toast.LENGTH_LONG).show()
                } else {
                    return super.onTextContextMenuItem(id)
                }
            } else -> return super.onTextContextMenuItem(id)
        }

        return true
    }

    // Convert selected text to html and add it to clipboard
    fun copy(editable: Editable, start: Int, end: Int) {
        val selectedText = editable.subSequence(start, end)
        val parser = AztecParser(alignmentRendering, plugins)
        val output = SpannableStringBuilder(selectedText)

        clearMetaSpans(output)
        parser.syncVisualNewlinesOfBlockElements(output)
        Format.postProcessSpannedText(output, isInCalypsoMode)

        // do not copy unnecessary block hierarchy, just the minimum required
        var deleteNext = false
        output.getSpans(0, output.length, IAztecBlockSpan::class.java)
                .sortedBy { it.nestingLevel }
                .reversed()
                .forEach {
                    if (deleteNext) {
                        output.removeSpan(it)
                    } else {
                        deleteNext = output.getSpanStart(it) == 0 && output.getSpanEnd(it) == output.length
                        if (deleteNext && it is AztecListItemSpan) {
                            deleteNext = false
                        }
                    }
                }

        val html = Format.removeSourceEditorFormatting(parser.toHtml(output), isInCalypsoMode, isInGutenbergMode)

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboard.primaryClip = ClipData.newHtmlText("aztec", output.toString(), html)
    }

    // copied from TextView with some changes
    fun paste(editable: Editable, min: Int, max: Int, asPlainText: Boolean = false) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip

        if (clip != null) {
            history.beforeTextChanged(this@AztecText)

            disableTextChangedListener()

            val length = text.length
            if (min == 0 &&
                    (max == length || (length == 1 && text.toString() == Constants.END_OF_BUFFER_MARKER_STRING))) {
                setText(Constants.REPLACEMENT_MARKER_STRING)
            } else {
                // prevent changes here from triggering the crash preventer
                disableCrashPreventerInputFilter()
                editable.delete(min, max)
                editable.insert(min, Constants.REPLACEMENT_MARKER_STRING)
                enableCrashPreventerInputFilter()
            }

            // don't let the pasted text be included in any existing style
            editable.getSpans(min, min + 1, Object::class.java)
                    .filter { editable.getSpanStart(it) != editable.getSpanEnd(it) && it !is IAztecBlockSpan }
                    .forEach {
                        if (editable.getSpanStart(it) == min) {
                            editable.setSpan(it, min + 1, editable.getSpanEnd(it), editable.getSpanFlags(it))
                        } else if (editable.getSpanEnd(it) == min + 1) {
                            editable.setSpan(it, editable.getSpanStart(it), min, editable.getSpanFlags(it))
                        }
                    }

            enableTextChangedListener()

            if (clip.itemCount > 0) {
                val textToPaste = if (asPlainText) clip.getItemAt(0).coerceToText(context).toString()
                else clip.getItemAt(0).coerceToHtmlText(AztecParser(alignmentRendering, plugins))

                val oldHtml = toPlainHtml().replace("<aztec_cursor>", "")
                val newHtml = oldHtml.replace(Constants.REPLACEMENT_MARKER_STRING, textToPaste + "<" + AztecCursorSpan.AZTEC_CURSOR_TAG + ">")

                fromHtml(newHtml, false)
                inlineFormatter.joinStyleSpans(0, length())
            }
            contentChangeWatcher.notifyContentChanged()
        }
    }

    fun clearMetaSpans(text: Spannable) {
        BaseInputConnection.removeComposingSpans(text)
        text.getSpans(0, text.length, SuggestionSpan::class.java).forEach { text.removeSpan(it) }
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)

        if (visibility == View.VISIBLE && focusOnVisible) {
            requestFocus()
        }
    }

    fun link(url: String, anchor: String, openInNewWindow: Boolean = false) {
        history.beforeTextChanged(this@AztecText)
        if (TextUtils.isEmpty(url) && linkFormatter.isUrlSelected()) {
            removeLink()
        } else if (linkFormatter.isUrlSelected()) {
            linkFormatter.editLink(url, anchor, openInNewWindow, linkFormatter.getUrlSpanBounds().first, linkFormatter.getUrlSpanBounds().second)
        } else {
            linkFormatter.addLink(url, anchor, openInNewWindow, selectionStart, selectionEnd)
        }
        contentChangeWatcher.notifyContentChanged()
    }

    fun removeLink() {
        val urlSpanBounds = linkFormatter.getUrlSpanBounds()

        linkFormatter.removeLink(urlSpanBounds.first, urlSpanBounds.second)
        onSelectionChanged(urlSpanBounds.first, urlSpanBounds.second)
    }

    private fun correctUrl(inputUrl: String): String {
        val url = inputUrl.trim()
        if (REGEXP_EMAIL.matches(url)) {
            return "mailto:$url"
        }
        if (!REGEXP_STANDALONE_URL.containsMatchIn(url)) {
            return "http://$url"
        }
        return url
    }

    @SuppressLint("InflateParams")
    fun showLinkDialog(presetUrl: String = "", presetAnchor: String = "", presetOpenInNewWindow: String = "" ) {
        val urlAndAnchor = linkFormatter.getSelectedUrlWithAnchor()

        val url = if (TextUtils.isEmpty(presetUrl)) urlAndAnchor.first else presetUrl
        val anchor = if (TextUtils.isEmpty(presetAnchor)) urlAndAnchor.second else presetAnchor
        val openInNewWindow = if (TextUtils.isEmpty(presetOpenInNewWindow)) urlAndAnchor.third else presetOpenInNewWindow == "checked=true"

        val builder = AlertDialog.Builder(context)

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_link, null)

        val urlInput = dialogView.findViewById<EditText>(R.id.linkURL)
        val anchorInput = dialogView.findViewById<EditText>(R.id.linkText)
        val openInNewWindowCheckbox = dialogView.findViewById<CheckBox>(R.id.openInNewWindow)

        urlInput.setText(url)
        anchorInput.setText(anchor)
        openInNewWindowCheckbox.isChecked = openInNewWindow

        builder.setView(dialogView)
        builder.setTitle(R.string.link_dialog_title)

        builder.setPositiveButton(R.string.link_dialog_button_ok, { _, _ ->
            val linkText = TextUtils.htmlEncode(correctUrl(urlInput.text.toString().trim { it <= ' ' }))
            val anchorText = anchorInput.text.toString().trim { it <= ' ' }

            link(linkText, anchorText, openInNewWindowCheckbox.isChecked)
        })

        if (linkFormatter.isUrlSelected()) {
            builder.setNeutralButton(R.string.link_dialog_button_remove_link, { _, _ ->
                removeLink()
            })
        }

        builder.setNegativeButton(R.string.link_dialog_button_cancel, { dialogInterface, _ ->
            dialogInterface.dismiss()
        })

        addLinkDialog = builder.create()
        addLinkDialog!!.show()
    }

    @SuppressLint("InflateParams")
    fun showBlockEditorDialog(unknownHtmlSpan: UnknownHtmlSpan, html: String = "") {
        val builder = AlertDialog.Builder(context)

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_block_editor, null)
        val source = dialogView.findViewById<SourceViewEditText>(R.id.source)

        var editHtml = html
        if (TextUtils.isEmpty(editHtml)) {
            editHtml = unknownHtmlSpan.rawHtml.toString()
        }

        source.displayStyledAndFormattedHtml(editHtml)
        builder.setView(dialogView)

        builder.setPositiveButton(R.string.block_editor_dialog_button_save, { _, _ ->
            val spanStart = text.getSpanStart(unknownHtmlSpan)

            val textBuilder = SpannableStringBuilder()
            textBuilder.append(AztecParser(alignmentRendering, plugins).fromHtml(source.getPureHtml(), context).trim())
            setSelection(spanStart)

            disableTextChangedListener()

            text.removeSpan(unknownHtmlSpan)
            val unknownClickSpan = text.getSpans(spanStart, spanStart + 1, UnknownClickableSpan::class.java).firstOrNull()
            if (unknownClickSpan != null) {
                text.removeSpan(unknownClickSpan)
            }

            text.replace(spanStart, spanStart + 1, textBuilder)

            val newUnknownSpan = textBuilder.getSpans(0, textBuilder.length, UnknownHtmlSpan::class.java).firstOrNull()
            if (newUnknownSpan != null) {
                newUnknownSpan.onUnknownHtmlTappedListener = this
            }

            enableTextChangedListener()

            inlineFormatter.joinStyleSpans(0, text.length)
        })

        builder.setNegativeButton(R.string.block_editor_dialog_button_cancel, { dialogInterface, _ ->
            dialogInterface.dismiss()
        })

        unknownBlockSpanStart = text.getSpanStart(unknownHtmlSpan)
        blockEditorDialog = builder.create()
        blockEditorDialog!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        blockEditorDialog!!.show()
    }

    private fun deleteInlineStyleFromTheBeginning() {
        inlineFormatter.tryRemoveLeadingInlineStyle()
        isLeadingStyleRemoved = true

        // Remove the end-of-buffer character if the text is empty (so hint can become visible)
        if (text.toString() == Constants.END_OF_BUFFER_MARKER.toString()) {
            disableTextChangedListener()
            text.delete(0, 1)
            enableTextChangedListener()
        }
        onSelectionChanged(0, 0)
    }

    fun insertImage(drawable: Drawable?, attributes: Attributes) {
        lineBlockFormatter.insertImage(drawable, attributes, onImageTappedListener, onMediaDeletedListener)
    }

    fun insertVideo(drawable: Drawable?, attributes: Attributes) {
        lineBlockFormatter.insertVideo(drawable, attributes, onVideoTappedListener, onMediaDeletedListener)
    }

    fun removeMedia(attributePredicate: AttributePredicate) {
        text.getSpans(0, text.length, AztecMediaSpan::class.java)
                .filter {
                    attributePredicate.matches(it.attributes)
                }
                .forEach {
                    val start = text.getSpanStart(it)
                    val end = text.getSpanEnd(it)

                    val clickableSpan = text.getSpans(start, end, AztecMediaClickableSpan::class.java).firstOrNull()

                    text.removeSpan(clickableSpan)
                    text.removeSpan(it)

                    text.delete(start, end)
                }
    }

    interface AttributePredicate {
        /**
         * Return true if the attributes list fulfills some condition
         */
        fun matches(attrs: Attributes): Boolean
    }

    fun updateElementAttributes(attributePredicate: AttributePredicate, attrs: AztecAttributes) {
        text.getSpans(0, text.length, IAztecAttributedSpan::class.java).firstOrNull {
            attributePredicate.matches(it.attributes)
        }?.attributes = attrs
    }

    fun resetAttributedMediaSpan(attributePredicate: AttributePredicate) {
        text.getSpans(0, text.length, AztecMediaSpan::class.java)
                .filter {
                    attributePredicate.matches(it.attributes) && text.getSpanStart(it) != -1 && text.getSpanEnd(it) != -1
                }
                .forEach {
                    editableText.setSpan(it, text.getSpanStart(it), text.getSpanEnd(it), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
    }

    fun setOverlayLevel(attributePredicate: AttributePredicate, index: Int, level: Int) {
        text.getSpans(0, text.length, AztecMediaSpan::class.java)
                .filter {
                    attributePredicate.matches(it.attributes)
                }
                .forEach {
                    it.setOverlayLevel(index, level)
                }
    }

    fun setOverlay(attributePredicate: AttributePredicate, index: Int, overlay: Drawable?, gravity: Int) {
        text.getSpans(0, text.length, AztecMediaSpan::class.java)
                .filter {
                    attributePredicate.matches(it.attributes)
                }
                .forEach {
                    // set the new overlay drawable
                    it.setOverlay(index, overlay, gravity)

                    invalidate()
                }
    }

    fun clearOverlays(attributePredicate: AttributePredicate) {
        text.getSpans(0, text.length, AztecMediaSpan::class.java)
                .filter {
                    attributePredicate.matches(it.attributes)
                }
                .forEach {
                    it.clearOverlays()

                    invalidate()
                    post {
                        // Refresh last history item so undo/redo works properly
                        // for media.
                        history.refreshLastHistoryItem(this@AztecText)
                    }
                }
    }

    fun getElementAttributes(attributePredicate: AttributePredicate): AztecAttributes {
        return getAllElementAttributes(attributePredicate).firstOrNull() ?: AztecAttributes()
    }

    fun getAllElementAttributes(attributePredicate: AttributePredicate): List<AztecAttributes> {
        return text
                .getSpans(0, text.length, IAztecAttributedSpan::class.java)
                .filter {
                    attributePredicate.matches(it.attributes)
                }
                .map { it.attributes }
    }

    override fun onUnknownHtmlTapped(unknownHtmlSpan: UnknownHtmlSpan) {
        showBlockEditorDialog(unknownHtmlSpan)
    }

    override fun executeEvent(data: TextWatcherEvent) {
        disableObservationQueue()

        if (data is TextWatcherEventInsertText) {
            // here replace the inserted thing with a new "normal" insertion
            val afterData = data.afterEventData
            setText(afterData.textAfter)
            setSelection(data.insertionStart + data.insertionLength)
        }

        enableObservationQueue()
    }

    fun enableCrashLogging(helper: AztecExceptionHandler.ExceptionHandlerHelper) {
        this.uncaughtExceptionHandler = AztecExceptionHandler(helper, this)
    }

    fun disableCrashLogging() {
        this.uncaughtExceptionHandler?.restoreDefaultHandler()
        this.uncaughtExceptionHandler = null
    }

    override fun dispatchHoverEvent(event: MotionEvent): Boolean {
        return if (accessibilityDelegate.onHoverEvent(event)) true else super.dispatchHoverEvent(event)
    }

}
