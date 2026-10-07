package com.streamdev.aiostreamer.app

import android.app.Application

/**
 * Application entry point.
 *
 * The original build used `adapter.GLOBALVARS` (which extended a signature-killer
 * base class to defeat tamper checks). The revival drops that crack scaffolding
 * and simply builds the dependency [Graph] once at startup.
 */
class StreamerApp : Application() {

    lateinit var graph: Graph
        private set

    override fun onCreate() {
        super.onCreate()
        graph = Graph(this)
    }

    companion object {
        /** Mobile Safari UA (kept for parity with the original GLOBALVARS.USERAGENT). */
        const val USER_AGENT_MOBILE =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 " +
            "(KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1"

        /** Desktop Chrome UA (kept for parity with the original GLOBALVARS.USERAGENT2). */
        const val USER_AGENT_DESKTOP =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36"
    }
}
