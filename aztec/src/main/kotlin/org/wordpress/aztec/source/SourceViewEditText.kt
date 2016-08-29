package org.wordpress.aztec.source

import android.content.Context
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.widget.EditText
import org.wordpress.aztec.R
import org.wordpress.aztec.util.TypefaceCache

class SourceViewEditText : EditText {

    @ColorInt var tagColor = ContextCompat.getColor(context, R.color.tag_color)
        internal set
    @ColorInt var attributeColor = ContextCompat.getColor(context, R.color.attribute_color)
        internal set

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

        tagColor = values.getColor(R.styleable.SourceViewEditText_tagColor, tagColor)
        attributeColor = values.getColor(R.styleable.SourceViewEditText_attributeColor, attributeColor)

        this.addTextChangedListener(HtmlStyleTextWatcher(tagColor, attributeColor))

        values.recycle()
    }
}