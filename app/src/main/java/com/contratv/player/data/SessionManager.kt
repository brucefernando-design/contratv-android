package com.contratv.player.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "ContraTVSession"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_STATUS = "status"
        private const val KEY_EXP_DATE = "exp_date"
        private const val DEFAULT_SERVER = "https://panelcontra.lovable.app"
    }

    var serverUrl: String
        get() = prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER) ?: DEFAULT_SERVER
        set(value) = prefs.edit().putString(KEY_SERVER_URL, value.trim().trimEnd('/')).apply()

    var username: String
        get() = prefs.getString(KEY_USERNAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USERNAME, value).apply()

    var password: String
        get() = prefs.getString(KEY_PASSWORD, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PASSWORD, value).apply()

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var status: String
        get() = prefs.getString(KEY_STATUS, "Active") ?: "Active"
        set(value) = prefs.edit().putString(KEY_STATUS, value).apply()

    var expDate: String
        get() = prefs.getString(KEY_EXP_DATE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_EXP_DATE, value).apply()

    fun saveLogin(server: String, user: String, pass: String, userStatus: String, exp: String) {
        prefs.edit()
            .putString(KEY_SERVER_URL, server.trim().trimEnd('/'))
            .putString(KEY_USERNAME, user.trim())
            .putString(KEY_PASSWORD, pass.trim())
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_STATUS, userStatus)
            .putString(KEY_EXP_DATE, exp)
            .apply()
    }

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_PASSWORD)
            .apply()
    }
}
