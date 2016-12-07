/*
 * Copyright (C) 2016 Automattic
 * Copyright (C) 2015 Matthew Lee
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wordpress.aztec

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.CharacterStyle
import android.text.style.ImageSpan
import android.text.style.ParagraphStyle
import org.wordpress.aztec.spans.*
import java.util.*

class AztecParser {

    internal var hiddenIndex = 0
    internal var closeMap: TreeMap<Int, HiddenHtmlSpan> = TreeMap()
    internal var openMap: TreeMap<Int, HiddenHtmlSpan> = TreeMap()
    internal var hiddenSpans: IntArray = IntArray(0)
    internal var spanCursorPosition = -1

    fun fromHtml(source: String, context: Context): Spanned {
        val spanned = SpannableStringBuilder(Html.fromHtml(source, null, AztecTagHandler(), context))

        //Fix ranges of block/line-block elements
        spanned.getSpans(0, spanned.length, AztecLineBlockSpan::class.java).forEach {
            val spanStart = spanned.getSpanStart(it)
            var spanEnd = spanned.getSpanEnd(it)
            spanEnd = if (0 < spanEnd && spanEnd < spanned.length && spanned[spanEnd] == '\n') spanEnd - 1 else spanEnd
            spanned.removeSpan(it)
            spanned.setSpan(it, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            if (spanEnd == spanned.length && spanned[spanEnd - 1] == '\n' && it !is AztecListSpan) {
                spanned.delete(spanned.length - 1, spanned.length)
            }
        }

        return spanned
    }

    fun toHtml(text: Spanned, withCursor: Boolean = false): String {
        val out = StringBuilder()

        val cleanedUpText = markBlockElementLineBreaks(text)

        // add a marker to the end of the text to aid nested group parsing
        val data = SpannableStringBuilder(cleanedUpText).append('\u200B')

        //if there is no list or hidden html span at the end of the text, then we don't need zwj
        if (data.getSpans(data.length - 1, data.length, HiddenHtmlSpan::class.java).isEmpty() &&
                data.getSpans(data.length - 1, data.length, AztecListSpan::class.java).isEmpty()) {
            data.delete(data.length - 1, data.length)
        }

        resetHiddenTagParser(data)

        val hidden = data.getSpans(0, data.length, HiddenHtmlSpan::class.java)
        hiddenSpans = IntArray(hidden.size * 2)
        hidden.forEach {
            hiddenSpans[hiddenIndex++] = it.startOrder
            hiddenSpans[hiddenIndex++] = it.endOrder

            // make sure every hidden span is attached to a character
            val start = data.getSpanStart(it)
            val end = data.getSpanEnd(it)
            if (start == end && data[start] == '\n') {
                data.insert(start, "" + '\uFEFF')
            }
        }
        hiddenIndex = 0
        Arrays.sort(hiddenSpans)

        if (withCursor) {
            val cursorSpans = data.getSpans(0, data.length, AztecCursorSpan::class.java)
            if (!cursorSpans.isEmpty()) { //there can be only one cursor
                spanCursorPosition = data.getSpanStart(cursorSpans[0])
            }
        } else {
            spanCursorPosition = -1
        }

        withinHtml(out, data)
        return tidy(out.toString())
    }


    //TODO tidy up the logic
    //Apply special span to \n that enclose block elements in editor mode to avoid converting them to <br>
    fun markBlockElementLineBreaks(input: Spanned): Spanned {
        val text = SpannableStringBuilder(input)

        text.getSpans(0, text.length, AztecLineBlockSpan::class.java).forEach {
            val spanStart = text.getSpanStart(it)
            val spanEnd = text.getSpanEnd(it)

            val followingBlockElement = spanStart - 2 > 0 && text.getSpans(spanStart - 2, spanStart - 2, AztecLineBlockSpan::class.java).isNotEmpty()

            if (spanStart > 2 && text[spanStart - 1] == '\n' && text[spanStart - 2] != '\n' && !followingBlockElement) {
                text.setSpan(BlockElementLinebreak(), spanStart - 1, spanStart, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else if (spanStart > 2 && text[spanStart - 1] == '\n' && text[spanStart - 2] == '\n' && followingBlockElement) {
                //Look back and adjust position any unnecessary BlockElementLinebreak's
                text.getSpans(spanStart - 1, spanStart - 1, BlockElementLinebreak::class.java).forEach {
                    text.setSpan(it, text.getSpanStart(it) - 1, text.getSpanEnd(it) - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

            //AztecHeadingSpan had a bit different logic then the block spans
            if (it is AztecHeadingSpan) {
                if (spanEnd > 0 && text.length > spanEnd && text[spanEnd] == '\n') {
                    text.setSpan(BlockElementLinebreak(), spanEnd, spanEnd, Spanned.SPAN_MARK_MARK)
                } else if (spanEnd > 0 && text.length > spanEnd && text[spanEnd - 1] == '\n') {
                    text.setSpan(BlockElementLinebreak(), spanEnd - 1, spanEnd - 1, Spanned.SPAN_MARK_MARK)
                }
            } else {
                if (it is AztecListSpan && spanEnd - 1 > spanStart && text.length > spanEnd
                        && (text[spanEnd - 1] == '\u200B' || text[spanEnd - 2] == '\n')) {
                    text.setSpan(BlockElementLinebreak(), spanEnd - 1, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                } else if (text.length >= spanEnd && spanEnd - 2 > spanStart
                        && (text[spanEnd - 1] == '\u200B' && text[spanEnd - 2] == '\n')) {
                    text.setSpan(BlockElementLinebreak(), spanEnd - 2, spanEnd - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                } else if (text.length >= spanEnd && spanEnd - 2 > spanStart
                        && (text[spanEnd - 1] == '\u200B' && text[spanEnd] == '\n')) {
                    text.setSpan(BlockElementLinebreak(), spanEnd - 1, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                } else if (text.length > spanEnd && (text[spanEnd] == '\u200B' || text[spanEnd] == '\n')) {
                    text.setSpan(BlockElementLinebreak(), spanEnd - 1, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }


        }
        return text
    }


    private fun resetHiddenTagParser(text: Spanned) {
        // keeps track of the next span to be closed
        hiddenIndex = 0

        // keeps the spans, which will be closed in the future, using the closing order index as key
        closeMap.clear()
        openMap.clear()

        val spans = text.getSpans(0, text.length, HiddenHtmlSpan::class.java)
        spans.forEach(HiddenHtmlSpan::reset)
    }

    private fun withinHtml(out: StringBuilder, text: Spanned) {
        var next: Int

        var i = 0

        while (i < text.length) {
            next = text.nextSpanTransition(i, text.length, ParagraphStyle::class.java)

            val styles = text.getSpans(i, next, ParagraphStyle::class.java)

            if (styles.size == 2) {
                if (styles[0] is AztecListSpan && styles[1] is AztecQuoteSpan) {
                    withinQuoteThenList(out, text, i, next, styles[0] as AztecListSpan, styles[1] as AztecQuoteSpan)
                } else if (styles[0] is AztecQuoteSpan && styles[1] is AztecListSpan) {
                    withinListThenQuote(out, text, i, next++, styles[1] as AztecListSpan)
                } else {
                    withinContent(out, text, i, next)
                }
            } else if (styles.size == 1) {
                if (styles[0] is AztecListSpan) {
                    withinList(out, text, i, next, styles[0] as AztecListSpan)
                } else if (styles[0] is AztecQuoteSpan) {
                    withinQuote(out, text, i, next)
                } else if (styles[0] is UnknownHtmlSpan) {
                    withinUnknown(styles[0] as UnknownHtmlSpan, out)
                } else if (styles[0] is ParagraphSpan) {
                    withinParagraph(out, text, i, next)
                } else {
                    withinContent(out, text, i, next)
                }
            } else {
                withinContent(out, text, i, next)
            }
            i = next
        }
    }

    private fun withinUnknown(unknownHtmlSpan: UnknownHtmlSpan, out: StringBuilder) {
        out.append(unknownHtmlSpan.getRawHtml())
    }

    private fun withinListThenQuote(out: StringBuilder, text: Spanned, start: Int, end: Int, list: AztecListSpan) {
        out.append("<${list.getStartTag()}><li>")
        withinQuote(out, text, start, end)
        out.append("</li></${list.getEndTag()}>")
    }

    private fun withinQuoteThenList(out: StringBuilder, text: Spanned, start: Int, end: Int, list: AztecListSpan, quote: AztecQuoteSpan) {
        out.append("<${quote.getStartTag()}>")
        withinList(out, text, start, end, list)
        out.append("</${quote.getEndTag()}>")
    }

    private fun withinList(out: StringBuilder, text: Spanned, start: Int, end: Int, list: AztecListSpan) {
        val newEnd = end - 1
        val listContent = text.subSequence(start..newEnd) as Spanned

        out.append("<${list.getStartTag()}>")
        val lines = TextUtils.split(listContent.toString(), "\n")

        for (i in lines.indices) {

            val lineLength = lines[i].length
            val lineStart = (0..i - 1).sumBy { lines[it].length + 1 }

            val isAtTheEndOfText = text.length == lineStart + 1

            val lineIsZWJ = lineLength == 1 && lines[i][0] == '\u200B'
            val isLastLineInList = lines.indices.last == i
            val lineEnd = lineStart + lineLength

            if (lineStart > lineEnd || (isAtTheEndOfText && lineIsZWJ) || (lineStart == lineEnd && isLastLineInList)) {
                continue
            }

            val itemSpanStart = start + lineStart + lineLength
            val itemSpans = text.getSpans(itemSpanStart, itemSpanStart + 1, AztecListItemSpan::class.java)

            if (itemSpans.isNotEmpty()) {
                out.append("<li${itemSpans[0].attributes}>")
//            } else if (i == lines.lastIndex) {
//                out.append("<li${list.lastItem.attributes}>")
            } else {
                out.append("<li>")
            }

            //special case for when cursor might be in empty list item
            if ((lineLength == 0 || lineIsZWJ)
                    && !listContent.getSpans(lineStart, lineEnd, AztecCursorSpan::class.java).isEmpty()
                    && !containsCursor(out)) {
                out.append(AztecCursorSpan.AZTEC_CURSOR_TAG)
            }

            withinContent(out, text.subSequence(start..newEnd) as Spanned, lineStart, lineEnd)
            out.append("</li>")
        }
        out.append("</${list.getEndTag()}>")
    }

    private fun withinParagraph(out: StringBuilder, text: Spanned, start: Int, end: Int) {
        var next: Int

        var i = start
        while (i < end) {
            next = text.nextSpanTransition(i, end, ParagraphSpan::class.java)

            val paragraphs = text.getSpans(i, next, ParagraphSpan::class.java)
            for (paragraph in paragraphs) {
                out.append("<${paragraph.getStartTag()}>")
            }

            withinContent(out, text, i, next)

            for (paragraph in paragraphs) {
                out.append("</${paragraph.getEndTag()}>")
            }
            i = next
        }
    }

    private fun withinHeading(out: StringBuilder, headingContent: Spanned, span: AztecHeadingSpan) {
        //remove the heading span from the text we are about to process
        val cleanHeading = SpannableStringBuilder(headingContent)
        cleanHeading.removeSpan(span)

        val lines = TextUtils.split(cleanHeading.toString(), "\n")
        for (i in lines.indices) {
            val lineLength = lines[i].length

            val lineStart = (0..i - 1).sumBy { lines[it].length + 1 }
            val lineEnd = lineStart + lineLength

            if (lineLength == 0) continue

            out.append("<${span.getStartTag()}>")
            withinContent(out, cleanHeading, lineStart, lineEnd, true)
            out.append("</${span.getEndTag()}>")
        }
    }

    private fun withinQuote(out: StringBuilder, text: Spanned, start: Int, end: Int) {
        var next: Int

        var i = start
        while (i < end) {
            next = text.nextSpanTransition(i, end, AztecQuoteSpan::class.java)

            val quotes = text.getSpans(i, next, AztecQuoteSpan::class.java)
            for (quote in quotes) {
                out.append("<${quote.getStartTag()}>")
            }

            withinContent(out, text, i, next)

            for (quote in quotes) {
                out.append("</${quote.getEndTag()}>")
            }
            i = next
        }
    }

    private fun withinContent(out: StringBuilder, text: Spanned, start: Int, end: Int, ignoreHeading: Boolean = false) {
        var next: Int

        var i = start
        while (i < end) {
            next = TextUtils.indexOf(text, '\n', i, end)
            if (next < 0) {
                next = end
            }

            var nl = 0
            while (next < end && text[next] == '\n') {
                val isVisualLinebreak = text.getSpans(next, next, BlockElementLinebreak::class.java).isNotEmpty()

                if (!isVisualLinebreak) {
                    nl++
                }
                next++
            }

            //account for possible zero-width joiner at the end of the line
            val zwjModifer = if (text[next - 1] == '\u200B') 1 else 0

            withinParagraph(out, text, i, next - nl - zwjModifer, nl, ignoreHeading)

            i = next
        }
    }

    // Copy from https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/text/Html.java,
    // remove some tag because we don't need them in Aztec.
    private fun withinParagraph(out: StringBuilder, text: Spanned, start: Int, end: Int, nl: Int, ignoreHeadingSpanCheck: Boolean = false) {
        var next: Int

        //special logic in case we encounter line that is a heading span
        if (!ignoreHeadingSpanCheck) {
            var isHeadingSpanEncountered = false
            text.getSpans(start, end, AztecHeadingSpan::class.java).forEach {
                //go inside heading span and style it's content
                withinHeading(out, text.subSequence(start, end) as Spanned, it)
                isHeadingSpanEncountered = true
            }
            if (isHeadingSpanEncountered) {
                for (i in 0..nl - 1) {
                    if (end + i == spanCursorPosition && !containsCursor(out)) {
                        out.append(AztecCursorSpan.AZTEC_CURSOR_TAG)
                    }
                    out.append("<br>")
                }
                return@withinParagraph
            }
        }

        run {
            var i = start

            while (i < end || start == end) {
                next = text.nextSpanTransition(i, end, CharacterStyle::class.java)

                val localCursorPosition = getLocalCursorPosition(text, if (i > 0) i - 1 else 0, next)

                val spans = text.getSpans(i, next, CharacterStyle::class.java)
                for (j in spans.indices) {
                    val span = spans[j]

                    if (span is AztecContentSpan) {
                        out.append("<${span.getStartTag()}>")
                    }

                    if (span is ImageSpan && span !is AztecCommentSpan && span !is UnknownHtmlSpan) {
                        out.append("<img src=\"")
                        out.append(span.source)
                        out.append("\">")

                        // Don't output the dummy character underlying the image.
                        i = next
                    }

                    if (span is AztecCommentSpan || span is CommentSpan) {
                        out.append("<!--")
                    }

                    if (span is HiddenHtmlSpan) {
                        parseHiddenSpans(i, out, span, text)
                    }
                }

                withinStyle(out, text, i, next)

                if (spanCursorPosition != -1 && localCursorPosition != -1 && !containsCursor(out)) {
                    //TODO sometimes cursor lands right before > symbol, until we figure out why use modifier to fix it
                    val cursorInsertionPoint = out.length - (next - localCursorPosition)
                    val modifier = if (out.length > cursorInsertionPoint && out[cursorInsertionPoint] == '>') 1 else 0
                    out.insert(cursorInsertionPoint + modifier, AztecCursorSpan.AZTEC_CURSOR_TAG)
                }

                for (j in spans.indices.reversed()) {
                    val span = spans[j]

                    if (span is AztecContentSpan) {
                        out.append("</${span.getEndTag()}>")
                    }

                    if (span is AztecCommentSpan || span is CommentSpan) {
                        out.append("-->")
                    }

                    if (span is HiddenHtmlSpan) {
                        parseHiddenSpans(next, out, span, text)
                    }
                }

                if (start == end)
                    break

                i = next
            }
        }

        for (i in 0..nl - 1) {
            //in case cursor the is near <br> tag
            if (end + i == spanCursorPosition && !containsCursor(out)) {
                out.append(AztecCursorSpan.AZTEC_CURSOR_TAG)
            }
            out.append("<br>")
        }
    }

    fun containsCursor(text: CharSequence): Boolean {
        return text.indexOf(AztecCursorSpan.AZTEC_CURSOR_TAG) != -1
    }

    fun getLocalCursorPosition(text: Spanned, start: Int, end: Int): Int {
        var cursorPosition = -1

        text.getSpans(start, end, AztecCursorSpan::class.java).forEach {
            cursorPosition = text.getSpanStart(it)

            //if the cursor is inside unknown html span we need to account for html inside it
            if (text.getSpans(start, end, UnknownHtmlSpan::class.java).isNotEmpty()) {
                val unknownSpan = text.getSpans(start, end, UnknownHtmlSpan::class.java)[0]

                val unknownSpanStart = text.getSpanStart(unknownSpan)
                val unknownSpanEnd = text.getSpanEnd(unknownSpan)

                if (cursorPosition == unknownSpanStart) {
                    return unknownSpanEnd - unknownSpan.getRawHtml().length
                } else if (cursorPosition == unknownSpanEnd) {
                    return unknownSpanEnd
                }
            }

            text.getSpans(start, end, BlockElementLinebreak::class.java).forEach {
                val spanStart = text.getSpanStart(it)
                val spanEnd = text.getSpanEnd(it)

                //special case for when cursor is before list
                val isBeforeList = text.getSpans(spanEnd, spanEnd + 1, AztecListItemSpan::class.java).isNotEmpty()

                if (isBeforeList && (cursorPosition == spanStart || cursorPosition == spanEnd)) {
                    cursorPosition--
                }
            }
        }

        return cursorPosition
    }

    private fun parseHiddenSpans(position: Int, out: StringBuilder, span: HiddenHtmlSpan, text: Spanned) {
        closeMap.put(span.endOrder, span)
        openMap.put(span.startOrder, span)

        var last: Int
        do {
            last = hiddenIndex

            if (hiddenIndex >= hiddenSpans.size)
                break

            val nextSpanIndex = hiddenSpans[hiddenIndex]

            if (openMap.contains(nextSpanIndex)) {

                val nextSpan = openMap[nextSpanIndex]!!
                if (!nextSpan.isOpened && text.getSpanStart(nextSpan) == position) {
                    out.append(nextSpan.startTag)
                    nextSpan.open()
                    hiddenIndex++
                }
            }

            if (closeMap.containsKey(nextSpanIndex)) {

                val nextSpan = closeMap[nextSpanIndex]!!
                if (!nextSpan.isParsed && text.getSpanEnd(nextSpan) == position) {
                    out.append(nextSpan.endTag)
                    nextSpan.parse()
                    hiddenIndex++
                }
            }

        } while (last != hiddenIndex)
    }

    private fun withinStyle(out: StringBuilder, text: CharSequence, start: Int, end: Int) {
        var i = start
        while (i < end) {
            val c = text[i]

            if (c == '\u200B') {
                i++
                continue
            }

            if (c == '<') {
                out.append("&lt;")
            } else if (c == '>') {
                out.append("&gt;")
            } else if (c == '&') {
                out.append("&amp;")
            } else if (c.toInt() >= 0xD800 && c.toInt() <= 0xDFFF) {
                if (c.toInt() < 0xDC00 && i + 1 < end) {
                    val d = text[i + 1]
                    if (d.toInt() >= 0xDC00 && d.toInt() <= 0xDFFF) {
                        i++
                        val codepoint = 0x010000 or c.toInt() - 0xD800 shl 10 or d.toInt() - 0xDC00
                        out.append("&#").append(codepoint).append(";")
                    }
                }
            } else if (c.toInt() > 0x7E || c < ' ') {
                if (c != '\n') {
                    out.append("&#").append(c.toInt()).append(";")
                }
            } else if (c == ' ') {
                while (i + 1 < end && text[i + 1] == ' ') {
                    out.append("&nbsp;")
                    i++
                }

                out.append(' ')
            } else {
                out.append(c)
            }
            i++
        }
    }

    private fun tidy(html: String): String {
        return html
                .replace("&#8203;", "")
                .replace("&#65279;", "")
                .replace("(</? ?br>)*</blockquote>".toRegex(), "</blockquote>")
                .replace("(</? ?br>)*</p>".toRegex(), "</p>")
                .replace("(</? ?br>)*</li>".toRegex(), "</li>")
    }
}
