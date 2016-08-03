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
import android.text.method.LinkMovementMethod
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

    private val historyList = LinkedList<SpannableStringBuilder>()
    private var historyWorking = false
    private var historyCursor = 0

    private lateinit var inputBefore: SpannableStringBuilder
    private lateinit var inputLast: Editable

    private var mOnSelectionChangedListener: AztecText.OnSelectionChangedListener? = null

    private var mSelectedStyles: ArrayList<TextFormat> = ArrayList()

    var mNewStyleSelected = false

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
        movementMethod = LinkMovementMethod.getInstance()
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
        mNewStyleSelected = true
        mSelectedStyles.clear()
        mSelectedStyles.addAll(styles)
    }

    fun setOnSelectionChangedListener(onSelectionChangedListener: AztecText.OnSelectionChangedListener) {
        mOnSelectionChangedListener = onSelectionChangedListener
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        if(selStart > 0 && selEnd > 0){
            mOnSelectionChangedListener?.onSelectionChanged(selStart, selEnd)
        }

    }

    // StyleSpan ===================================================================================

    private fun bold(valid: Boolean) {
        bold(valid, selectionStart, selectionEnd)
    }

    private fun bold(valid: Boolean, start: Int, end: Int) {
        if (valid) {
            styleValid(Typeface.BOLD, start, end)
        } else {
            styleInvalid(Typeface.BOLD, start, end)
        }
    }

    private fun italic(valid: Boolean) {
        italic(valid, selectionStart, selectionEnd)
    }

    private fun italic(valid: Boolean, start: Int, end: Int) {
        if (valid) {
            styleValid(Typeface.ITALIC, start, end)
        } else {
            styleInvalid(Typeface.ITALIC, start, end)
        }
    }

    private fun styleValid(style: Int, start: Int, end: Int) {
        when (style) {
            Typeface.NORMAL, Typeface.BOLD, Typeface.ITALIC, Typeface.BOLD_ITALIC -> {
            }
            else -> return
        }

        if (start >= end) {
            return
        }

        editableText.setSpan(StyleSpan(style), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
    }

    private fun styleInvalid(style: Int, start: Int, end: Int) {
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
    }

    private fun containStyle(style: Int, start: Int, end: Int): Boolean {
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

    private fun underline(valid: Boolean) {
        if (valid) {
            underlineValid(selectionStart, selectionEnd)
        } else {
            underlineInvalid(selectionStart, selectionEnd)
        }
    }

    private fun underlineValid(start: Int, end: Int) {
        if (start >= end) {
            return
        }

        editableText.setSpan(UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun underlineInvalid(start: Int, end: Int) {
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

    private fun containUnderline(start: Int, end: Int): Boolean {
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

    private fun strikethrough(valid: Boolean) {
        if (valid) {
            strikethroughValid(selectionStart, selectionEnd)
        } else {
            strikethroughInvalid(selectionStart, selectionEnd)
        }
    }

    private fun strikethroughValid(start: Int, end: Int) {
        if (start >= end) {
            return
        }

        editableText.setSpan(StrikethroughSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun strikethroughInvalid(start: Int, end: Int) {
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

    private fun containStrikethrough(start: Int, end: Int): Boolean {
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

    private fun bullet(valid: Boolean) {
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
        val lines = TextUtils.split(editableText.toString(), "\n")

        if (lines.isEmpty()) {
            editableText.append("\u200B")
            editableText.setSpan(AztecBulletSpan(bulletColor, bulletRadius, bulletGapWidth), 0, 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        }

        for (i in lines.indices) {
            if (containBullet(i)) {
                continue
            }

            var lineStart = 0
            for (j in 0..i - 1) {
                lineStart += lines[j].length + 1 // \n
            }

            val lineEnd = lineStart + lines[i].length
            if (lineStart > lineEnd) {
                continue
            }


            // Find selection area inside
            var bulletStart = 0
            var bulletEnd = 0
            if (lineStart <= start && end <= lineEnd) {
                bulletStart = lineStart
                bulletEnd = lineEnd
            }
//            else if (selectionStart <= lineStart && lineEnd <= selectionEnd) {
//                bulletStart = lineStart
//                bulletEnd = lineEnd
//            }

            else {
                continue
            }

            if (bulletStart <= bulletEnd) {
                if (lineStart == lineEnd) {   //line is empy
                    editableText.append("\u200B")
                    bulletEnd += 1
                }

                editableText.setSpan(AztecBulletSpan(bulletColor, bulletRadius, bulletGapWidth), bulletStart, bulletEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
            }
        }
    }

    private fun bulletInvalid() {
        bulletInvalid(selectionStart, selectionEnd)
    }

    private fun bulletInvalid(start: Int, end: Int) {
        val lines = TextUtils.split(editableText.toString(), "\n")

        for (i in lines.indices) {
            if (!containBullet(i)) {
                continue
            }

            var lineStart = 0
            for (j in 0..i - 1) {
                lineStart += lines[j].length + 1
            }

            val lineEnd = lineStart + lines[i].length
            if (lineStart > lineEnd) {
                continue
            }

            var bulletStart = 0
            var bulletEnd = 0
            if (lineStart <= start && end <= lineEnd) {
                bulletStart = lineStart
                bulletEnd = lineEnd
            } else {
                continue

            }

            if (bulletStart <= bulletEnd) {
                val spans = editableText.getSpans(bulletStart, bulletEnd, BulletSpan::class.java)
                for (span in spans) {
                    editableText.removeSpan(span)
                }
            }
        }
    }

    private fun containBullet(): Boolean {
        return containBullet(selectionStart, selectionEnd)
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
            if (!containBullet(i)) {
                return false
            }
        }

        return true
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

    private fun quote(valid: Boolean) {
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
                lineStart += lines[j].length + 1 // \n
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

    private fun quoteInvalid() {
        val lines = TextUtils.split(editableText.toString(), "\n")

        for (i in lines.indices) {
            if (!containQuote(i)) {
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

        val spans = editableText.getSpans(start, end, QuoteSpan::class.java)
        return spans.size > 0
    }


    fun addLink(link: String, anchor: String?, start: Int = selectionStart, end: Int = selectionEnd) {
        if (TextUtils.isEmpty(link)) {
            return
        }

        val cleanLink = link.trim()
        val newEnd: Int

        if (TextUtils.isEmpty(anchor)) {
            text.insert(start, cleanLink)
            newEnd = start + cleanLink.length
        } else {
            text.insert(start, anchor)
            newEnd = start + anchor!!.length
        }

        linkValid(link, start, newEnd)

    }


    fun editLink(link: String, anchor: String?, start: Int = selectionStart, end: Int = selectionEnd) {
        if (TextUtils.isEmpty(link)) {
            return
        }

        val cleanLink = link.trim()
        val newEnd: Int

        if (TextUtils.isEmpty(anchor)) {
            text.replace(start, end, cleanLink)
            newEnd = start + cleanLink.length
        } else {
            text.replace(start, end, anchor)
            newEnd = start + anchor!!.length
        }

        linkValid(link, start, newEnd)
    }

    fun removeLink(start: Int = selectionStart, end: Int = selectionEnd) {
        linkInvalid(start, end)
    }


    private fun linkValid(link: String, start: Int, end: Int) {
        if (start >= end) {
            return
        }
        linkInvalid(start, end)
        editableText.setSpan(AztecURLSpan(link, linkColor, linkUnderline), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        onSelectionChanged(end, end)
    }

    // Remove all span in selection, not like the boldInvalid()
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

    // Redo/Undo ===================================================================================

    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
        if (!historyEnable || historyWorking) {
            return
        }

        inputBefore = SpannableStringBuilder(text)
    }


    var inputStart = -1
    var inputEnd = -1

    var consumeEvent: Boolean = false
    var isBlockStyleFixRequired = false
    var isNewlineInputed = false
    var addnewBullet = false
    var fixingNewBullet = false


    override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
        if (start >= 1) {
            val previousCharacter = text[start - 1]
            if (previousCharacter.toString().equals("\u200B")) {
                isBlockStyleFixRequired = true
            }


            if (count == 1) {
                val currentCharacter = text[start]
                if (currentCharacter.toString().equals("\n") && !previousCharacter.toString().equals("\n")) {
                    isNewlineInputed = true
                    val paragraphSpans = getText().getSpans(start, start, LeadingMarginSpan::class.java)
                    if (!paragraphSpans.isEmpty()) {
                        addnewBullet = true
                    }

                }
            }

        }

        inputStart = start
        inputEnd = start + count
    }

    fun handleHistory(text: Editable) {

        //history
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

    fun handleLists(text: Editable) {
        if (isBlockStyleFixRequired && !isNewlineInputed) {
            consumeEvent = true
            if (fixingNewBullet) {
                fixingNewBullet = false
                text.delete(inputStart - 1, inputStart)
            } else {
                text.delete(inputStart - 1, inputStart)
            }
        } else if (isBlockStyleFixRequired && isNewlineInputed) {
            consumeEvent = true
            text.delete(inputStart - 1, inputStart + 1)
            consumeEvent = true
            bulletInvalid()
        } else if (!isBlockStyleFixRequired && addnewBullet) {
            consumeEvent = true
            //get bullet span from the last character
            val spans = text.getSpans(inputStart, inputStart, BulletSpan::class.java)
            if (!spans.isEmpty()) {
                text.setSpan(spans[0], text.getSpanStart(spans[0]), text.getSpanEnd(spans[0]) - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            bulletValid()
            fixingNewBullet = true
        }
    }


    override fun afterTextChanged(text: Editable) {
        if (consumeEvent) {
            consumeEvent = false

            addnewBullet = false
            isNewlineInputed = false
            isBlockStyleFixRequired = false
            fixingNewBullet = false
            return
        }

        handleHistory(text)

        handleLists(text)



        addnewBullet = false
        isNewlineInputed = false
        isBlockStyleFixRequired = false


        //because we use SPAN_INCLUSIVE_INCLUSIVE for inline styles
        //we need to make sure unselected styles are not applied
        clearInlineStyles(inputStart, inputEnd)

        //trailing styling
        if (!formattingHasChanged()) return

        if (formattingIsApplied()) {
            for (item in mSelectedStyles) {
                when (item) {
                    TextFormat.FORMAT_BOLD -> if (!contains(TextFormat.FORMAT_BOLD, inputStart, inputEnd)) {
                        styleValid(Typeface.BOLD, inputStart, inputEnd)
                    }
                    TextFormat.FORMAT_ITALIC -> if (!contains(TextFormat.FORMAT_ITALIC, inputStart, inputEnd)) {
                        styleValid(Typeface.ITALIC, inputStart, inputEnd)
                    }
                    else -> {
                        //do nothing
                    }
                }
            }
        }

        setFormattingChangesApplied()

        inputStart = -1
        inputEnd = -1
    }


    private val LOCKED = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE

    fun isEmpty(): Boolean {
        return text.isEmpty()
    }

    private fun formattingIsApplied(): Boolean {
        return !mSelectedStyles.isEmpty()
    }

    private fun formattingHasChanged(): Boolean {
        return mNewStyleSelected
    }

    private fun setFormattingChangesApplied() {
        mNewStyleSelected = false
    }

    private fun clearInlineStyles(start: Int, end: Int) {
        val stylesToRemove = ArrayList<TextFormat>()

        getAppliedStyles(start, end).forEach {
            if (!mSelectedStyles.contains(it)) {
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

    fun getAppliedStyles(selectionStart: Int, selectionEnd: Int): ArrayList<TextFormat> {
        val styles = ArrayList<TextFormat>()
        TextFormat.values().forEach {
            if (contains(it, selectionStart, selectionEnd)) {
                styles.add(it)
            }
        }
        return styles
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
        builder.append(parser.fromHtml(source, context))
        switchToAztecStyle(builder, 0, builder.length)
        text = builder
    }

    fun toHtml(): String {
        clearComposingText()
        val parser = AztecParser()
        return parser.toHtml(editableText)
    }

    private fun switchToAztecStyle(editable: Editable, start: Int, end: Int) {
        val bulletSpans = editable.getSpans(start, end, BulletSpan::class.java)
        for (span in bulletSpans) {
            val spanStart = editable.getSpanStart(span)
            var spanEnd = editable.getSpanEnd(span)
            spanEnd = if (0 < spanEnd && spanEnd < editable.length && editable[spanEnd] == '\n') spanEnd - 1 else spanEnd
            editable.removeSpan(span)
            editable.setSpan(AztecBulletSpan(bulletColor, bulletRadius, bulletGapWidth), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        }

        val quoteSpans = editable.getSpans(start, end, QuoteSpan::class.java)
        for (span in quoteSpans) {
            val spanStart = editable.getSpanStart(span)
            var spanEnd = editable.getSpanEnd(span)
            spanEnd = if (0 < spanEnd && spanEnd < editable.length && editable[spanEnd] == '\n') spanEnd - 1 else spanEnd
            editable.removeSpan(span)
            editable.setSpan(AztecQuoteSpan(quoteColor, quoteStripeWidth, quoteGapWidth), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        }

        val urlSpans = editable.getSpans(start, end, URLSpan::class.java)
        for (span in urlSpans) {
            val spanStart = editable.getSpanStart(span)
            val spanEnd = editable.getSpanEnd(span)
            editable.removeSpan(span)
            editable.setSpan(AztecURLSpan(span.url, linkColor, linkUnderline), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        }
    }

}// URLSpan =====================================================================================
