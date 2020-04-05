package org.wordpress.aztec.util

import android.content.ClipData
import android.content.Context
import android.text.Editable
import android.text.Spannable
import android.text.Spanned
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK
import android.widget.Button
import android.widget.ToggleButton
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import org.wordpress.aztec.AztecParser
import org.wordpress.aztec.R

fun Editable.getLast(kind: Class<*>): Any? {
    val spans = this.getSpans(0, this.length, kind)

    if (spans.isEmpty()) {
        return null
    } else {
        return (spans.size downTo 1)
                .firstOrNull { this.getSpanFlags(spans[it - 1]) == Spannable.SPAN_MARK_MARK }
                ?.let { spans[it - 1] }
    }
}

inline fun <reified T> Editable.getLast(): T? {
    val spans = this.getSpans(0, this.length, T::class.java)

    if (spans.isEmpty()) {
        return null
    } else {
        return (spans.size downTo 1)
                .firstOrNull { this.getSpanFlags(spans[it - 1]) == Spannable.SPAN_MARK_MARK }
                ?.let { spans[it - 1] }
    }
}

fun ClipData.Item.coerceToStyledText(context: Context, parser: AztecParser): CharSequence {
    val text = text ?: ""
    if (text is Spanned) {
        return text
    }

    val html = htmlText ?: ""
    return parser.fromHtml(html, context)
}

fun ClipData.Item.coerceToHtmlText(parser: AztecParser): String {
    // If the item has an explicit HTML value, simply return that.
    val htmlText = htmlText
    if (htmlText != null) {
        return htmlText
    }

    // If this Item has a plain text value, return it as HTML.
    val text = text ?: ""
    if (text is Spanned) {
        return parser.toHtml(text)
    }

    return text.toString()
}

/**
 * Some of the toggle button controls that exist on the Aztec formatting toolbar act as buttons but are
 * announced as switches so this function converts the accessibility properties to that of a button.
 */
fun ToggleButton.convertToButtonAccessibilityProperties() {
    ViewCompat.setAccessibilityDelegate(this, object : AccessibilityDelegateCompat() {
        override fun onInitializeAccessibilityNodeInfo(host: View?, info: AccessibilityNodeInfoCompat?) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            info?.className = Button::class.java.name
            info?.isCheckable = false
            info?.addAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat(ACTION_CLICK, context.getString(R.string.accessibility_action_click_label)))
        }
    })
}

/**
 * Method sets a customisable background drawable to all the toolbar buttons.
 * The AztecToolbarStyle can be overridden in the main app module to customise the color of the toolbar buttons.
 */
fun ToggleButton.setBackgroundDrawableRes(@DrawableRes backgroundDrawableRes: Int) {
    val wrapper = ContextThemeWrapper(context, R.style.AztecToolbarStyle)
    val drawable = AppCompatResources.getDrawable(wrapper, backgroundDrawableRes)
    this.background = drawable
}
