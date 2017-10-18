package org.wordpress.aztec.spans

import android.text.style.ClickableSpan
import android.view.View

class AztecMediaClickableSpan(private val mediaSpan: AztecMediaSpan) : ClickableSpan() {
    override fun onClick(view: View) {
        this.mediaSpan.onClick()
    }
}
