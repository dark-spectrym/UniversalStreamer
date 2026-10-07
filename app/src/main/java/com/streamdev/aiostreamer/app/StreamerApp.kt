package com.streamdev.aiostreamer.app

import android.app.Application
import com.streamdev.aiostreamer.core.CrashLog

/**
 * Application entry point.
 *
 * Installs a last-resort crash handler and builds the dependency [Graph] inside a
 * guard: a startup failure is recorded (and shown on screen by MainActivity)
 * instead of silently killing the process, so alpha crashes are diagnosable
 * without adb.
 */
class StreamerApp : Application() {

    @Volatile
    var graph: Graph? = null
        private set

    @Volatile
    var startupError: Throwable? = null
        private set

    override fun onCreate() {
        super.onCreate()

        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            CrashLog.save(this, throwable)
            previous?.uncaughtException(thread, throwable)
        }

        try {
            graph = Graph(this)
        } catch (e: Throwable) {
            startupError = e
            CrashLog.save(this, e)
        }
    }

    companion object {
        /** Mobile Safari UA (kept for parity with the original GLOBALVARS.USERAGENT). */
        const val USER_AGENT_MOBILE =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 " +
            "(KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1"

        /** Desktop UA (kept for parity with the original GLOBALVARS.USERAGENT2). */
        const val USER_AGENT_DESKTOP =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:152.0) Gecko/20100101 Firefox/152.0"
    }
}
