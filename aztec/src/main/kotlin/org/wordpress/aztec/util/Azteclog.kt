package org.wordpress.aztec.util

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.wordpress.android.util.AppLog
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.spans.IAztecAttributedSpan

class AztecLog {
    interface ExternalLogger {
        fun log(message : String)
        fun logException(tr : Throwable)
        fun logException(tr : Throwable, message : String)
    }

    companion object {
        fun logEditorContentDetails(aztecText: AztecText) {
            try {
                val logContentJSON = JSONObject()
                logContentJSON.put("content", aztecText.text.toString())
                logContentJSON.put("length", aztecText.text.length)
                val spansJSON = JSONArray()
                val spans = aztecText.text.getSpans(0, aztecText.text.length, IAztecAttributedSpan::class.java)
                spans.forEach {
                    val currenSpanJSON = JSONObject()
                    currenSpanJSON.put("clasz", it.javaClass.name)
                    currenSpanJSON.put("start", aztecText.text.getSpanStart(it))
                    currenSpanJSON.put("end", aztecText.text.getSpanEnd(it))
                    currenSpanJSON.put("flags", aztecText.text.getSpanFlags(it))
                    spansJSON.put(currenSpanJSON)
                }
                logContentJSON.put("spans", spansJSON)
                AppLog.d(AppLog.T.EDITOR, "Below are the details of the content in the editor:")
                AppLog.d(AppLog.T.EDITOR, logContentJSON.toString())
            } catch (e: JSONException) {
                AppLog.e(AppLog.T.EDITOR, "Uhh ohh! There was an error logging the content of the Editor. This should" +
                        "never happen.", e)
            }
        }
    }
}