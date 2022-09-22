package co.elastic.apm.android.common.okhttp.eventlistener;

import androidx.annotation.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import co.elastic.apm.android.common.MethodCaller;
import okhttp3.EventListener;

public class CompositeEventListener extends EventListener implements MethodCaller {
    private final List<EventListener> listeners;

    public CompositeEventListener(List<EventListener> listeners) {
        this.listeners = listeners;
    }

    @Override
    public void doCall(@NonNull Method method, @NonNull Object[] params) {
        for (EventListener listener : listeners) {
            try {
                method.invoke(listener, params);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static String getGeneratedName() {
        return CompositeEventListener.class.getPackage().getName() + ".Generated_" + CompositeEventListener.class.getSimpleName();
    }
}