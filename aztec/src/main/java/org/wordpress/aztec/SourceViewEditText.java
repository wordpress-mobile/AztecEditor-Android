package org.wordpress.aztec;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * An EditText with support for {@link org.wordpress.aztec.OnImeBackListener} and typeface setting
 * using a custom XML attribute.
 */
public class SourceViewEditText extends EditText {

    @ColorInt int tagColor = ContextCompat.getColor(getContext(), R.color.tag_color);
    @ColorInt int attributeColor = ContextCompat.getColor(getContext(), R.color.attribute_color);

    private OnImeBackListener mOnImeBackListener;

    public SourceViewEditText(Context context) {
        super(context);
    }

    public SourceViewEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SourceViewEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (this.mOnImeBackListener != null) {
                this.mOnImeBackListener.onImeBack();
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public void setOnImeBackListener(OnImeBackListener listener) {
        this.mOnImeBackListener = listener;
    }

    private void init(AttributeSet attrs) {

        TypedArray values = getContext().obtainStyledAttributes(attrs, R.styleable.SourceViewEditText);
        String typefaceName = values.getString(R.styleable.SourceViewEditText_fontFile);
        if (typefaceName != null) {
            try {
                Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/" + typefaceName);
                this.setTypeface(typeface);
            } catch (RuntimeException e) {
//                AppLog.e(AppLog.T.EDITOR, "Could not load typeface " + typefaceName);
            }
        }


        setBackgroundColor(values.getColor(R.styleable.SourceViewEditText_codeBackgroundColor, ContextCompat.getColor(getContext(), R.color.background)));
        setTextColor(values.getColor(R.styleable.SourceViewEditText_codeTextColor, ContextCompat.getColor(getContext(), R.color.text)));

        tagColor = values.getColor(R.styleable.SourceViewEditText_tagColor, tagColor);
        attributeColor = values.getColor(R.styleable.SourceViewEditText_attributeColor, attributeColor);

        this.addTextChangedListener(new HtmlStyleTextWatcher(tagColor, attributeColor));

        values.recycle();
    }

    public @ColorInt int getTagColor() {
        return tagColor;
    }

    public @ColorInt int getAttributeColor() {
        return attributeColor;
    }
}