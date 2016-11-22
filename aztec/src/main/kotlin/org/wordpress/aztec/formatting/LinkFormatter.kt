package org.wordpress.aztec.formatting

import android.content.Context
import android.text.Spanned
import android.text.TextUtils
import android.util.Patterns
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.spans.AztecURLSpan


class LinkFormatter(editor: AztecText, linkStyle: LinkStyle):AztecFormatter(editor) {

    data class LinkStyle(val linkColor: Int, val linkUnderline: Boolean)

    val linkStyle: LinkStyle

    init {
        this.linkStyle = linkStyle
    }


    fun isUrlSelected(): Boolean {
        val urlSpans = editableText.getSpans(selectionStart, selectionEnd, AztecURLSpan::class.java)
        return !urlSpans.isEmpty()
    }

    fun getSelectedUrlWithAnchor(): Pair<String, String> {
        val url: String
        var anchor: String

        if (!isUrlSelected()) {
            val clipboardUrl = getUrlFromClipboard(editor.context)

            url = if (TextUtils.isEmpty(clipboardUrl)) "" else clipboardUrl
            anchor = if (selectionStart == selectionEnd) "" else editor.getSelectedText()

        } else {
            val urlSpans = editableText.getSpans(selectionStart, selectionEnd, AztecURLSpan::class.java)
            val urlSpan = urlSpans[0]

            val spanStart = editableText.getSpanStart(urlSpan)
            val spanEnd = editableText.getSpanEnd(urlSpan)

            if (selectionStart < spanStart || selectionEnd > spanEnd) {
                //looks like some text that is not part of the url was included in selection
                anchor = editor.getSelectedText()
                url = ""
            } else {
                anchor = editableText.substring(spanStart, spanEnd)
                url = urlSpan.url
            }

            if (anchor == url) {
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
        val urlSpans = editableText.getSpans(selectionStart, selectionEnd, AztecURLSpan::class.java)

        val spanStart = editableText.getSpanStart(urlSpans[0])
        val spanEnd = editableText.getSpanEnd(urlSpans[0])

        if (selectionStart < spanStart || selectionEnd > spanEnd) {
            //looks like some text that is not part of the url was included in selection
            return Pair(selectionStart, selectionEnd)
        }
        return Pair(spanStart, spanEnd)
    }


    fun addLink(link: String, anchor: String, start: Int, end: Int) {
        val cleanLink = link.trim()
        val newEnd: Int

        val actualAnchor = if (TextUtils.isEmpty(anchor)) cleanLink else anchor

        if (start == end) {
            //insert anchor
            editableText.insert(start, actualAnchor)
            newEnd = start + actualAnchor.length
        } else {
            //apply span to text
            if (editor.getSelectedText() != anchor) {
                editableText.replace(start, end, actualAnchor)
            }
            newEnd = start + actualAnchor.length
        }

        linkValid(link, start, newEnd)
    }

    fun editLink(link: String, anchor: String?, start: Int = selectionStart, end: Int = selectionEnd) {
        val cleanLink = link.trim()
        val newEnd: Int

        if (TextUtils.isEmpty(anchor)) {
            editableText.replace(start, end, cleanLink)
            newEnd = start + cleanLink.length
        } else {
            //if the anchor was not changed do nothing to preserve original style of text
            if (editor.getSelectedText() != anchor) {
                editableText.replace(start, end, anchor)
            }
            newEnd = start + anchor!!.length
        }

        var attributes = getAttributes(end, start)
        attributes = attributes?.replace("href=[\"'].*[\"']".toRegex(), "href=\"$cleanLink\"")

        linkValid(cleanLink, start, newEnd, attributes)
    }

    private fun getAttributes(end: Int, start: Int): String? {
        val urlSpans = editableText.getSpans(start, end, AztecURLSpan::class.java)
        var attributes: String? = null
        if (urlSpans != null && urlSpans.size > 0) {
            attributes = urlSpans[0].attributes
        }
        return attributes
    }

    fun makeUrlSpan(url: String, attrs: String? = null): AztecURLSpan {
        return AztecURLSpan(url, linkStyle, attrs)
    }

    private fun linkValid(link: String, start: Int, end: Int, attributes: String? = null) {
        if (start >= end) {
            return
        }

        linkInvalid(start, end)
        editableText.setSpan(AztecURLSpan(link, linkStyle, attributes), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        editor.onSelectionChanged(end, end)
    }

    fun linkInvalid(start: Int, end: Int) {
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
                return before.size > 0 && after.size > 0
            }
        } else {
            val builder = StringBuilder()

            for (i in start..end - 1) {
                if (editableText.getSpans(i, i + 1, AztecURLSpan::class.java).size > 0) {
                    builder.append(editableText.subSequence(i, i + 1).toString())
                }
            }

            return editableText.subSequence(start, end).toString() == builder.toString()
        }
    }
}