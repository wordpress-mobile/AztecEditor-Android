package org.wordpress.aztec

import org.wordpress.android.util.AppLog
import org.xml.sax.Attributes
import org.xml.sax.helpers.AttributesImpl

class AztecAttributes(attributes: Attributes = AttributesImpl()) : AttributesImpl(attributes) {
    fun setValue(key: String, value: String) {
        val index = getIndex(key)
        if (index == -1) {
            addAttribute("", key, key, "string", value)
        } else {
            try {
                setValue(index, value)
            } catch (e: ArrayIndexOutOfBoundsException) {
                // we should not be here since `getIndex(key)` checks if the attribute is already available or not,
                // but apparently...https://github.com/wordpress-mobile/AztecEditor-Android/issues/705
                AppLog.e(AppLog.T.EDITOR, "Tried to set attribute: $key at index: $index")
            }
        }
    }

    fun isEmpty(): Boolean {
        return length == 0
    }

    fun removeAttribute(key: String) {
        if (hasAttribute(key)) {
            val index = getIndex(key)
            try {
                removeAttribute(index)
            } catch (e: ArrayIndexOutOfBoundsException) {
                // we should not be here since hasAttribute checked if the attribute is available or not,
                // but apparently...https://github.com/wordpress-mobile/AztecEditor-Android/issues/705
                AppLog.e(AppLog.T.EDITOR, "Tried to remove attribute: $key that is not in the list.")
                AppLog.e(AppLog.T.EDITOR, "Reported to be at index: $index")
            }
        }
    }

    fun hasAttribute(key: String): Boolean {
        return getValue(key) != null
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for (i in 0..this.length - 1) {
            sb.append(this.getLocalName(i))
            sb.append("=\"")
            sb.append(this.getValue(i))
            sb.append("\" ")
        }
        return sb.trimEnd().toString()
    }
}
