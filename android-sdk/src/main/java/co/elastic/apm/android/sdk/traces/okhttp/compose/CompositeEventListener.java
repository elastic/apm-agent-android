package co.elastic.apm.android.sdk.traces.okhttp.compose;

import java.util.List;

import okhttp3.EventListener;

public class CompositeEventListener extends EventListener {
    private final List<EventListener> listeners;

    public CompositeEventListener(List<EventListener> listeners) {
        this.listeners = listeners;
    }
}
