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

    private class Ul : AztecMarkingSpan
    private class Ol : AztecMarkingSpan
    private class Blockquote : AztecMarkingSpan
    private class Strike

    private var order = 0

    override fun handleTag(opening: Boolean, tag: String, output: Editable, xmlReader: XMLReader, attributes: Attributes?): Boolean {
        when (tag.toLowerCase()) {
            LIST_LI -> {
                if (!opening && output.length > 0 && output[output.length - 1] != '\n') {
                    output.append("\n")
                }
                return true
            }
            STRIKETHROUGH_S, STRIKETHROUGH_STRIKE, STRIKETHROUGH_DEL -> {
                if (opening) {
                    start(output, Strike())
                } else {
                    end(output, Strike::class.java, AztecStrikethroughSpan(tag))
                }
                return true
            }
            DIV, SPAN -> {
                if (opening) {
                    start(output, HiddenHtmlSpan(tag, Html.stringifyAttributes(attributes), order++))
                } else {
                    endHidden(output, order++)
                }
                return true
            }
            LIST_UL -> {
                handleBlockElement(output, opening, Ul(), AztecUnorderedListSpan())
                return true
            }
            LIST_OL -> {
                handleBlockElement(output, opening, Ol(), AztecOrderedListSpan())
                return true
            }
            BLOCKQUOTE -> {
                handleBlockElement(output, opening, Blockquote(), AztecQuoteSpan())
                return true
            }

        }
        return false
    }

    private fun handleBlockElement(output: Editable, opening: Boolean, mark: Any, replaces: Any) {

        if(output.length > 0){
            val nestedInBlockElement = isNestedInBlockElement(output, opening)

            val followingBlockElement = opening &&
                    output.getSpans(output.length -1,output.length -1, AztecBlockSpan::class.java).size >0

            if (!followingBlockElement && !nestedInBlockElement && (output[output.length - 1] != '\n' || opening)) {
                output.append("\n")
            } else if (replaces is AztecQuoteSpan && !opening && nestedInBlockElement){
                output.append("\n")
            }
        }

        if (opening) {
            start(output, mark)
        } else {
            end(output, mark.javaClass, replaces)
        }

    }

    fun isNestedInBlockElement(output: Editable, opening: Boolean): Boolean {
        val spanLookupIndex = if (opening) output.length else output.length - 1
        val minNumberOfSpans = if (opening) 0 else 1

        return output.getSpans(spanLookupIndex, spanLookupIndex, AztecMarkingSpan::class.java).size > minNumberOfSpans
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
            }
        }
    }

    private fun end(output: Editable, kind: Class<*>, vararg replaces: Any) {
        val last = getLast(output, kind)
        val start = output.getSpanStart(last)
        val end = output.length
        output.removeSpan(last)

        if (start != end) {
            for (replace in replaces) {
                output.setSpan(replace, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
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

        private fun getLast(text: Editable, kind: Class<*>): Any? {
            val spans = text.getSpans(0, text.length, kind)

            if (spans.size == 0) {
                return null
            } else {
                for (i in spans.size downTo 1) {
                    if (text.getSpanFlags(spans[i - 1]) == Spannable.SPAN_MARK_MARK) {
                        return spans[i - 1]
                    }
                }

                return null
            }
        }

        private fun getLastOpenHidden(text: Editable): HiddenHtmlSpan? {
            val spans = text.getSpans(0, text.length, HiddenHtmlSpan::class.java)

            if (spans.size == 0) {
                return null
            } else {
                for (i in spans.size downTo 1) {
                    if (text.getSpanFlags(spans[i - 1]) == Spannable.SPAN_MARK_MARK &&
                            !(spans[i - 1] as HiddenHtmlSpan).isClosed) {
                        return spans[i - 1]
                    }
                }

                return null
            }
        }
    }
}
