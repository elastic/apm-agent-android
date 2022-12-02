package co.elastic.apm.android.sdk.internal.concurrency;

public interface BackgroundExecutor<T> {

    void execute(BackgroundWork<T> work, Callback<T> callback);

    interface Callback<T> {
        void onFinish(Result<T> result);
    }
}
