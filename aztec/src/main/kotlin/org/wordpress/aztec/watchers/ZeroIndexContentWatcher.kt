package org.wordpress.aztec.watchers

import android.text.Editable
import android.text.TextWatcher
import org.wordpress.aztec.AztecText
import java.lang.ref.WeakReference

//on some combinations of API levels and keyboards there is no KEYCODE_DEL event coming from IME on zero index of EditText
//we are using this watcher to try and detect those instances in order to remove leading spans when necessary
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
        if (aztecText != null && textChangedEventDetails.inputEnd == 0 && textChangedEventDetails.inputStart == 1) {
            aztecText.disableOnSelectionListener()
        }
    }


    override fun afterTextChanged(text: Editable) {
    }

    companion object {
        fun install(editText: AztecText) {
            editText.addTextChangedListener(ZeroIndexContentWatcher(editText))
        }
    }
}
