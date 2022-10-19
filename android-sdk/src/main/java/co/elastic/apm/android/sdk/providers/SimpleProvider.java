package co.elastic.apm.android.sdk.providers;

public class SimpleProvider<T> implements Provider<T> {
    private final T object;

    public SimpleProvider(T object) {
        this.object = object;
    }

    @Override
    public T get() {
        return object;
    }
}
