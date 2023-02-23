package co.elastic.apm.android.sdk.internal.features.centralconfig;

import androidx.annotation.WorkerThread;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.internal.features.centralconfig.worker.CentralConfigFetchWorker;

final class WorkScheduler {
    private static final int DEFAULT_POLL_DELAY_SEC = (int) TimeUnit.MINUTES.toSeconds(5);
    private static final String UNIQUE_PERIODIC_WORK_NAME = "central_config_periodic_work";

    @WorkerThread
    static void scheduleInitialSync(WorkManager workManager) {
        if (currentScheduledWorkFound(workManager)) {
            Elog.getLogger().debug("Current central config work found, so skipping initial schedule");
            return;
        }

        scheduleSync(workManager, DEFAULT_POLL_DELAY_SEC);
    }

    @WorkerThread
    synchronized static void scheduleSync(WorkManager workManager, int timeIntervalInSeconds) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.METERED)
                .setRequiresCharging(true)
                .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(CentralConfigFetchWorker.class,
                timeIntervalInSeconds, TimeUnit.SECONDS)
                .setInputData(CentralConfigFetchWorker.createInputData(timeIntervalInSeconds))
                .setInitialDelay(timeIntervalInSeconds, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .build();

        workManager.enqueueUniquePeriodicWork(UNIQUE_PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest);

        Elog.getLogger().debug("Enqueued central config worker with time interval in seconds: {}", timeIntervalInSeconds);
    }

    private static boolean currentScheduledWorkFound(WorkManager workManager) {
        try {
            List<WorkInfo> infos = workManager.getWorkInfosForUniqueWork(UNIQUE_PERIODIC_WORK_NAME).get();
            if (infos != null && !infos.isEmpty()) {
                return !infos.get(0).getState().isFinished();
            }
        } catch (ExecutionException | InterruptedException e) {
            Elog.getLogger().error("Error trying to retrieve current periodic work", e);
        }
        return false;
    }
}
