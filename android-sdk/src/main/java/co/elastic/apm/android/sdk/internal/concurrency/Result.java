package co.elastic.apm.android.sdk.internal.concurrency;

public final class Result<T> {
    public final T value;
    public final Throwable error;

    private Result(T result, Throwable error) {
        this.value = result;
        this.error = error;
    }

    public static <T> Result<T> success(T value) {
        return new Result<>(value, null);
    }

    public static <T> Result<T> error(Throwable error) {
        return new Result<>(null, error);
    }

    public static <T> Result<T> error() {
        return new Result<>(null, null);
    }

    public boolean isSuccess() {
        return value != null;
    }
}
