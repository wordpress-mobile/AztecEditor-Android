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
import android.graphics.Typeface
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.content.ContextCompat
import android.text.*
import android.text.style.LeadingMarginSpan
import android.text.style.ParagraphStyle
import android.text.style.StyleSpan
import android.text.style.SuggestionSpan
import android.util.AttributeSet
import android.util.Patterns
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import org.wordpress.aztec.source.Format
import org.wordpress.aztec.spans.*
import org.wordpress.aztec.spans.AztecHeadingSpan.Heading
import org.wordpress.aztec.util.TypefaceCache
import java.util.*

class AztecText : EditText, TextWatcher {

    private var bulletColor = ContextCompat.getColor(context, R.color.bullet)
    private var bulletMargin = resources.getDimensionPixelSize(R.dimen.bullet_margin)
    private var bulletPadding = resources.getDimensionPixelSize(R.dimen.bullet_padding)
    private var bulletWidth = resources.getDimensionPixelSize(R.dimen.bullet_width)
    private var historyEnable = resources.getBoolean(R.bool.history_enable)
    private var historySize = resources.getInteger(R.integer.history_size)
    private var linkColor = ContextCompat.getColor(context, R.color.link)
    private var linkUnderline = resources.getBoolean(R.bool.link_underline)
    private var quoteBackground = ContextCompat.getColor(context, R.color.quote_background)
    private var quoteColor = ContextCompat.getColor(context, R.color.quote)
    private var quoteMargin = resources.getDimensionPixelSize(R.dimen.quote_margin)
    private var quotePadding = resources.getDimensionPixelSize(R.dimen.quote_padding)
    private var quoteWidth = resources.getDimensionPixelSize(R.dimen.quote_width)

    private var consumeEditEvent: Boolean = false
    private var textChangedEventDetails = TextChangedEvent("", 0, 0, 0)

    private var onSelectionChangedListener: OnSelectionChangedListener? = null

    private val selectedStyles = ArrayList<TextFormat>()

    private var isNewStyleSelected = false

    lateinit var history: History

    data class CarryOverSpan(val span: AztecInlineSpan, val start: Int, val end: Int)

    val carryOverSpans = ArrayList<CarryOverSpan>()

    interface OnSelectionChangedListener {
        fun onSelectionChanged(selStart: Int, selEnd: Int)
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

        val array = context.obtainStyledAttributes(attrs, R.styleable.AztecText)
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
        bulletColor = array.getColor(R.styleable.AztecText_bulletColor, bulletColor)
        bulletPadding = array.getDimensionPixelSize(R.styleable.AztecText_bulletPadding, bulletPadding)
        bulletMargin = array.getDimensionPixelSize(R.styleable.AztecText_bulletMargin, bulletMargin)
        bulletWidth = array.getDimensionPixelSize(R.styleable.AztecText_bulletWidth, bulletWidth)
        historyEnable = array.getBoolean(R.styleable.AztecText_historyEnable, historyEnable)
        historySize = array.getInt(R.styleable.AztecText_historySize, historySize)
        linkColor = array.getColor(R.styleable.AztecText_linkColor, linkColor)
        linkUnderline = array.getBoolean(R.styleable.AztecText_linkUnderline, linkUnderline)
        quoteBackground = array.getColor(R.styleable.AztecText_quoteBackground, quoteBackground)
        quoteColor = array.getColor(R.styleable.AztecText_quoteColor, quoteColor)
        quotePadding = array.getDimensionPixelSize(R.styleable.AztecText_quotePadding, quotePadding)
        quoteMargin = array.getDimensionPixelSize(R.styleable.AztecText_quoteMargin, quoteMargin)
        quoteWidth = array.getDimensionPixelSize(R.styleable.AztecText_quoteWidth, quoteWidth)
        array.recycle()

        if (historyEnable && historySize <= 0) {
            throw IllegalArgumentException("historySize must > 0")
        }

        history = History(historyEnable, historySize)

        // triggers ClickableSpan onClick() events
        movementMethod = EnhancedMovementMethod
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        addTextChangedListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeTextChangedListener(this)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        val customState = savedState.state
        val array = ArrayList(customState.getStringArrayList("historyList"))
        val list = LinkedList<String>()

        for (item in array) {
            list.add(item)
        }

        history.historyList = list
        history.historyCursor = customState.getInt("historyCursor")
        history.inputLast = customState.getString("inputLast")
        visibility = customState.getInt("visibility")
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        val bundle = Bundle()
        bundle.putStringArrayList("historyList", ArrayList<String>(history.historyList))
        bundle.putInt("historyCursor", history.historyCursor)
        bundle.putString("inputLast", history.inputLast)
        bundle.putInt("visibility", visibility)
        savedState.state = bundle
        return savedState
    }

    internal class SavedState : BaseSavedState {
        var state: Bundle = Bundle()

        constructor(superState: Parcelable) : super(superState) {
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeBundle(state)
        }
    }

    fun setSelectedStyles(styles: ArrayList<TextFormat>) {
        isNewStyleSelected = true
        selectedStyles?.clear()
        selectedStyles?.addAll(styles)
    }

    fun setOnSelectionChangedListener(onSelectionChangedListener: OnSelectionChangedListener) {
        this.onSelectionChangedListener = onSelectionChangedListener
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        onSelectionChangedListener?.onSelectionChanged(selStart, selEnd)

        setSelectedStyles(getAppliedStyles(if (selStart > 0 && !isTextSelected()) selStart - 1 else selStart, selEnd))

    }

    // Inline Styles ===================================================================================

    private fun bold(valid: Boolean) {
        if (valid) {
            applyInlineStyle(TextFormat.FORMAT_BOLD, selectionStart, selectionEnd)
        } else {
            removeInlineStyle(TextFormat.FORMAT_BOLD, selectionStart, selectionEnd)
        }
    }

    private fun italic(valid: Boolean) {
        if (valid) {
            applyInlineStyle(TextFormat.FORMAT_ITALIC, selectionStart, selectionEnd)
        } else {
            removeInlineStyle(TextFormat.FORMAT_ITALIC, selectionStart, selectionEnd)
        }
    }

    fun underline(valid: Boolean) {
        if (valid) {
            applyInlineStyle(TextFormat.FORMAT_UNDERLINED, selectionStart, selectionEnd)
        } else {
            removeInlineStyle(TextFormat.FORMAT_UNDERLINED, selectionEnd, selectionEnd)
        }
    }

    fun strikethrough(valid: Boolean) {
        if (valid) {
            applyInlineStyle(TextFormat.FORMAT_STRIKETHROUGH, selectionStart, selectionEnd)
        } else {
            removeInlineStyle(TextFormat.FORMAT_STRIKETHROUGH, selectionStart, selectionEnd)
        }
    }

    fun isSameInlineSpanType(firstSpan: AztecInlineSpan, secondSpan: AztecInlineSpan): Boolean {
        if (firstSpan.javaClass.equals(secondSpan.javaClass)) {
            //special check for StyleSpan
            if (firstSpan is StyleSpan && secondSpan is StyleSpan) {
                return firstSpan.style == secondSpan.style
            }else{
                return true
            }

        }

        return false
    }

    //TODO: Check if there is more efficient way to tidy spans
    private fun joinStyleSpans(start: Int, end: Int) {
        //joins spans on the left
        if (start > 1) {
            val spansInSelection = editableText.getSpans(start, end, AztecInlineSpan::class.java)

            val spansBeforeSelection = editableText.getSpans(start - 1, start, AztecInlineSpan::class.java)
            spansInSelection.forEach { innerSpan ->
                val inSelectionSpanEnd = editableText.getSpanEnd(innerSpan)

                spansBeforeSelection.forEach { outerSpan ->
                    val outerSpanStart = editableText.getSpanStart(outerSpan)

                    if (isSameInlineSpanType(innerSpan, outerSpan)) {
                        editableText.removeSpan(outerSpan)
                        editableText.setSpan(innerSpan, outerSpanStart, inSelectionSpanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
                    }

                }
            }
        }

        //joins spans on the right
        if (length() > end) {
            val spansInSelection = editableText.getSpans(start, end, AztecInlineSpan::class.java)
            val spansAfterSelection = editableText.getSpans(end, end + 1, AztecInlineSpan::class.java)
            spansInSelection.forEach { innerSpan ->
                val inSelectionSpanStart = editableText.getSpanStart(innerSpan)

                spansAfterSelection.forEach { outerSpan ->
                    val outerSpanEnd = editableText.getSpanEnd(outerSpan)

                    if (isSameInlineSpanType(innerSpan, outerSpan)) {
                        editableText.removeSpan(outerSpan)
                        editableText.setSpan(innerSpan, inSelectionSpanStart, outerSpanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
                    }
                }
            }
        }


        //joins spans withing selected text
        val spansInSelection = editableText.getSpans(start, end, AztecInlineSpan::class.java)
        val spansToUse = editableText.getSpans(start, end, AztecInlineSpan::class.java)

        spansInSelection.forEach { appliedSpan ->

            val spanStart = editableText.getSpanStart(appliedSpan)
            val spanEnd = editableText.getSpanEnd(appliedSpan)

            var neighbourSpan: AztecInlineSpan? = null

            spansToUse.forEach inner@ {
                val aSpanStart = editableText.getSpanStart(it)
                val aSpanEnd = editableText.getSpanEnd(it)
                if (isSameInlineSpanType(it, appliedSpan)) {
                    if (aSpanStart == spanEnd || aSpanEnd == spanStart) {
                        neighbourSpan = it
                        return@inner
                    }

                }
            }

            if (neighbourSpan != null) {
                val neighbourSpanStart = editableText.getSpanStart(neighbourSpan)
                val neighbourSpanEnd = editableText.getSpanEnd(neighbourSpan)

                if (neighbourSpanStart == -1 || neighbourSpanEnd == -1)
                    return@forEach

                //span we want to join is on the left
                if (spanStart == neighbourSpanEnd) {
                    editableText.setSpan(appliedSpan, neighbourSpanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
                } else if (spanEnd == neighbourSpanStart) {
                    editableText.setSpan(appliedSpan, spanStart, neighbourSpanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
                }

                editableText.removeSpan(neighbourSpan)
            }
        }
    }

    private fun applyInlineStyle(textFormat: TextFormat, start: Int, end: Int) {
        val spanToApply = makeInlineSpan(textFormat)

        if (start >= end) {
            return
        }

        var precedingSpan: AztecInlineSpan? = null
        var followingSpan: AztecInlineSpan? = null

        if (start >= 1) {
            val previousSpans = editableText.getSpans(start - 1, start, AztecInlineSpan::class.java)
            previousSpans.forEach {
                if (isSameInlineSpanType(it, spanToApply)) {
                    precedingSpan = it
                    return@forEach
                }
            }

            if (precedingSpan != null) {
                val spanStart = editableText.getSpanStart(precedingSpan)
                val spanEnd = editableText.getSpanEnd(precedingSpan)

                if (spanEnd > start) {
                    return@applyInlineStyle  //we are adding text inside span - no need to do anything special
                } else {
                    editableText.setSpan(precedingSpan, spanStart, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
                }

            }
        }

        if (length() > end) {
            val nextSpans = editableText.getSpans(end, end + 1, AztecInlineSpan::class.java)
            nextSpans.forEach {
                if (isSameInlineSpanType(it, spanToApply)) {
                    followingSpan = it
                    return@forEach
                }
            }

            if (followingSpan != null) {
                val spanEnd = editableText.getSpanEnd(followingSpan)
                editableText.setSpan(followingSpan, start, spanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
            }
        }

        if (precedingSpan == null && followingSpan == null) {
            var existingSpanOfSameStyle: AztecInlineSpan? = null

            val spans = editableText.getSpans(start, end, AztecInlineSpan::class.java)
            spans.forEach {
                if (isSameInlineSpanType(it, spanToApply)) {
                    existingSpanOfSameStyle = it
                    return@forEach
                }
            }

            //if we already have same span within selection - reuse it by changing it's bounds
            if (existingSpanOfSameStyle != null) {
                editableText.removeSpan(existingSpanOfSameStyle)
                editableText.setSpan(existingSpanOfSameStyle, start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
            } else {
                editableText.setSpan(spanToApply, start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
            }

        }

        joinStyleSpans(start, end)
    }

    fun removeInlineStyle(textFormat: TextFormat, start: Int, end: Int) {
        //for convenience sake we are initializing the span of same type we are planing to remove
        val spanToRemove = makeInlineSpan(textFormat)

        if (start >= end) {
            return
        }


        val spans = editableText.getSpans(start, end, AztecInlineSpan::class.java)
        val list = ArrayList<AztecPart>()

        spans.forEach {
            if (isSameInlineSpanType(it, spanToRemove)) {
                list.add(AztecPart(editableText.getSpanStart(it), editableText.getSpanEnd(it)))
                editableText.removeSpan(it)
            }
        }

        list.forEach {
            if (it.isValid) {
                if (it.start < start) {
                    applyInlineStyle(textFormat, it.start, start)
                }
                if (it.end > end) {
                    applyInlineStyle(textFormat, end, it.end)
                }
            }
        }

        joinStyleSpans(start, end)
    }

    //TODO: Come up with a better way to init spans and get their classes (all the "make" methods)
    fun makeBlockSpan(textFormat: TextFormat, attrs: String? = null): AztecBlockSpan {
        when (textFormat) {
            TextFormat.FORMAT_ORDERED_LIST -> return AztecOrderedListSpan(bulletColor, bulletMargin, bulletWidth, bulletPadding, attrs)
            TextFormat.FORMAT_UNORDERED_LIST -> return AztecUnorderedListSpan(bulletColor, bulletMargin, bulletWidth, bulletPadding, attrs)
            TextFormat.FORMAT_QUOTE -> return AztecQuoteSpan(quoteBackground, quoteColor, quoteMargin, quoteWidth, quotePadding, attrs)
            else -> return AztecOrderedListSpan(bulletColor, bulletMargin, bulletWidth, bulletPadding)
        }
    }


    fun makeBlockSpan(spanType: Class<AztecBlockSpan>, attrs: String? = null): LeadingMarginSpan {
        when (spanType) {
            AztecOrderedListSpan::class.java -> return AztecOrderedListSpan(bulletColor, bulletMargin, bulletWidth, bulletPadding, attrs)
            AztecUnorderedListSpan::class.java -> return AztecUnorderedListSpan(bulletColor, bulletMargin, bulletWidth, bulletPadding, attrs)
            AztecQuoteSpan::class.java -> return AztecQuoteSpan(quoteBackground, quoteColor, quoteMargin, quoteWidth, quotePadding, attrs)
            else -> return AztecOrderedListSpan(bulletColor, bulletMargin, bulletWidth, bulletPadding)
        }
    }

    fun makeInlineSpan(textFormat: TextFormat): AztecInlineSpan {
        when (textFormat) {
            TextFormat.FORMAT_HEADING_1 -> return AztecHeadingSpan(Heading.H1)
            TextFormat.FORMAT_HEADING_2 -> return AztecHeadingSpan(Heading.H2)
            TextFormat.FORMAT_HEADING_3 -> return AztecHeadingSpan(Heading.H3)
            TextFormat.FORMAT_HEADING_4 -> return AztecHeadingSpan(Heading.H4)
            TextFormat.FORMAT_HEADING_5 -> return AztecHeadingSpan(Heading.H5)
            TextFormat.FORMAT_HEADING_6 -> return AztecHeadingSpan(Heading.H6)
            TextFormat.FORMAT_BOLD -> return AztecStyleSpan(Typeface.BOLD)
            TextFormat.FORMAT_ITALIC -> return AztecStyleSpan(Typeface.ITALIC)
            TextFormat.FORMAT_STRIKETHROUGH -> return AztecStrikethroughSpan()
            TextFormat.FORMAT_UNDERLINED -> return AztecUnderlineSpan()
            else -> return AztecStyleSpan(Typeface.NORMAL)
        }
    }

    fun containsInlineStyle(textFormat: TextFormat, start: Int, end: Int): Boolean {
        val spanToCheck = makeInlineSpan(textFormat)

        if (start > end) {
            return false
        }

        if (start == end) {
            if (start - 1 < 0 || start + 1 > editableText.length) {
                return false
            } else {
                val before = editableText.getSpans(start - 1, start, AztecInlineSpan::class.java)
                        .filter { it -> isSameInlineSpanType(it, spanToCheck) }
                val after = editableText.getSpans(start, start + 1, AztecInlineSpan::class.java)
                        .filter { isSameInlineSpanType(it, spanToCheck) }
                return before.size > 0 && after.size > 0 && isSameInlineSpanType(before[0], after[0])
            }
        } else {
            val builder = StringBuilder()

            // Make sure no duplicate characters be added
            for (i in start..end - 1) {
                val spans = editableText.getSpans(i, i + 1, AztecInlineSpan::class.java)
                for (span in spans) {
                    if (isSameInlineSpanType(span, spanToCheck)) {
                        builder.append(editableText.subSequence(i, i + 1).toString())
                        break
                    }
                }
            }

            return editableText.subSequence(start, end).toString() == builder.toString()
        }

    }

    // HeadingSpan =================================================================================

    fun heading(format: Boolean, textFormat: TextFormat) {
        headingClear()

        if (format) {
            headingFormat(textFormat)
        }
    }

    private fun headingClear() {
        val lines = TextUtils.split(editableText.toString(), "\n")

        for (i in lines.indices) {
            if (!containsHeading(i)) {
                continue
            }

            var lineStart = 0

            for (j in 0..i - 1) {
                lineStart += lines[j].length + 1
            }

            val lineEnd = lineStart + lines[i].length

            if (lineStart >= lineEnd) {
                continue
            }

            var headingStart = 0
            var headingEnd = 0

            if ((lineStart <= selectionStart && selectionEnd <= lineEnd) ||
                    (lineStart >= selectionStart && selectionEnd >= lineEnd) ||
                    (lineStart <= selectionStart && selectionEnd >= lineEnd && selectionStart <= lineEnd) ||
                    (lineStart >= selectionStart && selectionEnd <= lineEnd && selectionEnd >= lineStart)) {
                headingStart = lineStart
                headingEnd = lineEnd
            }

            if (headingStart < headingEnd) {
                val spans = editableText.getSpans(headingStart, headingEnd, AztecHeadingSpan::class.java)

                for (span in spans) {
                    editableText.removeSpan(span)
                }
            }
        }

        refreshText()
    }

    private fun headingFormat(textFormat: TextFormat) {
        val lines = TextUtils.split(editableText.toString(), "\n")

        for (i in lines.indices) {
            var lineStart = 0

            for (j in 0..i - 1) {
                lineStart += lines[j].length + 1
            }

            val lineEnd = lineStart + lines[i].length

            if (lineStart >= lineEnd) {
                continue
            }

            var headingStart = 0
            var headingEnd = 0

            if ((lineStart <= selectionStart && selectionEnd <= lineEnd) ||
                    (lineStart >= selectionStart && selectionEnd >= lineEnd) ||
                    (lineStart <= selectionStart && selectionEnd >= lineEnd && selectionStart <= lineEnd) ||
                    (lineStart >= selectionStart && selectionEnd <= lineEnd && selectionEnd >= lineStart)) {
                headingStart = lineStart
                headingEnd = lineEnd
            }

            if (headingStart < headingEnd) {
                when (textFormat) {
                    TextFormat.FORMAT_HEADING_1 ->
                        editableText.setSpan(AztecHeadingSpan(Heading.H1), headingStart, headingEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    TextFormat.FORMAT_HEADING_2 ->
                        editableText.setSpan(AztecHeadingSpan(Heading.H2), headingStart, headingEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    TextFormat.FORMAT_HEADING_3 ->
                        editableText.setSpan(AztecHeadingSpan(Heading.H3), headingStart, headingEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    TextFormat.FORMAT_HEADING_4 ->
                        editableText.setSpan(AztecHeadingSpan(Heading.H4), headingStart, headingEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    TextFormat.FORMAT_HEADING_5 ->
                        editableText.setSpan(AztecHeadingSpan(Heading.H5), headingStart, headingEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    TextFormat.FORMAT_HEADING_6 ->
                        editableText.setSpan(AztecHeadingSpan(Heading.H6), headingStart, headingEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    else -> {
                    }
                }
            }
        }

        refreshText()
    }

    private fun containsHeading(textFormat: TextFormat, selStart: Int, selEnd: Int): Boolean {
        val lines = TextUtils.split(editableText.toString(), "\n")
        val list = ArrayList<Int>()

        for (i in lines.indices) {
            var lineStart = 0
            for (j in 0..i - 1) {
                lineStart += lines[j].length + 1
            }

            val lineEnd = lineStart + lines[i].length
            if (lineStart >= lineEnd) {
                continue
            }

            if (lineStart <= selStart && selEnd <= lineEnd) {
                list.add(i)
            } else if (selStart <= lineStart && lineEnd <= selEnd) {
                list.add(i)
            }
        }

        if (list.isEmpty()) return false

        for (i in list) {
            if (!containHeadingType(textFormat, i)) {
                return false
            }
        }

        return true
    }

    private fun containsHeading(index: Int): Boolean {
        val lines = TextUtils.split(editableText.toString(), "\n")

        if (index < 0 || index >= lines.size) {
            return false
        }

        var start = 0

        for (i in 0..index - 1) {
            start += lines[i].length + 1
        }

        val end = start + lines[index].length

        if (start >= end) {
            return false
        }

        val spans = editableText.getSpans(start, end, AztecHeadingSpan::class.java)
        return spans.size > 0
    }

    private fun containHeadingType(textFormat: TextFormat, index: Int): Boolean {
        val lines = TextUtils.split(editableText.toString(), "\n")

        if (index < 0 || index >= lines.size) {
            return false
        }

        var start = 0

        for (i in 0..index - 1) {
            start += lines[i].length + 1
        }

        val end = start + lines[index].length

        if (start >= end) {
            return false
        }

        val spans = editableText.getSpans(start, end, AztecHeadingSpan::class.java)

        for (span in spans) {
            when (textFormat) {
                TextFormat.FORMAT_HEADING_1 ->
                    return span.heading.equals(AztecHeadingSpan.Heading.H1)
                TextFormat.FORMAT_HEADING_2 ->
                    return span.heading.equals(AztecHeadingSpan.Heading.H2)
                TextFormat.FORMAT_HEADING_3 ->
                    return span.heading.equals(AztecHeadingSpan.Heading.H3)
                TextFormat.FORMAT_HEADING_4 ->
                    return span.heading.equals(AztecHeadingSpan.Heading.H4)
                TextFormat.FORMAT_HEADING_5 ->
                    return span.heading.equals(AztecHeadingSpan.Heading.H5)
                TextFormat.FORMAT_HEADING_6 ->
                    return span.heading.equals(AztecHeadingSpan.Heading.H6)
                else -> return false
            }
        }

        return false
    }

    // BulletSpan ==================================================================================

    fun orderedListValid(valid: Boolean) {
        if (valid) {
            if (containsList(TextFormat.FORMAT_UNORDERED_LIST, selectionStart, selectionEnd)) {
                switchListType(TextFormat.FORMAT_ORDERED_LIST)
            } else {
                applyBlockStyle(TextFormat.FORMAT_ORDERED_LIST)
            }
        } else {
            removeBlockStyle(TextFormat.FORMAT_ORDERED_LIST)
        }
    }

    fun unorderedListValid(valid: Boolean) {
        if (valid) {
            if (containsList(TextFormat.FORMAT_ORDERED_LIST, selectionStart, selectionEnd)) {
                switchListType(TextFormat.FORMAT_UNORDERED_LIST)
            } else {
                applyBlockStyle(TextFormat.FORMAT_UNORDERED_LIST)
            }
        } else {
            removeBlockStyle(TextFormat.FORMAT_UNORDERED_LIST)
        }
    }

    private fun switchListType(listTypeToSwitchTo: TextFormat, start: Int = selectionStart, end: Int = selectionEnd) {
        val spans = editableText.getSpans(start, end, AztecListSpan::class.java)

        if (spans.isEmpty()) return

        val existingListSpan = spans[0]

        val spanStart = editableText.getSpanStart(existingListSpan)
        val spanEnd = editableText.getSpanEnd(existingListSpan)
        val spanFlags = editableText.getSpanFlags(existingListSpan)
        editableText.removeSpan(existingListSpan)

        editableText.setSpan(makeBlockSpan(listTypeToSwitchTo), spanStart, spanEnd, spanFlags)
        onSelectionChanged(start, end)
    }

    private fun applyBlockStyle(blockElementType: TextFormat, start: Int = selectionStart, end: Int = selectionEnd) {
        if (start != end) {
            val selectedText = editableText.substring(start + 1..end - 1)

            //multiline text selected
            if (selectedText.indexOf("\n") != -1) {
                val indexOfFirstLineBreak = editableText.indexOf("\n", end)

                val endOfBlock = if (indexOfFirstLineBreak != -1) indexOfFirstLineBreak else editableText.length
                val startOfBlock = editableText.lastIndexOf("\n", start)

                val selectedLines = editableText.subSequence(startOfBlock + 1..endOfBlock - 1) as Editable

                var numberOfLinesWithSpanApplied = 0
                var numberOfLines = 0

                val lines = TextUtils.split(selectedLines.toString(), "\n")

                for (i in lines.indices) {
                    numberOfLines++
                    if (containsList(blockElementType, i, selectedLines)) {
                        numberOfLinesWithSpanApplied++
                    }
                }

                if (numberOfLines == numberOfLinesWithSpanApplied) {
                    removeBlockStyle(blockElementType)
                } else {
                    editableText.setSpan(makeBlockSpan(blockElementType), startOfBlock + 1, endOfBlock, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
                }
            }

        } else {
            val boundsOfSelectedText = getSelectedTextBounds(editableText, start, end)

            val startOfLine = boundsOfSelectedText.start
            var endOfLine = boundsOfSelectedText.endInclusive

            val isEmptyLine = startOfLine == endOfLine

            if (isEmptyLine) {
                disableTextChangedListener()
                editableText.insert(startOfLine, "\u200B")
                endOfLine += 1
            }

            val spanToApply = makeBlockSpan(blockElementType)

            var startOfBlock: Int = startOfLine
            var endOfBlock: Int = endOfLine


            if (startOfLine != 0) {
                val spansOnPreviousLine = editableText.getSpans(startOfLine - 1, startOfLine - 1, spanToApply.javaClass)
                if (!spansOnPreviousLine.isEmpty()) {
                    startOfBlock = editableText.getSpanStart(spansOnPreviousLine[0])
                    editableText.removeSpan(spansOnPreviousLine[0])
                }
            }

            if (endOfLine != editableText.length) {
                val spanOnNextLine = editableText.getSpans(endOfLine + 1, endOfLine + 1, spanToApply.javaClass)
                if (!spanOnNextLine.isEmpty()) {
                    endOfBlock = editableText.getSpanEnd(spanOnNextLine[0])
                    editableText.removeSpan(spanOnNextLine[0])
                }
            }

            editableText.setSpan(spanToApply, startOfBlock, endOfBlock, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)

            //if the line was empty trigger onSelectionChanged manually to update toolbar buttons status
            if (isEmptyLine) {
                onSelectionChanged(startOfLine, endOfLine)
            }
        }
    }

    private fun removeBlockStyle(textFormat: TextFormat) {
        removeBlockStyle(selectionStart, selectionEnd, makeBlockSpan(textFormat).javaClass)
    }

    private fun removeBlockStyle(start: Int = selectionStart, end: Int = selectionEnd,
                                 spanType: Class<AztecBlockSpan> = AztecBlockSpan::class.java, ignoreLineBounds: Boolean = false) {
        val spans = editableText.getSpans(start, end, spanType)
        spans.forEach {

            val spanStart = editableText.getSpanStart(it)
            var spanEnd = editableText.getSpanEnd(it)

            //if splitting block set a range that would be excluded from it
            val boundsOfSelectedText = if (ignoreLineBounds) IntRange(start, end) else getSelectedTextBounds(editableText, start, end)

            val startOfLine = boundsOfSelectedText.start
            val endOfLine = boundsOfSelectedText.endInclusive

            val spanPrecedesLine = spanStart < startOfLine
            val spanExtendsBeyondLine = endOfLine < spanEnd

            //remove the span from all the selected lines
            editableText.removeSpan(it)


            //reapply span top "top" and "bottom"
            if (spanPrecedesLine) {
                editableText.setSpan(makeBlockSpan(it.javaClass), spanStart, startOfLine - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            if (spanExtendsBeyondLine) {
                if (editableText[endOfLine] == '\n' && !ignoreLineBounds) {
                    disableTextChangedListener()
                    editableText.delete(endOfLine, endOfLine + 1)
                    spanEnd--
                }

                editableText.setSpan(makeBlockSpan(it.javaClass), endOfLine, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

        }


    }

    private fun containsList(textFormat: TextFormat, selStart: Int, selEnd: Int): Boolean {
        val lines = TextUtils.split(editableText.toString(), "\n")
        val list = ArrayList<Int>()


        for (i in lines.indices) {
            var lineStart = 0
            for (j in 0..i - 1) {
                lineStart += lines[j].length + 1
            }

            val lineEnd = lineStart + lines[i].length
            if (lineStart > lineEnd) {
                continue
            }

            if (lineStart <= selStart && selEnd <= lineEnd) {
                list.add(i)
            } else if (selStart <= lineStart && lineEnd <= selEnd) {
                list.add(i)
            }
        }

        if (list.isEmpty()) return false

        for (i in list) {
            if (!containsList(textFormat, i)) {
                return false
            }
        }

        return true
    }

    private fun containsList(textFormat: TextFormat, index: Int, text: Editable = editableText): Boolean {
        val lines = TextUtils.split(text.toString(), "\n")
        if (index < 0 || index >= lines.size) {
            return false
        }

        var start = 0
        for (i in 0..index - 1) {
            start += lines[i].length + 1
        }

        val end = start + lines[index].length
        if (start > end) {
            return false
        }

        val spans = editableText.getSpans(start, end, makeBlockSpan(textFormat).javaClass)
        return spans.size > 0
    }

    // QuoteSpan ===================================================================================

    fun quote(valid: Boolean) {
        if (valid) {
            applyBlockStyle(TextFormat.FORMAT_QUOTE)
        } else {
            removeBlockStyle(TextFormat.FORMAT_QUOTE)
        }
    }

    private fun containQuote(selStart: Int, selEnd: Int): Boolean {
        val lines = TextUtils.split(editableText.toString(), "\n")
        val list = ArrayList<Int>()

        for (i in lines.indices) {
            var lineStart = 0
            for (j in 0..i - 1) {
                lineStart += lines[j].length + 1
            }

            val lineEnd = lineStart + lines[i].length
            if (lineStart >= lineEnd) {
                continue
            }

            if (lineStart <= selStart && selEnd <= lineEnd) {
                list.add(i)
            } else if (selStart <= lineStart && lineEnd <= selEnd) {
                list.add(i)
            }
        }

        if (list.isEmpty()) return false

        for (i in list) {
            if (!containQuote(i)) {
                return false
            }
        }

        return true
    }

    private fun containQuote(index: Int): Boolean {
        val lines = TextUtils.split(editableText.toString(), "\n")
        if (index < 0 || index >= lines.size) {
            return false
        }

        var start = 0
        for (i in 0..index - 1) {
            start += lines[i].length + 1
        }

        val end = start + lines[index].length
        if (start >= end) {
            return false
        }

        val spans = editableText.getSpans(start, end, AztecQuoteSpan::class.java)
        return spans.size > 0
    }

    fun getSelectedText(): String {
        if (selectionStart == -1 || selectionEnd == -1) return ""
        return editableText.substring(selectionStart, selectionEnd)
    }

    fun isUrlSelected(): Boolean {
        val urlSpans = editableText.getSpans(selectionStart, selectionEnd, AztecURLSpan::class.java)
        return !urlSpans.isEmpty()
    }

    fun getSelectedUrlWithAnchor(): Pair<String, String> {
        val url: String
        var anchor: String

        if (!isUrlSelected()) {
            val clipboardUrl = getUrlFromClipboard(context)

            url = if (TextUtils.isEmpty(clipboardUrl)) "" else clipboardUrl
            anchor = if (selectionStart == selectionEnd) "" else getSelectedText()

        } else {
            val urlSpans = editableText.getSpans(selectionStart, selectionEnd, AztecURLSpan::class.java)
            val urlSpan = urlSpans[0]

            val spanStart = editableText.getSpanStart(urlSpan)
            val spanEnd = editableText.getSpanEnd(urlSpan)

            if (selectionStart < spanStart || selectionEnd > spanEnd) {
                //looks like some text that is not part of the url was included in selection
                anchor = getSelectedText()
                url = ""
            } else {
                anchor = editableText.substring(spanStart, spanEnd)
                url = urlSpan.url
            }

            if (anchor == url) {
                anchor = ""
            }
        }

        return Pair(url, anchor)

    }

    /**
     * Checks the Clipboard for text that matches the [Patterns.WEB_URL] pattern.
     * @return the URL text in the clipboard, if it exists; otherwise null
     */
    fun getUrlFromClipboard(context: Context?): String {
        if (context == null) return ""
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager

        val data = clipboard.primaryClip
        if (data == null || data.itemCount <= 0) return ""
        val clipText = data.getItemAt(0).text.toString()
        return if (Patterns.WEB_URL.matcher(clipText).matches()) clipText else ""
    }

    fun getUrlSpanBounds(): Pair<Int, Int> {
        val urlSpans = editableText.getSpans(selectionStart, selectionEnd, AztecURLSpan::class.java)

        val spanStart = text.getSpanStart(urlSpans[0])
        val spanEnd = text.getSpanEnd(urlSpans[0])

        if (selectionStart < spanStart || selectionEnd > spanEnd) {
            //looks like some text that is not part of the url was included in selection
            return Pair(selectionStart, selectionEnd)
        }
        return Pair(spanStart, spanEnd)
    }

    fun link(url: String, anchor: String) {
        if (TextUtils.isEmpty(url) && isUrlSelected()) {
            removeLink()
        } else if (isUrlSelected()) {
            editLink(url, anchor, getUrlSpanBounds().first, getUrlSpanBounds().second)
        } else {
            addLink(url, anchor, selectionStart, selectionEnd)
        }
    }

    fun addLink(link: String, anchor: String, start: Int, end: Int) {
        val cleanLink = link.trim()
        val newEnd: Int

        val actuallAnchor = if (TextUtils.isEmpty(anchor)) cleanLink else anchor

        if (start == end) {
            //insert anchor
            text.insert(start, actuallAnchor)
            newEnd = start + actuallAnchor.length
        } else {
            //apply span to text
            if (!getSelectedText().equals(anchor)) {
                text.replace(start, end, actuallAnchor)
            }
            newEnd = start + actuallAnchor.length
        }

        linkValid(link, start, newEnd)
    }

    fun editLink(link: String, anchor: String?, start: Int = selectionStart, end: Int = selectionEnd) {
        val cleanLink = link.trim()
        val newEnd: Int

        if (TextUtils.isEmpty(anchor)) {
            text.replace(start, end, cleanLink)
            newEnd = start + cleanLink.length
        } else {
            //if the anchor was not changed do nothing to preserve original style of text
            if (!getSelectedText().equals(anchor)) {
                text.replace(start, end, anchor)
            }
            newEnd = start + anchor!!.length
        }

        var attributes = getAttributes(end, start)
        attributes = attributes?.replace("href=[\"'].*[\"']".toRegex(), "href=\"$cleanLink\"")

        linkValid(cleanLink, start, newEnd, attributes)
    }

    private fun getAttributes(end: Int, start: Int): String? {
        val urlSpans = editableText.getSpans(start, end, AztecURLSpan::class.java)
        var attributes: String? = null
        if (urlSpans != null && urlSpans.size > 0) {
            attributes = urlSpans[0].attributes
        }
        return attributes
    }

    fun removeLink() {
        val urlSpanBounds = getUrlSpanBounds()

        linkInvalid(urlSpanBounds.first, urlSpanBounds.second)
        onSelectionChanged(urlSpanBounds.first, urlSpanBounds.second)
    }

    private fun linkValid(link: String, start: Int, end: Int, attributes: String? = null) {
        if (start >= end) {
            return
        }

        linkInvalid(start, end)
        editableText.setSpan(AztecURLSpan(link, linkColor, linkUnderline, attributes), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        onSelectionChanged(end, end)
    }

    private fun linkInvalid(start: Int, end: Int) {
        if (start >= end) {
            return
        }

        val spans = editableText.getSpans(start, end, AztecURLSpan::class.java)
        for (span in spans) {
            editableText.removeSpan(span)
        }
    }

    private fun containLink(start: Int, end: Int): Boolean {
        if (start > end) {
            return false
        }

        if (start == end) {
            if (start - 1 < 0 || start + 1 > editableText.length) {
                return false
            } else {
                val before = editableText.getSpans(start - 1, start, AztecURLSpan::class.java)
                val after = editableText.getSpans(start, start + 1, AztecURLSpan::class.java)
                return before.size > 0 && after.size > 0
            }
        } else {
            val builder = StringBuilder()

            for (i in start..end - 1) {
                if (editableText.getSpans(i, i + 1, AztecURLSpan::class.java).size > 0) {
                    builder.append(editableText.subSequence(i, i + 1).toString())
                }
            }

            return editableText.subSequence(start, end).toString() == builder.toString()
        }
    }

    fun applyComment(comment: AztecCommentSpan.Comment) {
        //check if we add a comment into a block element, at the end of the line, but not at the end of last line
        var applyingOnTheEndOfBlockLine = false
        editableText.getSpans(selectionStart, selectionEnd, AztecBlockSpan::class.java).forEach {
            if (editableText.getSpanEnd(it) > selectionEnd && editableText[selectionEnd] == '\n') {
                applyingOnTheEndOfBlockLine = true
                return@forEach
            }
        }

        val commentStartIndex = selectionStart + 1
        val commentEndIndex = selectionStart + comment.html.length + 1

        disableTextChangedListener()
        editableText.replace(selectionStart, selectionEnd, "\n" + comment.html + if (applyingOnTheEndOfBlockLine) "" else "\n")

        removeBlockStylesFromRange(commentStartIndex, commentEndIndex + 1, true)
        removeHeadingStylesFromRange(commentStartIndex, commentEndIndex + 1)
        removeInlineStylesFromRange(commentStartIndex, commentEndIndex + 1)

        val span = AztecCommentSpan(
                context,
                when (comment) {
                    AztecCommentSpan.Comment.MORE -> resources.getDrawable(R.drawable.img_more)
                    AztecCommentSpan.Comment.PAGE -> resources.getDrawable(R.drawable.img_page)
                }
        )

        editableText.setSpan(
                span,
                commentStartIndex,
                commentEndIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        setSelection(commentEndIndex + 1)
    }

    fun getAppliedHeading(selectionStart: Int, selectionEnd: Int): TextFormat? {
        if (contains(TextFormat.FORMAT_HEADING_1, selectionStart, selectionEnd)) {
            return TextFormat.FORMAT_HEADING_1
        } else if (contains(TextFormat.FORMAT_HEADING_2, selectionStart, selectionEnd)) {
            return TextFormat.FORMAT_HEADING_2
        } else if (contains(TextFormat.FORMAT_HEADING_3, selectionStart, selectionEnd)) {
            return TextFormat.FORMAT_HEADING_3
        } else if (contains(TextFormat.FORMAT_HEADING_4, selectionStart, selectionEnd)) {
            return TextFormat.FORMAT_HEADING_4
        } else if (contains(TextFormat.FORMAT_HEADING_5, selectionStart, selectionEnd)) {
            return TextFormat.FORMAT_HEADING_5
        } else if (contains(TextFormat.FORMAT_HEADING_6, selectionStart, selectionEnd)) {
            return TextFormat.FORMAT_HEADING_6
        } else {
            return null
        }
    }

    fun getAppliedStyles(selectionStart: Int, selectionEnd: Int): ArrayList<TextFormat> {
        val styles = ArrayList<TextFormat>()
        TextFormat.values().forEach {
            if (contains(it, selectionStart, selectionEnd)) {
                styles.add(it)
            }
        }
        return styles
    }

    fun isEmpty(): Boolean {
        return text.isEmpty()
    }

    private fun formattingIsApplied(): Boolean {
        return !selectedStyles.isEmpty()
    }

    private fun formattingHasChanged(): Boolean {
        return isNewStyleSelected
    }

    private fun setFormattingChangesApplied() {
        isNewStyleSelected = false
    }

    private fun clearInlineStyles(start: Int, end: Int, ignoreSelectedStyles: Boolean) {
        getAppliedStyles(start, end).forEach {
            if (!selectedStyles.contains(it) || ignoreSelectedStyles) {
                when (it) {
                    TextFormat.FORMAT_HEADING_1,
                    TextFormat.FORMAT_HEADING_2,
                    TextFormat.FORMAT_HEADING_3,
                    TextFormat.FORMAT_HEADING_4,
                    TextFormat.FORMAT_HEADING_5,
                    TextFormat.FORMAT_HEADING_6 -> removeInlineStyle(it, start, end)
                    TextFormat.FORMAT_BOLD -> removeInlineStyle(it, start, end)
                    TextFormat.FORMAT_ITALIC -> removeInlineStyle(it, start, end)
                    TextFormat.FORMAT_STRIKETHROUGH -> removeInlineStyle(it, start, end)
                    else -> {
                        //do nothing
                    }
                }
            }
        }
    }

    fun isTextSelected(): Boolean {
        return selectionStart != selectionEnd
    }

    fun toggleFormatting(textFormat: TextFormat) {

        history.beforeTextChanged(toFormattedHtml())

        when (textFormat) {
            TextFormat.FORMAT_PARAGRAPH -> heading(false, textFormat)
            TextFormat.FORMAT_HEADING_1,
            TextFormat.FORMAT_HEADING_2,
            TextFormat.FORMAT_HEADING_3,
            TextFormat.FORMAT_HEADING_4,
            TextFormat.FORMAT_HEADING_5,
            TextFormat.FORMAT_HEADING_6 -> heading(true, textFormat)
            TextFormat.FORMAT_BOLD -> bold(!contains(TextFormat.FORMAT_BOLD))
            TextFormat.FORMAT_ITALIC -> italic(!contains(TextFormat.FORMAT_ITALIC))
            TextFormat.FORMAT_STRIKETHROUGH -> strikethrough(!contains(TextFormat.FORMAT_STRIKETHROUGH))
            TextFormat.FORMAT_UNORDERED_LIST -> unorderedListValid(!contains(TextFormat.FORMAT_UNORDERED_LIST))
            TextFormat.FORMAT_ORDERED_LIST -> orderedListValid(!contains(TextFormat.FORMAT_ORDERED_LIST))
            TextFormat.FORMAT_QUOTE -> quote(!contains(TextFormat.FORMAT_QUOTE))
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
            TextFormat.FORMAT_HEADING_6 -> return containsHeading(format, selStart, selEnd)
            TextFormat.FORMAT_BOLD -> return containsInlineStyle(TextFormat.FORMAT_BOLD, selStart, selEnd)
            TextFormat.FORMAT_ITALIC -> return containsInlineStyle(TextFormat.FORMAT_ITALIC, selStart, selEnd)
            TextFormat.FORMAT_UNDERLINED -> return containsInlineStyle(TextFormat.FORMAT_UNDERLINED, selStart, selEnd)
            TextFormat.FORMAT_STRIKETHROUGH -> return containsInlineStyle(TextFormat.FORMAT_STRIKETHROUGH, selStart, selEnd)
            TextFormat.FORMAT_UNORDERED_LIST -> return containsList(TextFormat.FORMAT_UNORDERED_LIST, selStart, selEnd)
            TextFormat.FORMAT_ORDERED_LIST -> return containsList(TextFormat.FORMAT_ORDERED_LIST, selStart, selEnd)
            TextFormat.FORMAT_QUOTE -> return containQuote(selectionStart, selectionEnd)
            TextFormat.FORMAT_LINK -> return containLink(selStart, selEnd)
            else -> return false
        }
    }


    fun carryOverInlineSpans(start: Int, count: Int, after: Int) {
        carryOverSpans.clear()

        val charsAdded = after - count
        if (charsAdded > 0 && count > 0) {
            editableText.getSpans(start, start + count, AztecInlineSpan::class.java).forEach {
                val spanStart = editableText.getSpanStart(it)
                val spanEnd = editableText.getSpanEnd(it)


                if ((spanStart == start || spanEnd == count + start) && spanEnd < after) {
                    editableText.removeSpan(it)
                    carryOverSpans.add(CarryOverSpan(it, spanStart, spanEnd))
                }
            }
        }
    }

    fun reapplyCarriedOverInlineSpans() {
        carryOverSpans?.forEach {
            editableText.setSpan(it.span, it.start, it.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        carryOverSpans?.clear()
    }

    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
        carryOverInlineSpans(start, count, after)

        if (!isTextChangedListenerDisabled()) {
            history.beforeTextChanged(toFormattedHtml())
        }
    }

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
        reapplyCarriedOverInlineSpans()
        textChangedEventDetails = TextChangedEvent(text, start, before, count)
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

        history.handleHistory(this)

        handleBlockStyling(text, textChangedEventDetails)
        handleInlineStyling(text, textChangedEventDetails)
    }

    fun removeLeadingStyle(text: Editable, spanClass: Class<*>) {
        text.getSpans(0, 0, spanClass).forEach {
            if (text.length >= 1) {
                text.setSpan(it, 0, text.getSpanEnd(it), text.getSpanFlags(it))
            } else {
                text.removeSpan(it)
            }
        }
    }

    fun handleInlineStyling(text: Editable, textChangedEvent: TextChangedEvent) {
        //because we use SPAN_INCLUSIVE_INCLUSIVE for inline styles
        //we need to make sure unselected styles are not applied
        clearInlineStyles(textChangedEvent.inputStart, textChangedEvent.inputEnd, textChangedEvent.isNewLine())

        //trailing styling
        if (!formattingHasChanged() || textChangedEvent.isNewLine()) return

        if (formattingIsApplied()) {
            for (item in selectedStyles) {
                when (item) {
                    TextFormat.FORMAT_HEADING_1,
                    TextFormat.FORMAT_HEADING_2,
                    TextFormat.FORMAT_HEADING_3,
                    TextFormat.FORMAT_HEADING_4,
                    TextFormat.FORMAT_HEADING_5,
                    TextFormat.FORMAT_HEADING_6 -> if (contains(item, textChangedEvent.inputStart, textChangedEvent.inputEnd)) {
                        applyInlineStyle(item, textChangedEvent.inputStart, textChangedEvent.inputEnd)
                    }
                    TextFormat.FORMAT_BOLD,
                    TextFormat.FORMAT_ITALIC,
                    TextFormat.FORMAT_STRIKETHROUGH -> if (!contains(item, textChangedEvent.inputStart, textChangedEvent.inputEnd)) {
                        applyInlineStyle(item, textChangedEvent.inputStart, textChangedEvent.inputEnd)
                    }
                    else -> {
                        //do nothing
                    }
                }
            }
        }

        setFormattingChangesApplied()
    }

    fun handleBlockStyling(text: Editable, textChangedEvent: TextChangedEvent) {
        // preserve the attributes on the previous list item when adding a new one
        if (textChangedEvent.isNewLine() && textChangedEvent.inputEnd < text.length && text[textChangedEvent.inputEnd] == '\n') {
            val spans = text.getSpans(textChangedEvent.inputEnd, textChangedEvent.inputEnd + 1, AztecListItemSpan::class.java)
            if (spans.size == 1) {
                text.setSpan(spans[0], textChangedEvent.inputStart, textChangedEvent.inputEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        val inputStart = textChangedEvent.inputStart

        val spanToClose = textChangedEvent.getBlockSpansToClose(text)
        spanToClose.forEach {
            var spanEnd = text.getSpanEnd(it)
            var spanStart = text.getSpanStart(it)

            if (spanEnd == spanStart) {
                editableText.removeSpan(it)
            } else if (spanEnd <= text.length) {
                //case for when we remove block element row from first line of EditText end the next line is empty
                if (inputStart == 0 && spanStart > 0 && text[spanStart] == '\n') {
                    spanEnd += 1
                    disableTextChangedListener()
                    text.insert(spanStart, "\u200B")
                } else
                //case for when we remove block element row from other lines of EditText end the next line is empty
                    if (text[spanStart] == '\n') {
                        spanStart += 1

                        if (text[spanStart] == '\n' && text.length >= spanEnd && text.length > spanStart) {
                            spanEnd += 1
                            disableTextChangedListener()
                            text.insert(spanStart, "\u200B")
                        }
                    }

                editableText.setSpan(it,
                        spanStart,
                        spanEnd,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        val spanToOpen = textChangedEvent.getBlockSpanToOpen(text)
        spanToOpen.forEach {
            val textLength = text.length

            var spanEnd = text.getSpanEnd(it)
            val spanStart = text.getSpanStart(it)

            if (inputStart < spanStart) {
                editableText.setSpan(it,
                        inputStart,
                        spanEnd,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else {
                val indexOfLineEnd = text.indexOf('\n', spanEnd - 1, true)

                if (indexOfLineEnd == spanEnd) {
                    spanEnd += textChangedEvent.count
                } else if (indexOfLineEnd == -1) {
                    spanEnd = text.length
                } else {
                    spanEnd = indexOfLineEnd
                }

                if (spanEnd <= textLength) {
                    editableText.setSpan(it,
                            text.getSpanStart(it),
                            spanEnd,
                            Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
                }
            }
        }

        if (textChangedEvent.isAfterZeroWidthJoiner() && !textChangedEvent.isNewLine()) {
            disableTextChangedListener()
            text.delete(inputStart - 1, inputStart)
        } else if (textChangedEvent.isAfterZeroWidthJoiner() && textChangedEvent.isNewLine()) {
            removeBlockStyle()
            disableTextChangedListener()

            if (inputStart == 1) {
                text.delete(inputStart - 1, inputStart + 1)
            } else {
                text.delete(inputStart - 2, inputStart)
            }

        } else if (!textChangedEvent.isAfterZeroWidthJoiner() && textChangedEvent.isNewLine()) {
            //Add ZWJ to the new line at the end of block spans
            val blockSpans = getText().getSpans(inputStart, inputStart, AztecBlockSpan::class.java)
            if (!blockSpans.isEmpty() && text.getSpanEnd(blockSpans[0]) == inputStart + 1) {
                disableTextChangedListener()
                text.insert(inputStart + 1, "\u200B")
            }
        }


    }

    fun getSelectedTextBounds(editable: Editable, selectionStart: Int, selectionEnd: Int): IntRange {
        val startOfLine: Int
        val endOfLine: Int

        val indexOfFirstLineBreak: Int
        val indexOfLastLineBreak = editable.indexOf("\n", selectionEnd)

        if (indexOfLastLineBreak > 0) {
            val characterBeforeLastLineBreak = editable[indexOfLastLineBreak - 1]
            if (characterBeforeLastLineBreak != '\n') {
                indexOfFirstLineBreak = editable.lastIndexOf("\n", selectionStart - 1) + 1
            } else {
                indexOfFirstLineBreak = editable.lastIndexOf("\n", selectionStart)
            }
        } else {
            if (indexOfLastLineBreak == -1) {
                indexOfFirstLineBreak = editable.lastIndexOf("\n", selectionStart) + 1
            } else {
                indexOfFirstLineBreak = editable.lastIndexOf("\n", selectionStart)
            }
        }


        startOfLine = if (indexOfFirstLineBreak != -1) indexOfFirstLineBreak else 0
        endOfLine = if (indexOfLastLineBreak != -1) indexOfLastLineBreak else editable.length

        return IntRange(startOfLine, endOfLine)
    }

    fun redo() {
        history.redo(this)
    }

    fun undo() {
        history.undo(this)
    }

// Helper ======================================================================================

    fun clearFormats() {
        setText(editableText.toString())
        setSelection(editableText.length)
    }

    fun hideSoftInput() {
        clearFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    fun showSoftInput() {
        requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    fun fromHtml(source: String) {
        val builder = SpannableStringBuilder()
        val parser = AztecParser()
        builder.append(parser.fromHtml(Format.clearFormatting(source), context).trim())
        switchToAztecStyle(builder, 0, builder.length)
        disableTextChangedListener()
        setTextKeepState(builder)
        enableTextChangedListener()
    }

    fun toHtml(): String {
        val parser = AztecParser()
        val output = SpannableStringBuilder(text)

        clearMetaSpans(output)
        return Format.clearFormatting(parser.toHtml(output))
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
            editable.setSpan(makeBlockSpan(it.javaClass, it.attributes), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
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
            editable.setSpan(AztecURLSpan(span.url, linkColor, linkUnderline, span.attributes), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
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

    private fun removeBlockStylesFromRange(start: Int, end: Int, ignoreLineBounds: Boolean = false) {
        removeBlockStyle(start, end, AztecBlockSpan::class.java, ignoreLineBounds)
    }

    private fun removeHeadingStylesFromRange(start: Int, end: Int) {
        val spans = editableText.getSpans(start, end, AztecHeadingSpan::class.java)

        for (span in spans) {
            editableText.removeSpan(span)
        }
    }

    fun removeInlineStylesFromRange(start: Int, end: Int) {
        removeInlineStyle(TextFormat.FORMAT_BOLD, start, end)
        removeInlineStyle(TextFormat.FORMAT_ITALIC, start, end)
        removeInlineStyle(TextFormat.FORMAT_STRIKETHROUGH, start, end)
        removeInlineStyle(TextFormat.FORMAT_UNDERLINED, start, end)
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
                builder.append(parser.fromHtml(Format.clearFormatting(textToPaste.toString()), context).trim())
                Selection.setSelection(editable, max)

                disableTextChangedListener()
                editable.replace(min, max, builder)
                enableTextChangedListener()

                joinStyleSpans(0, editable.length) //TODO: see how this affects performance
            }
        }
    }

    fun clearMetaSpans(text: Spannable) {
        BaseInputConnection.removeComposingSpans(text)
        text.getSpans(0, text.length, SuggestionSpan::class.java).forEach { text.removeSpan(it) }
    }
}
