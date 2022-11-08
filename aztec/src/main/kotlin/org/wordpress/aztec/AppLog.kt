package org.wordpress.aztec

import android.text.TextUtils
import android.util.Log

/**
 * simple wrapper for Android log calls, enables recording and displaying log
 */
class AppLog private constructor() {
    // T for Tag
    enum class T {
        EDITOR
    }

    enum class LogLevel {
        v, d, i, w, e
    }

    companion object {
        const val TAG = "WordPress"

        /**
         * Sends a VERBOSE log message
         * @param tag Used to identify the source of a log message.
         * It usually identifies the class or activity where the log call occurs.
         * @param message The message you would like logged.
         */
        fun v(tag: T, message: String?) {
            var message = "$message"
            Log.v("$TAG-$tag", message)
        }

        /**
         * Sends a DEBUG log message
         * @param tag Used to identify the source of a log message.
         * It usually identifies the class or activity where the log call occurs.
         * @param message The message you would like logged.
         */
        fun d(tag: T, message: String?) {
            var message = "$message"
            Log.d("$TAG-$tag", message)
        }

        /**
         * Sends a INFO log message
         * @param tag Used to identify the source of a log message.
         * It usually identifies the class or activity where the log call occurs.
         * @param message The message you would like logged.
         */
        fun i(tag: T, message: String?) {
            var message = "$message"
            Log.i("$TAG-$tag", message)
        }

        /**
         * Sends a WARN log message
         * @param tag Used to identify the source of a log message.
         * It usually identifies the class or activity where the log call occurs.
         * @param message The message you would like logged.
         */
        fun w(tag: T, message: String?) {
            var message = "$message"
            Log.w("$TAG-$tag", message)
        }

        /**
         * Sends a ERROR log message
         * @param tag Used to identify the source of a log message.
         * It usually identifies the class or activity where the log call occurs.
         * @param message The message you would like logged.
         */
        fun e(tag: T, message: String?) {
            var message = "$message"
            Log.e("$TAG-$tag", message)
        }

        /**
         * Send a ERROR log message and log the exception.
         * @param tag Used to identify the source of a log message.
         * It usually identifies the class or activity where the log call occurs.
         * @param message The message you would like logged.
         * @param tr An exception to log
         */
        fun e(tag: T, message: String, tr: Throwable) {
            var message = "$message"
            Log.e("$TAG-$tag", message, tr)
        }

        /**
         * Sends a ERROR log message and the exception with StackTrace
         * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the
         * log call occurs.
         * @param tr An exception to log to get StackTrace
         */
        fun e(tag: T, tr: Throwable) {
            Log.e("$TAG-$tag", tr.message, tr)
        }

        /**
         * Sends a ERROR log message
         * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the
         * log call occurs.
         * @param volleyErrorMsg
         * @param statusCode
         */
        fun e(tag: T, volleyErrorMsg: String, statusCode: Int) {
            if (TextUtils.isEmpty(volleyErrorMsg)) {
                return
            }
            val logText = if (statusCode == -1) {
                volleyErrorMsg
            } else {
                "$volleyErrorMsg, status $statusCode"
            }
            Log.e("$TAG-$tag", logText)
        }
    }
}
