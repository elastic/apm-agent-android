package co.elastic.apm.android.sdk.utility;

public interface Provider<T> {
    T get();
}
