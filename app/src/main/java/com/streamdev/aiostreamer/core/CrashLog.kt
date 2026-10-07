package com.streamdev.aiostreamer.core

import android.content.Context
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Persists the most recent uncaught/startup crash stacktrace to a file so the
 * next launch can display it on screen — lets testers report a crash without adb.
 */
object CrashLog {
    private const val FILE = "last_crash.txt"

    fun save(context: Context, t: Throwable) {
        runCatching {
            val sw = StringWriter()
            t.printStackTrace(PrintWriter(sw))
            context.filesDir.resolve(FILE).writeText(sw.toString())
        }
    }

    fun read(context: Context): String? = runCatching {
        val f = context.filesDir.resolve(FILE)
        if (f.exists()) f.readText() else null
    }.getOrNull()

    fun clear(context: Context) {
        runCatching { context.filesDir.resolve(FILE).delete() }
    }
}
