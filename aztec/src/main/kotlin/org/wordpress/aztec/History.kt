package org.wordpress.aztec

import android.widget.EditText
import org.wordpress.aztec.source.SourceViewEditText
import java.util.*

class History(val historyEnabled: Boolean, val historySize: Int) {
    var historyCursor = 0
    var historyList = LinkedList<String>()
    var inputLast: String = ""

    private var historyListener: HistoryListener? = null

    private var historyWorking = false

    private lateinit var inputBefore: String

    fun beforeTextChanged(text: String) {
        if (historyEnabled && !historyWorking) {
            inputBefore = text
        }
    }

    fun handleHistory(editText: EditText) {
        if (!historyEnabled || historyWorking) {
            return
        }

        if (editText is AztecText) {
            inputLast = editText.toFormattedHtml()
        }
        else if (editText is SourceViewEditText) {
            inputLast = editText.text.toString()
        }

        if (inputLast == inputBefore) {
            return
        }

        while (historyCursor != historyList.size && historyCursor >= 0) {
            historyList.removeAt(historyCursor)
        }

        if (historyList.size >= historySize) {
            historyList.removeAt(0)
            historyCursor--
        }

        historyList.add(inputBefore)
        historyCursor = historyList.size

        updateActions()
    }

    fun redo(editText: EditText) {
        if (!redoValid()) {
            return
        }

        historyWorking = true

        editText.isFocusable = false
        editText.isFocusableInTouchMode = false

        if (historyCursor >= historyList.size - 1) {
            historyCursor = historyList.size

            if (editText is AztecText) {
                editText.fromHtml(inputLast)
            }
            else if (editText is SourceViewEditText) {
                editText.displayStyledHtml(inputLast)
            }
        } else {
            historyCursor++
            setTextFromHistory(editText)
        }

        historyWorking = false

        editText.isFocusable = true
        editText.isFocusableInTouchMode = true
        editText.requestFocus()

        updateActions()
    }

    fun undo(editText: EditText) {
        if (!undoValid()) {
            return
        }

        historyWorking = true
        historyCursor--

        editText.isFocusable = false
        editText.isFocusableInTouchMode = false


        setTextFromHistory(editText)

        historyWorking = false

        editText.isFocusable = true
        editText.isFocusableInTouchMode = true
        editText.requestFocus()

        updateActions()
    }

    private fun setTextFromHistory(editText: EditText) {
        if (editText is AztecText) {
            editText.fromHtml(historyList[historyCursor])
        } else if (editText is SourceViewEditText) {
            editText.displayStyledHtml(historyList[historyCursor])
        }
    }

    fun redoValid(): Boolean {
        if (!historyEnabled || historySize <= 0 || historyList.size <= 0 || historyWorking) {
            return false
        }

        return historyCursor < historyList.size - 1 || historyCursor >= historyList.size - 1
    }

    fun undoValid(): Boolean {
        if (!historyEnabled || historySize <= 0 || historyWorking) {
            return false
        }

        if (historyList.size <= 0 || historyCursor <= 0) {
            return false
        }

        return true
    }

    fun clearHistory() {
        inputLast = ""
        historyList.clear()
    }

    fun setHistoryListener(listener: HistoryListener) {
        historyListener = listener
    }

    fun updateActions() {
        historyListener?.onRedoEnabled(redoValid())
        historyListener?.onUndoEnabled(undoValid())
    }
}
