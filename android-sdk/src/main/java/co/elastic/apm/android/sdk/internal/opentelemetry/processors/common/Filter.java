package co.elastic.apm.android.sdk.internal.opentelemetry.processors.common;

public interface Filter<T> {

    boolean shouldInclude(T item);
}
