package org.wordpress.aztec.util

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import org.wordpress.android.util.AppLog
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class InstanceStateUtils {
    companion object {

        private fun cacheFilenameKey(varName: String): String {
            return "CACHEFILENAMEKEY_$varName"
        }

        private fun logCacheWriteException(externalLogger: AztecLog.ExternalLogger?, varName: String, e: Exception) {
            AppLog.w(AppLog.T.EDITOR, "Error trying to write cache for $varName. Exception: ${e.message}")
            externalLogger?.logException(e, "Error trying to write cache for $varName.")
        }

        open fun writeTempInstance(context: Context, externalLogger: AztecLog.ExternalLogger?, varName: String, obj: Any?, bundle: Bundle) {
            try {
                with(File.createTempFile(varName, ".inst", context.getCacheDir())) {
                    deleteOnExit() // just make sure if we miss deleting this cache file the VM will eventually do it

                    FileOutputStream(this).use { output ->
                        ObjectOutputStream(output).use { objectOutput ->
                            objectOutput.writeObject(obj)

                            // keep the filename in the bundle to use it to read the object back
                            bundle.putString(cacheFilenameKey(varName), this.path)
                        }
                    }
                }
            } catch (e: IOException) {
                logCacheWriteException(externalLogger, varName, e)
            } catch (e: SecurityException) {
                logCacheWriteException(externalLogger, varName, e)
            } catch (e: NullPointerException) {
                logCacheWriteException(externalLogger, varName, e)
            }
        }

        open fun <T> readAndPurgeTempInstance(varName: String, defaultValue: T, bundle: Bundle): T {
            // the full path is kept in the bundle so, get it from there
            val filename = bundle.getString(cacheFilenameKey(varName))

            if (TextUtils.isEmpty(filename)) {
                return defaultValue
            }

            val file = File(filename)

            if (!file.exists()) {
                return defaultValue
            }

            var obj: T = defaultValue

            with(file) {
                FileInputStream(this).use { input ->
                    ObjectInputStream(input).use { objectInput ->
                        val r: Any? = objectInput.readObject()

                        @Suppress("UNCHECKED_CAST")
                        obj = (r ?: defaultValue) as T
                    }
                }
                delete() // eagerly delete the cache file. If any is missed the VM will delete it on reboot.
            }

            return obj
        }
    }
}