package org.wordpress.aztec.watchers

import android.text.Editable
import android.text.TextWatcher
import org.wordpress.aztec.AztecText
import java.lang.ref.WeakReference

class ZeroIndexContentWatcher(aztecText: AztecText) : TextWatcher {

    private val aztecTextRef: WeakReference<AztecText?> = WeakReference(aztecText)
    private var textChangedEventDetails = TextChangedEvent("", 0, 0, 0)

    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
        textChangedEventDetails = TextChangedEvent(text.toString())
    }

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
        textChangedEventDetails.before = before
        textChangedEventDetails.text = text
        textChangedEventDetails.countOfCharacters = count
        textChangedEventDetails.start = start
        textChangedEventDetails.initialize()

        if (textChangedEventDetails.isNewLine()) return


        val aztecText = aztecTextRef.get()
        //last character was removed
        if(aztecText != null && textChangedEventDetails.inputEnd == 0 && textChangedEventDetails.inputStart == 1){
            aztecText.disableOnSelectionListener()
        }
    }


    override fun afterTextChanged(text: Editable) {
//        text.getSpans(0, text.length, EndOfParagraphMarker::class.java).forEach {
//            text.setSpan(it, text.getSpanStart(it), text.getSpanEnd(it), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//        }
    }

    companion object {
        fun install(editText: AztecText) {
            editText.addTextChangedListener(ZeroIndexContentWatcher(editText))
        }
    }
}
