package org.wordpress.aztec.handlers

import org.wordpress.aztec.spans.AztecPreformatSpan

class PreformatHandler : GenericBlockHandler<AztecPreformatSpan>(AztecPreformatSpan::class.java)
