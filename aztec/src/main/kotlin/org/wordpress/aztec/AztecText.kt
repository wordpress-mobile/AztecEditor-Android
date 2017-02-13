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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.*
import android.text.style.LeadingMarginSpan
import android.text.style.ParagraphStyle
import android.text.style.SuggestionSpan
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import android.widget.EditText
import org.wordpress.android.util.DisplayUtils
import org.wordpress.aztec.formatting.BlockFormatter
import org.wordpress.aztec.formatting.InlineFormatter
import org.wordpress.aztec.formatting.LineBlockFormatter
import org.wordpress.aztec.formatting.LinkFormatter
import org.wordpress.aztec.source.Format
import org.wordpress.aztec.spans.*
import org.wordpress.aztec.util.TypefaceCache
import org.xml.sax.Attributes
import java.util.*


class AztecText : EditText, TextWatcher {
    private var historyEnable = resources.getBoolean(R.bool.history_enable)
    private var historySize = resources.getInteger(R.integer.history_size)

    private var addLinkDialog: AlertDialog? = null
    private var consumeEditEvent: Boolean = false
    private var textChangedEventDetails = TextChangedEvent("", 0, 0, 0)

    private var onSelectionChangedListener: OnSelectionChangedListener? = null

    private var onImeBackListener: OnImeBackListener? = null

    private var onMediaTappedListener: OnMediaTappedListener? = null

    private var isViewInitialized = false
    private var previousCursorPosition = 0

    val selectedStyles = ArrayList<TextFormat>()

    private var isNewStyleSelected = false

    var isMediaAdded = false

    lateinit var history: History

    lateinit var inlineFormatter: InlineFormatter
    lateinit var blockFormatter: BlockFormatter
    lateinit var lineBlockFormatter: LineBlockFormatter
    lateinit var linkFormatter: LinkFormatter

    var imageGetter: Html.ImageGetter? = null

    interface OnSelectionChangedListener {
        fun onSelectionChanged(selStart: Int, selEnd: Int)
    }

    interface OnImeBackListener {
        fun onImeBack()
    }

    interface OnMediaTappedListener {
        fun mediaTapped(attrs: Attributes?, naturalWidth: Int, naturalHeight: Int)
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

    private fun init(attrs: AttributeSet?) {
        TypefaceCache.setCustomTypeface(context, this, TypefaceCache.TYPEFACE_MERRIWEATHER_REGULAR)

        val array = context.obtainStyledAttributes(attrs, R.styleable.AztecText, 0, R.style.AztecTextStyle)
        setLineSpacing(
                array.getDimension(
                        R.styleable.AztecText_lineSpacingExtra,
                        resources.getDimension(R.dimen.spacing_extra)
                ),
                array.getFloat(
                        R.styleable.AztecText_lineSpacingMultiplier,
                        resources.getString(R.dimen.spacing_multiplier).toFloat()
                )
        )
        setBackgroundColor(array.getColor(R.styleable.AztecText_backgroundColor, ContextCompat.getColor(context, R.color.background)))
        setTextColor(array.getColor(R.styleable.AztecText_textColor, ContextCompat.getColor(context, R.color.text)))
        setHintTextColor(array.getColor(R.styleable.AztecText_textColorHint, ContextCompat.getColor(context, R.color.text_hint)))

        historyEnable = array.getBoolean(R.styleable.AztecText_historyEnable, historyEnable)
        historySize = array.getInt(R.styleable.AztecText_historySize, historySize)

        inlineFormatter = InlineFormatter(this,
                InlineFormatter.CodeStyle(
                        array.getColor(R.styleable.AztecText_codeBackground, 0),
                        array.getFraction(R.styleable.AztecText_codeBackgroundAlpha, 1, 1, 0f),
                        array.getColor(R.styleable.AztecText_codeColor, 0)),
                LineBlockFormatter.HeaderStyle(
                        array.getDimensionPixelSize(R.styleable.AztecText_blockVerticalPadding, 0)))

        blockFormatter = BlockFormatter(this,
                BlockFormatter.ListStyle(
                        array.getColor(R.styleable.AztecText_bulletColor, 0),
                        array.getDimensionPixelSize(R.styleable.AztecText_bulletMargin, 0),
                        array.getDimensionPixelSize(R.styleable.AztecText_bulletPadding, 0),
                        array.getDimensionPixelSize(R.styleable.AztecText_bulletWidth, 0),
                        array.getDimensionPixelSize(R.styleable.AztecText_blockVerticalPadding, 0)),
                BlockFormatter.QuoteStyle(
                        array.getColor(R.styleable.AztecText_quoteBackground, 0),
                        array.getColor(R.styleable.AztecText_quoteColor, 0),
                        array.getDimensionPixelSize(R.styleable.AztecText_quoteMargin, 0),
                        array.getDimensionPixelSize(R.styleable.AztecText_quotePadding, 0),
                        array.getDimensionPixelSize(R.styleable.AztecText_quoteWidth, 0),
                        array.getDimensionPixelSize(R.styleable.AztecText_blockVerticalPadding, 0)
                ))

        linkFormatter = LinkFormatter(this, LinkFormatter.LinkStyle(array.getColor(
                R.styleable.AztecText_linkColor, 0),
                array.getBoolean(R.styleable.AztecText_linkUnderline, true)))

        lineBlockFormatter = LineBlockFormatter(this, LineBlockFormatter.HeaderStyle(
                array.getDimensionPixelSize(R.styleable.AztecText_blockVerticalPadding, 0)))

        array.recycle()

        if (historyEnable && historySize <= 0) {
            throw IllegalArgumentException("historySize must > 0")
        }

        history = History(historyEnable, historySize)

        // triggers ClickableSpan onClick() events
        movementMethod = EnhancedMovementMethod

        //Detect the press of backspace from hardware keyboard when no characters are deleted (eg. at 0 index of EditText)
        setOnKeyListener { v, keyCode, event ->
            var consumeKeyEvent = false
            history.beforeTextChanged(toFormattedHtml())
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                inlineFormatter.tryRemoveLeadingInlineStyle()
                consumeKeyEvent = blockFormatter.tryRemoveBlockStyleFromFirstLine()
            }

            if (consumeKeyEvent) {
                history.handleHistory(this@AztecText)
            }
            consumeKeyEvent
        }

        isViewInitialized = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        addTextChangedListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeTextChangedListener(this)
        if (addLinkDialog != null && addLinkDialog!!.isShowing) {
            addLinkDialog!!.dismiss()
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        val customState = savedState.state
        val array = ArrayList(customState.getStringArrayList("historyList"))
        val list = LinkedList<String>()

        list += array

        history.historyList = list
        history.historyCursor = customState.getInt("historyCursor")
        history.inputLast = customState.getString("inputLast")
        visibility = customState.getInt("visibility")

        val retainedHtml = customState.getString("retained_html")
        fromHtml(retainedHtml)

        val retainedSelectionStart = customState.getInt("selection_start")
        val retainedSelectionEnd = customState.getInt("selection_end")

        if (retainedSelectionEnd < editableText.length) {
            setSelection(retainedSelectionStart, retainedSelectionEnd)
        }


        val isDialogVisible = customState.getBoolean("isUrlDialogVisible", false)

        if (isDialogVisible) {
            val retainedUrl = customState.getString("retainedUrl", "")
            val retainedAnchor = customState.getString("retainedAnchor", "")

            showLinkDialog(retainedUrl, retainedAnchor)
        }

        isMediaAdded = customState.getBoolean("isMediaAdded")
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        val bundle = Bundle()
        bundle.putStringArrayList("historyList", ArrayList<String>(history.historyList))
        bundle.putInt("historyCursor", history.historyCursor)
        bundle.putString("inputLast", history.inputLast)
        bundle.putInt("visibility", visibility)
        bundle.putString("retained_html", toHtml(false))
        bundle.putInt("selection_start", selectionStart)
        bundle.putInt("selection_end", selectionEnd)

        if (addLinkDialog != null && addLinkDialog!!.isShowing) {
            bundle.putBoolean("isUrlDialogVisible", true)

            val urlInput = addLinkDialog!!.findViewById(R.id.linkURL) as EditText
            val anchorInput = addLinkDialog!!.findViewById(R.id.linkText) as EditText

            bundle.putString("retainedUrl", urlInput.text.toString())
            bundle.putString("retainedAnchor", anchorInput.text.toString())
        }

        bundle.putBoolean("isMediaAdded", isMediaAdded)

        savedState.state = bundle
        return savedState
    }

    internal class SavedState : BaseSavedState {
        var state: Bundle = Bundle()

        constructor(superState: Parcelable) : super(superState) {
        }

        constructor(parcel: Parcel) : super(parcel) {
            state = parcel.readBundle(javaClass.classLoader)
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeBundle(state)
        }


        companion object {
            @JvmField val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState {
                    return SavedState(source)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    fun setSelectedStyles(styles: ArrayList<TextFormat>) {
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

    fun setOnMediaTappedListener(listener: OnMediaTappedListener) {
        this.onMediaTappedListener = listener
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            onImeBackListener?.onImeBack()
        }
        return super.onKeyPreIme(keyCode, event)
    }

    public override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        if (!isViewInitialized) return
        if (selStart == selEnd && movedCursorIfBeforeZwjChar(selEnd)) return

        previousCursorPosition = selEnd

        onSelectionChangedListener?.onSelectionChanged(selStart, selEnd)

        setSelectedStyles(getAppliedStyles(selStart, selEnd))
    }

    private fun movedCursorIfBeforeZwjChar(selEnd: Int): Boolean {
        if (selEnd < text.length && text[selEnd] == Constants.ZWJ_CHAR) {
            if (selEnd == previousCursorPosition - 1 && selEnd > 0) {
                // moved left
                setSelection(selEnd - 1)
            } else {
                // moved right or dropped to right to the left of ZWJ
                setSelection(selEnd + 1)
            }
            return true
        }
        return false
    }

    fun getSelectedText(): String {
        if (selectionStart == -1 || selectionEnd == -1) return ""
        return editableText.substring(selectionStart, selectionEnd)
    }

    fun getAppliedStyles(selectionStart: Int, selectionEnd: Int): ArrayList<TextFormat> {
        val styles = ArrayList<TextFormat>()

        var newSelStart = if (selectionStart > selectionEnd) selectionEnd else selectionStart
        var newSelEnd = selectionEnd

        if (editableText.isEmpty()) {
            return styles
        }

        if (newSelStart == 0 && newSelEnd == 0) {
            newSelEnd++
        } else if (newSelStart == newSelEnd && editableText.length > selectionStart && editableText[selectionStart - 1] == '\n') {
            newSelEnd++
        } else if (newSelStart > 0 && !isTextSelected()) {
            newSelStart--
        }


        TextFormat.values().forEach {
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

    fun toggleFormatting(textFormat: TextFormat) {
        history.beforeTextChanged(toFormattedHtml())

        when (textFormat) {
            TextFormat.FORMAT_PARAGRAPH,
            TextFormat.FORMAT_HEADING_1,
            TextFormat.FORMAT_HEADING_2,
            TextFormat.FORMAT_HEADING_3,
            TextFormat.FORMAT_HEADING_4,
            TextFormat.FORMAT_HEADING_5,
            TextFormat.FORMAT_HEADING_6 -> lineBlockFormatter.applyHeading(textFormat)
            TextFormat.FORMAT_BOLD -> inlineFormatter.toggleBold()
            TextFormat.FORMAT_ITALIC -> inlineFormatter.toggleItalic()
            TextFormat.FORMAT_UNDERLINE -> inlineFormatter.toggleUnderline()
            TextFormat.FORMAT_STRIKETHROUGH -> inlineFormatter.toggleStrikethrough()
            TextFormat.FORMAT_UNORDERED_LIST -> blockFormatter.toggleUnorderedList()
            TextFormat.FORMAT_ORDERED_LIST -> blockFormatter.toggleOrderedList()
            TextFormat.FORMAT_QUOTE -> blockFormatter.toggleQuote()
            TextFormat.FORMAT_MORE -> lineBlockFormatter.applyMoreComment()
            TextFormat.FORMAT_PAGE -> lineBlockFormatter.applyPageComment()
            TextFormat.FORMAT_CODE -> inlineFormatter.toggleCode()
            else -> {
            }
        }

        history.handleHistory(this)
    }

    fun contains(format: TextFormat, selStart: Int = selectionStart, selEnd: Int = selectionEnd): Boolean {
        when (format) {
            TextFormat.FORMAT_HEADING_1,
            TextFormat.FORMAT_HEADING_2,
            TextFormat.FORMAT_HEADING_3,
            TextFormat.FORMAT_HEADING_4,
            TextFormat.FORMAT_HEADING_5,
            TextFormat.FORMAT_HEADING_6 -> return lineBlockFormatter.containsHeading(format, selStart, selEnd)
            TextFormat.FORMAT_BOLD -> return inlineFormatter.containsInlineStyle(TextFormat.FORMAT_BOLD, selStart, selEnd)
            TextFormat.FORMAT_ITALIC -> return inlineFormatter.containsInlineStyle(TextFormat.FORMAT_ITALIC, selStart, selEnd)
            TextFormat.FORMAT_UNDERLINE -> return inlineFormatter.containsInlineStyle(TextFormat.FORMAT_UNDERLINE, selStart, selEnd)
            TextFormat.FORMAT_STRIKETHROUGH -> return inlineFormatter.containsInlineStyle(TextFormat.FORMAT_STRIKETHROUGH, selStart, selEnd)
            TextFormat.FORMAT_UNORDERED_LIST -> return blockFormatter.containsList(TextFormat.FORMAT_UNORDERED_LIST, selStart, selEnd)
            TextFormat.FORMAT_ORDERED_LIST -> return blockFormatter.containsList(TextFormat.FORMAT_ORDERED_LIST, selStart, selEnd)
            TextFormat.FORMAT_QUOTE -> return blockFormatter.containQuote(selectionStart, selectionEnd)
            TextFormat.FORMAT_LINK -> return linkFormatter.containLink(selStart, selEnd)
            TextFormat.FORMAT_CODE -> return inlineFormatter.containsInlineStyle(TextFormat.FORMAT_CODE, selStart, selEnd)
            else -> return false
        }
    }

    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
        if (!isViewInitialized) return

        if (selectionEnd < text.length && text[selectionEnd] == Constants.ZWJ_CHAR) {
            setSelection(selectionEnd + 1)
        }

        prepareTextChangeEventDetails(after, count, start, text)

        blockFormatter.carryOverDeletedListItemAttributes(count, start, text, this.text)
        inlineFormatter.carryOverInlineSpans(start, count, after)

        if (!isTextChangedListenerDisabled()) {
            history.beforeTextChanged(toFormattedHtml())
        }
    }

    private fun prepareTextChangeEventDetails(after: Int, count: Int, start: Int, text: CharSequence) {
        var deletedFromBlock = false
        var blockStart = -1
        var blockEnd = -1
        if (count > 0 && after < count && !isTextChangedListenerDisabled()) {
            val block = this.text.getSpans(start, start + 1, AztecBlockSpan::class.java).firstOrNull()
            if (block != null) {
                blockStart = this.text.getSpanStart(block)
                blockEnd = this.text.getSpanEnd(block)
                deletedFromBlock = start < blockEnd && this.text[start] != '\n' &&
                        (start + count >= blockEnd || (start + count + 1 == blockEnd && text[start + count] == '\n')) &&
                        (start == 0 || text[start - 1] == '\n')

                // if we are removing all characters from the span, we must change the flag to SPAN_EXCLUSIVE_INCLUSIVE
                // because we want to allow a block span with empty text (such as list with a single empty first item)
                if (deletedFromBlock && after == 0 && blockEnd - blockStart == count) {
                    this.text.setSpan(block, blockStart, blockEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
                }
            }
        }

        if (deletedFromBlock) {
            textChangedEventDetails = TextChangedEvent(this.text.toString(), deletedFromBlock, blockStart)
        } else {
            textChangedEventDetails = TextChangedEvent(this.text.toString())
        }
    }

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
        if (!isViewInitialized) return

        inlineFormatter.reapplyCarriedOverInlineSpans()

        textChangedEventDetails.before = before
        textChangedEventDetails.text = text
        textChangedEventDetails.countOfCharacters = count
        textChangedEventDetails.start = start
        textChangedEventDetails.initialize()
    }

    override fun afterTextChanged(text: Editable) {
        if (isTextChangedListenerDisabled()) {
            enableTextChangedListener()
            return
        }


        if (textChangedEventDetails.inputStart == 0 && textChangedEventDetails.count == 0) {
            removeLeadingStyle(text, AztecInlineSpan::class.java)
            removeLeadingStyle(text, LeadingMarginSpan::class.java)
        }

        val newLine = textChangedEventDetails.isNewLine()
        inlineFormatter.handleInlineStyling(textChangedEventDetails)
        blockFormatter.handleBlockStyling(text, textChangedEventDetails)
        lineBlockFormatter.handleLineBlockStyling(textChangedEventDetails)

        isMediaAdded = text.getSpans(0, text.length, AztecMediaSpan::class.java).isNotEmpty()

        if (textChangedEventDetails.count > 0 && text.isEmpty()) {
            onSelectionChanged(0, 0)
        }

        // preserve the attributes on the previous list item when adding a new one
        blockFormatter.realignAttributesWhenAddingItem(text, textChangedEventDetails, newLine)

        history.handleHistory(this)
    }

    fun removeLeadingStyle(text: Editable, spanClass: Class<*>) {
        text.getSpans(0, 0, spanClass).forEach {
            if (text.isNotEmpty()) {
                text.setSpan(it, 0, text.getSpanEnd(it), text.getSpanFlags(it))
            } else {
                text.removeSpan(it)
            }
        }
    }


    fun redo() {
        history.redo(this)
    }

    fun undo() {
        history.undo(this)
    }

    // Helper ======================================================================================

    fun consumeCursorPosition(text: SpannableStringBuilder): Int {
        var cursorPosition = 0

        text.getSpans(0, text.length, AztecCursorSpan::class.java).forEach {
            cursorPosition = text.getSpanStart(it)
            text.removeSpan(it)
        }

        return cursorPosition
    }

    fun fromHtml(source: String) {
        disableTextChangedListener()
        editableText.clear()

        val builder = SpannableStringBuilder()
        val parser = AztecParser()
        builder.append(parser.fromHtml(Format.clearFormatting(source), onMediaTappedListener, context))
        switchToAztecStyle(builder, 0, builder.length)
        disableTextChangedListener()
        val cursorPosition = consumeCursorPosition(builder)

        setTextKeepState(builder)
        enableTextChangedListener()
        setSelection(cursorPosition)

        loadImages()
    }

    private fun loadImages() {
        val spans = this.text.getSpans(0, text.length, AztecMediaSpan::class.java)
        spans.forEach {
            val callbacks = object : Html.ImageGetter.Callbacks {

                override fun onUseDefaultImage() {
                    // we already have a default image loaded so, noop
                }

                override fun onImageLoaded(drawable: Drawable?) {
                    replaceImage(drawable)
                }

                override fun onImageLoadingFailed() {
                    val drawable = ContextCompat.getDrawable(context, R.drawable.ic_image_failed)
                    replaceImage(drawable)
                }

                private fun replaceImage(drawable: Drawable?) {
                    it.drawable = drawable
                    refreshText()
                }
            }

            /*
             * Following Android guidelines for keylines and spacing, screen edge margins should
             * be 16dp.  Therefore, the width of images should be the width of the screen minus
             * 16dp on both sides (i.e. 16 * 2 = 32).
             *
             * https://material.io/guidelines/layout/metrics-keylines.html#metrics-keylines-baseline-grids
             */
            val width = context.resources.displayMetrics.widthPixels - DisplayUtils.dpToPx(context, 32)
            imageGetter?.loadImage(it.getSource(), callbacks, width)
        }
    }

    fun toHtml(withCursorTag: Boolean = false): String {
        val parser = AztecParser()
        val output = SpannableStringBuilder(text)

        clearMetaSpans(output)

        if (withCursorTag) {
            output.setSpan(AztecCursorSpan(), selectionEnd, selectionEnd, Spanned.SPAN_MARK_MARK)
        }

        return Format.clearFormatting(parser.toHtml(output, withCursorTag))
    }

    fun toFormattedHtml(): String {
        val parser = AztecParser()
        val output = SpannableStringBuilder(text)
        clearMetaSpans(output)
        return Format.addFormatting(parser.toHtml(output))
    }

    private fun switchToAztecStyle(editable: Editable, start: Int, end: Int) {
        val blockSpans = editable.getSpans(start, end, AztecBlockSpan::class.java)
        blockSpans.forEach {
            val spanStart = editable.getSpanStart(it)
            val spanEnd = editable.getSpanEnd(it)
            editable.removeSpan(it)

            if (it is AztecListSpan) {
                editable.setSpan(blockFormatter.makeBlockSpan(it.javaClass as Class<AztecBlockSpan>, it.attributes, it.lastItem), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else {
                editable.setSpan(blockFormatter.makeBlockSpan(it.javaClass, it.attributes), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        val paragraphSpans = editable.getSpans(start, end, ParagraphSpan::class.java)
        for (span in paragraphSpans) {
            val spanStart = editable.getSpanStart(span)
            var spanEnd = editable.getSpanEnd(span)
            spanEnd = if (0 < spanEnd && spanEnd < editable.length && editable[spanEnd] == '\n') spanEnd - 1 else spanEnd
            editable.removeSpan(span)
            editable.setSpan(ParagraphSpan(span.attributes), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

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

        val headingSpan = editable.getSpans(start, end, AztecHeadingSpan::class.java)
        headingSpan.forEach {
            val spanStart = editable.getSpanStart(it)
            val spanEnd = editable.getSpanEnd(it)
            editable.removeSpan(it)
            editable.setSpan(AztecHeadingSpan(it.textFormat, it.attributes,
                    lineBlockFormatter.headerStyle.verticalPadding), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun disableTextChangedListener() {
        consumeEditEvent = true
    }

    fun enableTextChangedListener() {
        consumeEditEvent = false
    }

    fun isTextChangedListenerDisabled(): Boolean {
        return consumeEditEvent
    }

    fun refreshText() {
        disableTextChangedListener()
        val selStart = selectionStart
        val selEnd = selectionEnd
        text = editableText
        setSelection(selStart, selEnd)
        enableTextChangedListener()
    }

    fun removeHeadingStylesFromRange(start: Int, end: Int) {
        val spans = editableText.getSpans(start, end, AztecHeadingSpan::class.java)

        for (span in spans) {
            editableText.removeSpan(span)
        }
    }

    fun removeInlineStylesFromRange(start: Int, end: Int) {
        inlineFormatter.removeInlineStyle(TextFormat.FORMAT_BOLD, start, end)
        inlineFormatter.removeInlineStyle(TextFormat.FORMAT_ITALIC, start, end)
        inlineFormatter.removeInlineStyle(TextFormat.FORMAT_STRIKETHROUGH, start, end)
        inlineFormatter.removeInlineStyle(TextFormat.FORMAT_UNDERLINE, start, end)
        inlineFormatter.removeInlineStyle(TextFormat.FORMAT_CODE, start, end)
    }


    fun removeBlockStylesFromRange(start: Int, end: Int, ignoreLineBounds: Boolean = false) {
        blockFormatter.removeBlockStyle(start, end, AztecBlockSpan::class.java, ignoreLineBounds)
    }

    //logic party copied from TextView
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
                clearFocus() //hide text action menu
            }
            android.R.id.cut -> {
                copy(text, min, max)
                text.delete(min, max) //this will hide text action menu
            }
            else -> return super.onTextContextMenuItem(id)
        }

        return true
    }

    //Convert selected text to html and add it to clipboard
    fun copy(editable: Editable, start: Int, end: Int) {
        val selectedText = editable.subSequence(start, end)
        val parser = AztecParser()
        val output = SpannableStringBuilder(selectedText)

        //Strip block elements untill we figure out copy paste completely
        output.getSpans(0, output.length, ParagraphStyle::class.java).forEach { output.removeSpan(it) }
        clearMetaSpans(output)
        val html = Format.clearFormatting(parser.toHtml(output))

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboard.primaryClip = ClipData.newPlainText(null, html)
    }

    //copied from TextView with some changes
    private fun paste(editable: Editable, min: Int, max: Int) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null) {
            val parser = AztecParser()

            for (i in 0..clip.itemCount - 1) {
                val textToPaste = clip.getItemAt(i).coerceToText(context)

                val builder = SpannableStringBuilder()
                builder.append(parser.fromHtml(Format.clearFormatting(textToPaste.toString()), onMediaTappedListener,
                        context).trim())
                Selection.setSelection(editable, max)

                disableTextChangedListener()
                editable.replace(min, max, builder)
                enableTextChangedListener()

                inlineFormatter.joinStyleSpans(0, editable.length) //TODO: see how this affects performance
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

        linkFormatter.linkInvalid(urlSpanBounds.first, urlSpanBounds.second)
        onSelectionChanged(urlSpanBounds.first, urlSpanBounds.second)
    }

    fun showLinkDialog(presetUrl: String = "", presetAnchor: String = "") {
        val urlAndAnchor = linkFormatter.getSelectedUrlWithAnchor()

        val url = if (TextUtils.isEmpty(presetUrl)) urlAndAnchor.first else presetUrl
        val anchor = if (TextUtils.isEmpty(presetAnchor)) urlAndAnchor.second else presetAnchor

        val builder = AlertDialog.Builder(context)

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_link, null)

        val urlInput = dialogView.findViewById(R.id.linkURL) as EditText
        val anchorInput = dialogView.findViewById(R.id.linkText) as EditText

        urlInput.setText(url)
        anchorInput.setText(anchor)

        builder.setView(dialogView)
        builder.setTitle(R.string.dialog_title)

        builder.setPositiveButton(R.string.dialog_button_ok, { dialog, which ->
            val linkText = urlInput.text.toString().trim { it <= ' ' }
            val anchorText = anchorInput.text.toString().trim { it <= ' ' }

            link(linkText, anchorText)

        })

        if (linkFormatter.isUrlSelected()) {
            builder.setNeutralButton(R.string.dialog_button_remove_link, { dialogInterface, i ->
                removeLink()
            })
        }

        builder.setNegativeButton(R.string.dialog_button_cancel, { dialogInterface, i ->
            dialogInterface.dismiss()
        })

        addLinkDialog = builder.create()
        addLinkDialog!!.show()
    }


    //Custom input connection is used to detect the press of backspace when no characters are deleted
    //(eg. at 0 index of EditText)
    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        return AztecInputConnection(super.onCreateInputConnection(outAttrs), true)
    }

    private inner class AztecInputConnection(target: InputConnection, mutable: Boolean) : InputConnectionWrapper(target, mutable) {

        override fun sendKeyEvent(event: KeyEvent): Boolean {
            if (event.action === KeyEvent.ACTION_DOWN && event.keyCode === KeyEvent.KEYCODE_DEL) {
                history.beforeTextChanged(toFormattedHtml())

                inlineFormatter.tryRemoveLeadingInlineStyle()
                val isStyleRemoved = blockFormatter.tryRemoveBlockStyleFromFirstLine()
                if (isStyleRemoved) {
                    history.handleHistory(this@AztecText)
                    return false
                }

            }
            return super.sendKeyEvent(event)
        }

        override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
            if (beforeLength == 1 && afterLength == 0) {
                return sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)) && sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
            }

            return super.deleteSurroundingText(beforeLength, afterLength)
        }
    }

    fun insertMedia(drawable: Drawable?, attributes: Attributes) {
        lineBlockFormatter.insertMedia(drawable, attributes, onMediaTappedListener)
    }

    fun removeMedia(attributePredicate: AttributePredicate) {
        text.getSpans(0, text.length, AztecMediaSpan::class.java).forEach {
            if (it.attributes != null) {
                if (attributePredicate.matches(it.attributes as Attributes)) {
                    val start = text.getSpanStart(it)
                    val end = text.getSpanEnd(it)

                    val clickableSpan = text.getSpans(start, end, AztecMediaClickableSpan::class.java).firstOrNull()

                    text.removeSpan(clickableSpan)
                    text.removeSpan(it)

                    text.delete(start, end)
                }
            }
        }
    }

    interface AttributePredicate {
        /**
         * Return true if the attributes list fulfills some condition
         */
        fun matches(attrs: Attributes): Boolean
    }

    fun setOverlayLevel(attributePredicate: AttributePredicate, index: Int, level: Int, attrs: Attributes) {
        text.getSpans(0, text.length, AztecMediaSpan::class.java).forEach {
            if (it.attributes != null) {
                if (attributePredicate.matches(it.attributes as Attributes)) {
                    it.setOverayLevel(index, level)
                    it.attributes = attrs
                }
            }
        }
    }

    fun setOverlay(attributePredicate: AttributePredicate, index: Int, overlay: Drawable?, gravity: Int,
                   attributes: Attributes?) {
        text.getSpans(0, text.length, AztecMediaSpan::class.java).forEach {
            if (it.attributes != null) {
                if (attributePredicate.matches(it.attributes as Attributes)) {
                    // set the new overlay drawable
                    it.setOverlay(index, overlay, gravity)

                    if (attributes != null) {
                        it.attributes = attributes
                    }

                    invalidate()
                }
            }
        }
    }

    fun clearOverlays(attributePredicate: AttributePredicate, attributes: Attributes?) {
        text.getSpans(0, text.length, AztecMediaSpan::class.java).forEach {
            if (it.attributes != null) {
                if (attributePredicate.matches(it.attributes as Attributes)) {
                    it.clearOverlays()

                    if (attributes != null) {
                        it.attributes = attributes
                    }

                    invalidate()
                }
            }
        }
    }

    fun getMediaAttributes(attributePredicate: AttributePredicate): Attributes? {
        return getAllMediaAttributes(attributePredicate).firstOrNull()
    }

    fun getAllMediaAttributes(attributePredicate: AttributePredicate): List<Attributes?> {
        return text
                .getSpans(0, text.length, AztecMediaSpan::class.java)
                .filter {
                    if (it.attributes != null) {
                        return@filter attributePredicate.matches(it.attributes as Attributes)
                    } else {
                        return@filter false
                    }
                }
                .map { it.attributes }
    }
}
