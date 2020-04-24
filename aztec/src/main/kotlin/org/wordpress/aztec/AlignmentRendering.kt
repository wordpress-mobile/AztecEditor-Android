package org.wordpress.aztec

/**
 * With [SPAN_LEVEL] any alignment must be specified at the span level. Importantly, this
 * means that the View's gravity will always be ignored in determining the rendering of
 * the text's alignment.
 *
 * With [VIEW_LEVEL] alignment, the rendering of alignment is determined by the View's gravity.
 * Note that it is not possible to update the underlying alignment using [AztecText.toggleFormatting]
 * when you are using [VIEW_LEVEL] alignment rendering.
 */
enum class AlignmentRendering { SPAN_LEVEL, VIEW_LEVEL }
