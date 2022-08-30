package co.elastic.apm.android.sdk.services;

public interface Service extends Lifecycle {
    String name();

    class Names {
        public static final String ANDROID_PERMISSIONS = "android-permissions";
        public static final String NETWORK = "network";
        public static final String METADATA = "apm-metadata";
    }
}
