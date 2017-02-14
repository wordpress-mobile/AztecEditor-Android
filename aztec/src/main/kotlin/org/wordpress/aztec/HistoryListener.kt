package org.wordpress.aztec

interface HistoryListener {
    fun onRedoEnabled(state: Boolean)
    fun onUndoEnabled(state: Boolean)
}
