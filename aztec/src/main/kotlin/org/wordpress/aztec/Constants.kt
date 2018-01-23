package org.wordpress.aztec

object Constants {
    val MAGIC_CHAR = '\uFEFF' // '*'
    val MAGIC_STRING = "" + MAGIC_CHAR
    val REPLACEMENT_MARKER_CHAR = '\u202F'
    val REPLACEMENT_MARKER_STRING = "" + REPLACEMENT_MARKER_CHAR
    val ZWJ_CHAR = '\u200B'//'§'
    val ZERO_WIDTH_PLACEHOLDER_CHAR = '\u200C'
    val ZERO_WIDTH_PLACEHOLDER_STRING = "" + ZERO_WIDTH_PLACEHOLDER_CHAR
    val ZWJ_STRING = "" + ZWJ_CHAR
    val IMG_CHAR = '\uFFFC'
    val IMG_STRING = "" + IMG_CHAR
    val NEWLINE = '\n'
    val NEWLINE_STRING = "" + NEWLINE
    val END_OF_BUFFER_MARKER = ZWJ_CHAR
    val END_OF_BUFFER_MARKER_STRING = "" + ZWJ_CHAR
}
