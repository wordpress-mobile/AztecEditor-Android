/*
 * Copyright (C) 2016 Automattic
 * Copyright (C) 2015 Matthew Lee
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
import android.text.TextWatcher
import android.text.style.LeadingMarginSpan
import org.wordpress.aztec.formatting.InlineFormatter
import org.wordpress.aztec.spans.AztecInlineSpan
import java.lang.ref.WeakReference

class InlineTextWatcher(var inlineFormatter: InlineFormatter, aztecText: AztecText) : TextWatcher {
    private val aztecTextRef: WeakReference<AztecText?> = WeakReference(aztecText)

    private var textChangedEventDetails = TextChangedEvent("", 0, 0, 0)

    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
        prepareTextChangeEventDetails(after, count, start, text)

        inlineFormatter.carryOverInlineSpans(start, count, after)
    }

    private fun prepareTextChangeEventDetails(after: Int, count: Int, start: Int, text: CharSequence) {
//        var deletedFromBlock = false
//        var blockStart = -1
//        var blockEnd = -1
//        if (count > 0 && after < count && !isTextChangedListenerDisabled()) {
//            this.text.getSpans(start, start + 1, AztecBlockSpan::class.java).forEach {
//                if (it != null) {
//                    blockStart = this.text.getSpanStart(it)
//                    blockEnd = this.text.getSpanEnd(it)
//                    deletedFromBlock = start < blockEnd && this.text[start] != '\n' &&
//                            (start + count >= blockEnd || (start + count + 1 == blockEnd && text[start + count] == '\n')) &&
//                            (start == 0 || text[start - 1] == '\n')
//
//                    // if we are removing all characters from the span, we must change the flag to SPAN_EXCLUSIVE_INCLUSIVE
//                    // because we want to allow a block span with empty text (such as list with a single empty first item)
//                    if (deletedFromBlock && after == 0 && blockEnd - blockStart == count && text[start] != Constants.ZWJ_CHAR) {
//                        this.text.setSpan(it, blockStart, blockEnd, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
//                    }
//                }
//            }
//        }
//
//        if (deletedFromBlock) {
//            textChangedEventDetails = TextChangedEvent(this.text.toString(), deletedFromBlock, blockStart)
//        } else {
            textChangedEventDetails = TextChangedEvent(text.toString())
//        }
    }

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
        inlineFormatter.reapplyCarriedOverInlineSpans()

        textChangedEventDetails.before = before
        textChangedEventDetails.text = text
        textChangedEventDetails.countOfCharacters = count
        textChangedEventDetails.start = start
        textChangedEventDetails.initialize()
    }

    override fun afterTextChanged(text: Editable) {
        if (aztecTextRef.get()?.isTextChangedListenerDisabled() ?: true) {
            return
        }

        if (textChangedEventDetails.inputStart == 0 && textChangedEventDetails.count == 0) {
            removeLeadingStyle(text, AztecInlineSpan::class.java)
            removeLeadingStyle(text, LeadingMarginSpan::class.java)
        }

        inlineFormatter.handleInlineStyling(textChangedEventDetails)
    }

    fun removeLeadingStyle(text: Editable, spanClass: Class<*>) {
        text.getSpans(0, 0, spanClass).forEach {
            if (text.isNotEmpty()) {
                text.setSpan(it, 0, text.getSpanEnd(it), text.getSpanFlags(it))
            } else {
                text.removeSpan(it)
            }
        }
    }

    companion object {
        fun install(inlineFormatter: InlineFormatter, text: AztecText) {
            text.addTextChangedListener(InlineTextWatcher(inlineFormatter, text))
        }
    }
}
