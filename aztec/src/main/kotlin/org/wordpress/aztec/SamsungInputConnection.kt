package org.wordpress.aztec

import android.os.Build
import android.widget.TextView
import android.view.inputmethod.BaseInputConnection
import android.text.Editable
import android.view.inputmethod.CompletionInfo
import android.view.inputmethod.CorrectionInfo
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.ExtractedText
import android.view.inputmethod.InputConnection
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.InputContentInfo

/**
 * Wrapper around proprietary Samsung InputConnection. Forwards all the calls to it, except for getExtractedText
 */
class SamsungInputConnection(
        private val mTextView: TextView,
        private val baseInputConnection: InputConnection,
) : BaseInputConnection(mTextView, true) {

    override fun getEditable(): Editable {
        return mTextView.editableText
    }

    override fun beginBatchEdit(): Boolean {
        return baseInputConnection.beginBatchEdit()
    }

    override fun endBatchEdit(): Boolean {
        return baseInputConnection.endBatchEdit()
    }

    override fun clearMetaKeyStates(states: Int): Boolean {
        return baseInputConnection.clearMetaKeyStates(states)
    }

    override fun sendKeyEvent(event: KeyEvent?): Boolean {
        return super.sendKeyEvent(event)
    }

    override fun commitCompletion(text: CompletionInfo?): Boolean {
        return baseInputConnection.commitCompletion(text)
    }

    override fun commitCorrection(correctionInfo: CorrectionInfo?): Boolean {
        return baseInputConnection.commitCorrection(correctionInfo)
    }

    override fun performEditorAction(actionCode: Int): Boolean {
        return baseInputConnection.performEditorAction(actionCode)
    }

    override fun performContextMenuAction(id: Int): Boolean {
        return baseInputConnection.performContextMenuAction(id)
    }

    // Extracted text on Samsung devices on Android 13 is somehow used for Grammarly suggestions which causes a lot of
    // issues with spans and cursors. We do not use extracted text, so returning null
    // (default behavior of BaseInputConnection) fixes the problem
    override fun getExtractedText(request: ExtractedTextRequest?, flags: Int): ExtractedText? {
        return null
    }

    override fun performPrivateCommand(action: String?, data: Bundle?): Boolean {
        return baseInputConnection.performPrivateCommand(action, data)
    }

    override fun setComposingText(text: CharSequence?, newCursorPosition: Int): Boolean {
        return baseInputConnection.setComposingText(text, newCursorPosition)
    }

    override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
        return baseInputConnection.commitText(text, newCursorPosition)
    }

    override fun commitContent(inputContentInfo: InputContentInfo, flags: Int, opts: Bundle?): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            baseInputConnection.commitContent(inputContentInfo, flags, opts)
        } else {
            super.commitContent(inputContentInfo, flags, opts)
        }
    }

    override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
        return baseInputConnection.deleteSurroundingText(beforeLength, afterLength)
    }

    override fun requestCursorUpdates(cursorUpdateMode: Int): Boolean {
        return baseInputConnection.requestCursorUpdates(cursorUpdateMode)
    }

    override fun reportFullscreenMode(enabled: Boolean): Boolean {
        return baseInputConnection.reportFullscreenMode(enabled)
    }

    override fun setSelection(start: Int, end: Int): Boolean {
        return baseInputConnection.setSelection(start, end)
    }

    override fun finishComposingText(): Boolean {
        return baseInputConnection.finishComposingText()
    }

    override fun setComposingRegion(start: Int, end: Int): Boolean {
        return baseInputConnection.setComposingRegion(start, end)
    }

    override fun deleteSurroundingTextInCodePoints(beforeLength: Int, afterLength: Int): Boolean {
        return baseInputConnection.deleteSurroundingTextInCodePoints(beforeLength, afterLength)
    }

    override fun getCursorCapsMode(reqModes: Int): Int {
        return baseInputConnection.getCursorCapsMode(reqModes)
    }

    override fun getSelectedText(flags: Int): CharSequence? {
        return baseInputConnection.getSelectedText(flags)
    }

    override fun getTextAfterCursor(length: Int, flags: Int): CharSequence {
        return baseInputConnection.getTextAfterCursor(length, flags)
    }

    override fun getTextBeforeCursor(length: Int, flags: Int): CharSequence {
        return baseInputConnection.getTextBeforeCursor(length, flags)
    }
}
