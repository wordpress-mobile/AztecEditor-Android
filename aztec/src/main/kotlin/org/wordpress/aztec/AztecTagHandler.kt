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

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.Spannable
import android.text.Spanned
import org.wordpress.aztec.spans.*
import org.xml.sax.Attributes

class AztecTagHandler : Html.TagHandler {

    private var order = 0

    override fun handleTag(opening: Boolean, tag: String, output: Editable,
                           context: Context, attributes: Attributes,
                           nestingLevel: Int): Boolean {

        when (tag.toLowerCase()) {
            LIST_LI -> {
                handleElement(output, opening, AztecListItemSpan(nestingLevel, AztecAttributes(attributes)))
                return true
            }
            STRIKETHROUGH_S, STRIKETHROUGH_STRIKE, STRIKETHROUGH_DEL -> {
                handleElement(output, opening, AztecStrikethroughSpan(tag, AztecAttributes(attributes)))
                return true
            }
            DIV, SPAN -> {
                if (opening) {
                    start(output, HiddenHtmlSpan(tag, AztecAttributes(attributes), order++))
                } else {
                    endHidden(output, order++)
                }
                return true
            }
            LIST_UL -> {
                handleElement(output, opening, AztecUnorderedListSpan(nestingLevel, AztecAttributes(attributes)))
                return true
            }
            LIST_OL -> {
                handleElement(output, opening, AztecOrderedListSpan(nestingLevel, AztecAttributes(attributes)))
                return true
            }
            BLOCKQUOTE -> {
                handleElement(output, opening, AztecQuoteSpan(nestingLevel, AztecAttributes(attributes)))
                return true
            }
            IMAGE -> {
                if (opening) {
                    val mediaSpan = createImageSpan(AztecAttributes(attributes), context)
                    start(output, mediaSpan)
                    start(output, AztecMediaClickableSpan(mediaSpan))
                    output.append(Constants.IMG_CHAR)
                } else {
                    end(output, AztecImageSpan::class.java)
                    end(output, AztecMediaClickableSpan::class.java)
                }
                return true
            }
            VIDEO -> {
                if (opening) {
                    val mediaSpan = createVideoSpan(AztecAttributes(attributes), context, nestingLevel)
                    start(output, mediaSpan)
                    start(output, AztecMediaClickableSpan(mediaSpan))
                    output.append(Constants.IMG_CHAR)
                } else {
                    end(output, AztecVideoSpan::class.java)
                    end(output, AztecMediaClickableSpan::class.java)
                }
                return true
            }
            PARAGRAPH -> {
                handleElement(output, opening, ParagraphSpan(nestingLevel, AztecAttributes(attributes)))
                return true
            }
            LINE -> {
                if (opening) {
                    // Add an extra newline above the line to prevent weird typing on the line above
                    start(output, AztecHorizontalRuleSpan(context, ContextCompat.getDrawable(context, R.drawable.img_hr), nestingLevel))

                    output.append(Constants.MAGIC_CHAR)
                } else {
                    end(output, AztecHorizontalRuleSpan::class.java)
                }
                return true
            }
            PREFORMAT -> {
                handleElement(output, opening, AztecPreformatSpan(nestingLevel, AztecAttributes(attributes)))
                return true
            }
            else -> {
                if (tag.length == 2 && Character.toLowerCase(tag[0]) == 'h' && tag[1] >= '1' && tag[1] <= '6') {
                    handleElement(output, opening, AztecHeadingSpan(nestingLevel, tag, AztecAttributes(attributes)))
                    return true
                }
            }
        }
        return false
    }

    private fun createImageSpan(attributes: AztecAttributes, context: Context) : AztecMediaSpan {
        val styles = context.obtainStyledAttributes(R.styleable.AztecText)
        val loadingDrawable = ContextCompat.getDrawable(context, styles.getResourceId(R.styleable.AztecText_drawableLoading, R.drawable.ic_image_loading))
        styles.recycle()
        return AztecImageSpan(context, loadingDrawable, attributes)
    }

    private fun createVideoSpan(attributes: AztecAttributes,
                                context: Context, nestingLevel: Int) : AztecMediaSpan {
        val styles = context.obtainStyledAttributes(R.styleable.AztecText)
        val loadingDrawable = ContextCompat.getDrawable(context, styles.getResourceId(R.styleable.AztecText_drawableLoading, R.drawable.ic_image_loading))
        styles.recycle()
        return AztecVideoSpan(context, loadingDrawable, nestingLevel, attributes)
    }

    private fun handleElement(output: Editable, opening: Boolean, span: Any) {
        if (opening) {
            start(output, span)
        } else {
            end(output, span.javaClass)
        }
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

    private fun end(output: Editable, kind: Class<*>) {
        val last = getLast(output, kind)
        val start = output.getSpanStart(last)
        val end = output.length

        if (start != end) {
            output.setSpan(last, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        else if (start == end && IAztecBlockSpan::class.java.isAssignableFrom(kind)) {
            //if block element is empty add a ZWJ to make it non empty and extend span
            output.append(Constants.ZWJ_CHAR)
            output.setSpan(last, start, output.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
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
        private val PREFORMAT = "pre"
        private val IMAGE = "img"
        private val VIDEO = "video"
        private val LINE = "hr"

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
                spans.sortByDescending { it.startOrder }
                return spans.firstOrNull { text.getSpanFlags(it) == Spannable.SPAN_MARK_MARK && !(it as HiddenHtmlSpan).isClosed }
            }
        }
    }
}