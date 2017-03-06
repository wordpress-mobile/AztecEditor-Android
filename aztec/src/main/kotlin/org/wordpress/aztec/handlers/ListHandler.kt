package org.wordpress.aztec.handlers

import org.wordpress.aztec.spans.AztecListSpan

class ListHandler : GenericBlockHandler<AztecListSpan>(AztecListSpan::class.java) {
    override fun shouldHandle(): Boolean {
        return block.span.nestingLevel in (nestingLevel - 1)..(nestingLevel)
    }
}