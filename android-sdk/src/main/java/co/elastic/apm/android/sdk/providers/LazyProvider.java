package co.elastic.apm.android.sdk.providers;

public class LazyProvider<T> {
    private final Provider<T> provider;
    private T object;

    public LazyProvider(Provider<T> provider) {
        this.provider = provider;
    }

    public T get() {
        if (object == null) {
            object = provider.get();
        }

        return object;
    }

    public static <T> LazyProvider<T> of(Provider<T> provider) {
        return new LazyProvider<>(provider);
    }
}
