package org.wordpress.aztec

import org.ccil.cowan.tagsoup.HTMLSchema

class AztecHtmlSchema : HTMLSchema() {
    // Remove unnecessary default values for attributes, which are not mandatory
    init {
        fixIframeElement()
        fixLinkElement()
        fixBrElement()
    }

    private fun fixIframeElement() {
        val iframe = getElementType("iframe")

        var index = iframe.atts().getIndex("frameborder")
        iframe.atts().setValue(index, null)

        index = iframe.atts().getIndex("scrolling")
        iframe.atts().setValue(index, null)
    }

    private fun fixLinkElement() {
        val iframe = getElementType("a")

        val index = iframe.atts().getIndex("shape")
        iframe.atts().setValue(index, null)
    }

    private fun fixBrElement() {
        val iframe = getElementType("br")

        val index = iframe.atts().getIndex("clear")
        iframe.atts().setValue(index, null)
    }
}
