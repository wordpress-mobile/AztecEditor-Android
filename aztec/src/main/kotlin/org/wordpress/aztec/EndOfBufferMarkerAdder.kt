package org.wordpress.aztec

import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView

class EndOfBufferMarkerAdder : TextWatcher {

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(text: Editable) {
        // NOTE: According to the documentation, by the time this afterTextChanged have been called, the text might
        //  have been changed by other TextWatcher's afterTextChanged calls. This might introduce some inconsistency
        //  and "random" bugs.

        if (text.isEmpty()) {
            // well, text is empty. no need for end-of-text marker
            return
        }

        when (text[text.length - 1]) {
            Constants.NEWLINE ->
                // need to add a ZWJ so a block element can render at the last line.
                text.append("" + Constants.END_OF_BUFFER_MARKER)

            Constants.END_OF_BUFFER_MARKER -> {
                // there's a marker but let's make sure it's still needed.

                if (text.length < 2) {
                    // it seems that the marker is alone. Let's leave it there so blocks can render.
                    return
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
        }// by the way, the cursor will be adjusted "automatically" by RichTextEditText's onSelectionChanged to
        //  before the marker
    }

    companion object {
        fun install(text: TextView) {
            text.addTextChangedListener(EndOfBufferMarkerAdder())
        }
    }
}
