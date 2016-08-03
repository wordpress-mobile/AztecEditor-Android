package org.wordpress.aztec

import org.ccil.cowan.tagsoup.AttributesImpl
import org.ccil.cowan.tagsoup.ElementType
import org.ccil.cowan.tagsoup.HTMLSchema

class AztecHtmlSchema : HTMLSchema() {

    init {
        fixIframeElement()
    }

    // Remove unnecessary default values for attributes, which are not mandatory
    private fun fixIframeElement() {
        val iframe = getElementType("iframe")

        var index = iframe.atts().getIndex("frameborder")
        iframe.atts().setValue(index, null)

        index = iframe.atts().getIndex("scrolling")
        iframe.atts().setValue(index, null)
    }
}
