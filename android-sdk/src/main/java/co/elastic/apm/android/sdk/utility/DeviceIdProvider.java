package co.elastic.apm.android.sdk.utility;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

import co.elastic.apm.android.sdk.BuildConfig;

public class DeviceIdProvider {

    private static final String DEVICE_ID_KEY = "device_id";

    public static String getDeviceId(Context appContext) {
        SharedPreferences sharedPreferences = getSharedPreferences(appContext);
        String deviceId = sharedPreferences.getString(DEVICE_ID_KEY, null);

        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            storeDeviceId(sharedPreferences, deviceId);
        }

        return deviceId;
    }

    private static void storeDeviceId(SharedPreferences sharedPreferences, String deviceId) {
        sharedPreferences.edit().putString(DEVICE_ID_KEY, deviceId).apply();
    }

    private static SharedPreferences getSharedPreferences(Context appContext) {
        return appContext.getSharedPreferences(BuildConfig.LIBRARY_PACKAGE_NAME + ".prefs", Context.MODE_PRIVATE);
    }
}
