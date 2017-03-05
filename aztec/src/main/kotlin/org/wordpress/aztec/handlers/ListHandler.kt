package org.wordpress.aztec.handlers

import org.wordpress.aztec.spans.AztecListSpan

class ListHandler : GenericBlockHandler<AztecListSpan>(AztecListSpan::class.java)