package co.elastic.apm.android.sdk.internal.concurrency.impl;

import co.elastic.apm.android.sdk.internal.concurrency.BackgroundExecutor;
import co.elastic.apm.android.sdk.internal.concurrency.BackgroundWork;
import co.elastic.apm.android.sdk.internal.concurrency.Result;

public class SimpleBackgroundExecutor<T> implements BackgroundExecutor<T> {

    @Override
    public void execute(BackgroundWork<T> work, Callback<T> callback) {
        new Thread(() -> {
            try {
                T result = work.execute();
                if (result != null) {
                    callback.onFinish(Result.success(result));
                } else {
                    callback.onFinish(Result.error());
                }
            } catch (Throwable t) {
                callback.onFinish(Result.error(t));
            }
        }).start();
    }
}
