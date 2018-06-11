package org.wordpress.aztec

import org.wordpress.android.util.AppLog
import org.xml.sax.Attributes
import org.xml.sax.helpers.AttributesImpl

class AztecAttributes(attributes: Attributes = AttributesImpl()) : AttributesImpl(attributes) {
    fun setValue(key: String, value: String) {
        val index = getIndex(key)

        if (index == -1) {
            try {
                addAttribute("", key, key, "string", value)
            } catch (e: ArrayIndexOutOfBoundsException) {
                // https://github.com/wordpress-mobile/AztecEditor-Android/issues/705
                AppLog.e(AppLog.T.EDITOR, "Error adding attribute with name: $key and value: $value")
                logInternalState()
                throw e
            }
        } else {
            setValue(index, value)
        }
    }

    private fun logInternalState() {
        AppLog.e(AppLog.T.EDITOR, "AttributesImpl has an internal length of $length")
        // Since we're not sure the internal state of the Obj is correct we're wrapping toString in a try/catch
        try {
            AppLog.e(AppLog.T.EDITOR, "Dumping internal state:")
            AppLog.e(AppLog.T.EDITOR, toString())
        } catch (t: Throwable) {
            AppLog.e(AppLog.T.EDITOR, "Error dumping internal state!")
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
                // https://github.com/wordpress-mobile/AztecEditor-Android/issues/705
                AppLog.e(AppLog.T.EDITOR, "Tried to remove attribute: $key that is not in the list")
                AppLog.e(AppLog.T.EDITOR, "Reported to be at index: $index")
                logInternalState()
                throw e
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
