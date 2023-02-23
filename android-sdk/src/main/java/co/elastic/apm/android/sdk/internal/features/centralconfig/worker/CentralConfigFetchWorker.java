package co.elastic.apm.android.sdk.internal.features.centralconfig.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.slf4j.Logger;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.internal.features.centralconfig.CentralConfigurationManager;

public class CentralConfigFetchWorker extends Worker {

    public CentralConfigFetchWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Logger logger = Elog.getLogger(CentralConfigFetchWorker.class);
        logger.info("Starting central config worker");
        try {
            CentralConfigurationManager configurationManager = new CentralConfigurationManager(getApplicationContext());
            Integer maxAgeInSeconds = configurationManager.sync();
            if (maxAgeInSeconds != null) {
                CentralConfigurationManager.scheduleSync(getApplicationContext(), maxAgeInSeconds);
            }
            logger.info("Central config worker succeeded");
            return Result.success();
        } catch (Throwable t) {
            logger.error("Central config worker failed", t);
            return Result.retry();
        }
    }
}
