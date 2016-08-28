package org.wordpress.aztec.source

import android.content.Context
import android.graphics.Typeface
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.EditText
import org.wordpress.aztec.R

/**
 * An EditText with support for [OnImeBackListener] and typeface setting
 * using a custom XML attribute.
 */
class SourceViewEditText : EditText {

    @ColorInt var tagColor = ContextCompat.getColor(context, R.color.tag_color)
        internal set
    @ColorInt var attributeColor = ContextCompat.getColor(context, R.color.attribute_color)
        internal set

    private var onImeBackListener: OnImeBackListener? = null

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs)
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            if (this.onImeBackListener != null) {
                this.onImeBackListener!!.onImeBack()
            }
        }
        return super.onKeyPreIme(keyCode, event)
    }

    fun setOnImeBackListener(listener: OnImeBackListener) {
        this.onImeBackListener = listener
    }

    private fun init(attrs: AttributeSet) {

        val values = context.obtainStyledAttributes(attrs, R.styleable.SourceViewEditText)
        val typefaceName = values.getString(R.styleable.SourceViewEditText_fontFile)
        if (typefaceName != null) {
            try {
                val typeface = Typeface.createFromAsset(context.assets, "fonts/" + typefaceName)
                this.typeface = typeface
            } catch (e: RuntimeException) {
                //                AppLog.e(AppLog.T.EDITOR, "Could not load typeface " + typefaceName);
            }

        }

        setBackgroundColor(values.getColor(R.styleable.SourceViewEditText_codeBackgroundColor, ContextCompat.getColor(context, R.color.background)))
        setTextColor(values.getColor(R.styleable.SourceViewEditText_codeTextColor, ContextCompat.getColor(context, R.color.text)))

        tagColor = values.getColor(R.styleable.SourceViewEditText_tagColor, tagColor)
        attributeColor = values.getColor(R.styleable.SourceViewEditText_attributeColor, attributeColor)

        this.addTextChangedListener(HtmlStyleTextWatcher(tagColor, attributeColor))

        values.recycle()
    }
}