package org.wordpress.aztec

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.SuggestionSpan
import android.view.KeyEvent
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.CompletionInfo
import android.view.inputmethod.CorrectionInfo
import android.view.inputmethod.ExtractedText
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputContentInfo
import org.wordpress.aztec.spans.IAztecSpan

/**
 * Wrapper around proprietary Samsung InputConnection. Forwards all the calls to it, except for getExtractedText
 */
class SamsungInputConnection(
        private val mTextView: AztecText,
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
    // (default behavior of BaseInputConnection) prevents Grammarly from messing up content most of the time
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
        val isSameStringValue = text.toString() == editable.toString()
        val incomingTextHasSuggestions = text is Spanned &&
                text.getSpans(0, text.length, SuggestionSpan::class.java).isNotEmpty()

        // Despite returning null from getExtractedText, in some cases Grammarly still tries to replace content of the
        // editor with own content containing suggestions. This mostly works ok, but Aztec spans are finicky, and tend
        // to get messed when content of the editor is replaced. In this method we remove Aztec spans before committing
        // the change, and reapply them afterward
        if (isSameStringValue && incomingTextHasSuggestions) {
            // create a clean spannable string from editable to temporarily hold spans
            val tempString = SpannableStringBuilder(editable.toString())

            // store Aztec and Suggestions spans in temp string
            TextUtils.copySpansFrom(editable, 0, editable.length, IAztecSpan::class.java, tempString, 0)
            TextUtils.copySpansFrom(text as Spanned, 0, editable.length, SuggestionSpan::class.java, tempString, 0)

            // remove all the Aztec spans from the current content of editor
            editable.getSpans(0, editable.length, IAztecSpan::class.java).forEach { editable.removeSpan(it) }

            // commit the text
            val result = baseInputConnection.commitText(text, newCursorPosition)

            // re-add the spans we removed before committing the text
            TextUtils.copySpansFrom(tempString, 0, editable.length, IAztecSpan::class.java, editable, 0)
            TextUtils.copySpansFrom(tempString, 0, editable.length, SuggestionSpan::class.java, editable, 0)

            return result
        }
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
