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
        AppLog.e(AppLog.T.EDITOR, "Dumping internal state:")
        AppLog.e(AppLog.T.EDITOR, "length = $length")
        // Since the toString can throw OOB error we need to wrap it in a try/catch
        try {
            AppLog.e(AppLog.T.EDITOR, toString())
        } catch (e: ArrayIndexOutOfBoundsException) {
            // No need to log anything here. `toString` already writes to log details, but we need to shallow the exception
            // we don't want to crash logging state of the app
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
        try {
            for (i in 0..this.length - 1) {
                sb.append(this.getLocalName(i))
                sb.append("=\"")
                sb.append(this.getValue(i))
                sb.append("\" ")
            }
        } catch (e: ArrayIndexOutOfBoundsException) {
            // https://github.com/wordpress-mobile/AztecEditor-Android/issues/705
            AppLog.e(AppLog.T.EDITOR, "IOOB occurred in toString. Dumping partial state:")
            AppLog.e(AppLog.T.EDITOR, sb.trimEnd().toString())
            throw e
        }

        return sb.trimEnd().toString()
    }
}
