package co.elastic.apm.android.sdk.data.providers;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

import co.elastic.apm.android.sdk.BuildConfig;

public class DeviceIdProvider implements Provider<String> {

    private static final String DEVICE_ID_KEY = "device_id";
    private final Context appContext;

    public DeviceIdProvider(Context appContext) {
        this.appContext = appContext;
    }

    @Override
    public String get() {
        SharedPreferences sharedPreferences = getSharedPreferences(appContext);
        String deviceId = sharedPreferences.getString(DEVICE_ID_KEY, null);

        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            storeDeviceId(sharedPreferences, deviceId);
        }

        return deviceId;
    }

    private void storeDeviceId(SharedPreferences sharedPreferences, String deviceId) {
        sharedPreferences.edit().putString(DEVICE_ID_KEY, deviceId).apply();
    }

    private SharedPreferences getSharedPreferences(Context appContext) {
        return appContext.getSharedPreferences(BuildConfig.LIBRARY_PACKAGE_NAME + ".prefs", Context.MODE_PRIVATE);
    }
}