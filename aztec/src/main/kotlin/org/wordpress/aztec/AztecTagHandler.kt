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
import android.text.style.QuoteSpan
import org.wordpress.aztec.spans.*
import org.xml.sax.Attributes
import org.xml.sax.XMLReader

class AztecTagHandler : Html.TagHandler {

    private class Ul
    private class Ol
    private class Blockquote
    private class Strike

    private var order = 0

    override fun handleTag(opening: Boolean, tag: String, output: Editable, xmlReader: XMLReader, attributes: Attributes?) : Boolean {
        when (tag.toLowerCase()) {
            LIST_LI -> {
                if (!opening) {
                    val mark = output.length
                    output.append("\n")
                    output.setSpan(BlockElementLinebreak(), mark, output.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
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
                if (output.length > 0 && output[output.length - 1] != '\n') {
                    val mark = output.length
                    output.append("\n\n")
                    output.setSpan(BlockElementLinebreak(), mark, output.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                if (opening) {
                    start(output, Ul())
                } else {
                    end(output, Ul::class.java, AztecUnorderedListSpan())
                }
                return true
            }
            LIST_OL -> {
                if (output.length > 0 && output[output.length - 1] != '\n') {
                    val mark = output.length
                    output.append("\n\n")
                    output.setSpan(BlockElementLinebreak(), mark, output.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                if (opening) {
                    start(output, Ol())
                } else {
                    end(output, Ol::class.java, AztecOrderedListSpan())
                }
                return true
            }
            BLOCKQUOTE -> {
                if (output.length > 0 && output[output.length - 1] != '\n') {
                    val mark = output.length
                    output.append("\n\n")
                    output.setSpan(BlockElementLinebreak(), mark, output.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                if (opening) {
                    start(output, Blockquote())
                } else {
                    end(output, Blockquote::class.java, QuoteSpan())
                }
                return true
            }

        }
        return false
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
