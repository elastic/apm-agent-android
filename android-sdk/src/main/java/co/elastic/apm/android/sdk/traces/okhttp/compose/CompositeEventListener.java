package co.elastic.apm.android.sdk.traces.okhttp.compose;

import androidx.annotation.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import okhttp3.EventListener;

public class CompositeEventListener extends EventListener {
    private final List<EventListener> listeners;

    public CompositeEventListener(List<EventListener> listeners) {
        this.listeners = listeners;
    }

    public void doCall(@NonNull Method method, @NonNull Object[] params) {
        for (EventListener listener : listeners) {
            try {
                method.invoke(listener, params);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
