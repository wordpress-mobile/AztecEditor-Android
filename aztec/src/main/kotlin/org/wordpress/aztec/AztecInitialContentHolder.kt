package org.wordpress.aztec

import android.os.Parcel
import android.os.Parcelable
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Arrays

class AztecInitialContentHolder() : Parcelable {

    enum class EditorHasChanges {
        CHANGES, NO_CHANGES, UNKNOWN
    }

    constructor(parcel : Parcel) : this() {
        initialEditorContentParsedSHA256 = ByteArray(parcel.readInt())
        parcel.readByteArray(initialEditorContentParsedSHA256)
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(initialEditorContentParsedSHA256.size)
        dest?.writeByteArray(initialEditorContentParsedSHA256)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<AztecInitialContentHolder> = object : Parcelable.Creator<AztecInitialContentHolder> {
            override fun createFromParcel(`in`: Parcel): AztecInitialContentHolder {
                return AztecInitialContentHolder(`in`)
            }

            override fun newArray(size: Int): Array<AztecInitialContentHolder?> {
                return arrayOfNulls(size)
            }
        }
    }

    private var initialEditorContentParsedSHA256: ByteArray = ByteArray(0)

    fun needToSetInitialValue(): Boolean {
        return (initialEditorContentParsedSHA256.isEmpty() || Arrays.equals(initialEditorContentParsedSHA256, calculateSHA256("")))
    }

    fun setInitialContent(source: String) {
        try {
            // Do not recalculate the hash if it's not the first call to `fromHTML`.
            if (needToSetInitialValue()) {
                //   val initialHTMLParsed = toPlainHtml(false)
                initialEditorContentParsedSHA256 = calculateSHA256(source)
            }
        } catch (e: Throwable) {
            // Do nothing here. `toPlainHtml` can throw exceptions, also calculateSHA256 -> NoSuchAlgorithmException
        }
    }

    @Throws(NoSuchAlgorithmException::class)
    private fun calculateSHA256(s: String): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(s.toByteArray())
        return digest.digest()
    }

    fun hasChanges(source: String): EditorHasChanges {
        if (!initialEditorContentParsedSHA256.isEmpty()) {
            try {
                if (Arrays.equals(initialEditorContentParsedSHA256, calculateSHA256(source))) {
                    return EditorHasChanges.NO_CHANGES
                }
                return EditorHasChanges.CHANGES
            } catch (e: Throwable) {
                // Do nothing here. `toPlainHtml` can throw exceptions, also calculateSHA256 -> NoSuchAlgorithmException
            }
        }
        return EditorHasChanges.UNKNOWN
    }
}