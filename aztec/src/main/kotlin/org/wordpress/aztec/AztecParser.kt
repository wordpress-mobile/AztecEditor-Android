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
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.*
import org.wordpress.aztec.spans.*
import java.util.*

class AztecParser {

    internal var hiddenIndex = 0
    internal var closeMap: TreeMap<Int, HiddenHtmlSpan> = TreeMap()
    internal var openMap: TreeMap<Int, HiddenHtmlSpan> = TreeMap()
    internal var hiddenSpans: IntArray = IntArray(0)

    fun fromHtml(source: String, context: Context): Spanned {
        return Html.fromHtml(source, null, AztecTagHandler(), context)
    }

    fun toHtml(text: Spanned): String {
        val out = StringBuilder()

        // add a marker to the end of the text to aid nested group parsing
        val data = SpannableStringBuilder(text).append('\u200B')

        resetHiddenTagParser(text)

        val hidden = text.getSpans(0, text.length, HiddenHtmlSpan::class.java)
        hiddenSpans = IntArray(hidden.size * 2)
        hidden.forEach {
            hiddenSpans[hiddenIndex++] = it.startOrder
            hiddenSpans[hiddenIndex++] = it.endOrder
        }
        hiddenIndex = 0
        Arrays.sort(hiddenSpans)

        withinHtml(out, data)
        return tidy(out.toString())
    }

    private fun resetHiddenTagParser(text: Spanned) {
        // keeps track of the next span to be closed
        hiddenIndex = 0

        // keeps the spans, which will be closed in the future, using the closing order index as key
        closeMap.clear()
        openMap.clear()

        val spans = text.getSpans(0, text.length, HiddenHtmlSpan::class.java)
        spans.forEach {
            it.reset()
        }
    }

    private fun withinHtml(out: StringBuilder, text: Spanned) {
        var next: Int

        var i = 0

        while (i < text.length) {
            next = text.nextSpanTransition(i, text.length, ParagraphStyle::class.java)

            val styles = text.getSpans(i, next, ParagraphStyle::class.java)
            if (styles.size == 2) {
                if (styles[0] is AztecListSpan && styles[1] is AztecQuoteSpan) {
                    withinQuoteThenList(out, text, i, next++, styles[0] as AztecListSpan, styles[1] as AztecQuoteSpan)
                } else if (styles[0] is AztecQuoteSpan && styles[1] is AztecListSpan) {
                    withinListThenQuote(out, text, i, next++, styles[1] as AztecListSpan, styles[0] as AztecQuoteSpan)
                } else {
                    withinContent(out, text, i, next)
                }
            } else if (styles.size == 1) {
                if (styles[0] is AztecListSpan) {
                    withinList(out, text, i, next, styles[0] as AztecListSpan)
                } else if (styles[0] is AztecQuoteSpan) {
                    withinQuote(out, text, i, next++)
                } else if (styles[0] is UnknownHtmlSpan) {
                    withinUnknown(styles[0] as UnknownHtmlSpan, out)
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

    private fun withinListThenQuote(out: StringBuilder, text: Spanned, start: Int, end: Int, list: AztecListSpan, quote: AztecQuoteSpan) {
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
        var newStart = start
        var newEnd = end - 1

        if (text[newStart] == '\n') {
            newStart += 1

            if (text.length < newEnd + 1) {
                newEnd += 1
            }
        }

        out.append("<${list.getStartTag()}>")
        val lines = TextUtils.split(text.substring(newStart..newEnd), "\n")

        for (i in lines.indices) {

            val lineLength = lines[i].length

            var lineStart = 0
            for (j in 0..i - 1) {
                lineStart += lines[j].length + 1
            }

            val isAtTheEndOfText = text.length == lineStart + 1

            val lineIsZWJ = lineLength == 1 && lines[i][0] == '\u200B'
            val isLastLineInList = lines.indices.last == i

            val lineEnd = lineStart + lineLength

            if (lineStart > lineEnd || (isAtTheEndOfText && lineIsZWJ) || (lineLength == 0 && isLastLineInList)) {
                continue
            }
            val itemSpans = text.getSpans(newStart + lineStart + lineLength, newStart + lineStart + lineLength + 1, AztecListItemSpan::class.java)

            if (itemSpans.size > 0) {
                out.append("<li${itemSpans[0].attributes}>")
            } else {
                out.append("<li>")
            }
            withinContent(out, text.subSequence(newStart..newEnd) as Spanned, lineStart, lineEnd)
            out.append("</li>")
        }
        out.append("</${list.getEndTag()}>")
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

    private fun withinContent(out: StringBuilder, text: Spanned, start: Int, end: Int) {
        var next: Int

        var i = start
        while (i < end) {
            next = TextUtils.indexOf(text, '\n', i, end)
            if (next < 0) {
                next = end
            }

            var nl = 0
            while (next < end && text[next] == '\n') {
                next++
                nl++
            }

            //account for possible zero-width joiner at the end of the line
            val zwjModifer = if (text[next - 1] == '\u200B') 1 else 0

            withinParagraph(out, text, i, next - nl - zwjModifer, nl)

            i = next
        }
    }

    // Copy from https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/text/Html.java,
    // remove some tag because we don't need them in Aztec.
    private fun withinParagraph(out: StringBuilder, text: Spanned, start: Int, end: Int, nl: Int) {
        var next: Int

        run {
            var i = start

            while (i < end || start == end) {
                next = text.nextSpanTransition(i, end, CharacterStyle::class.java)

                val spans = text.getSpans(i, next, CharacterStyle::class.java)
                for (j in spans.indices) {
                    val span = spans[j]

                    if (span is AztecContentSpan) {
                        out.append("<${span.getStartTag()}>")
                    }

                    if (span is ImageSpan && span !is UnknownHtmlSpan) {
                        out.append("<img src=\"")
                        out.append(span.source)
                        out.append("\">")

                        // Don't output the dummy character underlying the image.
                        i = next
                    }

                    if (span is CommentSpan) {
                        out.append("<!--")
                    }

                    if (span is HiddenHtmlSpan) {
                        parseHiddenSpans(i, out, span, text)
                    }
                }

                withinStyle(out, text, i, next)

                for (j in spans.indices.reversed()) {
                    val span = spans[j]

                    if (span is AztecContentSpan) {
                        out.append("</${span.getEndTag()}>")
                    }

                    if (span is CommentSpan) {
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
            out.append("<br>")
        }
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
                out.append("&#").append(c.toInt()).append(";")
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
        return html.replace("</ul>(<br>)?".toRegex(), "</ul>")
                .replace("(<br>)*(<ul>?)".toRegex(), "$2")
                .replace("</ol>(<br>)?".toRegex(), "</ol>")
                .replace("(<br>)*(<ol>?)".toRegex(), "$2")
                .replace("</blockquote>(<br>)?".toRegex(), "</blockquote>")
                .replace("&#8203;", "")
                .replace("(<br>)*</blockquote>".toRegex(), "</blockquote>")
                .replace("(<br>)*</li>".toRegex(), "</li>")

    }
}
