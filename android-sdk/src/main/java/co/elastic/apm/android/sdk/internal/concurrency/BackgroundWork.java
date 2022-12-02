package co.elastic.apm.android.sdk.internal.concurrency;

public interface BackgroundWork<T> {
    T execute();
}
