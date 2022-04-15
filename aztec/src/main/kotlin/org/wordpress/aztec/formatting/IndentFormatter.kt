package org.wordpress.aztec.formatting

import org.wordpress.aztec.AztecText
import org.wordpress.aztec.spans.AztecDynamicImageSpan
import org.wordpress.aztec.spans.AztecHeadingSpan
import org.wordpress.aztec.spans.AztecPreformatSpan
import org.wordpress.aztec.spans.AztecQuoteSpan
import org.wordpress.aztec.spans.IAztecBlockSpan
import org.wordpress.aztec.spans.ParagraphSpan

/**
 * Class that handles indents of text blocks
 */
class IndentFormatter(editor: AztecText) : AztecFormatter(editor) {
    /**
     * This function tries to indent all the selected lines that can be indented. It means adding a `\t` character
     * at the beginning of the line (after the `\n` which marks the end of previous line).
     */
    fun indent() {
        // Contains all the indices which should be indented at once to avoid unnecessary redrawing
        val indicesToIndent = mutableSetOf<Int>()
        // Look for the line break before the selection start
        val previousLineBreak = editableText.substring(0, selectionStart).lastIndexOf("\n") + 1
        // Check whether the current line can be indented and indents it if necessary
        if (selectionCanBeIndented(previousLineBreak, selectionStart)) {
            indicesToIndent.add(previousLineBreak)
        }
        // This cycle goes line by line and tries to add an indent if possible
        var startIndex = selectionStart
        while (startIndex in selectionStart until selectionEnd) {
            val nextLineBreak = editableText.indexOf("\n", startIndex)
            // Next line break is found and it's within our selection
            if (nextLineBreak != -1 && nextLineBreak < selectionEnd) {
                // Check if the element after the line break can be indented
                if (selectionCanBeIndented(nextLineBreak + 1, nextLineBreak + 2)) {
                    indicesToIndent.add(nextLineBreak + 1)
                }
                // Move index after the line break
                startIndex = nextLineBreak + 1
            } else {
                break
            }
        }
        var offset = 0
        // Make sure we keep selection when we insert all the indents
        val previousSelectionStart = selectionStart
        val previousSelectionEnd = selectionEnd
        indicesToIndent.sorted().forEach {
            editableText.insert(it + offset, "\t")
            offset += 1
        }
        if (offset > 0) {
            // Set the selection over the same indented block
            editor.setSelection(previousSelectionStart + 1, previousSelectionEnd + offset)
        }
    }

    /**
     * This function tries to outdent all the selected lines that can be outdented. It means removing the `\t` character
     * at the beginning of the line (after the `\n` which marks the end of previous line).
     */
    fun outdent() {
        // Set of indices where we want to remove the indent
        val indicesToOutdent = mutableSetOf<Int>()
        // Look for the line break before the selection start
        val previousLineBreak = editableText.substring(0, selectionStart).lastIndexOf("\n")
        // Check whether there is an indent which can be removed
        if (previousLineBreak > 0 && selectionCanBeOutdented(previousLineBreak + 1, selectionStart)) {
            indicesToOutdent.add(previousLineBreak + 1)
        }
        // Special use case when the whole entry doesn't start with line break but starts with an indent
        if (previousLineBreak == -1 && editableText.startsWith("\t")) {
            if (selectionCanBeOutdented(0, 2)) {
                indicesToOutdent.add(0)
            }
        }
        var startIndex = selectionStart
        // Cycle goes over all the indents after a line break and removes them if they are within the selection
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
        // Keep selection over the outdent
        val previousSelectionStart = selectionStart
        val previousSelectionEnd = selectionEnd
        // Remove the indents from the set
        indicesToOutdent.sorted().forEach {
            editableText.replace(it - offset, it + 1 - offset, "")
            offset += 1
        }
        if (offset > 0) {
            editor.setSelection(previousSelectionStart - 1, previousSelectionEnd - offset)
        }
    }

    /**
     * Function that checks whether the selection either contains block spans that can be indented or doesn't contain
     * any spans that cannot be indented (media spans). The remaining use cases are simple text without any spans.
     */
    private fun selectionCanBeIndented(start: Int, end: Int): Boolean {
        val mediaSpans = editableText.getSpans(start, end, AztecDynamicImageSpan::class.java)
        if (mediaSpans.isNotEmpty()) return false
        val spans = editableText.getSpans(start, end, IAztecBlockSpan::class.java)
        return spans.isEmpty() || spans.all {
            it is ParagraphSpan || it is AztecHeadingSpan || it is AztecQuoteSpan || it is AztecPreformatSpan
        }
    }

    /**
     * Function that checks whether the selection either contains block spans that can be outdented or doesn't contain
     * any spans that cannot be indented (media spans). We also check that the selection starts with a `\t` that can
     * be removed.
     */
    private fun selectionCanBeOutdented(start: Int, end: Int): Boolean {
        val mediaSpans = editableText.getSpans(start + 1, end, AztecDynamicImageSpan::class.java)
        if (mediaSpans.isNotEmpty()) return false
        val spans = editableText.getSpans(start + 1, end, IAztecBlockSpan::class.java)
        return editableText[start] == "\t"[0] && (spans.isEmpty() || spans.all {
            it is ParagraphSpan || it is AztecHeadingSpan || it is AztecQuoteSpan || it is AztecPreformatSpan
        })
    }

    /**
     * Checks whether any line of the selection can be indented
     */
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

    /**
     * Checks whether any line of the selection can be outdented
     */
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

