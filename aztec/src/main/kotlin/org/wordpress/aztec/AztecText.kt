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
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.DrawableRes
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatEditText
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
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.BaseInputConnection
import android.widget.EditText
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
import org.wordpress.aztec.spans.CommentSpan
import org.wordpress.aztec.spans.EndOfParagraphMarker
import org.wordpress.aztec.spans.IAztecAttributedSpan
import org.wordpress.aztec.spans.IAztecBlockSpan
import org.wordpress.aztec.spans.UnknownClickableSpan
import org.wordpress.aztec.spans.UnknownHtmlSpan
import org.wordpress.aztec.toolbar.AztecToolbar
import org.wordpress.aztec.util.AztecLog
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
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.ArrayList
import java.util.Arrays
import java.util.LinkedList

@Suppress("UNUSED_PARAMETER")
class AztecText : AppCompatEditText, TextWatcher, UnknownHtmlSpan.OnUnknownHtmlTappedListener, IEventInjector {
    companion object {
        val BLOCK_EDITOR_HTML_KEY = "RETAINED_BLOCK_HTML_KEY"
        val BLOCK_EDITOR_START_INDEX_KEY = "BLOCK_EDITOR_START_INDEX_KEY"
        val BLOCK_DIALOG_VISIBLE_KEY = "BLOCK_DIALOG_VISIBLE_KEY"

        val LINK_DIALOG_VISIBLE_KEY = "LINK_DIALOG_VISIBLE_KEY"
        val LINK_DIALOG_URL_KEY = "LINK_DIALOG_URL_KEY"
        val LINK_DIALOG_ANCHOR_KEY = "LINK_DIALOG_ANCHOR_KEY"

        val HISTORY_LIST_KEY = "HISTORY_LIST_KEY"
        val HISTORY_CURSOR_KEY = "HISTORY_CURSOR_KEY"

        val SELECTION_START_KEY = "SELECTION_START_KEY"
        val SELECTION_END_KEY = "SELECTION_END_KEY"

        val INPUT_LAST_KEY = "INPUT_LAST_KEY"
        val VISIBILITY_KEY = "VISIBILITY_KEY"
        val IS_MEDIA_ADDED_KEY = "IS_MEDIA_ADDED_KEY"
        val RETAINED_HTML_KEY = "RETAINED_HTML_KEY"

        val DEFAULT_IMAGE_WIDTH = 800

        var watchersNestingLevel: Int = 0

        private fun getPlaceholderDrawableFromResID(context: Context, @DrawableRes drawableId: Int, maxImageWidthForVisualEditor: Int): BitmapDrawable {
            val drawable = ContextCompat.getDrawable(context, drawableId)
            var bitmap: Bitmap
            if (drawable is BitmapDrawable) {
                bitmap = drawable.bitmap
                bitmap = ImageUtils.getScaledBitmapAtLongestSide(bitmap, maxImageWidthForVisualEditor)
            } else if (drawable is VectorDrawableCompat || drawable is VectorDrawable) {
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
    }

    private var historyEnable = resources.getBoolean(R.bool.history_enable)
    private var historySize = resources.getInteger(R.integer.history_size)

    private var addLinkDialog: AlertDialog? = null
    private var blockEditorDialog: AlertDialog? = null
    private var consumeEditEvent: Boolean = false
    private var consumeSelectionChangedEvent: Boolean = false
    private var isInlineTextHandlerEnabled: Boolean = true
    private var bypassObservationQueue: Boolean = false

    private var onSelectionChangedListener: OnSelectionChangedListener? = null
    private var onImeBackListener: OnImeBackListener? = null
    private var onImageTappedListener: OnImageTappedListener? = null
    private var onVideoTappedListener: OnVideoTappedListener? = null
    private var onAudioTappedListener: OnAudioTappedListener? = null
    private var onMediaDeletedListener: OnMediaDeletedListener? = null
    private var onVideoInfoRequestedListener: OnVideoInfoRequestedListener? = null
    var externalLogger: AztecLog.ExternalLogger? = null

    private var isViewInitialized = false
    private var isLeadingStyleRemoved = false

    private var isHandlingBackspaceEvent = false

    var commentsVisible = resources.getBoolean(R.bool.comments_visible)

    var isInCalypsoMode = true

    var consumeHistoryEvent: Boolean = false

    private var unknownBlockSpanStart = -1

    private var formatToolbar: AztecToolbar? = null

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

    var maxImagesWidth: Int = 0
    var minImagesWidth: Int = 0

    var observationQueue: ObservationQueue = ObservationQueue(this)
    var textWatcherEventBuilder: TextWatcherEvent.Builder = TextWatcherEvent.Builder()

    private var uncaughtExceptionHandler: AztecExceptionHandler? = null

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

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    fun setCalypsoMode(isCompatibleWithCalypso: Boolean) {
        isInCalypsoMode = isCompatibleWithCalypso
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

        verticalParagraphMargin = styles.getDimensionPixelSize(R.styleable.AztecText_blockVerticalPadding, 0)

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
                BlockFormatter.HeaderStyle(
                        verticalParagraphMargin),
                BlockFormatter.PreformatStyle(
                        styles.getColor(R.styleable.AztecText_preformatBackground, 0),
                        styles.getFraction(R.styleable.AztecText_preformatBackgroundAlpha, 1, 1, 0f),
                        styles.getColor(R.styleable.AztecText_preformatColor, 0),
                        verticalParagraphMargin)
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

        setupZeroIndexBackspaceDetection()

        //disable auto suggestions/correct for older devices
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            inputType = inputType or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        }

        install()

        // Needed to properly initialize the cursor position
        setSelection(0)

        enableTextChangedListener()

        isViewInitialized = true
    }

    // detect the press of backspace when no characters are deleted (eg. at 0 index of EditText)
    private fun setupZeroIndexBackspaceDetection() {
        //hardware keyboard
        setOnKeyListener { _, _, event ->
            handleBackspace(event)
        }

        //software keyboard
        val emptyEditTextBackspaceDetector = InputFilter { source, start, end, dest, dstart, dend ->
            if (selectionStart == 0 && selectionEnd == 0
                    && end == 0 && start == 0
                    && dstart == 0 && dend == 0
                    && !isHandlingBackspaceEvent) {
                isHandlingBackspaceEvent = true

                // Prevent the forced backspace from being added to the history stack
                consumeHistoryEvent = true

                handleBackspace(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                isHandlingBackspaceEvent = false
            }
            source
        }

        filters = arrayOf(emptyEditTextBackspaceDetector)
    }

    private fun handleBackspace(event: KeyEvent): Boolean {
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
                .add(HeadingHandler())
                .add(ListHandler())
                .add(ListItemHandler())
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

    override fun onRestoreInstanceState(state: Parcelable?) {
        disableTextChangedListener()

        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        val customState = savedState.state
        val array = readAndPurgeTempInstance<ArrayList<String>>(HISTORY_LIST_KEY, ArrayList<String>())
        val list = LinkedList<String>()

        list += array

        history.historyList = list
        history.historyCursor = customState.getInt(HISTORY_CURSOR_KEY)
        history.inputLast = readAndPurgeTempInstance<String>(INPUT_LAST_KEY, "")
        visibility = customState.getInt(VISIBILITY_KEY)

        val retainedHtml = readAndPurgeTempInstance<String>(RETAINED_HTML_KEY, "")
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

            showLinkDialog(retainedUrl, retainedAnchor)
        }

        val isBlockEditorDialogVisible = customState.getBoolean(BLOCK_DIALOG_VISIBLE_KEY, false)
        if (isBlockEditorDialogVisible) {
            val retainedBlockHtmlIndex = customState.getInt(BLOCK_EDITOR_START_INDEX_KEY, -1)
            if (retainedBlockHtmlIndex != -1) {
                val unknownSpan = text.getSpans(retainedBlockHtmlIndex, retainedBlockHtmlIndex + 1, UnknownHtmlSpan::class.java).firstOrNull()
                if (unknownSpan != null) {
                    val retainedBlockHtml = readAndPurgeTempInstance<String>(BLOCK_EDITOR_HTML_KEY, "")
                    showBlockEditorDialog(unknownSpan, retainedBlockHtml)
                }
            }
        }

        isMediaAdded = customState.getBoolean(IS_MEDIA_ADDED_KEY)

        enableTextChangedListener()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        val bundle = Bundle()
        writeTempInstance(HISTORY_LIST_KEY, ArrayList<String>(history.historyList))
        bundle.putInt(HISTORY_CURSOR_KEY, history.historyCursor)
        writeTempInstance(INPUT_LAST_KEY, history.inputLast)
        bundle.putInt(VISIBILITY_KEY, visibility)
        writeTempInstance(RETAINED_HTML_KEY, toHtml(false))
        bundle.putInt(SELECTION_START_KEY, selectionStart)
        bundle.putInt(SELECTION_END_KEY, selectionEnd)

        if (addLinkDialog != null && addLinkDialog!!.isShowing) {
            bundle.putBoolean(LINK_DIALOG_VISIBLE_KEY, true)

            val urlInput = addLinkDialog!!.findViewById<EditText>(R.id.linkURL)
            val anchorInput = addLinkDialog!!.findViewById<EditText>(R.id.linkText)

            bundle.putString(LINK_DIALOG_URL_KEY, urlInput?.text?.toString())
            bundle.putString(LINK_DIALOG_ANCHOR_KEY, anchorInput?.text?.toString())
        }

        if (blockEditorDialog != null && blockEditorDialog!!.isShowing) {
            val source = blockEditorDialog!!.findViewById<SourceViewEditText>(R.id.source)

            bundle.putBoolean(BLOCK_DIALOG_VISIBLE_KEY, true)
            bundle.putInt(BLOCK_EDITOR_START_INDEX_KEY, unknownBlockSpanStart)
            writeTempInstance(BLOCK_EDITOR_HTML_KEY, source?.getPureHtml(false))
        }

        bundle.putBoolean(IS_MEDIA_ADDED_KEY, isMediaAdded)

        savedState.state = bundle
        return savedState
    }

    private fun writeTempInstance(filename: String, obj: Any?) {
        with(File.createTempFile(filename, ".inst", context.getCacheDir())) {
            deleteOnExit() // just make sure the file gets deleted if we don't do it ourselves
            with(File(context.getCacheDir(), "$filename.inst")) {
                if (this.exists()) {
                    this.createNewFile()
                }

                with(FileOutputStream(this)) {
                    with(ObjectOutputStream(this)) {
                        writeObject(obj)
                        close()
                    }
                    close()
                }
            }
        }
    }

    private fun <T> readAndPurgeTempInstance(filename: String, defaultValue: T): T {
        var obj: T = defaultValue

        with(File(context.getCacheDir(), "$filename.inst")) {
            with(FileInputStream(this)) {
                with(ObjectInputStream(this)) {
                    val r: Any? = readObject()

                    @Suppress("UNCHECKED_CAST")
                    obj = (r ?: defaultValue) as T
                    close()
                }
            }
            delete() // delete the file, no longer needed
        }

        return obj
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
                .map { (it as IToolbarButton).action.textFormat }
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
            AztecTextFormat.FORMAT_BOLD,
            AztecTextFormat.FORMAT_ITALIC,
            AztecTextFormat.FORMAT_UNDERLINE,
            AztecTextFormat.FORMAT_STRIKETHROUGH,
            AztecTextFormat.FORMAT_CODE -> inlineFormatter.toggle(textFormat)
            AztecTextFormat.FORMAT_UNORDERED_LIST -> blockFormatter.toggleUnorderedList()
            AztecTextFormat.FORMAT_ORDERED_LIST -> blockFormatter.toggleOrderedList()
            AztecTextFormat.FORMAT_ALIGN_LEFT,
            AztecTextFormat.FORMAT_ALIGN_CENTER,
            AztecTextFormat.FORMAT_ALIGN_RIGHT -> return blockFormatter.toggleTextAlignment(textFormat)
            AztecTextFormat.FORMAT_QUOTE -> blockFormatter.toggleQuote()
            AztecTextFormat.FORMAT_HORIZONTAL_RULE -> lineBlockFormatter.applyHorizontalRule()
            else -> {
                plugins.filter { it is IToolbarButton && textFormat == it.action.textFormat }
                        .map { it as IToolbarButton }
                        .forEach { it.toggle() }
            }
        }
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
            AztecTextFormat.FORMAT_ITALIC,
            AztecTextFormat.FORMAT_UNDERLINE,
            AztecTextFormat.FORMAT_STRIKETHROUGH,
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

    fun setToolbar(toolbar: AztecToolbar) {
        formatToolbar = toolbar
    }

    private fun addWatcherNestingLevel() : Int {
        watchersNestingLevel++
        return watchersNestingLevel
    }

    private fun subWatcherNestingLevel() : Int {
        watchersNestingLevel--
        return watchersNestingLevel
    }

    private fun isEventObservableCandidate() : Boolean {
        return (observationQueue.hasActiveBuckets() && !bypassObservationQueue && (watchersNestingLevel == 1))
    }

    fun isObservationQueueBeingPopulated() : Boolean {
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
    }

    fun undo() {
        history.undo(this)
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

    fun fromHtml(source: String) {
        val builder = SpannableStringBuilder()
        val parser = AztecParser(plugins)

        val cleanSource = Format.removeSourceEditorFormatting(source, isInCalypsoMode)
        builder.append(parser.fromHtml(cleanSource, context))

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
                        refreshText()
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
                        refreshText()
                    }
                }
            }
            videoThumbnailGetter?.loadVideoThumbnail(it.getSource(), callbacks, maxImagesWidth, minImagesWidth)

            // Call the Video listener and ask for more info about the current video
            videoListenerRef?.onVideoInfoRequested(it.attributes)
        }
    }

    // returns regular or "calypso" html depending on the mode
    fun toHtml(withCursorTag: Boolean = false): String {
        val html = toPlainHtml(withCursorTag)

        if (isInCalypsoMode) {
            // calypso format is a mix of newline characters and html
            // paragraphs and line breaks are added on server, from newline characters
            return Format.addSourceEditorFormatting(html, true)
        } else {
            return html
        }
    }

    // platform agnostic HTML
    fun toPlainHtml(withCursorTag: Boolean = false): String {
        val parser = AztecParser(plugins)
        var output: SpannableStringBuilder
        try {
            output = SpannableStringBuilder(text)
        } catch (e: java.lang.ArrayIndexOutOfBoundsException) {
            // FIXME: Remove this log once we've data to replicate the issue, and fix it in some way.
            AppLog.e(AppLog.T.EDITOR, "There was an error creating SpannableStringBuilder. See #452 for details.")
            // No need to log the exception here. The ExceptionHandler does this for us.
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

        return EndOfBufferMarkerAdder.removeEndOfTextMarker(parser.toHtml(output, withCursorTag))
    }

    fun toFormattedHtml(): String {
        return Format.addSourceEditorFormatting(toHtml(), isInCalypsoMode)
    }

    private fun switchToAztecStyle(editable: Editable, start: Int, end: Int) {
        editable.getSpans(start, end, IAztecBlockSpan::class.java).forEach { blockFormatter.setBlockStyle(it) }
        editable.getSpans(start, end, EndOfParagraphMarker::class.java).forEach { it.verticalPadding = verticalParagraphMargin }

        val urlSpans = editable.getSpans(start, end, AztecURLSpan::class.java)
        for (span in urlSpans) {
            val spanStart = editable.getSpanStart(span)
            val spanEnd = editable.getSpanEnd(span)
            editable.removeSpan(span)
            editable.setSpan(linkFormatter.makeUrlSpan(span.url, span.attributes), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        val codeSpans = editable.getSpans(start, end, AztecCodeSpan::class.java)
        codeSpans.forEach {
            val spanStart = editable.getSpanStart(it)
            val spanEnd = editable.getSpanEnd(it)
            editable.removeSpan(it)
            editable.setSpan(inlineFormatter.makeInlineSpan(it.javaClass, it.attributes), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

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

    fun refreshText() {
        disableTextChangedListener()
        val selStart = selectionStart
        val selEnd = selectionEnd
        setFocusOnParentView()
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
        inlineFormatter.removeInlineStyle(AztecTextFormat.FORMAT_ITALIC, start, end)
        inlineFormatter.removeInlineStyle(AztecTextFormat.FORMAT_STRIKETHROUGH, start, end)
        inlineFormatter.removeInlineStyle(AztecTextFormat.FORMAT_UNDERLINE, start, end)
        inlineFormatter.removeInlineStyle(AztecTextFormat.FORMAT_CODE, start, end)
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

        when (id) {
            android.R.id.paste -> paste(text, min, max)
            android.R.id.copy -> {
                copy(text, min, max)
                clearFocus() // hide text action menu
            }
            android.R.id.cut -> {
                copy(text, min, max)
                text.delete(min, max) // this will hide text action menu

                // if we are cutting text from the beginning of editor, remove leading inline style
                if (min == 0) {
                    deleteInlineStyleFromTheBeginning()
                }
            }
            else -> return super.onTextContextMenuItem(id)
        }

        return true
    }

    // Convert selected text to html and add it to clipboard
    fun copy(editable: Editable, start: Int, end: Int) {
        val selectedText = editable.subSequence(start, end)
        val parser = AztecParser(plugins)
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

        val html = Format.removeSourceEditorFormatting(parser.toHtml(output), isInCalypsoMode)

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboard.primaryClip = ClipData.newHtmlText("aztec", output.toString(), html)
    }

    // copied from TextView with some changes
    fun paste(editable: Editable, min: Int, max: Int) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip

        if (clip != null) {
            history.beforeTextChanged(this@AztecText)

            disableTextChangedListener()

            if (min == 0 && max == text.length) {
                setText(Constants.REPLACEMENT_MARKER_STRING)
            } else {
                editable.delete(min, max)
                editable.insert(min, Constants.REPLACEMENT_MARKER_STRING)
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
                val textToPaste = clip.getItemAt(0).coerceToHtmlText(AztecParser(plugins))

                val oldHtml = toPlainHtml().replace("<aztec_cursor>", "")
                val newHtml = oldHtml.replace(Constants.REPLACEMENT_MARKER_STRING, textToPaste + "<" + AztecCursorSpan.AZTEC_CURSOR_TAG + ">")

                fromHtml(newHtml)
                inlineFormatter.joinStyleSpans(0, length())
            }
        }
    }

    fun clearMetaSpans(text: Spannable) {
        BaseInputConnection.removeComposingSpans(text)
        text.getSpans(0, text.length, SuggestionSpan::class.java).forEach { text.removeSpan(it) }
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)

        if (visibility == View.VISIBLE) {
            requestFocus()
        }
    }

    fun link(url: String, anchor: String) {
        history.beforeTextChanged(this@AztecText)
        if (TextUtils.isEmpty(url) && linkFormatter.isUrlSelected()) {
            removeLink()
        } else if (linkFormatter.isUrlSelected()) {
            linkFormatter.editLink(url, anchor, linkFormatter.getUrlSpanBounds().first, linkFormatter.getUrlSpanBounds().second)
        } else {
            linkFormatter.addLink(url, anchor, selectionStart, selectionEnd)
        }
    }

    fun removeLink() {
        val urlSpanBounds = linkFormatter.getUrlSpanBounds()

        linkFormatter.removeLink(urlSpanBounds.first, urlSpanBounds.second)
        onSelectionChanged(urlSpanBounds.first, urlSpanBounds.second)
    }

    @SuppressLint("InflateParams")
    fun showLinkDialog(presetUrl: String = "", presetAnchor: String = "") {
        val urlAndAnchor = linkFormatter.getSelectedUrlWithAnchor()

        val url = if (TextUtils.isEmpty(presetUrl)) urlAndAnchor.first else presetUrl
        val anchor = if (TextUtils.isEmpty(presetAnchor)) urlAndAnchor.second else presetAnchor

        val builder = AlertDialog.Builder(context)

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_link, null)

        val urlInput = dialogView.findViewById<EditText>(R.id.linkURL)
        val anchorInput = dialogView.findViewById<EditText>(R.id.linkText)

        urlInput.setText(url)
        anchorInput.setText(anchor)

        builder.setView(dialogView)
        builder.setTitle(R.string.link_dialog_title)

        builder.setPositiveButton(R.string.link_dialog_button_ok, { _, _ ->
            val linkText = urlInput.text.toString().trim { it <= ' ' }
            val anchorText = anchorInput.text.toString().trim { it <= ' ' }

            link(linkText, anchorText)
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
            textBuilder.append(AztecParser(plugins).fromHtml(source.getPureHtml(), context).trim())
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
            setSelection(data.insertionStart+data.insertionLength)
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
}
