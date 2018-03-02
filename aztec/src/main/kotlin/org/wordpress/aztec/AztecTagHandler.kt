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
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.Spanned
import org.wordpress.aztec.plugins.IAztecPlugin
import org.wordpress.aztec.plugins.html2visual.IHtmlTagHandler
import org.wordpress.aztec.spans.AztecAudioSpan
import org.wordpress.aztec.spans.AztecHeadingSpan
import org.wordpress.aztec.spans.AztecHorizontalRuleSpan
import org.wordpress.aztec.spans.AztecImageSpan
import org.wordpress.aztec.spans.AztecListItemSpan
import org.wordpress.aztec.spans.AztecMediaClickableSpan
import org.wordpress.aztec.spans.AztecMediaSpan
import org.wordpress.aztec.spans.AztecOrderedListSpan
import org.wordpress.aztec.spans.AztecPreformatSpan
import org.wordpress.aztec.spans.AztecQuoteSpan
import org.wordpress.aztec.spans.AztecStrikethroughSpan
import org.wordpress.aztec.spans.AztecUnorderedListSpan
import org.wordpress.aztec.spans.AztecVideoSpan
import org.wordpress.aztec.spans.HiddenHtmlSpan
import org.wordpress.aztec.spans.IAztecAttributedSpan
import org.wordpress.aztec.spans.IAztecNestable
import org.wordpress.aztec.spans.ParagraphSpan
import org.wordpress.aztec.util.getLast
import org.xml.sax.Attributes
import java.util.ArrayList

class AztecTagHandler(val context: Context, val plugins: List<IAztecPlugin> = ArrayList()) : Html.TagHandler {
    private val loadingDrawable: Drawable

    init {
        val styles = context.obtainStyledAttributes(R.styleable.AztecText)
        loadingDrawable = ContextCompat.getDrawable(context, styles.getResourceId(R.styleable.AztecText_drawableLoading, R.drawable.ic_image_loading))
        styles.recycle()
    }

    override fun handleTag(opening: Boolean, tag: String, output: Editable,
                           context: Context, attributes: Attributes,
                           nestingLevel: Int): Boolean {
        val wasTagHandled = processTagHandlerPlugins(tag, opening, output, attributes, nestingLevel)
        if (wasTagHandled) {
            return true
        }

        when (tag.toLowerCase()) {
            LIST_LI -> {
                handleElement(output, opening, AztecListItemSpan(nestingLevel, AztecAttributes(attributes)))
                return true
            }
            STRIKETHROUGH_S, STRIKETHROUGH_STRIKE, STRIKETHROUGH_DEL -> {
                handleElement(output, opening, AztecStrikethroughSpan(tag, AztecAttributes(attributes)))
                return true
            }
            DIV, SPAN, FIGURE, FIGCAPTION, SECTION -> {
                handleElement(output, opening, HiddenHtmlSpan(tag, AztecAttributes(attributes), nestingLevel))
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
                handleMediaElement(opening, output, AztecImageSpan(context, loadingDrawable, nestingLevel, AztecAttributes(attributes)))
                return true
            }
            VIDEO -> {
                if (opening) {
                    handleMediaElement(true, output, AztecVideoSpan(context, loadingDrawable, nestingLevel, AztecAttributes(attributes)))
                    handleMediaElement(false, output, AztecVideoSpan(context, loadingDrawable, nestingLevel, AztecAttributes(attributes)))
                }
                return true
            }
            AUDIO -> {
                if (opening) {
                    handleMediaElement(true, output, AztecAudioSpan(context, loadingDrawable, nestingLevel, AztecAttributes(attributes)))
                    handleMediaElement(false, output, AztecAudioSpan(context, loadingDrawable, nestingLevel, AztecAttributes(attributes)))
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

    private fun processTagHandlerPlugins(tag: String, opening: Boolean, output: Editable, attributes: Attributes, nestingLevel: Int): Boolean {
        plugins.filter { it is IHtmlTagHandler }
                .map { it as IHtmlTagHandler }
                .forEach({
                    if (it.canHandleTag(tag)) {
                        val wasHandled = it.handleTag(opening, tag, output, attributes, nestingLevel)
                        if (wasHandled) {
                            return true
                        }
                    }
                })
        return false
    }

    private fun handleMediaElement(opening: Boolean, output: Editable, mediaSpan: AztecMediaSpan) {
        if (opening) {
            start(output, mediaSpan)
            start(output, AztecMediaClickableSpan(mediaSpan))
            output.append(Constants.IMG_CHAR)
        } else {
            end(output, mediaSpan.javaClass)
            end(output, AztecMediaClickableSpan::class.java)
        }
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

    private fun end(output: Editable, kind: Class<*>) {
        val last = output.getLast(kind)
        val start = output.getSpanStart(last)
        val end = output.length

        if (start != end) {
            output.setSpan(last, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (last is IAztecAttributedSpan) {
                // Apply the 'style' attribute if present
                last.applyInlineStyleAttributes(output, start, end)
            }
        } else if (start == end && IAztecNestable::class.java.isAssignableFrom(kind)) {
            // if block element is empty add a ZWJ to make it non empty and extend span
            if (HiddenHtmlSpan::class.java.isAssignableFrom(kind)) {
                output.append(Constants.MAGIC_CHAR)
            } else {
                output.append(Constants.ZWJ_CHAR)
            }
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
        private val FIGURE = "figure"
        private val FIGCAPTION = "figcaption"
        private val SECTION = "section"
        private val BLOCKQUOTE = "blockquote"
        private val PARAGRAPH = "p"
        private val PREFORMAT = "pre"
        private val IMAGE = "img"
        private val VIDEO = "video"
        private val AUDIO = "audio"
        private val LINE = "hr"
    }
}
