package org.wordpress.aztec.placeholders

import android.content.Context
import android.graphics.drawable.Drawable
import kotlinx.coroutines.runBlocking
import org.wordpress.aztec.AztecAttributes
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.spans.AztecMediaSpan
import org.wordpress.aztec.spans.IAztecFullWidthImageSpan
import org.wordpress.aztec.spans.IAztecSpan

class AztecPlaceholderSpan(
        context: Context,
        drawable: Drawable?,
        override var nestingLevel: Int,
        attributes: AztecAttributes = AztecAttributes(),
        onMediaDeletedListener: AztecText.OnMediaDeletedListener? = null,
        editor: AztecText? = null,
        private val adapter: PlaceholderManager.PlaceholderAdapter,
        override val TAG: String) :
        AztecMediaSpan(context, drawable, attributes, onMediaDeletedListener, editor), IAztecFullWidthImageSpan, IAztecSpan {
    override fun onClick() {

    }

    override fun getMaxWidth(editorWidth: Int): Int {
        return runBlocking { adapter.calculateWidth(attributes, editorWidth) }
    }
}
