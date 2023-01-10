package co.elastic.apm.android.test.attributes.common;

import android.os.Build;

import org.robolectric.util.ReflectionHelpers;

import co.elastic.apm.android.test.testutils.MainApp;

public class ResourcesApp extends MainApp {

    public static final String RUNTIME_VERSION = "0.0.0";
    public static final String DEVICE_MODEL_NAME = "Universe E10";
    public static final String DEVICE_MANUFACTURER = "Droidlastic";

    @Override
    public void onCreate() {
        ReflectionHelpers.setStaticField(Build.class, "MODEL", DEVICE_MODEL_NAME);
        ReflectionHelpers.setStaticField(Build.class, "MANUFACTURER", DEVICE_MANUFACTURER);
        System.setProperty("java.vm.version", RUNTIME_VERSION);
        super.onCreate();
    }
}
