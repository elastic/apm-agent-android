package co.elastic.apm.android.sdk.internal.features.launchtime;

import co.elastic.apm.android.common.internal.logging.Elog;

public final class LaunchTimeTracker {
    private static long initialTimeInNanos = 0;
    private static long launchTimeInNanos = 0;
    private static boolean finalizedTracking = false;
    private static boolean timeAlreadyQueried = false;

    static void startTimer() {
        Elog.getLogger().info("Initializing app launch time tracker");
        initialTimeInNanos = System.nanoTime();
    }

    public static boolean stopTimer() {
        if (finalizedTracking) {
            return false;
        }

        launchTimeInNanos = System.nanoTime() - initialTimeInNanos;
        initialTimeInNanos = 0;
        finalizedTracking = true;

        return true;
    }

    public static long getElapsedTimeInNanos() {
        if (!finalizedTracking) {
            throw new IllegalStateException("No tracked time available");
        }
        if (timeAlreadyQueried) {
            throw new IllegalStateException("Launch time already queried");
        }

        long time = launchTimeInNanos;
        launchTimeInNanos = 0;
        timeAlreadyQueried = true;

        return time;
    }
}
