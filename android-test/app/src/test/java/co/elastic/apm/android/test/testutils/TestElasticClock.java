package co.elastic.apm.android.test.testutils;

import java.util.concurrent.TimeUnit;

import io.opentelemetry.sdk.common.Clock;

public class TestElasticClock implements Clock {
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
}
