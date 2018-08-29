package org.wordpress.aztec.formatting

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.util.Patterns
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.spans.AztecURLSpan

class LinkFormatter(editor: AztecText, val linkStyle: LinkStyle) : AztecFormatter(editor) {

    data class LinkStyle(val linkColor: Int, val linkUnderline: Boolean)

    fun isUrlSelected(): Boolean {
        val urlSpans = editableText.getSpans(selectionStart, selectionEnd, AztecURLSpan::class.java)
        return !urlSpans.isEmpty()
    }

    fun getSelectedUrlWithAnchor(): Triple<String, String, Boolean> {
        val url: String
        var anchor: String
        var openInNewWindow = false

        if (!isUrlSelected()) {
            val clipboardUrl = getUrlFromClipboard(editor.context)

            url = if (TextUtils.isEmpty(clipboardUrl)) "" else clipboardUrl
            anchor = if (selectionStart == selectionEnd) "" else editor.getSelectedText()
        } else {
            val urlSpan = editableText.getSpans(selectionStart, selectionEnd, AztecURLSpan::class.java).first()

            val spanStart = editableText.getSpanStart(urlSpan)
            val spanEnd = editableText.getSpanEnd(urlSpan)

            if (selectionStart < spanStart || selectionEnd > spanEnd) {
                // looks like some text that is not part of the url was included in selection
                anchor = editor.getSelectedText()
                url = ""
            } else {
                anchor = editableText.substring(spanStart, spanEnd)
                url = urlSpan.url
            }

            if (anchor == url) {
                anchor = ""
            }

            openInNewWindow = if (urlSpan.attributes.hasAttribute("target")) urlSpan.attributes.getValue("target") == ("_blank") else false
        }

        return Triple(url, anchor, openInNewWindow)
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
        val clipText = data.getItemAt(0).coerceToText(context).toString()
        return if (Patterns.WEB_URL.matcher(clipText).matches()) clipText else ""
    }

    fun getUrlSpanBounds(): Pair<Int, Int> {
        val urlSpan = editableText.getSpans(selectionStart, selectionEnd, AztecURLSpan::class.java).first()

        val spanStart = editableText.getSpanStart(urlSpan)
        val spanEnd = editableText.getSpanEnd(urlSpan)

        if (selectionStart < spanStart || selectionEnd > spanEnd) {
            // looks like some text that is not part of the url was included in selection
            return Pair(selectionStart, selectionEnd)
        }
        return Pair(spanStart, spanEnd)
    }

    fun addLink(link: String, anchor: String, openInNewWindow: Boolean = false, start: Int, end: Int) {
        val cleanLink = link.trim()

        val actualAnchor = if (TextUtils.isEmpty(anchor)) cleanLink else anchor

        val ssb = SpannableStringBuilder(actualAnchor)
        val attributes = getAttributes(end, start)
        toggleOpenInNewWindowAttributes(openInNewWindow, attributes)

        setLinkSpan(ssb, cleanLink, 0, actualAnchor.length, attributes)

        if (start == end) {
            // insert anchor
            editableText.insert(start, ssb)
        } else {
            // apply span to text
            if (editor.getSelectedText() != anchor) {
                editableText.replace(start, end, ssb)
            } else {
                setLinkSpan(editableText, cleanLink, start, end, attributes)
            }
        }
    }

    private fun toggleOpenInNewWindowAttributes(openInNewWindow: Boolean = false, attributes: AztecAttributes = AztecAttributes()): AztecAttributes {
        if (openInNewWindow) {
            attributes.setValue("target", "_blank")
            attributes.setValue("rel", "noopener")
        } else {
            attributes.removeAttribute("target")
            if (attributes.hasAttribute("rel") && attributes.getValue("rel") == "noopener") {
                attributes.removeAttribute("rel")
            }
        }
        return attributes
    }

    fun editLink(link: String, anchor: String?, openInNewWindow: Boolean = false, start: Int = selectionStart, end: Int = selectionEnd) {
        val cleanLink = link.trim()
        val newEnd: Int

        if (TextUtils.isEmpty(anchor)) {
            editableText.replace(start, end, cleanLink)
            newEnd = start + cleanLink.length
        } else {
            // if the anchor was not changed do nothing to preserve original style of text
            if (editor.getSelectedText() != anchor) {
                editableText.replace(start, end, anchor)
            }
            newEnd = start + anchor!!.length
        }

        val attributes = getAttributes(end, start)
        attributes.setValue("href", cleanLink)
        toggleOpenInNewWindowAttributes(openInNewWindow, attributes)

        linkValid(cleanLink, start, newEnd, attributes)
    }

    private fun getAttributes(end: Int, start: Int): AztecAttributes {
        val urlSpan = editableText.getSpans(start, end, AztecURLSpan::class.java).firstOrNull()
        var attributes = AztecAttributes()
        if (urlSpan != null) {
            attributes = urlSpan.attributes
        }
        return attributes
    }

    fun makeUrlSpan(url: String, attrs: AztecAttributes = AztecAttributes()): AztecURLSpan {
        return AztecURLSpan(url, linkStyle, attrs)
    }

    private fun linkValid(link: String, start: Int, end: Int, attributes: AztecAttributes = AztecAttributes()) {
        if (start >= end) {
            return
        }

        removeLink(start, end)
        setLinkSpan(editableText, link, start, end, attributes)
        editor.onSelectionChanged(end, end)
    }

    fun removeLink(start: Int, end: Int) {
        if (start >= end) {
            return
        }

        val spans = editableText.getSpans(start, end, AztecURLSpan::class.java)
        for (span in spans) {
            editableText.removeSpan(span)
        }
    }

    fun containLink(start: Int, end: Int): Boolean {
        if (start > end) {
            return false
        }

        if (start == end) {
            if (start - 1 < 0 || start + 1 > editableText.length) {
                return false
            } else {
                val before = editableText.getSpans(start - 1, start, AztecURLSpan::class.java)
                val after = editableText.getSpans(start, start + 1, AztecURLSpan::class.java)
                return before.isNotEmpty() && after.isNotEmpty()
            }
        } else {
            val builder = StringBuilder()

            (start..end - 1)
                    .filter { editableText.getSpans(it, it + 1, AztecURLSpan::class.java).isNotEmpty() }
                    .forEach { builder.append(editableText.subSequence(it, it + 1).toString()) }

            return editableText.subSequence(start, end).toString() == builder.toString()
        }
    }

    fun setLinkSpan(spannable: Spannable, link: String, start: Int, end: Int, attributes: AztecAttributes = AztecAttributes()) {
        spannable.setSpan(AztecURLSpan(link, linkStyle, attributes), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}