package org.wordpress.aztec

object Constants {
    val MAGIC_CHAR = '\uFEFF' //'*'
    val MAGIC_STRING = "" + MAGIC_CHAR
    val ZWJ_CHAR = '\u200B'//'§'
    val ZWJ_STRING = "" + ZWJ_CHAR
    val IMG_CHAR = '\uFFFC'
    val IMG_STRING = "" + IMG_CHAR
    val NEWLINE = '\n'
    val END_OF_BUFFER_MARKER = ZWJ_CHAR
    val END_OF_BUFFER_MARKER_STRING = "" + ZWJ_CHAR
}
