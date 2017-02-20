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
import android.text.*
import android.text.style.CharacterStyle
import org.wordpress.aztec.AztecText.OnMediaTappedListener
import org.wordpress.aztec.spans.*
import java.util.*

class AztecParser {

    internal var hiddenIndex = 0
    internal var closeMap: TreeMap<Int, HiddenHtmlSpan> = TreeMap()
    internal var openMap: TreeMap<Int, HiddenHtmlSpan> = TreeMap()
    internal var hiddenSpans: IntArray = IntArray(0)
    internal var spanCursorPosition = -1

    fun fromHtml(source: String, onMediaTappedListener: OnMediaTappedListener?,
                 onUnknownHtmlClickListener: UnknownHtmlSpan.OnUnknownHtmlClickListener?, context: Context): Spanned {
        val tidySource = tidy(source)

        val spanned = SpannableStringBuilder(Html.fromHtml(tidySource, AztecTagHandler(),
                onMediaTappedListener, onUnknownHtmlClickListener, context))

        addVisualNewlinesToBlockElements(spanned)
        addZwjCharToBlockSpans(spanned)

        return spanned
    }

    fun toHtml(text: Spanned, withCursor: Boolean = false): String {
        val out = StringBuilder()

        // add a marker to the end of the text to aid nested group parsing
        val data = SpannableStringBuilder(text).append(Constants.ZWJ_CHAR)

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
                data.insert(start, "" + Constants.MAGIC_CHAR)
            }
        }
        hiddenIndex = 0
        Arrays.sort(hiddenSpans)

        if (withCursor) {
            val cursorSpan = data.getSpans(0, data.length, AztecCursorSpan::class.java).firstOrNull()
            if (cursorSpan != null) { //there can be only one cursor
                spanCursorPosition = data.getSpanStart(cursorSpan)
            }
        } else {
            spanCursorPosition = -1
        }

        withinHtml(out, data)
        return tidy(out.toString())
    }

    private fun addZwjCharToBlockSpans(spanned: SpannableStringBuilder) {
        // add ZWJ char after newline of block spans so they can be closed by hitting enter
        spanned.getSpans(0, spanned.length, AztecBlockSpan::class.java).forEach {
            val start = spanned.getSpanStart(it)
            val end = spanned.getSpanEnd(it)

            if (spanned[end - 1] == '\n' && (end - start == 1 || spanned[end - 2] == '\n')) {
                spanned.insert(end - 1, Constants.ZWJ_STRING)

                if (end - start == 1) {
                    spanned.setSpan(it, start, end + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }
    }

    private fun markBlockElementLineBreak(text: Spannable, startPos: Int) {
        text.setSpan(BlockElementLinebreak(), startPos, startPos, Spanned.SPAN_MARK_MARK)
    }

    fun addVisualNewlinesToBlockElements(spanned: Editable) {
        // add visual newlines at starts
        spanned.getSpans(0, spanned.length, AztecLineBlockSpan::class.java).forEach {
            val spanStart = spanned.getSpanStart(it)

            // no need for newline if at text start
            if (spanStart < 1) {
                return@forEach
            }

            // no need for newline if there's already one
            if (spanned[spanStart - 1] == '\n') {
                return@forEach
            }

            // well, it seems we need a visual newline so, add one and mark it as such
            spanned.insert(spanStart, "\n")
            markBlockElementLineBreak(spanned, spanStart)
        }

        // add visual newlines at ends
        spanned.getSpans(0, spanned.length, AztecLineBlockSpan::class.java).forEach {
            val spanEnd = spanned.getSpanEnd(it)

            // no need for newline if at text end
            if (spanEnd == spanned.length) {
                return@forEach
            }

            // no need for newline if there's one and marked as visual
            if (spanned[spanEnd] == '\n'
                    && spanned.getSpans(spanEnd, spanEnd, BlockElementLinebreak::class.java).isNotEmpty()) {
                return@forEach
            }

            // well, it seems we need a visual newline so, add one and mark it as such
            spanned.insert(spanEnd, "\n")
            markBlockElementLineBreak(spanned, spanEnd)
        }
    }

    // Always try to put a visual newline before block elements and only put one after if needed
    fun syncVisualNewlinesOfBlockElements(spanned: Editable) {
        // clear any visual newline marking. We'll mark them with a fresh set of passes
        spanned.getSpans(0, spanned.length, BlockElementLinebreak::class.java).forEach {
            spanned.removeSpan(it)
        }

        spanned.getSpans(0, spanned.length, AztecLineBlockSpan::class.java).forEach {
            val spanStart = spanned.getSpanStart(it)

            if (spanStart < 1) {
                // no newline if at text start so, return
                return@forEach
            }

            if (spanStart < 2) {
                // no visual newline can exist unless there are at least 2 chars before the block (one will be the newline
                //  and the other will be the leading content) so, return
                return@forEach
            }

            if (spanned[spanStart - 1] != '\n') {
                // no newline before so, nothing to mark as visual newline
                return@forEach
            }

            if (spanned.getSpans(spanStart - 1, spanStart - 1, BlockElementLinebreak::class.java).isNotEmpty()) {
                // there's a visual newline already set so, nothing to do here
                return@forEach
            }

            // at last, all checks passed so, let's mark the newline as visual!
            markBlockElementLineBreak(spanned, spanStart - 1)
        }

        // add visual newlines at ends
        spanned.getSpans(0, spanned.length, AztecLineBlockSpan::class.java).forEach {
            val spanEnd = spanned.getSpanEnd(it)

            if (spanEnd == spanned.length) {
                // no newline if at text end
                return@forEach
            }

            if (spanned[spanEnd] != '\n') {
                // no newline after so, nothing to mark as visual newline
                return@forEach
            }

            val firstNonNewlineCharIndex = spanEnd
                    + spanned.subSequence(spanEnd..spanned.length-1).indexOfFirst { it != '\n' }
                    - 1 // go back one to reach the last newline
            if (firstNonNewlineCharIndex != -1 && spanned.getSpans(firstNonNewlineCharIndex, firstNonNewlineCharIndex,
                    BlockElementLinebreak::class.java).isNotEmpty()) {
                // there's a visual newline already set after us so, nothing to do here. Counting on the 1st phase (the
                //  newlines at block starts) to have done its job.
                return@forEach
            }

            // at last, all checks passed so, let's mark the newline as visual!
            markBlockElementLineBreak(spanned, spanEnd)
        }
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
        withinHtml(out, text, 0, text.length, null)
    }

    private fun withinHtml(out: StringBuilder, text: Spanned, start: Int, end: Int,
            grandParents: ArrayList<AztecParagraphStyle>?) {
        var next: Int
        var i = start
        var parents: ArrayList<AztecParagraphStyle>?

        do {
            var paragraph = text.getSpans(i, end,AztecParagraphStyle::class.java)
                    .firstOrNull { grandParents?.contains(it)?.not() ?: true }

            if (paragraph == null) {
                // no paragraph found so, just consume all available chars
                next = end
                parents = grandParents
            } else if (text.getSpanStart(paragraph) > i) {
                // the start of the paragraph is further down so, we'll handle it at next iteration.
                next = text.getSpanStart(paragraph)
                paragraph = null
                parents = grandParents
            } else {
                // nice, we found the start of a paragraph so, prepare to go deeper to parse it
                next = text.getSpanEnd(paragraph)
                parents = ArrayList<AztecParagraphStyle>(grandParents ?: ArrayList())
                parents.add(paragraph)
            }

            when (paragraph) {
                is AztecBlockSpan -> withinBlock(out, text, i, next, paragraph, parents)
                is UnknownHtmlSpan -> withinUnknown(out, text, i, next, paragraph)
                else -> withinContent(out, text, i, next)
            }

            i = next
        } while (i < end)

        consumeCursorIfInInput(out, text, text.length)
    }

    private fun withinUnknown(out: StringBuilder, text: Spanned, start: Int, end: Int, unknownHtmlSpan: UnknownHtmlSpan) {
        consumeCursorIfInInput(out, text, start)
        out.append(unknownHtmlSpan.rawHtml)
        consumeCursorIfInInput(out, text, end)
    }

    private fun withinBlock(out: StringBuilder, text: Spanned, start: Int, end: Int,
                            blockSpan: AztecBlockSpan, parents: ArrayList<AztecParagraphStyle>?) {
        out.append("<${blockSpan.getStartTag()}>")
        withinHtml(out, text, start, end, parents)
        out.append("</${blockSpan.getEndTag()}>")
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

            withinParagraph(out, text, i, next - nl, nl, ignoreHeading)

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
                    out.append("<br>")
                    consumeCursorIfInInput(out, text,  end + i)
                }
                return@withinParagraph
            }
        }

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

                    if (span is AztecCommentSpan || span is CommentSpan) {
                        out.append("<!--")
                    }

                    if (span is AztecMediaSpan) {
                        out.append(span.getHtml())
                        i = next
                    }

                    if (span is HiddenHtmlSpan) {
                        parseHiddenSpans(i, out, span, text)
                    }
                }

                withinStyle(out, text, i, next, nl)

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
            out.append("<br>")
            consumeCursorIfInInput(out, text,  end + i)
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

    private fun withinStyle(out: StringBuilder, text: CharSequence, start: Int, end: Int, nl: Int) {
        var i = start
        while (i < end) {
            val c = text[i]

            if (c == Constants.ZWJ_CHAR) {
                i++
                continue
            }

            consumeCursorIfInInput(out, text, i)

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
                        val codepoint = 0x010000 or ((c.toInt() - 0xD800) shl 10) or (d.toInt() - 0xDC00)
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
                    consumeCursorIfInInput(out, text, i)
                }

                out.append(' ')
            } else {
                out.append(c)
            }
            i++
        }

        if (nl == 0 && text.length > i && text[i] == '\n') {
            consumeCursorIfInInput(out, text, i)
        }
    }

    /**
     * Append a cursor to the output string if input string has one at the specified position.
     * Cursor is removed from the input if found at that position.
     *
     * The algorithm that uses this function goes like this: While traversing the input (spannable) string and producing
     *   the output chunk by chunk, look for the cursor span in the input string at a location before or after the chunk.
     *   If cursor is found then remove it while appending a cursor literal to the output string. This way, the cursor
     *   gets inserted without the need to know which position in the output string corresponds to the position in the
     *   input string.
     */
    private fun consumeCursorIfInInput(out: StringBuilder, text: CharSequence, position: Int) {
        val cursorSpan = (text as SpannableStringBuilder).getSpans(position, position, AztecCursorSpan::class.java)
                .firstOrNull()
        if (cursorSpan != null) {
            out.append(AztecCursorSpan.AZTEC_CURSOR_TAG)

            // remove the cursor mark from the input string. It's work is finished.
            text.removeSpan(cursorSpan)
        }
    }

    private fun tidy(html: String): String {
        return html
                .replace("&#8203;", "")
                .replace("&#65279;", "")
                .replace("(</? ?br>)*((aztec_cursor)?)</blockquote>".toRegex(), "$2</blockquote>")
                .replace("(</? ?br>)*((aztec_cursor)?)</p>".toRegex(), "$2</p>")
                .replace("(</? ?br>)*((aztec_cursor)?)</li>".toRegex(), "$2</li>")
    }
}
