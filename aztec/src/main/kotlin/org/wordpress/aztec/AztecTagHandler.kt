/*
 * Copyright (C) 2016 Automattic
 * Copyright (C) 2015 Matthew Lee
 * Copyright (C) 2013-2015 Dominik Schürmann <dominik@dominikschuermann.de>
 * Copyright (C) 2013-2015 Juha Kuitunen
 * Copyright (C) 2013 Mohammed Lakkadshaw
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

import android.text.Editable
import android.text.Spannable
import android.text.Spanned
import org.wordpress.aztec.spans.*
import org.xml.sax.Attributes
import org.xml.sax.XMLReader

class AztecTagHandler : Html.TagHandler {

    private var order = 0

    override fun handleTag(opening: Boolean, tag: String, output: Editable, xmlReader: XMLReader, attributes: Attributes?): Boolean {
        val attributeString = Html.stringifyAttributes(attributes).toString()

        when (tag.toLowerCase()) {
            LIST_LI -> {
                if (opening) {
                    start(output, AztecListItemSpan(attributeString))
                } else {
                    endList(output)
                }
                return true
            }
            STRIKETHROUGH_S, STRIKETHROUGH_STRIKE, STRIKETHROUGH_DEL -> {
                if (opening) {
                    start(output, AztecStrikethroughSpan(tag, attributeString))
                } else {
                    end(output, AztecStrikethroughSpan::class.java)
                }
                return true
            }
            DIV, SPAN -> {
                if (opening) {
                    start(output, HiddenHtmlSpan(tag, attributeString, order++))
                } else {
                    endHidden(output, order++)
                }
                return true
            }
            LIST_UL -> {
                handleBlockElement(output, opening, AztecUnorderedListSpan(attributeString))
                return true
            }
            LIST_OL -> {
                handleBlockElement(output, opening, AztecOrderedListSpan(attributeString))
                return true
            }
            BLOCKQUOTE -> {
                handleBlockElement(output, opening, AztecQuoteSpan(attributeString))
                return true
            }
            PARAGRAPH -> {
                handleBlockElement(output, opening, ParagraphSpan(attributeString))
                return true
            }
            else -> {
                if (tag.length == 2 && Character.toLowerCase(tag[0]) == 'h' && tag[1] >= '1' && tag[1] <= '6') {
                    handleBlockElement(output, opening, AztecHeadingSpan(tag, attributeString))
                    return true
                }
            }

        }
        return false
    }

    private fun handleBlockElement(output: Editable, opening: Boolean, span: Any) {
        if (output.isNotEmpty()) {
            val nestedInBlockElement = isNestedInBlockElement(output, opening)

            val followingBlockElement = opening && output[output.length-1] == '\n' &&
                    output.getSpans(output.length - 1, output.length - 1, AztecLineBlockSpan::class.java).isNotEmpty()

            if (!followingBlockElement && !nestedInBlockElement && (output[output.length - 1] != '\n' || opening)) {
                output.append("\n")
            } else if (span !is AztecListSpan && !opening && nestedInBlockElement) {
                output.append("\n")
            }
        }

        if (opening) {
            start(output, span)
        } else {
            end(output, span.javaClass)
        }

    }

    fun isNestedInBlockElement(output: Editable, opening: Boolean): Boolean {
        val spanLookupIndex = if (opening) output.length else output.length - 1
        val minNumberOfSpans = if (opening) 0 else 1

        return output.getSpans(spanLookupIndex, spanLookupIndex, AztecLineBlockSpan::class.java).size > minNumberOfSpans
    }


    private fun start(output: Editable, mark: Any) {
        output.setSpan(mark, output.length, output.length, Spanned.SPAN_MARK_MARK)
    }

    private fun endHidden(output: Editable, order: Int) {
        val last = getLastOpenHidden(output)
        if (last != null) {
            last.close(order)
            val start = output.getSpanStart(last)
            val end = output.length

            if (start != end) {
                output.setSpan(last, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else {
                output.setSpan(last, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            }
        }
    }

    private fun endList(output: Editable) {
        val last = getLast(output, AztecListItemSpan::class.java) as AztecListItemSpan
        if (output.isEmpty() || output.last() != '\n' ||
                output.getSpans(output.length, output.length, AztecListItemSpan::class.java).isNotEmpty()) {
            output.append("\n")
        }
        val end = output.length
        output.setSpan(last, end - 1, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val list = output.getSpans(0, output.length, AztecListSpan::class.java).last()
        list.lastItem = last
    }

    private fun end(output: Editable, kind: Class<*>) {
        val last = getLast(output, kind)
        val start = output.getSpanStart(last)
        val end = output.length

        output.removeSpan(last) // important to keep the correct order of spans!
        if (start != end) {
            output.setSpan(last, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    companion object {
        private val LIST_LI = "li"
        private val LIST_UL = "ul"
        private val LIST_OL = "ol"
        private val STRIKETHROUGH_S = "s"
        private val STRIKETHROUGH_STRIKE = "strike"
        private val STRIKETHROUGH_DEL = "del"
        private val DIV = "div"
        private val SPAN = "span"
        private val BLOCKQUOTE = "blockquote"
        private val PARAGRAPH = "p"

        private fun getLast(text: Editable, kind: Class<*>): Any? {
            val spans = text.getSpans(0, text.length, kind)

            if (spans.isEmpty()) {
                return null
            } else {
                return (spans.size downTo 1)
                        .firstOrNull { text.getSpanFlags(spans[it - 1]) == Spannable.SPAN_MARK_MARK }
                        ?.let { spans[it - 1] }
            }
        }

        private fun getLastOpenHidden(text: Editable): HiddenHtmlSpan? {
            val spans = text.getSpans(0, text.length, HiddenHtmlSpan::class.java)

            if (spans.isEmpty()) {
                return null
            } else {
                return (spans.size downTo 1)
                        .firstOrNull { text.getSpanFlags(spans[it - 1]) == Spannable.SPAN_MARK_MARK && !(spans[it - 1] as HiddenHtmlSpan).isClosed }
                        ?.let { spans[it - 1] }
            }
        }
    }
}