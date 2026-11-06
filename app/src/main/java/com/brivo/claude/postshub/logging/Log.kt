package com.brivo.claude.postshub.logging

import kotlinx.serialization.SerializationException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Log {
    private const val SUBSYSTEM = "PostsHub"

    val app = Logger("$SUBSYSTEM.App")
    val api = Logger("$SUBSYSTEM.API")
    val store = Logger("$SUBSYSTEM.Store")
    val list = Logger("$SUBSYSTEM.List")
    val detail = Logger("$SUBSYSTEM.Detail")
    val row = Logger("$SUBSYSTEM.Row")
    val favorites = Logger("$SUBSYSTEM.Favorites")
    val filter = Logger("$SUBSYSTEM.Filter")

    fun timestamp(): String {
        val f = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
        return f.format(Date())
    }

    fun dumpDecodingError(error: Throwable, data: String, logger: Logger) {
        val preview = data.take(500)
        when (error) {
            is SerializationException ->
                logger.event("decode failure: ${error::class.simpleName} — ${error.message}")
            else ->
                logger.event("decode failure: ${error.message}")
        }
        logger.event("response preview: $preview")
    }
}

class Logger(private val tag: String) {
    fun event(message: String) {
        val ts = Log.timestamp()
        android.util.Log.d(tag, "$ts $message")
    }
}
