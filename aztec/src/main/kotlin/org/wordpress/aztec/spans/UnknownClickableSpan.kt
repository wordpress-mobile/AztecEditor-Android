package org.wordpress.aztec.spans

import android.text.style.ClickableSpan
import android.view.View

class UnknownClickableSpan(private val unknownHtmlSpan: UnknownHtmlSpan) : ClickableSpan() {

    override fun onClick(widget: View) {
        this.unknownHtmlSpan.onClick(widget)
    }
}
