package co.elastic.apm.android.sdk.traces.common.attributes;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

import co.elastic.apm.android.sdk.BuildConfig;
import co.elastic.apm.android.sdk.attributes.AttributesBuilderVisitor;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

public class DeviceIdVisitor implements AttributesBuilderVisitor {

    private static final String DEVICE_ID_KEY = "device_id";
    private final Context appContext;

    public DeviceIdVisitor(Context appContext) {
        this.appContext = appContext;
    }

    @Override
    public void visit(AttributesBuilder builder) {
        builder.put(ResourceAttributes.DEVICE_ID, getId());
    }

    private String getId() {
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
