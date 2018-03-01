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
import android.support.v4.text.TextDirectionHeuristicsCompat
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import org.wordpress.aztec.extensions.toCssString
import org.wordpress.aztec.plugins.IAztecPlugin
import org.wordpress.aztec.plugins.html2visual.ISpanPostprocessor
import org.wordpress.aztec.plugins.visual2html.IHtmlPostprocessor
import org.wordpress.aztec.plugins.visual2html.IInlineSpanHandler
import org.wordpress.aztec.plugins.visual2html.ISpanPreprocessor
import org.wordpress.aztec.source.CssStyleFormatter
import org.wordpress.aztec.spans.AztecCursorSpan
import org.wordpress.aztec.spans.AztecHorizontalRuleSpan
import org.wordpress.aztec.spans.AztecListItemSpan
import org.wordpress.aztec.spans.AztecListSpan
import org.wordpress.aztec.spans.AztecMediaSpan
import org.wordpress.aztec.spans.AztecURLSpan
import org.wordpress.aztec.spans.AztecVisualLinebreak
import org.wordpress.aztec.spans.CommentSpan
import org.wordpress.aztec.spans.IAztecBlockSpan
import org.wordpress.aztec.spans.IAztecFullWidthImageSpan
import org.wordpress.aztec.spans.IAztecInlineSpan
import org.wordpress.aztec.spans.IAztecNestable
import org.wordpress.aztec.spans.IAztecParagraphStyle
import org.wordpress.aztec.spans.IAztecSurroundedWithNewlines
import org.wordpress.aztec.spans.UnknownHtmlSpan
import org.wordpress.aztec.util.SpanWrapper
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator

class AztecParser(val plugins: List<IAztecPlugin> = ArrayList()) {

    fun fromHtml(source: String, context: Context): Spanned {
        val tidySource = tidy(source)

        val spanned = SpannableStringBuilder(Html.fromHtml(tidySource, AztecTagHandler(context, plugins), context, plugins))

        addVisualNewlinesToBlockElements(spanned)
        markBlockElementsAsParagraphs(spanned)
        cleanupZWJ(spanned)
        unbiasNestingLevel(spanned)

        postprocessSpans(spanned)

        return spanned
    }

    fun toHtml(text: Spanned, withCursor: Boolean = false): String {
        val out = StringBuilder()

        val data = SpannableStringBuilder(text)
        preprocessSpans(data)

        // remove any ForegroundColorSpans since they are not needed for parsing html.
        clearForegroundColorSpans(data)

        if (!withCursor) {
            val cursorSpan = data.getSpans(0, data.length, AztecCursorSpan::class.java).firstOrNull()
            cursorSpan?.let {
                data.removeSpan(cursorSpan)
            }
        }

        withinHtml(out, data)
        val html = postprocessHtml(tidy(out.toString()))
        return html
    }

    private fun preprocessSpans(spannable: SpannableStringBuilder) {
        plugins.filter { it is ISpanPreprocessor }
            .map { it as ISpanPreprocessor }
            .forEach {
                it.beforeSpansProcessed(spannable)
            }
    }

    private fun clearForegroundColorSpans(spannable: SpannableStringBuilder) {
        spannable.getSpans(0, spannable.length, ForegroundColorSpan::class.java)
                .forEach { spannable.removeSpan(it) }
    }

    private fun postprocessHtml(source: String): String {
        var html = source
        plugins.filter { it is IHtmlPostprocessor }
            .map { it as IHtmlPostprocessor }
            .forEach {
                html = it.onHtmlProcessed(html)
            }
        return html
    }

    private fun postprocessSpans(spannable: SpannableStringBuilder) {
        plugins.filter { it is ISpanPostprocessor }
            .map { it as ISpanPostprocessor }
            .forEach {
                it.afterSpansProcessed(spannable)
            }
    }

    private fun markBlockElementLineBreak(text: Spannable, startPos: Int) {
        text.setSpan(AztecVisualLinebreak(), startPos, startPos, Spanned.SPAN_MARK_MARK)
    }

    fun addVisualNewlinesToBlockElements(spanned: Editable) {
        // add visual newlines at starts
        spanned.getSpans(0, spanned.length, IAztecSurroundedWithNewlines::class.java).forEach {
            val parent = IAztecNestable.getParent(spanned, SpanWrapper(spanned, it))

            // a list item "repels" a child list so the list will appear in the next line
            val parentListItem = spanned.getSpans(spanned.getSpanStart(it), spanned.getSpanEnd(it), AztecListItemSpan::class.java)
                    .filter { item -> item.nestingLevel < it.nestingLevel }
                    .sortedBy { item -> item.nestingLevel }
                    .firstOrNull()
            val repelling = it is AztecListSpan && parentListItem != null

            val spanStart = spanned.getSpanStart(it)
            val spanEnd = spanned.getSpanEnd(it)

            // no need for newline if empty span. This fix 'PARAGRAPH span must start at paragraph boundary'. See: #501.
            if (spanStart == spanEnd) {
                return@forEach
            }

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

            if (repelling && spanStart > 0 && spanned[spanStart - 1] == '\n' && spanStart - 1 >= spanned.getSpanStart(parentListItem)) {
                return@forEach
            }

            // well, it seems we need a visual newline so, add one and mark it as such
            spanned.insert(spanStart, "\n")

            // expand all same-start parents to include the new newline
            SpanWrapper.getSpans<IAztecNestable>(spanned, spanStart + 1, spanStart + 2)
                    .filter { subParent -> subParent.span.nestingLevel < it.nestingLevel && subParent.start == spanStart + 1 }
                    .forEach { subParent -> subParent.start-- }

            markBlockElementLineBreak(spanned, spanStart)
        }

        // add visual newlines at ends
        spanned.getSpans(0, spanned.length, IAztecSurroundedWithNewlines::class.java).forEach {
            val spanEnd = spanned.getSpanEnd(it)

            // no need for newline if at text end
            if (spanEnd == spanned.length) {
                return@forEach
            }

            // no need for newline if there's one and marked as visual
            if (spanned[spanEnd] == '\n'
                    && spanned.getSpans(spanEnd, spanEnd, AztecVisualLinebreak::class.java).isNotEmpty()) {
                // but still, expand the span to include the newline for block spans, because they are paragraphs
                if (it is IAztecParagraphStyle) {
                    spanned.setSpan(it, spanned.getSpanStart(it), spanEnd + 1, spanned.getSpanFlags(it))
                }

                return@forEach
            }

            // well, it seems we need a visual newline so, add one and mark it as such
            spanned.insert(spanEnd, "\n")

            // expand the span to include the new newline for block spans, because they are paragraphs
            if (it is IAztecParagraphStyle) {
                spanned.setSpan(it, spanned.getSpanStart(it), spanEnd + 1, spanned.getSpanFlags(it))
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
        spanned.getSpans(0, spanned.length, IAztecSurroundedWithNewlines::class.java).forEach {
            val spanEnd = spanned.getSpanEnd(it)

            // block spans include a newline at the end, we need to account for that
            val newlineExpected = if (it is IAztecBlockSpan && spanEnd > 0) spanEnd - 1 else spanEnd

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

        spanned.getSpans(0, spanned.length, IAztecSurroundedWithNewlines::class.java).forEach {
            val parent = IAztecNestable.getParent(spanned, SpanWrapper(spanned, it))

            // a list item "repels" a child list so the list will appear in the next line
            val repelling = it is AztecListSpan && parent?.span is AztecListItemSpan

            val spanStart = spanned.getSpanStart(it)

            // we're looking for newlines before the spans, no need to continue if span at the beginning
            if (spanStart == 0) {
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

            if (spanned.getSpans(spanStart, spanStart, IAztecSurroundedWithNewlines::class.java).any { before ->
                spanned.getSpanEnd(before) == spanStart
            }) {
                // the newline before us is the end of a previous block element so, return
                return@forEach
            }

            if (spanStart > 1 && !repelling && spanned[spanStart - 2] == '\n') {
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
        SpanWrapper.getSpans(text, 0, text.length, IAztecBlockSpan::class.java)
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
        text.getSpans(0, text.length, IAztecNestable::class.java).forEach { it.nestingLevel -= 2 }
    }

    private fun withinHtml(out: StringBuilder, text: Spanned) {
        withinHtml(out, text, 0, text.length, null, -1)
    }

    private fun withinHtml(out: StringBuilder, text: Spanned, start: Int, end: Int,
                           grandParents: ArrayList<IAztecNestable>?, nestingLevel: Int) {
        var next: Int
        var i = start
        var parents: ArrayList<IAztecNestable>?

        do {
            val nestableElements = text.getSpans(i, end, IAztecNestable::class.java)
                    .filter { it !is IAztecFullWidthImageSpan }
                    .toTypedArray()

            nestableElements.sortWith(Comparator { a, b ->
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
            var nestable = nestableElements.firstOrNull { it.nestingLevel > nestingLevel }

            if (nestable == null) {
                // no nestable found so, just consume all available chars
                next = end
                parents = grandParents
            } else if (text.getSpanStart(nestable) > i) {
                // the start of the nestable is further down so, we'll handle it at next iteration.
                next = text.getSpanStart(nestable)
                nestable = null
                parents = grandParents
            } else {
                // nice, we found the start of a nestable so, prepare to go deeper to parse it
                next = text.getSpanEnd(nestable)
                parents = ArrayList<IAztecNestable>(grandParents ?: ArrayList())
                parents.add(nestable)
            }

            when (nestable) {
                is IAztecParagraphStyle -> withinNestable(out, text, i, next, nestable, parents, nestable.nestingLevel)
                is UnknownHtmlSpan -> withinUnknown(out, text, i, next, nestable)
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

    private fun withinNestable(out: StringBuilder, text: Spanned, start: Int, end: Int,
                               nestable: IAztecParagraphStyle, parents: ArrayList<IAztecNestable>?, nestingLevel: Int) {

        if (nestable.shouldParseAlignmentToHtml()) {
            CssStyleFormatter.removeStyleAttribute(nestable.attributes, CssStyleFormatter.CSS_TEXT_ALIGN_ATTRIBUTE)

            nestable.align?.let {
                val direction = TextDirectionHeuristicsCompat.FIRSTSTRONG_LTR
                val isRtl = direction.isRtl(text, start, end - start)

                CssStyleFormatter.addStyleAttribute(nestable.attributes,
                        CssStyleFormatter.CSS_TEXT_ALIGN_ATTRIBUTE, nestable.align!!.toCssString(isRtl))
            }
        }

        out.append("<${nestable.startTag}>")
        withinHtml(out, text, start, end, parents, nestingLevel)
        out.append("</${nestable.endTag}>")

        if (end > 0
                && text[end - 1] == Constants.NEWLINE
                && text.getSpans(end - 1, end, AztecVisualLinebreak::class.java).isEmpty()
                && !(parents?.any { it != nestable && text.getSpanEnd(it) == end } ?: false)) {
            out.append("<br>")
        }
    }

    private fun withinContent(out: StringBuilder, text: Spanned, start: Int, end: Int,
                              parents: ArrayList<IAztecNestable>?) {
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
                                parents: ArrayList<IAztecNestable>?) {
        var next: Int

        var i = start

        while (i < end || start == end) {
            next = text.nextSpanTransition(i, end, CharacterStyle::class.java)

            if (i == next)
                break

            val spans = text.getSpans(i, next, CharacterStyle::class.java).toMutableList()

            fixOrderOfNestedMediaAndUrlSpans(spans, text)

            for (j in spans.indices) {
                val span = spans[j]

                if (span is IAztecInlineSpan) {
                    out.append("<${span.startTag}>")
                }

                if (span is CommentSpan) {
                    out.append("<!--")
                    if (span.isHidden) {
                        i = next
                        out.append(span.text)
                    }
                }

                plugins.filter { it is IInlineSpanHandler && it.canHandleSpan(span) }
                        .map { it as IInlineSpanHandler }
                        .forEach {
                            it.handleSpanStart(out, span)
                            if (!it.shouldParseContent()) {
                                i = next
                            }
                        }

                if (span is AztecHorizontalRuleSpan) {
                    out.append("<${span.startTag}>")
                    i = next
                }

                if (span is AztecMediaSpan) {
                    out.append(span.getHtml())
                    i = next
                }
            }

            withinStyle(out, text, i, next, nl)

            for (j in spans.indices.reversed()) {
                val span = spans[j]

                if (span is IAztecInlineSpan) {
                    out.append("</${span.endTag}>")
                }

                if (span is CommentSpan) {
                    out.append("-->")
                }

                plugins.filter { it is IInlineSpanHandler && it.canHandleSpan(span) }
                        .map { it as IInlineSpanHandler }
                        .forEach {
                            it.handleSpanEnd(out, span)
                        }
            }

            if (start == end)
                break

            i = next
        }

        for (z in 0..nl - 1) {
            val parentSharesEnd = parents?.any { text.getSpanEnd(it) == end + 1 + z } ?: false
            if (parentSharesEnd) {
                continue
            }

            out.append("<br>")
            consumeCursorIfInInput(out, text, end + z)
        }
    }

    private fun fixOrderOfNestedMediaAndUrlSpans(spans: MutableList<CharacterStyle>, text: Spanned) {
        val urlSpan = spans.firstOrNull { it is AztecURLSpan }
        val mediaSpan = spans.firstOrNull { it is AztecMediaSpan }

        if (urlSpan != null && mediaSpan != null) {
            val urlSpanStart = text.getSpanStart(urlSpan)
            val urlSpanEnd = text.getSpanEnd(urlSpan)

            val isUrlSpanFollowsMediaSpan = spans.indexOf(urlSpan) > spans.indexOf(mediaSpan)
            val isMediaSpanWithinUrlSpan = text.getSpanStart(mediaSpan) >= urlSpanStart && text.getSpanEnd(mediaSpan) <= urlSpanEnd

            if (isUrlSpanFollowsMediaSpan && isMediaSpanWithinUrlSpan) {
                Collections.swap(spans, spans.indexOf(urlSpan), spans.indexOf(mediaSpan))
            }
        }
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
            } else if (c == ' ') {
                while (i + 1 < end && text[i + 1] == ' ') {
                    out.append("&nbsp;")
                    i++
                    consumeCursorIfInInput(out, text, i)
                }

                out.append(' ')
            } else if (c != '\n') {
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
        if (text is SpannableStringBuilder) {
            val cursorSpan = text.getSpans(position, position, AztecCursorSpan::class.java).firstOrNull()
            if (cursorSpan != null) {
                out.append(AztecCursorSpan.AZTEC_CURSOR_TAG)

                // remove the cursor mark from the input string. It's work is finished.
                text.removeSpan(cursorSpan)
            }
        }
    }

    private fun tidy(html: String): String {
        return html
                .replace(Constants.ZWJ_STRING, "")
                .replace(Constants.MAGIC_STRING, "")
                .replace("(</? ?br>)*((aztec_cursor)?)</blockquote>".toRegex(), "$2</blockquote>")
                .replace("(</? ?br>)*((aztec_cursor)?)</li>".toRegex(), "$2</li>")
                .replace("(</? ?br>)*((aztec_cursor)?)</p>".toRegex(), "$2</p>")
                .replace("(</? ?br>)*((aztec_cursor)?)</pre>".toRegex(), "$2</pre>")
    }
}
