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

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.content.ContextCompat
import android.text.*
import android.text.style.*
import android.util.AttributeSet
import android.util.Patterns
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import org.wordpress.aztec.util.TypefaceCache
import java.util.*

class AztecText : EditText, TextWatcher {

    private val REMOVED_SPANS_BUNDLE_KEY = "REMOVED_SPANS_BUNDLE_KEY"
    private val SUPER_STATE_BUNDLE_KEY = "SUPER_STATE_BUNDLE_KEY"

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

    private val historyList = LinkedList<SpannableStringBuilder>()
    private var historyWorking = false
    private var historyCursor = 0

    private lateinit var inputBefore: SpannableStringBuilder
    private lateinit var inputLast: Editable

    private var onSelectionChangedListener: OnSelectionChangedListener? = null

    private val selectedStyles = ArrayList<TextFormat>()

    private var isNewStyleSelected = false

    var removedSpans = ArrayList<Int>()

    interface OnSelectionChangedListener {
        fun onSelectionChanged(selStart: Int, selEnd: Int)
    }

    constructor(context: Context) : super(context) {
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

        // triggers ClickableSpan onClick() events
        movementMethod = EnhancedMovementMethod

        removedSpans.clear()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val bundle = Bundle()
        bundle.putIntegerArrayList(REMOVED_SPANS_BUNDLE_KEY, removedSpans)
        bundle.putParcelable(SUPER_STATE_BUNDLE_KEY, superState)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state != null && state is Bundle) {
            removedSpans = state.getIntegerArrayList(REMOVED_SPANS_BUNDLE_KEY)
            super.onRestoreInstanceState(state.getParcelable(SUPER_STATE_BUNDLE_KEY))
        }
        else {
            super.onRestoreInstanceState(BaseSavedState.EMPTY_STATE)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        addTextChangedListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeTextChangedListener(this)
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


    fun isSameInlineSpanType(firstSpan: CharacterStyle, secondSpan: CharacterStyle): Boolean {
        if (firstSpan.javaClass.equals(secondSpan.javaClass)) {
            if (firstSpan is HiddenHtmlSpan) return false

            //special check for StyleSpan
            if (firstSpan is StyleSpan && secondSpan is StyleSpan) {
                return firstSpan.style == secondSpan.style
            } else {
                return true
            }
        }

        return false
    }


    //TODO: Check if there is more efficient way to tidy spans
    private fun joinStyleSpans(start: Int, end: Int) {
        //joins spans on the left
        if (start > 1) {
            val spansInSelection = editableText.getSpans(start, end, CharacterStyle::class.java)

            val spansBeforeSelection = editableText.getSpans(start - 1, start, CharacterStyle::class.java)
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
            val spansInSelection = editableText.getSpans(start, end, CharacterStyle::class.java)
            val spansAfterSelection = editableText.getSpans(end, end + 1, CharacterStyle::class.java)
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
        val spansInSelection = editableText.getSpans(start, end, CharacterStyle::class.java)
        val spansToUse = editableText.getSpans(start, end, CharacterStyle::class.java)

        spansInSelection.forEach { appliedSpan ->

            val spanStart = editableText.getSpanStart(appliedSpan)
            val spanEnd = editableText.getSpanEnd(appliedSpan)

            var neighbourSpan: CharacterStyle? = null

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

        val spanToApply = makeDummyInlineSpan(textFormat)

        if (start >= end) {
            return
        }


        var precedingSpan: CharacterStyle? = null
        var followingSpan: CharacterStyle? = null

        if (start > 1) {
            val previousSpans = editableText.getSpans(start - 1, start, CharacterStyle::class.java)
            previousSpans.forEach {
                if (isSameInlineSpanType(it, spanToApply)) {
                    precedingSpan = it
                    return@forEach
                }
            }

            if (precedingSpan != null) {
                val spanStart = editableText.getSpanStart(precedingSpan)
                editableText.setSpan(precedingSpan, spanStart, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
            }
        }

        if (length() > end) {
            val nextSpans = editableText.getSpans(end, end + 1, CharacterStyle::class.java)
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
            var existingSpanOfSameStyle: CharacterStyle? = null

            val spans = editableText.getSpans(start, end, CharacterStyle::class.java)
            spans.forEach {
                if (isSameInlineSpanType(it, spanToApply)) {
                    existingSpanOfSameStyle = it
                    return@forEach
                }
            }

            //if we already have same span within selection - reuse it by changing it's bounds
            if (existingSpanOfSameStyle != null) {
                editableText.removeSpan(editableText)
                editableText.setSpan(existingSpanOfSameStyle, start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
            } else {
                editableText.setSpan(spanToApply, start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
            }

        }

        joinStyleSpans(start, end)
    }


    fun removeInlineStyle(textFormat: TextFormat, start: Int, end: Int) {
        //for convenience sake we are initializing the span of same type we are planing to remove
        val spanToRemove = makeDummyInlineSpan(textFormat)

        if (start >= end) {
            return
        }


        val spans = editableText.getSpans(start, end, CharacterStyle::class.java)
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


    fun makeDummyInlineSpan(textFormat: TextFormat): CharacterStyle {
        when (textFormat) {
            TextFormat.FORMAT_BOLD -> return StyleSpan(Typeface.BOLD)
            TextFormat.FORMAT_ITALIC -> return StyleSpan(Typeface.ITALIC)
            TextFormat.FORMAT_STRIKETHROUGH -> return AztecStrikethroughSpan()
            TextFormat.FORMAT_UNDERLINED -> return UnderlineSpan()
            else -> return StyleSpan(Typeface.NORMAL)
        }
    }

    fun containsInlineStyle(textFormat: TextFormat, start: Int, end: Int): Boolean {
        val spanToCheck = makeDummyInlineSpan(textFormat)

        if (start > end) {
            return false
        }

        if (start == end) {
            if (start - 1 < 0 || start + 1 > editableText.length) {
                return false
            } else {
                val before = editableText.getSpans(start - 1, start, CharacterStyle::class.java)
                val after = editableText.getSpans(start, start + 1, CharacterStyle::class.java)
                return before.size > 0 && after.size > 0 && isSameInlineSpanType(before[0], after[0])
            }
        } else {
            val builder = StringBuilder()

            // Make sure no duplicate characters be added
            for (i in start..end - 1) {
                val spans = editableText.getSpans(i, i + 1, CharacterStyle::class.java)
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
    // BulletSpan ==================================================================================

    fun bullet(valid: Boolean) {
        if (valid) {
            bulletValid()
        } else {
            bulletInvalid()
        }
    }

    private fun bulletValid() {
        bulletValid(selectionStart, selectionEnd)
    }

    private fun bulletValid(start: Int, end: Int) {
        if (start != end) {
            val selectedText = editableText.substring(start + 1..end - 1)

            //multiline text selected
            if (selectedText.indexOf("\n") != -1) {
                val indexOfFirstLineBreak = editableText.indexOf("\n", end)

                val endOfList = if (indexOfFirstLineBreak != -1) indexOfFirstLineBreak else editableText.length
                val startOfList = editableText.lastIndexOf("\n", start)

                val selectedLines = editableText.subSequence(startOfList + 1..endOfList - 1) as Editable

                var numberOfLinesWithSpanApplied = 0
                var numberOfLines = 0

                val lines = TextUtils.split(selectedLines.toString(), "\n")

                for (i in lines.indices) {
                    numberOfLines++
                    if (containBullet(i, selectedLines)) {
                        numberOfLinesWithSpanApplied++
                    }
                }

                if (numberOfLines == numberOfLinesWithSpanApplied) {
                    bulletInvalid()
                } else {
                    editableText.setSpan(AztecBulletSpan(bulletColor, bulletMargin, bulletWidth, bulletPadding), startOfList + 1, endOfList, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
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

            editableText.setSpan(AztecBulletSpan(bulletColor, bulletMargin, bulletWidth, bulletPadding), startOfLine, endOfLine, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)

            //if the line was empty trigger onSelectionChanged manually to update toolbar buttons status
            if (isEmptyLine) {
                onSelectionChanged(startOfLine, endOfLine)
            }
        }
    }

    private fun bulletInvalid() {
        bulletInvalid(selectionStart, selectionEnd)
    }

    private fun bulletInvalid(start: Int, end: Int) {
        val spans = editableText.getSpans(start, end, BulletSpan::class.java)
        //check if the span extends
        if (spans.isEmpty()) return

        //for now we will assume that only one span can be applied at time
        val span = spans[0]


        val spanStart = editableText.getSpanStart(span)
        val spanEnd = editableText.getSpanEnd(span)

        val boundsOfSelectedText = getSelectedTextBounds(editableText, start, end)

        val startOfLine = boundsOfSelectedText.start
        val endOfLine = boundsOfSelectedText.endInclusive

        val spanPrecedesLine = spanStart < startOfLine
        val spanExtendsBeyondLine = endOfLine < spanEnd

        //remove the span from all the selected lines
        editableText.removeSpan(span)


        //reapply span top "top" and "bottom"
        if (spanPrecedesLine) {
            editableText.setSpan(AztecBulletSpan(bulletColor, bulletMargin, bulletWidth, bulletPadding), spanStart, startOfLine - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        if (spanExtendsBeyondLine) {
            editableText.setSpan(AztecBulletSpan(bulletColor, bulletMargin, bulletWidth, bulletPadding), endOfLine, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun containBullet(selStart: Int, selEnd: Int): Boolean {
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
            if (!containBullet(i)) {
                return false
            }
        }

        return true
    }

    private fun containBullet(index: Int, text: Editable): Boolean {
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

        val spans = editableText.getSpans(start, end, BulletSpan::class.java)
        return spans.size > 0
    }

    private fun containBullet(index: Int): Boolean {
        val lines = TextUtils.split(editableText.toString(), "\n")
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

        val spans = editableText.getSpans(start, end, BulletSpan::class.java)
        return spans.size > 0
    }

    // QuoteSpan ===================================================================================

    fun quote(valid: Boolean) {
        if (valid) {
            quoteValid()
        } else {
            quoteInvalid()
        }
    }

    private fun quoteValid() {
        val lines = TextUtils.split(editableText.toString(), "\n")

        for (i in lines.indices) {
            if (containQuote(i)) {
                continue
            }

            var lineStart = 0
            for (j in 0..i - 1) {
                lineStart = lineStart + lines[j].length + 1 // \n
            }

            val lineEnd = lineStart + lines[i].length
            if (lineStart >= lineEnd) {
                continue
            }

            var quoteStart = 0
            var quoteEnd = 0
            if (lineStart <= selectionStart && selectionEnd <= lineEnd) {
                quoteStart = lineStart
                quoteEnd = lineEnd
            } else if (selectionStart <= lineStart && lineEnd <= selectionEnd) {
                quoteStart = lineStart
                quoteEnd = lineEnd
            }

            if (quoteStart < quoteEnd) {
                editableText.setSpan(AztecQuoteSpan(quoteBackground, quoteColor, quoteMargin, quoteWidth, quotePadding), quoteStart, quoteEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    private fun quoteInvalid() {
        val lines = TextUtils.split(editableText.toString(), "\n")

        for (i in lines.indices) {
            if (!containQuote(i)) {
                continue
            }

            var lineStart = 0
            for (j in 0..i - 1) {
                lineStart = lineStart + lines[j].length + 1
            }

            val lineEnd = lineStart + lines[i].length
            if (lineStart >= lineEnd) {
                continue
            }

            var quoteStart = 0
            var quoteEnd = 0
            if (lineStart <= selectionStart && selectionEnd <= lineEnd) {
                quoteStart = lineStart
                quoteEnd = lineEnd
            } else if (selectionStart <= lineStart && lineEnd <= selectionEnd) {
                quoteStart = lineStart
                quoteEnd = lineEnd
            }

            if (quoteStart < quoteEnd) {
                val spans = editableText.getSpans(quoteStart, quoteEnd, QuoteSpan::class.java)
                for (span in spans) {
                    editableText.removeSpan(span)
                }
            }
        }
    }

    private fun containQuote(selStart: Int, selEnd: Int): Boolean {
        val lines = TextUtils.split(editableText.toString(), "\n")
        val list = ArrayList<Int>()

        for (i in lines.indices) {
            var lineStart = 0
            for (j in 0..i - 1) {
                lineStart = lineStart + lines[j].length + 1
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
            start = start + lines[i].length + 1
        }

        val end = start + lines[index].length
        if (start >= end) {
            return false
        }

        val spans = editableText.getSpans(start, end, QuoteSpan::class.java)
        return spans.size > 0
    }

    fun getSelectedText(): String {
        if (selectionStart == -1 || selectionEnd == -1) return ""
        return editableText.substring(selectionStart, selectionEnd)
    }

    fun isUrlSelected(): Boolean {
        val urlSpans = editableText.getSpans(selectionStart, selectionEnd, URLSpan::class.java)
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
            val urlSpans = editableText.getSpans(selectionStart, selectionEnd, URLSpan::class.java)
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

            if (anchor.equals(url)) {
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
        val urlSpans = editableText.getSpans(selectionStart, selectionEnd, URLSpan::class.java)

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

        linkValid(link, start, newEnd)
    }

    fun removeLink() {
        val urlSpanBounds = getUrlSpanBounds()

        linkInvalid(urlSpanBounds.first, urlSpanBounds.second)
        onSelectionChanged(urlSpanBounds.first, urlSpanBounds.second)
    }

    private fun linkValid(link: String, start: Int, end: Int) {
        if (start >= end) {
            return
        }

        linkInvalid(start, end)
        editableText.setSpan(AztecURLSpan(link, linkColor, linkUnderline), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        onSelectionChanged(end, end)
    }

    private fun linkInvalid(start: Int, end: Int) {
        if (start >= end) {
            return
        }

        val spans = editableText.getSpans(start, end, URLSpan::class.java)
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
                val before = editableText.getSpans(start - 1, start, URLSpan::class.java)
                val after = editableText.getSpans(start, start + 1, URLSpan::class.java)
                return before.size > 0 && after.size > 0
            }
        } else {
            val builder = StringBuilder()

            for (i in start..end - 1) {
                if (editableText.getSpans(i, i + 1, URLSpan::class.java).size > 0) {
                    builder.append(editableText.subSequence(i, i + 1).toString())
                }
            }

            return editableText.subSequence(start, end).toString() == builder.toString()
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

    private fun clearInlineStyles(start: Int, end: Int) {
        getAppliedStyles(start, end).forEach {
            if (!selectedStyles.contains(it)) {
                when (it) {
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
        when (textFormat) {
            TextFormat.FORMAT_BOLD -> bold(!contains(TextFormat.FORMAT_BOLD))
            TextFormat.FORMAT_ITALIC -> italic(!contains(TextFormat.FORMAT_ITALIC))
            TextFormat.FORMAT_STRIKETHROUGH -> strikethrough(!contains(TextFormat.FORMAT_STRIKETHROUGH))
            TextFormat.FORMAT_BULLET -> bullet(!contains(TextFormat.FORMAT_BULLET))
            TextFormat.FORMAT_QUOTE -> quote(!contains(TextFormat.FORMAT_QUOTE))
            else -> {
                //Do nothing for now
            }
        }
    }

    fun contains(format: TextFormat, selStart: Int = selectionStart, selEnd: Int = selectionEnd): Boolean {
        when (format) {
            TextFormat.FORMAT_BOLD -> return containsInlineStyle(TextFormat.FORMAT_BOLD, selStart, selEnd)
            TextFormat.FORMAT_ITALIC -> return containsInlineStyle(TextFormat.FORMAT_ITALIC, selStart, selEnd)
            TextFormat.FORMAT_UNDERLINED -> return containsInlineStyle(TextFormat.FORMAT_UNDERLINED, selStart, selEnd)
            TextFormat.FORMAT_STRIKETHROUGH -> return containsInlineStyle(TextFormat.FORMAT_STRIKETHROUGH, selStart, selEnd)
            TextFormat.FORMAT_BULLET -> return containBullet(selStart, selEnd)
            TextFormat.FORMAT_QUOTE -> return containQuote(selectionStart, selectionEnd)
            TextFormat.FORMAT_LINK -> return containLink(selStart, selEnd)
            else -> return false
        }
    }

    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {

        if (historyEnable && !historyWorking) {
            inputBefore = SpannableStringBuilder(text)
        }

        if (count > after) {
            val hidden = inputBefore.getSpans(start, start + count, HiddenHtmlSpan::class.java)
            hidden.forEach {
                if (inputBefore.getSpanStart(it) >= start && inputBefore.getSpanEnd(it) <= start + count) {
                    removedSpans.add(it.startOrder)
                    removedSpans.add(it.endOrder)
                }
            }
        }
    }

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
        textChangedEventDetails = TextChangedEvent(text, start, before, count)
    }

    override fun afterTextChanged(text: Editable) {
        if (isTextChangedListenerDisabled()) {
            enableTextChangedListener()
            return
        }

        //clear all spans from EditText when it get's empty
        if (textChangedEventDetails.inputStart == 0 && textChangedEventDetails.count == 0 && text.toString().equals("")) {
            disableTextChangedListener()
            setText(null)
            onSelectionChanged(0, 0)
        }

        handleHistory()

        handleLists(text, textChangedEventDetails)
        handleInlineStyling(text, textChangedEventDetails)
    }

    fun handleHistory() {
        if (!historyEnable || historyWorking) {
            return
        }

        inputLast = SpannableStringBuilder(text)
        if (text.toString() == inputBefore.toString()) {
            return
        }

        if (historyList.size >= historySize) {
            historyList.removeAt(0)
        }

        historyList.add(inputBefore)
        historyCursor = historyList.size
    }

    fun handleInlineStyling(text: Editable, textChangedEvent: TextChangedEvent) {
        //because we use SPAN_INCLUSIVE_INCLUSIVE for inline styles
        //we need to make sure unselected styles are not applied
        clearInlineStyles(textChangedEvent.inputStart, textChangedEvent.inputEnd)

        //trailing styling
        if (!formattingHasChanged()) return

        if (formattingIsApplied()) {
            for (item in selectedStyles) {
                when (item) {
                    TextFormat.FORMAT_BOLD -> if (!contains(item, textChangedEvent.inputStart, textChangedEvent.inputStart)) {
                        applyInlineStyle(TextFormat.FORMAT_BOLD, textChangedEvent.inputStart, textChangedEvent.inputEnd)
                    }
                    TextFormat.FORMAT_ITALIC -> if (!contains(item, textChangedEvent.inputStart, textChangedEvent.inputEnd)) {
                        applyInlineStyle(TextFormat.FORMAT_ITALIC, textChangedEvent.inputStart, textChangedEvent.inputEnd)
                    }
                    TextFormat.FORMAT_STRIKETHROUGH -> if (!contains(item, textChangedEvent.inputStart, textChangedEvent.inputEnd)) {
                        applyInlineStyle(TextFormat.FORMAT_STRIKETHROUGH, textChangedEvent.inputStart, textChangedEvent.inputEnd)
                    }
                    else -> {
                        //do nothing
                    }
                }
            }
        }

        setFormattingChangesApplied()
    }


    //TODO: Add support for NumberedLists
    fun handleLists(text: Editable, textChangedEvent: TextChangedEvent) {
        val inputStart = textChangedEvent.inputStart

        val spanToOpen = textChangedEvent.getSpanToOpen(text)

        //we might need to open span to add text to it
        if (spanToOpen != null) {
            editableText.setSpan(spanToOpen,
                    text.getSpanStart(spanToOpen),
                    text.getSpanEnd(spanToOpen) + textChangedEvent.count,
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        }


        if (textChangedEvent.isAfterZeroWidthJoiner() && !textChangedEvent.isNewline()) {
            disableTextChangedListener()
            text.delete(inputStart - 1, inputStart)
        } else if (textChangedEvent.isAfterZeroWidthJoiner() && textChangedEvent.isNewline()) {
            bulletInvalid()
            disableTextChangedListener()
            if (inputStart == 1) {
                text.delete(inputStart - 1, inputStart + 1)
            } else {
                text.delete(inputStart - 2, inputStart)
            }
        } else if (!textChangedEvent.isAfterZeroWidthJoiner() && textChangedEvent.isNewline()) {
            val paragraphSpans = getText().getSpans(textChangedEvent.inputStart, textChangedEvent.inputStart, BulletSpan::class.java)
            if (!paragraphSpans.isEmpty()) {
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
        if (!redoValid()) {
            return
        }

        historyWorking = true

        if (historyCursor >= historyList.size - 1) {
            historyCursor = historyList.size
            text = inputLast
        } else {
            historyCursor++
            text = historyList[historyCursor]
        }

        setSelection(editableText.length)
        historyWorking = false
    }

    fun undo() {
        if (!undoValid()) {
            return
        }

        historyWorking = true

        historyCursor--
        text = historyList[historyCursor]
        setSelection(editableText.length)

        historyWorking = false
    }

    fun redoValid(): Boolean {
        if (!historyEnable || historySize <= 0 || historyList.size <= 0 || historyWorking) {
            return false
        }

        return historyCursor < historyList.size - 1 || historyCursor >= historyList.size - 1
    }

    fun undoValid(): Boolean {
        if (!historyEnable || historySize <= 0 || historyWorking) {
            return false
        }

        if (historyList.size <= 0 || historyCursor <= 0) {
            return false
        }

        return true
    }

    fun clearHistory() {
        historyList.clear()
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
        builder.append(parser.fromHtml(source, context).trim())
        switchToAztecStyle(builder, 0, builder.length)
        disableTextChangedListener()
        text = builder
    }

    fun toHtml(): String {
        clearComposingText() //remove formatting provided by autosuggestion (like <u>)
        val parser = AztecParser()
        return parser.toHtml(editableText, removedSpans)
    }

    private fun switchToAztecStyle(editable: Editable, start: Int, end: Int) {
        val bulletSpans = editable.getSpans(start, end, BulletSpan::class.java)
        for (span in bulletSpans) {
            val spanStart = editable.getSpanStart(span)
            var spanEnd = editable.getSpanEnd(span)
            spanEnd = if (0 < spanEnd && spanEnd < editable.length && editable[spanEnd] == '\n') spanEnd - 1 else spanEnd
            editable.removeSpan(span)
            editable.setSpan(AztecBulletSpan(bulletColor, bulletMargin, bulletWidth, bulletPadding), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        val quoteSpans = editable.getSpans(start, end, QuoteSpan::class.java)
        for (span in quoteSpans) {
            val spanStart = editable.getSpanStart(span)
            var spanEnd = editable.getSpanEnd(span)
            spanEnd = if (0 < spanEnd && spanEnd < editable.length && editable[spanEnd] == '\n') spanEnd - 1 else spanEnd
            editable.removeSpan(span)
            editable.setSpan(AztecQuoteSpan(quoteBackground, quoteColor, quoteMargin, quoteWidth, quotePadding), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        val urlSpans = editable.getSpans(start, end, URLSpan::class.java)
        for (span in urlSpans) {
            val spanStart = editable.getSpanStart(span)
            val spanEnd = editable.getSpanEnd(span)
            editable.removeSpan(span)
            editable.setSpan(AztecURLSpan(span.url, linkColor, linkUnderline), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
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
}
