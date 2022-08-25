package co.elastic.apm.android.sdk.services;

public interface Service extends Lifecycle {
    String name();

    class Names {
        public static String NETWORK = "network";
    }
}
