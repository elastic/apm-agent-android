package co.elastic.apm.android.sdk.internal.features.centralconfig.initializer;

import android.content.Context;

import co.elastic.apm.android.sdk.internal.features.centralconfig.CentralConfigurationManager;
import co.elastic.apm.android.sdk.internal.utilities.concurrency.BackgroundExecutor;
import co.elastic.apm.android.sdk.internal.utilities.concurrency.Result;
import co.elastic.apm.android.sdk.internal.utilities.concurrency.impl.SimpleBackgroundExecutor;

public class CentralConfigInitializer implements BackgroundExecutor.Callback<Integer> {
    private final Context context;
    private final BackgroundExecutor executor;
    private final CentralConfigurationManager manager;

    public CentralConfigInitializer(Context context, BackgroundExecutor executor, CentralConfigurationManager manager) {
        this.context = context;
        this.executor = executor;
        this.manager = manager;
    }

    public CentralConfigInitializer(Context context) {
        this(context, new SimpleBackgroundExecutor(), new CentralConfigurationManager(context));
    }

    public void initialize() {
        executor.execute(manager::sync, this);
    }

    @Override
    public void onFinish(Result<Integer> result) {
        if (result.isSuccess) {
            Integer maxAgeInSeconds = result.value;
            if (maxAgeInSeconds != null) {
                CentralConfigurationManager.scheduleSync(context, maxAgeInSeconds);
            } else {
                CentralConfigurationManager.scheduleInitialSync(context);
            }
        }
    }
}
