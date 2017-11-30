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

package org.wordpress.aztec.watchers

import android.text.Editable
import android.text.TextWatcher
import android.text.style.LeadingMarginSpan
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.formatting.InlineFormatter
import org.wordpress.aztec.spans.IAztecInlineSpan
import java.lang.ref.WeakReference

class InlineTextWatcher(var inlineFormatter: InlineFormatter, aztecText: AztecText) : TextWatcher {
    private val aztecTextRef: WeakReference<AztecText?> = WeakReference(aztecText)

    private var textChangedEventDetails = TextChangedEvent("", 0, 0, 0)

    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
        if (aztecTextRef.get()?.isTextChangedListenerDisabled() ?: true) {
            return
        }
        textChangedEventDetails = TextChangedEvent(text.toString())
    }

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
        if (aztecTextRef.get()?.isTextChangedListenerDisabled() ?: true) {
            return
        }

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
            removeLeadingStyle(text, IAztecInlineSpan::class.java)
            removeLeadingStyle(text, LeadingMarginSpan::class.java)
        }

        if (aztecTextRef.get()?.isInlineTextHandlerEnabled() ?: true) {
            inlineFormatter.handleInlineStyling(textChangedEventDetails)
        } else {
            aztecTextRef.get()?.enableInlineTextHandling()
        }
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
