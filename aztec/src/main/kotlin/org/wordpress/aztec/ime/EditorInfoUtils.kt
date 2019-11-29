package org.wordpress.aztec.ime

import android.os.Build
import android.view.inputmethod.EditorInfo
import java.util.Arrays

object EditorInfoUtils {
    @JvmStatic
    fun areEditorInfosTheSame(ed1: EditorInfo, ed2: EditorInfo): Boolean {
        if (ed1 == ed2) {
            return true
        }

        if (ed1.actionId == ed2.actionId
                && (ed1.actionLabel != null && ed1.actionLabel.equals(ed2.actionLabel) || ed1.actionLabel == null && ed2.actionLabel == null)
                && ed1.inputType == ed2.inputType
                && ed1.imeOptions == ed2.imeOptions
                && (ed1.privateImeOptions != null && ed1.privateImeOptions.equals(ed2.privateImeOptions) || ed1.privateImeOptions == null && ed2.privateImeOptions == null)
                && ed1.initialSelStart == ed2.initialSelStart
                && ed1.initialSelEnd == ed2.initialSelEnd
                && ed1.initialCapsMode == ed2.initialCapsMode
                && ed1.fieldId == ed2.fieldId
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                // specific comparisons here
                if (ed1.contentMimeTypes != null && ed2.contentMimeTypes != null) {
                    return Arrays.equals(ed1.contentMimeTypes, ed2.contentMimeTypes)
                }
            }
            return true
        }
        return false
    }

    @JvmStatic
    fun copyEditorInfo(ed1: EditorInfo) : EditorInfo {
        val copy = EditorInfo()
        copy.actionId = ed1.actionId
        copy.actionLabel = ed1.actionLabel?.toString()
        copy.inputType = ed1.inputType
        copy.imeOptions = ed1.imeOptions
        copy.privateImeOptions = ed1.privateImeOptions?.toString()
        copy.initialSelStart = ed1.initialSelStart
        copy.initialSelEnd = ed1.initialSelEnd
        copy.initialCapsMode = ed1.initialCapsMode
        copy.fieldId = ed1.fieldId
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            // specific comparisons here
            if (ed1.contentMimeTypes != null) {
                copy.contentMimeTypes = Arrays.copyOf(ed1.contentMimeTypes, ed1.contentMimeTypes.size)
            }
        }
        return copy
    }
}