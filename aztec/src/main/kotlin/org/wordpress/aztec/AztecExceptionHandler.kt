package org.wordpress.aztec

import android.app.Activity
import org.wordpress.android.util.AppLog
import java.lang.Thread.UncaughtExceptionHandler

class AztecExceptionHandler(private val activity: Activity, private val visualEditor: AztecText) : UncaughtExceptionHandler {

    // Store the current exception handler
    private val rootHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

    init {
        // we replace the exception handler now with us -- we will properly dispatch the exceptions ...
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, ex: Throwable) {
        // Try to report the HTML code of the content, but do not report exceptions that can occur logging the content
        try {
            AppLog.e(AppLog.T.EDITOR, "HTML Content of Aztec Editor before the crash " + visualEditor?.toPlainHtml(false))
        } catch (e: Throwable) {
            AppLog.e(AppLog.T.EDITOR, "Visual Content of Aztec Editor before the crash " + visualEditor?.text)
        }

        rootHandler?.uncaughtException(thread, ex)
    }

    fun restoreDefault() {
        Thread.setDefaultUncaughtExceptionHandler(rootHandler)
    }
}
