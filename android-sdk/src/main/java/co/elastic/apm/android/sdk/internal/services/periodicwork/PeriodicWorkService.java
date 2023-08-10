package co.elastic.apm.android.sdk.internal.services.periodicwork;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.utilities.concurrency.DaemonThreadFactory;

public class PeriodicWorkService implements Service, Runnable {
    private final Set<PeriodicTask> tasks = new HashSet<>();
    private final AtomicBoolean isStopped = new AtomicBoolean(false);
    private ScheduledExecutorService executorService;
    private static final long DELAY_BETWEEN_WORK_RUNS_IN_MILLIS = 1000;

    public void addTask(PeriodicTask task) {
        tasks.add(task);
    }

    @Override
    public void start() {
        if (isStopped.get()) {
            throw new IllegalStateException("The periodic work service has been stopped");
        }
        executorService = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());
        scheduleNextWorkRun();
    }

    @Override
    public void stop() {
        isStopped.set(true);
    }

    @Override
    public String name() {
        return Names.PERIODIC_WORK;
    }

    @Override
    public void run() {
        for (PeriodicTask task : tasks) {
            try {
                task.execute();
            } catch (Throwable t) {
                Elog.getLogger().error("Failed to execute periodic task", t);
            }
        }
        if (!isStopped.get()) {
            scheduleNextWorkRun();
        }
    }

    private void scheduleNextWorkRun() {
        Elog.getLogger().debug("Scheduling next work for PeriodicWorkService");
        executorService.schedule(this, DELAY_BETWEEN_WORK_RUNS_IN_MILLIS, TimeUnit.MILLISECONDS);
    }
}
