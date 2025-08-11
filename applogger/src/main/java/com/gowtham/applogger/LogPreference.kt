package com.gowtham.applogger

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class LogPreference(context: Context) {

    companion object {
        private const val PREF_NAME = "log_prefs"
        private const val KEY_LOG_ENABLED = "log_enabled"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun isLogEnabled(): Boolean =
        prefs.getBoolean(KEY_LOG_ENABLED, true)

    fun setLogEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_LOG_ENABLED, enabled) }
    }
}