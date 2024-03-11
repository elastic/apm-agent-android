package co.elastic.apm.android.test.attributes.common;

import android.os.Build;

import org.robolectric.util.ReflectionHelpers;

import co.elastic.apm.android.sdk.ElasticApmConfiguration;
import co.elastic.apm.android.test.testutils.MainApp;
import co.elastic.apm.android.test.testutils.base.BaseRobolectricTestApplication;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;

public class ResourcesApp extends BaseRobolectricTestApplication {

    public static final String RUNTIME_VERSION = "0.0.0";
    public static final String DEVICE_MODEL_NAME = "Universe E10";
    public static final String DEVICE_MANUFACTURER = "Droidlastic";
    public static final String RESOURCE_KEY = "global.key";
    public static final String RESOURCE_VALUE = "global.value";

    @Override
    public void onCreate() {
        super.onCreate();
        ReflectionHelpers.setStaticField(Build.class, "MODEL", DEVICE_MODEL_NAME);
        ReflectionHelpers.setStaticField(Build.class, "MANUFACTURER", DEVICE_MANUFACTURER);
        System.setProperty("java.vm.version", RUNTIME_VERSION);
        initializeAgentWithCustomConfig(ElasticApmConfiguration.builder()
                .setResource(Resource.create(Attributes.builder().put(RESOURCE_KEY, RESOURCE_VALUE).build()))
                .build());
    }
}
