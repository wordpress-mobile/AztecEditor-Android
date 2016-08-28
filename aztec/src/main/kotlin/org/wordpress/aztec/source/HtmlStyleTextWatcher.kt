package org.wordpress.aztec.source

import android.support.annotation.ColorInt
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher

class HtmlStyleTextWatcher(@ColorInt private val tagColor: Int, @ColorInt private val attributeColor: Int) : TextWatcher {

    private enum class Operation {
        INSERT, DELETE, REPLACE, NONE
    }

    private var offset: Int = 0
    private var modifiedText: CharSequence? = null
    private var lastOperation: Operation? = null

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        if (s == null) {
            return
        }

        val lastCharacterLocation = start + count - 1
        if (s.length > lastCharacterLocation && lastCharacterLocation >= 0) {
            if (after < count) {
                if (after > 0) {
                    // Text was deleted and replaced by some other text
                    lastOperation = Operation.REPLACE
                } else {
                    // Text was deleted only
                    lastOperation = Operation.DELETE
                }

                offset = start
                modifiedText = s.subSequence(start + after, start + count)
            }
        }
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (s == null) {
            return
        }

        val lastCharacterLocation = start + count - 1
        if (s.length > lastCharacterLocation) {
            if (count > 0) {
                if (before > 0) {
                    // Text was added, replacing some existing text
                    lastOperation = Operation.REPLACE
                    modifiedText = s.subSequence(start, start + count)
                } else {
                    // Text was added only
                    lastOperation = Operation.INSERT
                    offset = start
                    modifiedText = s.subSequence(start + before, start + count)
                }
            }
        }
    }

    override fun afterTextChanged(s: Editable?) {
        if (modifiedText == null || s == null) {
            return
        }

        var spanRange: SpanRange?

        // If the modified text included a tag or entity symbol ("<", ">", "&" or ";"), find its match and restyle
        if (modifiedText!!.toString().contains("<")) {
            spanRange = getRespanRangeForChangedOpeningSymbol(s, "<")
        } else if (modifiedText!!.toString().contains(">")) {
            spanRange = getRespanRangeForChangedClosingSymbol(s, ">")
        } else if (modifiedText!!.toString().contains("&")) {
            spanRange = getRespanRangeForChangedOpeningSymbol(s, "&")
        } else if (modifiedText!!.toString().contains(";")) {
            spanRange = getRespanRangeForChangedClosingSymbol(s, ";")
        } else {
            // If the modified text didn't include any tag or entity symbols, restyle if the modified text is inside
            // a tag or entity
            spanRange = getRespanRangeForNormalText(s, "<")
            if (spanRange == null) {
                spanRange = getRespanRangeForNormalText(s, "&")
            }
        }

        if (spanRange != null) {
            updateSpans(s, spanRange)
        }

        modifiedText = null
        lastOperation = Operation.NONE
    }

    /**
     * For changes made which contain at least one opening symbol (e.g. '<' or '&'), whether added or deleted, returns
     * the range of text which should have its style reapplied.
     * @param content the content after modification
     * *
     * @param openingSymbol the opening symbol recognized (e.g. '<' or '&')
     * *
     * @return the range of characters to re-apply spans to
     */
    protected fun getRespanRangeForChangedOpeningSymbol(content: Editable, openingSymbol: String): SpanRange? {
        // For simplicity, re-parse the document if text was replaced
        if (lastOperation == Operation.REPLACE) {
            return SpanRange(0, content.length)
        }

        val closingSymbol = getMatchingSymbol(openingSymbol)

        val firstOpeningTagLoc = offset + modifiedText!!.toString().indexOf(openingSymbol)
        val closingTagLoc: Int
        if (lastOperation == Operation.INSERT) {
            // Apply span from the first added opening symbol until the closing symbol in the content matching the
            // last added opening symbol
            // e.g. pasting "<b><" before "/b>" - we want the span to be applied to all of "<b></b>"
            val lastOpeningTagLoc = offset + modifiedText!!.toString().lastIndexOf(openingSymbol)
            closingTagLoc = content.toString().indexOf(closingSymbol, lastOpeningTagLoc)
        } else {
            // Apply span until the first closing tag that appears after the deleted text
            closingTagLoc = content.toString().indexOf(closingSymbol, offset)
        }

        if (closingTagLoc > 0) {
            return SpanRange(firstOpeningTagLoc, closingTagLoc + 1)
        }
        return null
    }

    /**
     * For changes made which contain at least one closing symbol (e.g. '>' or ';') and no opening symbols, whether
     * added or deleted, returns the range of text which should have its style reapplied.
     * @param content the content after modification
     * *
     * @param closingSymbol the closing symbol recognized (e.g. '>' or ';')
     * *
     * @return the range of characters to re-apply spans to
     */
    protected fun getRespanRangeForChangedClosingSymbol(content: Editable, closingSymbol: String): SpanRange? {
        // For simplicity, re-parse the document if text was replaced
        if (lastOperation == Operation.REPLACE) {
            return SpanRange(0, content.length)
        }

        val openingSymbol = getMatchingSymbol(closingSymbol)

        val firstClosingTagInModLoc = offset + modifiedText!!.toString().indexOf(closingSymbol)
        val firstClosingTagAfterModLoc = content.toString().indexOf(closingSymbol, offset + modifiedText!!.length)

        val openingTagLoc = content.toString().lastIndexOf(openingSymbol, firstClosingTagInModLoc - 1)
        if (openingTagLoc >= 0) {
            if (firstClosingTagAfterModLoc >= 0) {
                return SpanRange(openingTagLoc, firstClosingTagAfterModLoc + 1)
            } else {
                return SpanRange(openingTagLoc, content.length)
            }
        }
        return null
    }

    /**
     * For changes made which contain no opening or closing symbols, checks whether the changed text is inside a tag,
     * and if so returns the range of text which should have its style reapplied.
     * @param content the content after modification
     * *
     * @param openingSymbol the opening symbol of the tag to check for (e.g. '<' or '&')
     * *
     * @return the range of characters to re-apply spans to
     */
    protected fun getRespanRangeForNormalText(content: Editable, openingSymbol: String): SpanRange? {
        val closingSymbol = getMatchingSymbol(openingSymbol)

        val openingTagLoc = content.toString().lastIndexOf(openingSymbol, offset)
        if (openingTagLoc >= 0) {
            val closingTagLoc = content.toString().indexOf(closingSymbol, openingTagLoc)
            if (closingTagLoc >= offset) {
                return SpanRange(openingTagLoc, closingTagLoc + 1)
            }
        }
        return null
    }

    /**
     * Clears and re-applies spans to `content` within range `spanRange` according to rules in
     * [HtmlStyleUtils].
     * @param content the content to re-style
     * *
     * @param spanRange the range within `content` to be re-styled
     */
    protected fun updateSpans(content: Spannable, spanRange: SpanRange) {
        var spanStart = spanRange.openingTagLoc
        var spanEnd = spanRange.closingTagLoc

        if (spanStart > content.length || spanEnd > content.length) {
            //            AppLog.d(T.EDITOR, "The specified span range was beyond the Spannable's length");
            return
        } else if (spanStart >= spanEnd) {
            // If the span start is after the end position (probably due to a multi-line deletion), selective
            // re-styling won't work
            // Instead, do a clean re-styling of the whole document
            spanStart = 0
            spanEnd = content.length
        }

        HtmlStyleUtils.clearSpans(content, spanStart, spanEnd)
        HtmlStyleUtils.styleHtmlForDisplay(content, spanStart, spanEnd, tagColor, attributeColor)
    }

    /**
     * Returns the closing/opening symbol corresponding to the given opening/closing symbol.
     */
    private fun getMatchingSymbol(symbol: String): String {
        when (symbol) {
            "<" -> return ">"
            ">" -> return "<"
            "&" -> return ";"
            ";" -> return "&"
            else -> return ""
        }
    }

    /**
     * Stores a pair of integers describing a range of values.
     */
    protected class SpanRange(val openingTagLoc: Int, val closingTagLoc: Int)
}