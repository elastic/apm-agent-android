package co.elastic.apm.android.sdk.internal.features.centralconfig.poll;

import java.util.concurrent.ThreadFactory;

public final class PollThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    }
}
