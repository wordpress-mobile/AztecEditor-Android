package org.wordpress.aztec.source

import android.content.Context
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.EditText
import org.wordpress.aztec.R
import org.wordpress.aztec.util.TypefaceCache
import java.util.*

class SourceViewEditText : EditText, TextWatcher {

    @ColorInt var tagColor = ContextCompat.getColor(context, R.color.tag_color)
        internal set
    @ColorInt var attributeColor = ContextCompat.getColor(context, R.color.attribute_color)
        internal set

    private var historyEnable = resources.getBoolean(R.bool.history_enable)
    private var historySize = resources.getInteger(R.integer.history_size)

    private val historyList = LinkedList<SpannableStringBuilder>()
    private var historyWorking = false
    private var historyCursor = 0

    private lateinit var inputBefore: SpannableStringBuilder
    private lateinit var inputLast: Editable

    private var styleTextWatcher: HtmlStyleTextWatcher? = null

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet) {

        TypefaceCache.setCustomTypeface(context, this, TypefaceCache.TYPEFACE_DEJAVU_SANS_MONO)

        val values = context.obtainStyledAttributes(attrs, R.styleable.SourceViewEditText)
        setBackgroundColor(values.getColor(R.styleable.SourceViewEditText_codeBackgroundColor, ContextCompat.getColor(context, R.color.background)))
        setTextColor(values.getColor(R.styleable.SourceViewEditText_codeTextColor, ContextCompat.getColor(context, R.color.text)))

        historyEnable = values.getBoolean(R.styleable.SourceViewEditText_codeHistoryEnable, historyEnable)
        historySize = values.getInt(R.styleable.SourceViewEditText_codeHistorySize, historySize)

        tagColor = values.getColor(R.styleable.SourceViewEditText_tagColor, tagColor)
        attributeColor = values.getColor(R.styleable.SourceViewEditText_attributeColor, attributeColor)

        styleTextWatcher = HtmlStyleTextWatcher(tagColor, attributeColor)

        values.recycle()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        addTextChangedListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeTextChangedListener(this)
    }

    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
        if (historyEnable && !historyWorking) {
            inputBefore = SpannableStringBuilder(text)
        }

        styleTextWatcher?.beforeTextChanged(text, start, count, after)
    }

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
        styleTextWatcher?.onTextChanged(text, start, before, count)
    }

    override fun afterTextChanged(text: Editable?) {
        handleHistory()

        styleTextWatcher?.afterTextChanged(text)
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
}