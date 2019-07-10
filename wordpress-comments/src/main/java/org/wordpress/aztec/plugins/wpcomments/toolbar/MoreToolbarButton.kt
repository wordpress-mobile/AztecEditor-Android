package org.wordpress.aztec.plugins.wpcomments.toolbar

import androidx.core.content.ContextCompat
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ToggleButton
import org.wordpress.android.util.DeviceUtils
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.Constants
import org.wordpress.aztec.plugins.IToolbarButton
import org.wordpress.aztec.plugins.wpcomments.R
import org.wordpress.aztec.plugins.wpcomments.spans.WordPressCommentSpan
import org.wordpress.aztec.spans.IAztecNestable
import org.wordpress.aztec.toolbar.AztecToolbar
import org.wordpress.aztec.toolbar.IToolbarAction

class MoreToolbarButton(val visualEditor: AztecText) : IToolbarButton {
    override val action: IToolbarAction = CommentsToolbarAction.MORE
    override val context = visualEditor.context!!

    override fun toggle() {
        visualEditor.removeInlineStylesFromRange(visualEditor.selectionStart, visualEditor.selectionEnd)
        visualEditor.removeBlockStylesFromRange(visualEditor.selectionStart, visualEditor.selectionEnd, true)

        val nestingLevel = IAztecNestable.getNestingLevelAt(visualEditor.editableText, visualEditor.selectionStart)

        val span = WordPressCommentSpan(
                WordPressCommentSpan.Comment.MORE.html,
                visualEditor.context,
                ContextCompat.getDrawable(visualEditor.context, R.drawable.img_more)!!,
                nestingLevel,
                visualEditor
        )

        val ssb = SpannableStringBuilder(Constants.MAGIC_STRING)
        ssb.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val start = visualEditor.selectionStart
        visualEditor.editableText.replace(start, visualEditor.selectionEnd, ssb)

        val newSelectionPosition = visualEditor.editableText.indexOf(Constants.MAGIC_CHAR, start) + 1
        visualEditor.setSelection(newSelectionPosition)
    }

    override fun matchesKeyShortcut(keyCode: Int, event: KeyEvent): Boolean {
        if (DeviceUtils.getInstance().isChromebook(context)) {
            return false // This opens the terminal in Chromebooks
        }

        return keyCode == KeyEvent.KEYCODE_T && event.isAltPressed && event.isCtrlPressed // Read More = Alt + Ctrl + T
    }

    override fun inflateButton(parent: ViewGroup) {
        LayoutInflater.from(context).inflate(R.layout.more_button, parent)
    }

    override fun toolbarStateAboutToChange(toolbar: AztecToolbar, enable: Boolean) {
        toolbar.findViewById<ToggleButton>(R.id.format_bar_button_more).isEnabled = enable
    }
}
