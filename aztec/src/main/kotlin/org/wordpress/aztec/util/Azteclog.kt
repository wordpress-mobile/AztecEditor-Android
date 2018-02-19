package org.wordpress.aztec.util

import android.text.Spannable
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.wordpress.android.util.AppLog
import org.wordpress.aztec.AztecText

class AztecLog {
    interface ExternalLogger {
        fun log(message : String)
        fun logException(tr : Throwable)
        fun logException(tr : Throwable, message : String)
    }

    companion object {
        fun logContentDetails(aztecText: AztecText) {
            AppLog.d(AppLog.T.EDITOR, "Below are the details of the content in the editor:")
            logContentDetails(aztecText.text)
        }

        fun logContentDetails(text: Spannable) {
            try {
                val logContentJSON = JSONObject()
                logContentJSON.put("content", text.toString())
                logContentJSON.put("length", text.length)
                val spansJSON = JSONArray()
                val spans = text.getSpans(0, text.length, Any::class.java)
                spans.forEach {
                    val currenSpanJSON = JSONObject()
                    currenSpanJSON.put("clazz", it.javaClass.name)
                    currenSpanJSON.put("start", text.getSpanStart(it))
                    currenSpanJSON.put("end", text.getSpanEnd(it))
                    currenSpanJSON.put("flags", text.getSpanFlags(it))
                    spansJSON.put(currenSpanJSON)
                }
                logContentJSON.put("spans", spansJSON)
                AppLog.d(AppLog.T.EDITOR, logContentJSON.toString())
            } catch (e: JSONException) {
                AppLog.e(AppLog.T.EDITOR, "Uhh ohh! There was an error logging the content of the Editor. This should" +
                        "never happen.", e)
            }
        }
    }
}