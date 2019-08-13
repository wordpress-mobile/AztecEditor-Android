package org.wordpress.aztec

interface IHistoryListener {
    fun onRedoEnabled()
    fun onUndoEnabled()
    fun onUndo()
    fun onRedo()
}
