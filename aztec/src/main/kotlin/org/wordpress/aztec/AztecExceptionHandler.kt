package org.wordpress.aztec

import android.os.Build
import android.util.Log
import org.wordpress.android.util.AppLog
import org.wordpress.aztec.exceptions.DynamicLayoutGetBlockIndexOutOfBoundsException
import org.wordpress.aztec.util.AztecLog
import java.lang.Thread.UncaughtExceptionHandler

class AztecExceptionHandler(private val logHelper: ExceptionHandlerHelper?, private val visualEditor: AztecText) : UncaughtExceptionHandler {

    interface ExceptionHandlerHelper {
        fun shouldLog(ex: Throwable) : Boolean
    }

    // Store the current exception handler
    private val rootHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

    init {
        // we replace the exception handler now with us -- we will properly dispatch the exceptions ...
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, ex: Throwable) {
        // Check if we should log the content or not
        var shouldLog = true
        try {
            shouldLog = logHelper?.shouldLog(ex) ?: true
        } catch (e: Throwable) {
            AppLog.w(AppLog.T.EDITOR, "There was an exception in the Logger Helper. Set the logging to true")
        }

        if (shouldLog) {
            // Try to report the HTML code of the content, the spans details, but do not report exceptions that can occur logging the content
            try {
                AppLog.e(AppLog.T.EDITOR, "HTML content of Aztec Editor before the crash:")
                AppLog.e(AppLog.T.EDITOR, visualEditor.toPlainHtml(false))
            } catch (e: Throwable) {
                AppLog.e(AppLog.T.EDITOR, "Oops! There was an error logging the HTML code.")
            }
            try {
                AztecLog.logContentDetails(visualEditor)
            } catch (e: Throwable) {
                // nop
            }
        }

        // Detect ArrayIndexOutOfBoundsException on Android 8, and report it to the parent app
        // See: https://github.com/wordpress-mobile/WordPress-Android/issues/8828
        if (ex is ArrayIndexOutOfBoundsException) {
            val stackTrace = Log.getStackTraceString(ex)
            var detected = false
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O &&
                    stackTrace.contains("android.text.DynamicLayout.getBlockIndex(DynamicLayout.java:646)")) {
                detected = true
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1 &&
                    stackTrace.contains("android.text.DynamicLayout.getBlockIndex(DynamicLayout.java:648)")) {
                detected = true
            }
            if (detected) {
                visualEditor.externalLogger?.logException(DynamicLayoutGetBlockIndexOutOfBoundsException("Error #8828", ex))
            }
        }

        rootHandler?.uncaughtException(thread, ex)
    }

    fun restoreDefaultHandler() {
        Thread.setDefaultUncaughtExceptionHandler(rootHandler)
    }
}
