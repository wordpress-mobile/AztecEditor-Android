package org.wordpress.aztec.spans

interface IAztecInlineSpan : IAztecSpan

/**
 * Extend this interface if an inline span should be exclusive, meaning
 * only a single inline format can be applied to this span.
 */
interface IAztecExclusiveInlineSpan : IAztecInlineSpan
