package org.wordpress.aztec.plugins.html2visual

import android.text.Editable

/**
 * An interface for HTML content processing plugins. Plugin implementing this interface will be responsible for handling
 * the entire content of an element, as it will not be parsed.
 */
interface IHtmlContentHandler {
    fun canHandleTag(tag: String): Boolean
    fun handleContent(content: String, output: Editable, nestingLevel: Int)
}