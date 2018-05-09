package org.wordpress.aztec.plugins.wpcomments.handlers

import org.wordpress.aztec.handlers.GenericBlockHandler
import org.wordpress.aztec.plugins.wpcomments.spans.GutenbergCommentSpan

class GutenbergCommentHandler : GenericBlockHandler<GutenbergCommentSpan>(GutenbergCommentSpan::class.java)