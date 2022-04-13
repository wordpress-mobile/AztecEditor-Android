package org.wordpress.aztec.formatting

import org.wordpress.aztec.AztecText
import org.wordpress.aztec.spans.AztecDynamicImageSpan
import org.wordpress.aztec.spans.AztecHeadingSpan
import org.wordpress.aztec.spans.AztecPreformatSpan
import org.wordpress.aztec.spans.AztecQuoteSpan
import org.wordpress.aztec.spans.IAztecBlockSpan
import org.wordpress.aztec.spans.ParagraphSpan

class IndentFormatter(editor: AztecText) : AztecFormatter(editor) {
    fun indent() {
        val previousLineBreak = editableText.substring(0, selectionStart).lastIndexOf("\n") + 1
        val indicesToIndent = mutableSetOf<Int>()
        if (selectionCanBeIndented(previousLineBreak, selectionStart)) {
            indicesToIndent.add(previousLineBreak)
        }
        var startIndex = selectionStart
        while (startIndex in selectionStart until selectionEnd) {
            val nextLineBreak = editableText.indexOf("\n", startIndex)
            if (nextLineBreak != -1 && nextLineBreak < selectionEnd) {
                if (selectionCanBeIndented(nextLineBreak + 1, nextLineBreak + 2)) {
                    indicesToIndent.add(nextLineBreak + 1)
                }
                startIndex = nextLineBreak + 1
            } else {
                break
            }
        }
        var offset = 0
        val previousSelectionStart = selectionStart
        val previousSelectionEnd = selectionEnd
        indicesToIndent.sorted().forEach {
            editableText.insert(it + offset, "\t")
            offset += 1
        }
        if (offset > 0) {
            editor.setSelection(previousSelectionStart + 1, previousSelectionEnd + offset)
        }
    }

    fun outdent() {
        val previousLineBreak = editableText.substring(0, selectionStart).lastIndexOf("\n")
        val indicesToOutdent = mutableSetOf<Int>()
        if (previousLineBreak > 0 && selectionCanBeOutdented(previousLineBreak + 1, selectionStart)) {
            indicesToOutdent.add(previousLineBreak + 1)
        }
        if (editableText.startsWith("\t")) {
            if (selectionCanBeOutdented(0, 2)) {
                indicesToOutdent.add(0)
            }
        }
        var startIndex = selectionStart
        while (startIndex in selectionStart until selectionEnd) {
            val nextIndentedLineBreak = editableText.indexOf("\n\t", startIndex)
            if (nextIndentedLineBreak != -1 && nextIndentedLineBreak < selectionEnd && nextIndentedLineBreak < selectionEnd) {
                if (selectionCanBeOutdented(nextIndentedLineBreak + 1, nextIndentedLineBreak + 2)) {
                    indicesToOutdent.add(nextIndentedLineBreak + 1)
                }
                startIndex = nextIndentedLineBreak + 1
            } else {
                break
            }
        }
        var offset = 0
        val previousSelectionStart = selectionStart
        val previousSelectionEnd = selectionEnd
        indicesToOutdent.sorted().forEach {
            editableText.replace(it - offset, it + 1 - offset, "")
            offset += 1
        }
        if (offset > 0) {
            editor.setSelection(previousSelectionStart - 1, previousSelectionEnd - offset)
        }
    }

    private fun selectionCanBeIndented(start: Int, end: Int): Boolean {
        val mediaSpans = editableText.getSpans(start, end, AztecDynamicImageSpan::class.java)
        if (mediaSpans.isNotEmpty()) return false
        val spans = editableText.getSpans(start, end, IAztecBlockSpan::class.java)
        return spans.isEmpty() || spans.all {
            it is ParagraphSpan || it is AztecHeadingSpan || it is AztecQuoteSpan || it is AztecPreformatSpan
        }
    }

    private fun selectionCanBeOutdented(start: Int, end: Int): Boolean {
        val mediaSpans = editableText.getSpans(start + 1, end, AztecDynamicImageSpan::class.java)
        if (mediaSpans.isNotEmpty()) return false
        val spans = editableText.getSpans(start + 1, end, IAztecBlockSpan::class.java)
        return editableText[start] == "\t"[0] && (spans.isEmpty() || spans.all {
            it is ParagraphSpan || it is AztecHeadingSpan || it is AztecQuoteSpan || it is AztecPreformatSpan
        })
    }

    fun isIndentAvailable(): Boolean {
        var start = selectionStart
        while (start in 0..selectionEnd) {
            var end = editableText.indexOf("\n", start)
            if (end == -1 || end > selectionEnd) {
                end = selectionEnd
            }
            if (selectionCanBeIndented(start, end)) {
                return true
            } else {
                start = end + 1
            }
        }
        return false
    }

    fun isOutdentAvailable(): Boolean {
        val previousLineBreak = editableText.substring(0, selectionStart).lastIndexOf("\n")
        if (previousLineBreak > 0 && selectionCanBeOutdented(previousLineBreak + 1, selectionStart)) {
            return true
        }
        var start = selectionStart
        while (start in 0..selectionEnd) {
            var end = editableText.indexOf("\n", start)
            if (end == -1 || end > selectionEnd) {
                end = selectionEnd
            }
            if (selectionCanBeOutdented(start, end)) {
                return true
            } else {
                start = end + 1
            }
        }
        return false
    }
}

