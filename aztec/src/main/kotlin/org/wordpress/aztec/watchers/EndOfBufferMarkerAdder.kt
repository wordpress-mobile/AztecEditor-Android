package org.wordpress.aztec.watchers

import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.widget.TextView
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import org.wordpress.aztec.spans.IAztecBlockSpan

class EndOfBufferMarkerAdder(text: Editable) : TextWatcher {
    init {
        ensureEndOfTextMarker(text)
    }

    var deletedText = false

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        deletedText = before > 0
    }

    override fun afterTextChanged(text: Editable) {
        // NOTE: According to the documentation, by the time this afterTextChanged have been called, the text might
        //  have been changed by other TextWatcher's afterTextChanged calls. This might introduce some inconsistency
        //  and "random" bugs.

        ensureEndOfTextMarker(text, deletedText)

        // by the way, the cursor will be adjusted "automatically" by RichTextEditText's onSelectionChanged to before the marker
    }

    companion object {
        fun install(editText: AztecText) {
            editText.addTextChangedListener(EndOfBufferMarkerAdder(editText.text))
        }

        fun ensureEndOfTextMarker(text: Editable, deletedText: Boolean = false): Editable {
            // NOTE: According to the documentation, by the time this afterTextChanged have been called, the text might
            //  have been changed by other TextWatcher's afterTextChanged calls. This might introduce some inconsistency
            //  and "random" bugs.

            if (text.isEmpty()) {
                if (text.getSpans(0, 0, IAztecBlockSpan::class.java).isNotEmpty()) {
                    // need to add a end-of-text marker so a block element can render in the empty text.
                    text.append(Constants.END_OF_BUFFER_MARKER)
                }
                return text
            } else if (text.length == 1 && text[0] == Constants.END_OF_BUFFER_MARKER && deletedText) {
                // if text got deleted, the only character is the end-of-buffer marker and there is
                // no block style applied, we can remove the marker and show the hint
                if (text.getSpans(0, 1, IAztecBlockSpan::class.java).isEmpty()) {
                    text.delete(0, 1)
                }
                return text
            }

            when (text[text.length - 1]) {
                Constants.NEWLINE ->
                    // need to add a ZWJ so a block element can render at the last line.
                    text.append(Constants.END_OF_BUFFER_MARKER)

                Constants.END_OF_BUFFER_MARKER -> {
                    // there's a marker but let's make sure it's still needed.

                    if (text.length < 2) {
                        // it seems that the marker is alone. Let's leave it there so blocks can render.
                        return text
                    }

                    // there's a marker but let's make sure it's still needed. Remove it if no newline before it.
                    if (text[text.length - 2] != Constants.NEWLINE) {
                        // dangling end marker. Let's remove it.
                        text.delete(text.length - 1, text.length)
                    }
                }

                else ->
                    // there's some char at text-end so, let's just make sure we don't have dangling text-end markers around
                    do {
                        val lastEndOfTextMarkerIndex = text.toString().lastIndexOf(Constants.END_OF_BUFFER_MARKER)

                        if (lastEndOfTextMarkerIndex == -1) {
                            break
                        }

                        text.delete(lastEndOfTextMarkerIndex, lastEndOfTextMarkerIndex + 1)
                    } while (true)
            } // by the way, the cursor will be adjusted "automatically" by RichTextEditText's onSelectionChanged to
            //  before the marker

            return text
        }

        fun ensureEndOfTextMarker(text: String): String {
            val sb = SpannableStringBuilder(text)
            ensureEndOfTextMarker(sb)
            return sb.toString()
        }

        @JvmStatic
        fun <T : CharSequence> removeEndOfTextMarker(string: T): T {
            if (string.isNotEmpty() && string[string.length - 1] == Constants.END_OF_BUFFER_MARKER) {
                string.substring(0, string.length - 2)
            }

            return string
        }

        fun safeLength(textView: TextView): Int {
            if (textView.length() == 0) {
                return 0
            } else if (textView.text[textView.length() - 1] == Constants.END_OF_BUFFER_MARKER) {
                return textView.length() - 1
            } else {
                return textView.length()
            }
        }

        fun safeLength(charSequence: CharSequence): Int {
            if (charSequence.length == 0) {
                return 0
            } else if (charSequence[charSequence.length - 1] == Constants.END_OF_BUFFER_MARKER) {
                return charSequence.length - 1
            } else {
                return charSequence.length
            }
        }

        fun strip(string: String): String {
            if (string.isEmpty()) {
                return string
            } else if (string[string.length - 1] == Constants.END_OF_BUFFER_MARKER) {
                return string.substring(0, string.length - 1)
            } else {
                return string
            }
        }
    }
}
