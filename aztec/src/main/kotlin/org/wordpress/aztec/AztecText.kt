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
import android.text.*
import android.text.style.*
import android.util.AttributeSet
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import java.util.*

class AztecText : EditText, TextWatcher {

    private var bulletColor = 0
    private var bulletRadius = 0
    private var bulletGapWidth = 0
    private var historyEnable = true
    private var historySize = 100
    private var linkColor = 0
    private var linkUnderline = true
    private var quoteColor = 0
    private var quoteStripeWidth = 0
    private var quoteGapWidth = 0

    private var consumeEditEvent: Boolean = false
    private var textChangedEvent = TextChangedEvent("", 0, 0, 0)

    private val historyList = LinkedList<SpannableStringBuilder>()
    private var historyWorking = false
    private var historyCursor = 0

    var consumeSelectionEvent: Boolean = false

    private lateinit var inputBefore: SpannableStringBuilder
    private lateinit var inputLast: Editable


    private var onSelectionChangedListener: OnSelectionChangedListener? = null

    private var selectedStyles: ArrayList<TextFormat> = ArrayList()

    var isNewStyleSelected = false


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
        val array = context.obtainStyledAttributes(attrs, R.styleable.AztecText)
        bulletColor = array.getColor(R.styleable.AztecText_bulletColor, 0)
        bulletRadius = array.getDimensionPixelSize(R.styleable.AztecText_bulletRadius, 0)
        bulletGapWidth = array.getDimensionPixelSize(R.styleable.AztecText_bulletGapWidth, 0)
        historyEnable = array.getBoolean(R.styleable.AztecText_historyEnable, true)
        historySize = array.getInt(R.styleable.AztecText_historySize, 100)
        linkColor = array.getColor(R.styleable.AztecText_linkColor, 0)
        linkUnderline = array.getBoolean(R.styleable.AztecText_linkUnderline, true)
        quoteColor = array.getColor(R.styleable.AztecText_quoteColor, 0)
        quoteStripeWidth = array.getDimensionPixelSize(R.styleable.AztecText_quoteStripeWidth, 0)
        quoteGapWidth = array.getDimensionPixelSize(R.styleable.AztecText_quoteCapWidth, 0)
        array.recycle()

        if (historyEnable && historySize <= 0) {
            throw IllegalArgumentException("historySize must > 0")
        }

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

    fun setSelectedStyles(styles: ArrayList<TextFormat>) {
        isNewStyleSelected = true
        selectedStyles.clear()
        selectedStyles.addAll(styles)
    }

    fun setOnSelectionChangedListener(onSelectionChangedListener: OnSelectionChangedListener) {
        this.onSelectionChangedListener = onSelectionChangedListener
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        if (!consumeSelectionEvent) {
            consumeSelectionEvent = false
            onSelectionChangedListener?.onSelectionChanged(selStart, selEnd)
        }
    }

    // StyleSpan ===================================================================================

    fun bold(valid: Boolean) {
        if (valid) {
            styleValid(Typeface.BOLD, selectionStart, selectionEnd)
        } else {
            styleInvalid(Typeface.BOLD, selectionStart, selectionEnd)
        }
    }

    fun italic(valid: Boolean) {
        if (valid) {
            styleValid(Typeface.ITALIC, selectionStart, selectionEnd)
        } else {
            styleInvalid(Typeface.ITALIC, selectionStart, selectionEnd)
        }
    }

    fun styleValid(style: Int, start: Int, end: Int) {
        when (style) {
            Typeface.NORMAL, Typeface.BOLD, Typeface.ITALIC -> {
            }
            else -> return
        }

        if (start >= end) {
            return
        }

        var precedingSpan: StyleSpan? = null
        var followingSpan: StyleSpan? = null

        if (start > 1) {
            val previousSpans = editableText.getSpans(start - 1, start, StyleSpan::class.java)
            previousSpans.forEach {
                if (it.style == style) {
                    precedingSpan = it
                    return@forEach
                }
            }

        }

        if (length() > end) {
            val nextSpans = editableText.getSpans(end, end + 1, StyleSpan::class.java)
            nextSpans.forEach {
                if (it.style == style) {
                    followingSpan = it
                    return@forEach
                }
            }
        }


        if (precedingSpan != null) {
            val spanStart = editableText.getSpanStart(precedingSpan)
            editableText.setSpan(precedingSpan, spanStart, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        }


        if (followingSpan != null) {
            val spanEnd = editableText.getSpanEnd(followingSpan)
            editableText.setSpan(followingSpan, start, spanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        }

        if (precedingSpan == null && followingSpan == null) {
            var existingSpanOfSameStyle: StyleSpan? = null

            val spans = editableText.getSpans(start, end, StyleSpan::class.java)
            spans.forEach {
                if (it.style == style) {
                    existingSpanOfSameStyle = it
                    return@forEach
                }
            }

            if (existingSpanOfSameStyle != null) {
                editableText.removeSpan(editableText)
                editableText.setSpan(existingSpanOfSameStyle, start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
            } else {
                editableText.setSpan(StyleSpan(style), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
            }

        }


        joinStyleSpans(start, end)
    }




    //TODO: Check if there is more efficient way to do it
    //TODO: Make it work with other spans (underline, strikethrough)
    fun joinStyleSpans(start: Int, end: Int) {
        //joins spans on the left
        if (start > 1) {
            val spansInSelection = editableText.getSpans(start, end, StyleSpan::class.java)

            val spansBeforeSelection = editableText.getSpans(start - 1, start, StyleSpan::class.java)
            spansInSelection.forEach { innerSpan ->
                val inSelectionSpanEnd = editableText.getSpanEnd(innerSpan)

                spansBeforeSelection.forEach { outerSpan ->
                    val outerSpanStart = editableText.getSpanStart(outerSpan)

                    if (innerSpan.style == outerSpan.style) {
                        editableText.removeSpan(outerSpan)
                        editableText.setSpan(innerSpan, outerSpanStart, inSelectionSpanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
                    }


                }
            }
        }

        //joins spans on the right
        if (length() > end) {
            val spansInSelection = editableText.getSpans(start, end, StyleSpan::class.java)

            val spansAfterSelection = editableText.getSpans(end, end + 1, StyleSpan::class.java)
            spansInSelection.forEach { innerSpan ->
                val inSelectionSpanStart = editableText.getSpanStart(innerSpan)
                spansAfterSelection.forEach { outerSpan ->
                    val outerSpanEnd = editableText.getSpanEnd(outerSpan)

                    if (innerSpan.style == outerSpan.style) {
                        editableText.removeSpan(outerSpan)
                        editableText.setSpan(innerSpan, inSelectionSpanStart, outerSpanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
                    }


                }
            }


        }


        //joins spans withing
        val spansInSelection = editableText.getSpans(start, end, StyleSpan::class.java)
        val spansToUse = editableText.getSpans(start, end, StyleSpan::class.java)

        spansInSelection.forEach { appliedSpan ->

            val spanStart = editableText.getSpanStart(appliedSpan)
            val spanEnd = editableText.getSpanEnd(appliedSpan)

            var neighbourSpan: StyleSpan? = null


            spansToUse.forEach {
                val aSpanStart = editableText.getSpanStart(it)
                val aSpanEnd = editableText.getSpanEnd(it)
                if (it.style == appliedSpan.style) {

                    if (aSpanStart == spanEnd || aSpanEnd == spanStart) {
                        neighbourSpan = it
                        return@forEach
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


    protected fun styleInvalid(style: Int, start: Int, end: Int) {
        when (style) {
            Typeface.NORMAL, Typeface.BOLD, Typeface.ITALIC, Typeface.BOLD_ITALIC -> {
            }
            else -> return
        }

        if (start >= end) {
            return
        }

        val spans = editableText.getSpans(start, end, StyleSpan::class.java)
        val list = ArrayList<AztecPart>()

        for (span in spans) {
            if (span.style == style) {
                list.add(AztecPart(editableText.getSpanStart(span), editableText.getSpanEnd(span)))
                editableText.removeSpan(span)
            }
        }

        for (part in list) {
            if (part.isValid) {
                if (part.start < start) {
                    styleValid(style, part.start, start)
                }

                if (part.end > end) {
                    styleValid(style, end, part.end)
                }
            }
        }

        joinStyleSpans(start, end)
    }

    protected fun containStyle(style: Int, start: Int, end: Int): Boolean {
        when (style) {
            Typeface.NORMAL, Typeface.BOLD, Typeface.ITALIC, Typeface.BOLD_ITALIC -> {
            }
            else -> return false
        }

        if (start > end) {
            return false
        }

        if (start == end) {
            if (start - 1 < 0 || start + 1 > editableText.length) {
                return false
            } else {
                val before = editableText.getSpans(start - 1, start, StyleSpan::class.java)
                val after = editableText.getSpans(start, start + 1, StyleSpan::class.java)
                return before.size > 0 && after.size > 0 && before[0].style == style && after[0].style == style
            }
        } else {
            val builder = StringBuilder()

            // Make sure no duplicate characters be added
            for (i in start..end - 1) {
                val spans = editableText.getSpans(i, i + 1, StyleSpan::class.java)
                for (span in spans) {
                    if (span.style == style) {
                        builder.append(editableText.subSequence(i, i + 1).toString())
                        break
                    }
                }
            }

            return editableText.subSequence(start, end).toString() == builder.toString()
        }
    }

    // UnderlineSpan ===============================================================================

    fun underline(valid: Boolean) {
        if (valid) {
            underlineValid(selectionStart, selectionEnd)
        } else {
            underlineInvalid(selectionStart, selectionEnd)
        }
    }

    protected fun underlineValid(start: Int, end: Int) {
        if (start >= end) {
            return
        }

        editableText.setSpan(UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    protected fun underlineInvalid(start: Int, end: Int) {
        if (start >= end) {
            return
        }

        val spans = editableText.getSpans(start, end, UnderlineSpan::class.java)
        val list = ArrayList<AztecPart>()

        for (span in spans) {
            list.add(AztecPart(editableText.getSpanStart(span), editableText.getSpanEnd(span)))
            editableText.removeSpan(span)
        }

        for (part in list) {
            if (part.isValid) {
                if (part.start < start) {
                    underlineValid(part.start, start)
                }

                if (part.end > end) {
                    underlineValid(end, part.end)
                }
            }
        }
    }

    protected fun containUnderline(start: Int, end: Int): Boolean {
        if (start > end) {
            return false
        }

        if (start == end) {
            if (start - 1 < 0 || start + 1 > editableText.length) {
                return false
            } else {
                val before = editableText.getSpans(start - 1, start, UnderlineSpan::class.java)
                val after = editableText.getSpans(start, start + 1, UnderlineSpan::class.java)
                return before.size > 0 && after.size > 0
            }
        } else {
            val builder = StringBuilder()

            for (i in start..end - 1) {
                if (editableText.getSpans(i, i + 1, UnderlineSpan::class.java).size > 0) {
                    builder.append(editableText.subSequence(i, i + 1).toString())
                }
            }

            return editableText.subSequence(start, end).toString() == builder.toString()
        }
    }

    // StrikethroughSpan ===========================================================================

    fun strikethrough(valid: Boolean) {
        if (valid) {
            strikethroughValid(selectionStart, selectionEnd)
        } else {
            strikethroughInvalid(selectionStart, selectionEnd)
        }
    }

    protected fun strikethroughValid(start: Int, end: Int) {
        if (start >= end) {
            return
        }

        editableText.setSpan(StrikethroughSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    protected fun strikethroughInvalid(start: Int, end: Int) {
        if (start >= end) {
            return
        }

        val spans = editableText.getSpans(start, end, StrikethroughSpan::class.java)
        val list = ArrayList<AztecPart>()

        for (span in spans) {
            list.add(AztecPart(editableText.getSpanStart(span), editableText.getSpanEnd(span)))
            editableText.removeSpan(span)
        }

        for (part in list) {
            if (part.isValid) {
                if (part.start < start) {
                    strikethroughValid(part.start, start)
                }

                if (part.end > end) {
                    strikethroughValid(end, part.end)
                }
            }
        }
    }

    protected fun containStrikethrough(start: Int, end: Int): Boolean {
        if (start > end) {
            return false
        }

        if (start == end) {
            if (start - 1 < 0 || start + 1 > editableText.length) {
                return false
            } else {
                val before = editableText.getSpans(start - 1, start, StrikethroughSpan::class.java)
                val after = editableText.getSpans(start, start + 1, StrikethroughSpan::class.java)
                return before.size > 0 && after.size > 0
            }
        } else {
            val builder = StringBuilder()

            for (i in start..end - 1) {
                if (editableText.getSpans(i, i + 1, StrikethroughSpan::class.java).size > 0) {
                    builder.append(editableText.subSequence(i, i + 1).toString())
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
                    editableText.setSpan(AztecBulletSpan(bulletColor, bulletRadius, bulletGapWidth), startOfList + 1, endOfList, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
                }


            }

        } else {
            val boundsOfSelectedText = getSelectedTextBounds(editableText, start, end)

            val startOfLine = boundsOfSelectedText.start
            var endOfLine = boundsOfSelectedText.endInclusive

            if (startOfLine == endOfLine) {   //line is empty
                consumeEditEvent = true
                editableText.insert(startOfLine, "\u200B")
                endOfLine += 1
            }

            editableText.setSpan(AztecBulletSpan(bulletColor, bulletRadius, bulletGapWidth), startOfLine, endOfLine, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
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
            editableText.setSpan(AztecBulletSpan(bulletColor, bulletRadius, bulletGapWidth), spanStart, startOfLine - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        if (spanExtendsBeyondLine) {
            editableText.setSpan(AztecBulletSpan(bulletColor, bulletRadius, bulletGapWidth), endOfLine, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
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

    protected fun quoteValid() {
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
                editableText.setSpan(AztecQuoteSpan(quoteColor, quoteStripeWidth, quoteGapWidth), quoteStart, quoteEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    protected fun quoteInvalid() {
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

    protected fun containQuote(selStart: Int, selEnd: Int): Boolean {
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

        for (i in list) {
            if (!containQuote(i)) {
                return false
            }
        }

        return true
    }

    protected fun containQuote(index: Int): Boolean {
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

    // When AztecText lose focus, use this method
    @JvmOverloads fun link(link: String?, start: Int = selectionStart, end: Int = selectionEnd) {
        if (link != null && !TextUtils.isEmpty(link.trim { it <= ' ' })) {
            linkValid(link, start, end)
        } else {
            linkInvalid(start, end)
        }
    }

    protected fun linkValid(link: String, start: Int, end: Int) {
        if (start >= end) {
            return
        }

        linkInvalid(start, end)
        editableText.setSpan(AztecURLSpan(link, linkColor, linkUnderline), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    // Remove all span in selection, not like the boldInvalid()
    protected fun linkInvalid(start: Int, end: Int) {
        if (start >= end) {
            return
        }

        val spans = editableText.getSpans(start, end, URLSpan::class.java)
        for (span in spans) {
            editableText.removeSpan(span)
        }
    }

    protected fun containLink(start: Int, end: Int): Boolean {
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
        val stylesToRemove = ArrayList<TextFormat>()

        getAppliedStyles(start, end).forEach {
            if (!selectedStyles.contains(it)) {
                stylesToRemove.add(it)
            }
        }
        stylesToRemove.forEach {
            when (it) {
                TextFormat.FORMAT_BOLD -> styleInvalid(Typeface.BOLD, start, end)
                TextFormat.FORMAT_ITALIC -> styleInvalid(Typeface.ITALIC, start, end)
                else -> {
                    //do nothing
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
            TextFormat.FORMAT_BULLET -> bullet(!contains(TextFormat.FORMAT_BULLET))
            TextFormat.FORMAT_QUOTE -> quote(!contains(TextFormat.FORMAT_QUOTE))
            else -> {
                //Do nothing for now
            }
        }
    }


    operator fun contains(format: TextFormat): Boolean {
        when (format) {
            TextFormat.FORMAT_BOLD -> return containStyle(Typeface.BOLD, selectionStart, selectionEnd)
            TextFormat.FORMAT_ITALIC -> return containStyle(Typeface.ITALIC, selectionStart, selectionEnd)
            TextFormat.FORMAT_UNDERLINED -> return containUnderline(selectionStart, selectionEnd)
            TextFormat.FORMAT_STRIKETHROUGH -> return containStrikethrough(selectionStart, selectionEnd)
            TextFormat.FORMAT_BULLET -> return containBullet(selectionStart, selectionEnd)
            TextFormat.FORMAT_QUOTE -> return containQuote(selectionStart, selectionEnd)
            TextFormat.FORMAT_LINK -> return containLink(selectionStart, selectionEnd)
            else -> return false
        }
    }

    fun contains(format: TextFormat, selStart: Int, selEnd: Int): Boolean {
        when (format) {
            TextFormat.FORMAT_BOLD -> return containStyle(Typeface.BOLD, selStart, selEnd)
            TextFormat.FORMAT_ITALIC -> return containStyle(Typeface.ITALIC, selStart, selEnd)
            TextFormat.FORMAT_UNDERLINED -> return containUnderline(selStart, selEnd)
            TextFormat.FORMAT_STRIKETHROUGH -> return containStrikethrough(selStart, selEnd)
            TextFormat.FORMAT_BULLET -> return containBullet(selStart, selEnd)
            TextFormat.FORMAT_QUOTE -> return containQuote(selectionStart, selectionEnd)
            TextFormat.FORMAT_LINK -> return containLink(selStart, selEnd)
            else -> return false
        }
    }

    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
        if (!historyEnable || historyWorking) {
            return
        }

        inputBefore = SpannableStringBuilder(text)
    }

    var clearingEditText = false

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
//        //clear all spans from EditText when it get's empty
//        if (start == 0 && count == 0 && text.toString().equals("") && !clearingEditText) {
//            clearingEditText = true
//            consumeSelectionEvent = true
//            setText(null)
//            onSelectionChanged(0, 0)
//        }else if(clearingEditText){
//            clearingEditText = false
//        }

        textChangedEvent = TextChangedEvent(text, start, before, count)
    }

    override fun afterTextChanged(text: Editable) {
        if (consumeEditEvent) {
            consumeEditEvent = false
            return
        }

        handleLists(text)

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


        //because we use SPAN_INCLUSIVE_INCLUSIVE for inline styles
        //we need to make sure unselected styles are not applied
        clearInlineStyles(textChangedEvent.inputStart, textChangedEvent.inputEnd)

        //trailing styling
        if (!formattingHasChanged()) return

        if (formattingIsApplied()) {
            for (item in selectedStyles) {
                when (item) {
                    TextFormat.FORMAT_BOLD -> if (!contains(TextFormat.FORMAT_BOLD,
                            textChangedEvent.inputStart, textChangedEvent.inputStart)) {
                        styleValid(Typeface.BOLD, textChangedEvent.inputStart, textChangedEvent.inputEnd)
                    }
                    TextFormat.FORMAT_ITALIC -> if (!contains(TextFormat.FORMAT_ITALIC, textChangedEvent.inputStart, textChangedEvent.inputEnd)) {
                        styleValid(Typeface.ITALIC, textChangedEvent.inputStart, textChangedEvent.inputEnd)
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
    fun handleLists(text: Editable) {
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
            consumeEditEvent = true
            text.delete(inputStart - 1, inputStart)
        } else if (textChangedEvent.isAfterZeroWidthJoiner() && textChangedEvent.isNewline()) {
            bulletInvalid()
            consumeEditEvent = true
            if (inputStart == 1) {
                text.delete(inputStart - 1, inputStart + 1)
            } else {
                text.delete(inputStart - 2, inputStart)
            }
        } else if (!textChangedEvent.isAfterZeroWidthJoiner() && textChangedEvent.isNewline()) {
            val paragraphSpans = getText().getSpans(textChangedEvent.inputStart, textChangedEvent.inputStart, BulletSpan::class.java)
            if (!paragraphSpans.isEmpty()) {
                consumeEditEvent = true
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
        consumeEditEvent = true
        text = builder
    }

    fun toHtml(): String {
        clearComposingText() //remove formatting provided by autosuggestion (like <u>)
        val parser = AztecParser()
        return parser.toHtml(editableText)
    }

    protected fun switchToAztecStyle(editable: Editable, start: Int, end: Int) {
        val bulletSpans = editable.getSpans(start, end, BulletSpan::class.java)
        for (span in bulletSpans) {
            val spanStart = editable.getSpanStart(span)
            var spanEnd = editable.getSpanEnd(span)
            spanEnd = if (0 < spanEnd && spanEnd < editable.length && editable[spanEnd] == '\n') spanEnd - 1 else spanEnd
            editable.removeSpan(span)
            editable.setSpan(AztecBulletSpan(bulletColor, bulletRadius, bulletGapWidth), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        val quoteSpans = editable.getSpans(start, end, QuoteSpan::class.java)
        for (span in quoteSpans) {
            val spanStart = editable.getSpanStart(span)
            var spanEnd = editable.getSpanEnd(span)
            spanEnd = if (0 < spanEnd && spanEnd < editable.length && editable[spanEnd] == '\n') spanEnd - 1 else spanEnd
            editable.removeSpan(span)
            editable.setSpan(AztecQuoteSpan(quoteColor, quoteStripeWidth, quoteGapWidth), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        val urlSpans = editable.getSpans(start, end, URLSpan::class.java)
        for (span in urlSpans) {
            val spanStart = editable.getSpanStart(span)
            val spanEnd = editable.getSpanEnd(span)
            editable.removeSpan(span)
            editable.setSpan(AztecURLSpan(span.url, linkColor, linkUnderline), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    companion object {
        val FORMAT_BOLD = 0x01
        val FORMAT_ITALIC = 0x02
        val FORMAT_UNDERLINED = 0x03
        val FORMAT_STRIKETHROUGH = 0x04
        val FORMAT_BULLET = 0x05
        val FORMAT_QUOTE = 0x06
        val FORMAT_LINK = 0x07
    }
// URLSpan =====================================================================================
}
