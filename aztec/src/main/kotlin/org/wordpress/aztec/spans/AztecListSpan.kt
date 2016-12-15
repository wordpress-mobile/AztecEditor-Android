package org.wordpress.aztec.spans


interface AztecListSpan : AztecBlockSpan {
    var lastItem: AztecListItemSpan
}
