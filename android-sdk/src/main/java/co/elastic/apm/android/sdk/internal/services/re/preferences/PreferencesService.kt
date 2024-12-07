package co.elastic.apm.android.sdk.internal.services.re.preferences

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import co.elastic.apm.android.sdk.BuildConfig
import co.elastic.apm.android.sdk.internal.services.re.Service

class PreferencesService(application: Application) : Service {
    private val preferences: SharedPreferences by lazy {
        application.getSharedPreferences(
            BuildConfig.LIBRARY_PACKAGE_NAME + ".prefs",
            Context.MODE_PRIVATE
        )
    }

    fun store(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    fun store(key: String, value: Int) {
        preferences.edit().putInt(key, value).apply()
    }

    fun store(key: String, value: Long) {
        preferences.edit().putLong(key, value).apply()
    }

    fun retrieveString(key: String): String? {
        return preferences.getString(key, null)
    }

    fun retrieveInt(key: String, defaultValue: Int): Int {
        return preferences.getInt(key, defaultValue)
    }

    fun retrieveLong(key: String, defaultValue: Long): Long {
        return preferences.getLong(key, defaultValue)
    }
}