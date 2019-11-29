package org.wordpress.aztec

import android.os.Handler
import android.os.Looper
import android.widget.EditText
import org.wordpress.aztec.source.SourceViewEditText
import java.util.LinkedList

class History(val historyEnabled: Boolean, val historySize: Int) {
    var historyCursor = 0
    var historyList = LinkedList<String>()
    var inputLast: String = ""

    private var historyListener: IHistoryListener? = null

    private var historyWorking = false

    private lateinit var inputBefore: String

    private val mainHandler = Handler(Looper.getMainLooper())
    private val historyRunnable: HistoryRunnable?
    private var textChangedPending = false

    // Time in ms to wait before applying change history to the stack
    var historyThrottleTime = 500L

    init {
        if (historyEnabled) {
            historyRunnable = HistoryRunnable(this)
        } else {
            historyRunnable = null
        }
    }

    fun beforeTextChanged(editText: EditText) {
        if (historyEnabled && !historyWorking) {
            mainHandler.removeCallbacks(historyRunnable)
            if (!textChangedPending) {
                textChangedPending = true
                historyRunnable?.text =
                    when (editText) {
                        is AztecText -> editText.toFormattedHtml()
                        is SourceViewEditText -> editText.text.toString()
                        else -> ""
                    }
                historyRunnable?.editText = editText
            }
            mainHandler.postDelayed(historyRunnable, historyThrottleTime)
        }
    }

    protected fun doHandleHistory(inputBefore: String, editText: EditText?) {
        textChangedPending = false
        inputLast = when (editText) {
            is AztecText -> editText.toFormattedHtml()
            is SourceViewEditText -> editText.text.toString()
            else -> ""
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

    /**
     * Useful for replacing the last history item after background
     * processing has completed. Example: uploading media.
     */
    fun refreshLastHistoryItem(editText: EditText) {
        if (!historyEnabled || historyWorking) {
            return
        }
        if (editText is AztecText) {
            inputLast = editText.toFormattedHtml()
        } else if (editText is SourceViewEditText) {
            inputLast = editText.text.toString()
        }
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
            } else if (editText is SourceViewEditText) {
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
        historyListener?.onRedo()
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
        historyListener?.onUndo()
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

        return historyCursor < historyList.size
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

    fun setHistoryListener(listener: IHistoryListener) {
        historyListener = listener
    }

    fun updateActions() {
        historyListener?.onRedoEnabled()
        historyListener?.onUndoEnabled()
    }

    /**
     * Only updates the history stack after a present of milliseconds has passed.
     */
    inner class HistoryRunnable(val history: History) : Runnable {
        var text: String = ""
        var editText: EditText? = null
        override fun run() {
            history.doHandleHistory(text, editText)
        }
    }
}
