package org.wordpress.aztec.formatting

import android.content.Context
import android.text.Spanned
import android.text.TextUtils
import android.util.Patterns
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.spans.AztecURLSpan


class LinkFormatter(editor: AztecText) {

    val editor: AztecText

    init {
        this.editor = editor
    }


    fun isUrlSelected(): Boolean {
        val urlSpans = editor.editableText.getSpans(editor.selectionStart, editor.selectionEnd, AztecURLSpan::class.java)
        return !urlSpans.isEmpty()
    }

    fun getSelectedUrlWithAnchor(): Pair<String, String> {
        val url: String
        var anchor: String

        if (!isUrlSelected()) {
            val clipboardUrl = getUrlFromClipboard(editor.context)

            url = if (TextUtils.isEmpty(clipboardUrl)) "" else clipboardUrl
            anchor = if (editor.selectionStart == editor.selectionEnd) "" else editor.getSelectedText()

        } else {
            val urlSpans = editor.editableText.getSpans(editor.selectionStart, editor.selectionEnd, AztecURLSpan::class.java)
            val urlSpan = urlSpans[0]

            val spanStart = editor.editableText.getSpanStart(urlSpan)
            val spanEnd = editor.editableText.getSpanEnd(urlSpan)

            if (editor.selectionStart < spanStart || editor.selectionEnd > spanEnd) {
                //looks like some text that is not part of the url was included in selection
                anchor = editor.getSelectedText()
                url = ""
            } else {
                anchor = editor.editableText.substring(spanStart, spanEnd)
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
        val urlSpans = editor.editableText.getSpans(editor.selectionStart, editor.selectionEnd, AztecURLSpan::class.java)

        val spanStart = editor.editableText.getSpanStart(urlSpans[0])
        val spanEnd = editor.editableText.getSpanEnd(urlSpans[0])

        if (editor.selectionStart < spanStart || editor.selectionEnd > spanEnd) {
            //looks like some text that is not part of the url was included in selection
            return Pair(editor.selectionStart, editor.selectionEnd)
        }
        return Pair(spanStart, spanEnd)
    }


    fun addLink(link: String, anchor: String, start: Int, end: Int) {
        val cleanLink = link.trim()
        val newEnd: Int

        val actualAnchor = if (TextUtils.isEmpty(anchor)) cleanLink else anchor

        if (start == end) {
            //insert anchor
            editor.editableText.insert(start, actualAnchor)
            newEnd = start + actualAnchor.length
        } else {
            //apply span to text
            if (editor.getSelectedText() != anchor) {
                editor.editableText.replace(start, end, actualAnchor)
            }
            newEnd = start + actualAnchor.length
        }

        linkValid(link, start, newEnd)
    }

    fun editLink(link: String, anchor: String?, start: Int = editor.selectionStart, end: Int = editor.selectionEnd) {
        val cleanLink = link.trim()
        val newEnd: Int

        if (TextUtils.isEmpty(anchor)) {
            editor.editableText.replace(start, end, cleanLink)
            newEnd = start + cleanLink.length
        } else {
            //if the anchor was not changed do nothing to preserve original style of text
            if (editor.getSelectedText() != anchor) {
                editor.editableText.replace(start, end, anchor)
            }
            newEnd = start + anchor!!.length
        }

        var attributes = getAttributes(end, start)
        attributes = attributes?.replace("href=[\"'].*[\"']".toRegex(), "href=\"$cleanLink\"")

        linkValid(cleanLink, start, newEnd, attributes)
    }

    private fun getAttributes(end: Int, start: Int): String? {
        val urlSpans = editor.editableText.getSpans(start, end, AztecURLSpan::class.java)
        var attributes: String? = null
        if (urlSpans != null && urlSpans.size > 0) {
            attributes = urlSpans[0].attributes
        }
        return attributes
    }


    private fun linkValid(link: String, start: Int, end: Int, attributes: String? = null) {
        if (start >= end) {
            return
        }

        linkInvalid(start, end)
        editor.editableText.setSpan(AztecURLSpan(link, editor.linkColor, editor.linkUnderline, attributes), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        editor.onSelectionChanged(end, end)
    }

    fun linkInvalid(start: Int, end: Int) {
        if (start >= end) {
            return
        }

        val spans = editor.editableText.getSpans(start, end, AztecURLSpan::class.java)
        for (span in spans) {
            editor.editableText.removeSpan(span)
        }
    }

    fun containLink(start: Int, end: Int): Boolean {
        if (start > end) {
            return false
        }

        if (start == end) {
            if (start - 1 < 0 || start + 1 > editor.editableText.length) {
                return false
            } else {
                val before =editor.editableText.getSpans(start - 1, start, AztecURLSpan::class.java)
                val after = editor.editableText.getSpans(start, start + 1, AztecURLSpan::class.java)
                return before.size > 0 && after.size > 0
            }
        } else {
            val builder = StringBuilder()

            for (i in start..end - 1) {
                if (editor.editableText.getSpans(i, i + 1, AztecURLSpan::class.java).size > 0) {
                    builder.append(editor.editableText.subSequence(i, i + 1).toString())
                }
            }

            return editor.editableText.subSequence(start, end).toString() == builder.toString()
        }
    }
}