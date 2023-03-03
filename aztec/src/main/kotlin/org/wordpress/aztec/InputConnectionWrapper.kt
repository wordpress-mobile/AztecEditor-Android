package org.wordpress.aztec

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.inputmethod.CompletionInfo
import android.view.inputmethod.CorrectionInfo
import android.view.inputmethod.ExtractedText
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputContentInfo
import androidx.annotation.RequiresApi

/**
 * Wrapper around proprietary Samsung InputConnection. Forwards all the calls to it, except for getExtractedText and
 * some custom logic in commitText
 */
abstract class InputConnectionWrapper(private val inputConnection: InputConnection) : InputConnection {
    override fun beginBatchEdit(): Boolean {
        return inputConnection.beginBatchEdit()
    }

    override fun endBatchEdit(): Boolean {
        return inputConnection.endBatchEdit()
    }

    override fun clearMetaKeyStates(states: Int): Boolean {
        return inputConnection.clearMetaKeyStates(states)
    }

    override fun sendKeyEvent(event: KeyEvent?): Boolean {
        return inputConnection.sendKeyEvent(event)
    }

    override fun commitCompletion(text: CompletionInfo?): Boolean {
        return inputConnection.commitCompletion(text)
    }

    override fun commitCorrection(correctionInfo: CorrectionInfo?): Boolean {
        return inputConnection.commitCorrection(correctionInfo)
    }

    override fun performEditorAction(actionCode: Int): Boolean {
        return inputConnection.performEditorAction(actionCode)
    }

    override fun performContextMenuAction(id: Int): Boolean {
        return inputConnection.performContextMenuAction(id)
    }

    override fun getExtractedText(request: ExtractedTextRequest?, flags: Int): ExtractedText? {
        return inputConnection.getExtractedText(request, flags)
    }

    override fun performPrivateCommand(action: String?, data: Bundle?): Boolean {
        return inputConnection.performPrivateCommand(action, data)
    }

    override fun setComposingText(text: CharSequence?, newCursorPosition: Int): Boolean {
        return inputConnection.setComposingText(text, newCursorPosition)
    }

    override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
        return inputConnection.commitText(text, newCursorPosition)
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    override fun commitContent(inputContentInfo: InputContentInfo, flags: Int, opts: Bundle?): Boolean {
        return inputConnection.commitContent(inputContentInfo, flags, opts)
    }

    override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
        return inputConnection.deleteSurroundingText(beforeLength, afterLength)
    }

    override fun requestCursorUpdates(cursorUpdateMode: Int): Boolean {
        return inputConnection.requestCursorUpdates(cursorUpdateMode)
    }

    override fun reportFullscreenMode(enabled: Boolean): Boolean {
        return inputConnection.reportFullscreenMode(enabled)
    }

    override fun setSelection(start: Int, end: Int): Boolean {
        return inputConnection.setSelection(start, end)
    }

    override fun finishComposingText(): Boolean {
        return inputConnection.finishComposingText()
    }

    override fun setComposingRegion(start: Int, end: Int): Boolean {
        return inputConnection.setComposingRegion(start, end)
    }

    override fun deleteSurroundingTextInCodePoints(beforeLength: Int, afterLength: Int): Boolean {
        return inputConnection.deleteSurroundingTextInCodePoints(beforeLength, afterLength)
    }

    override fun getCursorCapsMode(reqModes: Int): Int {
        return inputConnection.getCursorCapsMode(reqModes)
    }

    override fun getSelectedText(flags: Int): CharSequence? {
        return inputConnection.getSelectedText(flags)
    }

    override fun getTextAfterCursor(length: Int, flags: Int): CharSequence {
        return inputConnection.getTextAfterCursor(length, flags)
    }

    override fun getTextBeforeCursor(length: Int, flags: Int): CharSequence {
        return inputConnection.getTextBeforeCursor(length, flags)
    }

    override fun getHandler(): Handler? {
        return inputConnection.handler
    }

    override fun closeConnection() {
        inputConnection.closeConnection()
    }
}
