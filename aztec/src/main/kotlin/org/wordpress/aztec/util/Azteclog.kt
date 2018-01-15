package org.wordpress.aztec.util

class AztecLog {
    interface ExternalLogger {
        fun log(message : String)
        fun logException(tr : Throwable)
        fun logException(tr : Throwable, message : String)
    }
}