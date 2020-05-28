package org.wordpress.aztec.plugins.shortcodes.extensions

import android.text.Layout
import android.text.Spanned
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.plugins.shortcodes.CaptionShortcodePlugin
import org.wordpress.aztec.plugins.shortcodes.spans.CaptionShortcodeSpan
import org.wordpress.aztec.plugins.shortcodes.spans.createCaptionShortcodeSpan
import org.wordpress.aztec.spans.AztecImageSpan
import org.wordpress.aztec.spans.IAztecAlignmentSpan
import org.wordpress.aztec.spans.IAztecNestable
import org.wordpress.aztec.util.SpanWrapper

fun AztecText.getImageCaption(attributePredicate: AztecText.AttributePredicate): String {
    return this.text.getSpans(0, this.text.length, AztecImageSpan::class.java)
        .firstOrNull {
            attributePredicate.matches(it.attributes)
        }
        ?.getCaption() ?: ""
}

fun AztecText.getImageCaptionAttributes(attributePredicate: AztecText.AttributePredicate): AztecAttributes {
    return this.text.getSpans(0, this.text.length, AztecImageSpan::class.java)
        .firstOrNull {
            attributePredicate.matches(it.attributes)
        }
        ?.getCaptionAttributes() ?: AztecAttributes()
}

@JvmOverloads
fun AztecText.setImageCaption(attributePredicate: AztecText.AttributePredicate, value: String, attrs: AztecAttributes? = null) {
    this.text.getSpans(0, this.text.length, AztecImageSpan::class.java)
        .firstOrNull {
            attributePredicate.matches(it.attributes)
        }
        ?.setCaption(value, attrs)
}

fun AztecText.removeImageCaption(attributePredicate: AztecText.AttributePredicate) {
    this.text.getSpans(0, this.text.length, AztecImageSpan::class.java)
        .firstOrNull {
            attributePredicate.matches(it.attributes)
        }
        ?.removeCaption()
}

fun AztecText.hasImageCaption(attributePredicate: AztecText.AttributePredicate): Boolean {
    this.text.getSpans(0, this.text.length, AztecImageSpan::class.java)
            .firstOrNull {
                attributePredicate.matches(it.attributes)
            }
            ?.let {
                val wrapper = SpanWrapper(text, it)
                return text.getSpans(wrapper.start, wrapper.end, CaptionShortcodeSpan::class.java).isNotEmpty()
        }

    return false
}

fun AztecImageSpan.getCaption(): String {
    textView?.text?.let {
        val wrapper = SpanWrapper(textView!!.text, this)
        textView!!.text.getSpans(wrapper.start, wrapper.end, CaptionShortcodeSpan::class.java).firstOrNull()?.let {
            return it.caption
        }
    }
    return ""
}

fun AztecImageSpan.getCaptionAttributes(): AztecAttributes {
    textView?.text?.let {
        val wrapper = SpanWrapper(textView!!.text, this)
        textView!!.text.getSpans(wrapper.start, wrapper.end, CaptionShortcodeSpan::class.java).firstOrNull()?.let {
            return it.attributes
        }
    }
    return AztecAttributes()
}

@JvmOverloads
fun AztecImageSpan.setCaption(value: String, attrs: AztecAttributes? = null) {
    textView?.text?.let {
        val wrapper = SpanWrapper(textView!!.text, this)

        var captionSpan = textView?.text?.getSpans(wrapper.start, wrapper.end, CaptionShortcodeSpan::class.java)?.firstOrNull()
        if (captionSpan == null) {
            captionSpan = createCaptionShortcodeSpan(
                    AztecAttributes(),
                    CaptionShortcodePlugin.HTML_TAG,
                    IAztecNestable.getNestingLevelAt(textView!!.text, wrapper.start),
                    textView!!)
            textView!!.text.setSpan(captionSpan, wrapper.start, wrapper.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        captionSpan.caption = value

        if (captionSpan is IAztecAlignmentSpan) {
            attrs?.let {
                captionSpan.attributes = attrs

                if (captionSpan.attributes.hasAttribute(CaptionShortcodePlugin.ALIGN_ATTRIBUTE)) {
                    when (captionSpan.attributes.getValue(CaptionShortcodePlugin.ALIGN_ATTRIBUTE)) {
                        CaptionShortcodePlugin.ALIGN_RIGHT_ATTRIBUTE_VALUE -> captionSpan.align = Layout.Alignment.ALIGN_OPPOSITE
                        CaptionShortcodePlugin.ALIGN_CENTER_ATTRIBUTE_VALUE -> captionSpan.align = Layout.Alignment.ALIGN_CENTER
                        CaptionShortcodePlugin.ALIGN_LEFT_ATTRIBUTE_VALUE -> captionSpan.align = Layout.Alignment.ALIGN_NORMAL
                    }
                } else {
                    captionSpan.align = null
                }
            }
        }
    }
}

fun AztecImageSpan.removeCaption() {
    textView?.text?.let {
        val wrapper = SpanWrapper(textView!!.text, this)
        textView!!.text.getSpans(wrapper.start, wrapper.end, CaptionShortcodeSpan::class.java).firstOrNull()?.remove()
    }
}
