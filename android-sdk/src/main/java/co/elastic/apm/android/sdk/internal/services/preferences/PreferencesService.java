package co.elastic.apm.android.sdk.internal.services.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import co.elastic.apm.android.sdk.BuildConfig;
import co.elastic.apm.android.sdk.internal.services.Service;

public class PreferencesService implements Service {
    private final SharedPreferences preferences;

    public PreferencesService(Context context) {
        preferences = context.getSharedPreferences(BuildConfig.LIBRARY_PACKAGE_NAME + ".prefs", Context.MODE_PRIVATE);
    }

    @Override
    public void start() {
        // No op
    }

    @Override
    public void stop() {
        // No op
    }

    public void store(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }

    @Nullable
    public String retrieve(String key) {
        return preferences.getString(key, null);
    }

    @Override
    public String name() {
        return Names.PREFERENCES;
    }
}
