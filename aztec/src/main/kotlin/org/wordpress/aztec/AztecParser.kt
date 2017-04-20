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
import org.wordpress.aztec.util.SpanWrapper
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
        markBlockElementsAsParagraphs(spanned)
        cleanupZWJ(spanned)
        unbiasNestingLevel(spanned)

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

    private fun markBlockElementLineBreak(text: Spannable, startPos: Int) {
        text.setSpan(AztecVisualLinebreak(), startPos, startPos, Spanned.SPAN_MARK_MARK)
    }

    fun addVisualNewlinesToBlockElements(spanned: Editable) {
        // add visual newlines at starts
        spanned.getSpans(0, spanned.length, AztecSurroundedWithNewlines::class.java).forEach {
            val parent = AztecNestable.getParent(spanned, SpanWrapper(spanned, it))

            // a list item "repels" a child list so the list will appear in the next line
            val repelling = (parent?.span is AztecListItemSpan) && (it is AztecListSpan)

            val spanStart = spanned.getSpanStart(it)

            // no need for newline if at text start, unless repelling needs to happen
            if (!repelling && spanStart < 1) {
                return@forEach
            }

            val parentStart = parent?.start ?: 0

            // no need for newline if we're at the start of our parent, unless repelling needs to happen
            if (!repelling && spanStart == parentStart) {
                return@forEach
            }

            // no need for newline if there's already one, unless repelling needs to happen
            if (!repelling && spanned[spanStart - 1] == '\n') {
                return@forEach
            }

            // well, it seems we need a visual newline so, add one and mark it as such
            spanned.insert(spanStart, "\n")

            // expand all same-start parents to include the new newline
            SpanWrapper.getSpans<AztecNestable>(spanned, spanStart + 1, spanStart + 2)
                    .filter { parent -> parent.span.nestingLevel < it.nestingLevel && parent.start == spanStart + 1 }
                    .forEach { parent -> parent.start-- }

            markBlockElementLineBreak(spanned, spanStart)
        }

        // add visual newlines at ends
        spanned.getSpans(0, spanned.length, AztecSurroundedWithNewlines::class.java).forEach {
            val spanEnd = spanned.getSpanEnd(it)

            // no need for newline if at text end
            if (spanEnd == spanned.length) {
                return@forEach
            }

            // no need for newline if there's one and marked as visual
            if (spanned[spanEnd] == '\n'
                    && spanned.getSpans(spanEnd, spanEnd, AztecVisualLinebreak::class.java).isNotEmpty()) {

                // but still, expand the span to include the newline for block spans, because they are paragraphs
                if (it is AztecBlockSpan) {
                    spanned.setSpan(it, spanned.getSpanStart(it), spanEnd + 1, spanned.getSpanFlags(it))
                }

                //mark newlines at the end of <p> - this will give us visual padding
                //and will mark a place for additional /n during processing
                if (it is ParagraphSpan) {
                    spanned.setSpan(EndOfParagraphMarker(), spanEnd, spanEnd + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                return@forEach
            }

            // well, it seems we need a visual newline so, add one and mark it as such
            spanned.insert(spanEnd, "\n")

            // expand the span to include the new newline for block spans, because they are paragraphs
            if (it is AztecBlockSpan) {
                spanned.setSpan(it, spanned.getSpanStart(it), spanEnd + 1, spanned.getSpanFlags(it))
            }

            //mark newlines at the end of <p> - this will give us visual padding
            //and will mark a place for additional /n during processing
            if (it is ParagraphSpan) {
                spanned.setSpan(EndOfParagraphMarker(), spanEnd, spanEnd + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            markBlockElementLineBreak(spanned, spanEnd)
        }
    }

    // Always try to put a visual newline before block elements and only put one after if needed
    fun syncVisualNewlinesOfBlockElements(spanned: Editable) {
        // clear any visual newline marking. We'll mark them with a fresh set of passes
        spanned.getSpans(0, spanned.length, AztecVisualLinebreak::class.java).forEach {
            spanned.removeSpan(it)
        }

        // add visual newlines at ends
        spanned.getSpans(0, spanned.length, AztecSurroundedWithNewlines::class.java).forEach {
            val spanEnd = spanned.getSpanEnd(it)

            // block spans include a newline at the end, we need to account for that
            val newlineExpected = if (it is AztecBlockSpan) spanEnd - 1 else spanEnd

            if (spanEnd == spanned.length) {
                // no visual newline if at text end
                return@forEach
            }

            if (spanned[newlineExpected] != '\n') {
                // no newline inside the end of the span so, nothing to mark as visual newline
                return@forEach
            }

            // at last, all checks passed so, let's mark the newline as visual!
            markBlockElementLineBreak(spanned, newlineExpected)
        }

        spanned.getSpans(0, spanned.length, AztecSurroundedWithNewlines::class.java).forEach {
            val parent = AztecNestable.getParent(spanned, SpanWrapper(spanned, it))

            // a list item "repels" a child list so the list will appear in the next line
            val repelling = (parent?.span is AztecListItemSpan) && (it is AztecListSpan)

            val spanStart = spanned.getSpanStart(it)

            if (!repelling && spanStart < 1) {
                // no visual newline if at text start and not repelling so, return
                return@forEach
            }

            if (!repelling && spanStart < 2) {
                // if not repelling, no visual newline can exist unless there are at least 2 chars before the block
                //  (one will be the newline and the other will be the leading content) so, return
                return@forEach
            }

            if (spanned[spanStart - 1] != '\n') {
                // no newline before so, nothing to mark as visual newline
                return@forEach
            }

            if (spanned.getSpans(spanStart, spanStart, AztecSurroundedWithNewlines::class.java).any { before ->
                spanned.getSpanEnd(before) == spanStart
            }) {
                // the newline before us is the end of a previous block element so, return
                return@forEach
            }

            if (!repelling && spanned[spanStart - 2] == '\n') {
                // there's another newline before and we're not repelling a parent so, the adjacent one is not a visual one so, return
                return@forEach
            }

            if (spanned.getSpans(spanStart - 1, spanStart - 1, AztecVisualLinebreak::class.java).isNotEmpty()) {
                // the newline is already marked as visual so, nothing more to do here
                return@forEach
            }

            // at last, all checks passed so, let's mark the newline as visual!
            markBlockElementLineBreak(spanned, spanStart - 1)
        }

    }

    private fun markBlockElementsAsParagraphs(text: Spannable) {
        SpanWrapper.getSpans(text, 0, text.length, AztecBlockSpan::class.java)
                .map { it -> it.flags = Spanned.SPAN_PARAGRAPH }
    }

    private fun cleanupZWJ(text: Editable) {
        var lastIndex = text.length
        do {
            lastIndex = text.lastIndexOf(Constants.ZWJ_CHAR, lastIndex)
            if (lastIndex == text.length - 1) {
                // ZWJ at the end of text will serve as end-of-text marker so, let it be and finish.
                return
            }

            if (lastIndex > -1) {
                text.delete(lastIndex, lastIndex + 1)
            }
        } while (lastIndex > -1)
    }

    private fun unbiasNestingLevel(text: Spanned) {
        // while parsing html, the converter wraps the markup in a <html><body> pair so, nesting starts from 2
        text.getSpans(0, text.length, AztecNestable::class.java).forEach { it.nestingLevel -= 2 }
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
        withinHtml(out, text, 0, text.length, null, -1)
    }

    private fun withinHtml(out: StringBuilder, text: Spanned, start: Int, end: Int,
                           grandParents: ArrayList<AztecNestable>?, nestingLevel: Int) {
        var next: Int
        var i = start
        var parents: ArrayList<AztecNestable>?

        do {
            val paragraphs = text.getSpans(i, end, AztecNestable::class.java)
                    .filter { it !is AztecFullWidthImageSpan }
                    .toTypedArray()

            paragraphs.sortWith(Comparator { a, b ->
                val startComparison = text.getSpanStart(a).compareTo(text.getSpanStart(b))
                if (startComparison == 0) {
                    val nestingComparison = a.nestingLevel.compareTo(b.nestingLevel)
                    if (nestingComparison == 0) {
                        // warning: elements at same nesting level start at same position. This is probably an error but
                        //  still, just just try to compare by span end
                        return@Comparator text.getSpanEnd(a).compareTo(text.getSpanEnd(b))
                    } else {
                        return@Comparator nestingComparison
                    }
                } else {
                    return@Comparator startComparison
                }
            })
            var paragraph = paragraphs.firstOrNull { it.nestingLevel > nestingLevel }

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
                parents = ArrayList<AztecNestable>(grandParents ?: ArrayList())
                parents.add(paragraph)
            }

            when (paragraph) {
                is AztecBlockSpan -> withinBlock(out, text, i, next, paragraph, parents, paragraph.nestingLevel)
                is UnknownHtmlSpan -> withinUnknown(out, text, i, next, paragraph)
                else -> withinContent(out, text, i, next, parents)
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
                            blockSpan: AztecBlockSpan, parents: ArrayList<AztecNestable>?, nestingLevel: Int) {
        out.append("<${blockSpan.getStartTag()}>")
        withinHtml(out, text, start, end, parents, nestingLevel)
        out.append("</${blockSpan.getEndTag()}>")

        if (end > 0
                && text[end - 1] == Constants.NEWLINE
                && text.getSpans(end - 1, end, AztecVisualLinebreak::class.java).isEmpty()
                && !(parents?.any { it != blockSpan && text.getSpanEnd(it) == end } ?: false)) {
            out.append("<br>")
        }
    }

    private fun withinContent(out: StringBuilder, text: Spanned, start: Int, end: Int,
                              parents: ArrayList<AztecNestable>?) {
        var next: Int

        var i = start
        while (i < end) {
            next = TextUtils.indexOf(text, '\n', i, end)
            if (next < 0) {
                next = end
            }

            var nl = 0
            while (next < end && text[next] == '\n') {
                val isVisualLinebreak = text.getSpans(next, next, AztecVisualLinebreak::class.java).isNotEmpty()

                if (!isVisualLinebreak) {
                    nl++
                }
                next++
            }

            withinParagraph(out, text, i, next - nl, nl, parents)

            i = next
        }
    }

    // Copy from https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/text/Html.java,
    // remove some tag because we don't need them in Aztec.
    private fun withinParagraph(out: StringBuilder, text: Spanned, start: Int, end: Int, nl: Int,
                                parents: ArrayList<AztecNestable>?) {
        var next: Int

        var i = start

        while (i < end || start == end) {
            next = text.nextSpanTransition(i, end, CharacterStyle::class.java)

            val spans = text.getSpans(i, next, CharacterStyle::class.java)
            for (j in spans.indices) {
                val span = spans[j]

                if (span is AztecInlineSpan) {
                    out.append("<${span.getStartTag()}>")
                }

                if (span is CommentSpan) {
                    out.append("<!--")
                }

                if (span is AztecCommentSpan) {
                    out.append("<!--")
                    out.append(span.commentText)
                    i = next
                }

                if (span is AztecHorizontalLineSpan) {
                    out.append("<${span.getStartTag()}>")
                    i = next
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

                if (span is AztecInlineSpan) {
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

        for (z in 0..nl - 1) {
            val parentSharesEnd = parents?.any {text.getSpanEnd(it) == end + 1 + z } ?: false
            if (parentSharesEnd) {
                continue
            }

            out.append("<br>")
            consumeCursorIfInInput(out, text, end + z)
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
                .replace("\n".toRegex(), "")
    }
}
