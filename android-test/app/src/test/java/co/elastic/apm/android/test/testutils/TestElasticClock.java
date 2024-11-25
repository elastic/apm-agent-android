package co.elastic.apm.android.test.testutils;

import java.util.concurrent.TimeUnit;

import co.elastic.apm.android.sdk.internal.services.periodicwork.PeriodicTask;
import io.opentelemetry.sdk.common.Clock;

public class TestElasticClock implements Clock, PeriodicTask {
    private Long forcedNow = null;

    public void setForcedNow(Long forcedNow) {
        this.forcedNow = forcedNow;
    }

    @Override
    public long now() {
        if (forcedNow != null) {
            return forcedNow;
        }
        return TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    }

    @Override
    public long nanoTime() {
        return System.nanoTime();
    }

    @Override
    public boolean shouldRunTask() {
        return false;
    }

    @Override
    public void runTask() {

    }

    @Override
    public boolean isTaskFinished() {
        return false;
    }
}
