package org.wordpress.aztec.handlers

import org.wordpress.aztec.spans.AztecQuoteSpan

class QuoteHandler : GenericBlockHandler<AztecQuoteSpan>(AztecQuoteSpan::class.java)