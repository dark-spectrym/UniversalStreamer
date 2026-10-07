package com.streamdev.aiostreamer.core.prefs

import android.content.Context
import android.content.SharedPreferences

/**
 * Thin wrapper over the app's `settings` SharedPreferences file.
 *
 * Kotlin re-implementation of the original `helper.SharedPref`, keeping the same
 * file name and keys so a user's existing settings survive the upgrade. Unlike
 * the original static singleton, it is initialised once from [StreamerApp].
 */
class SharedPref private constructor(private val prefs: SharedPreferences) {

    fun getString(key: String, default: String = ""): String = prefs.getString(key, default) ?: default
    fun getInt(key: String, default: Int = 0): Int = prefs.getInt(key, default)
    fun getBoolean(key: String, default: Boolean = false): Boolean = prefs.getBoolean(key, default)

    fun put(key: String, value: String) = prefs.edit().putString(key, value).apply()
    fun put(key: String, value: Int) = prefs.edit().putInt(key, value).apply()
    fun put(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).apply()
    fun remove(key: String) = prefs.edit().remove(key).apply()

    companion object {
        private const val FILE = "settings"

        @Volatile private var instance: SharedPref? = null

        fun init(context: Context): SharedPref =
            instance ?: synchronized(this) {
                instance ?: SharedPref(
                    context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE),
                ).also { instance = it }
            }

        fun get(): SharedPref = instance
            ?: error("SharedPref.init(context) must be called before use (see StreamerApp).")
    }
}
