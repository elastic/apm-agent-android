package co.elastic.apm.android.sdk.data.providers;

public interface Provider<T> {
    T get();
}
