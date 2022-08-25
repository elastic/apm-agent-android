package co.elastic.apm.android.sdk.services;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import co.elastic.apm.android.sdk.providers.LazyProvider;

public final class ServiceManager implements Lifecycle {
    private final HashMap<String, Service> services = new HashMap<>();
    private final Map<String, WeakReference<LazyProvider<Service>>> serviceLazyProviders = Collections.synchronizedMap(new HashMap<>());

    public void addService(Service service) {
        String name = service.name();
        verifyNotExisting(name);
        services.put(name, service);
    }

    @Override
    public void start() {
        for (Service service : services.values()) {
            service.start();
        }
    }

    @Override
    public void stop() {
        for (Service service : services.values()) {
            service.stop();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Service> T getService(String name) {
        Service service = services.get(name);
        if (service == null) {
            throw new IllegalArgumentException("Service not found: " + name);
        }

        return (T) service;
    }

    @SuppressWarnings("unchecked")
    public <T extends Service> LazyProvider<T> getServiceProvider(String name) {
        LazyProvider<T> provider = findExistingProvider(name);

        if (provider == null) {
            provider = LazyProvider.of(() -> getService(name));
            serviceLazyProviders.put(name, new WeakReference<>((LazyProvider<Service>) provider));
        }

        return provider;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private <T extends Service> LazyProvider<T> findExistingProvider(String name) {
        LazyProvider<T> provider = null;

        WeakReference<LazyProvider<Service>> providerRef = serviceLazyProviders.get(name);
        if (providerRef != null) {
            provider = (LazyProvider<T>) providerRef.get();
        }

        return provider;
    }

    private void verifyNotExisting(String name) {
        if (services.containsKey(name)) {
            throw new IllegalArgumentException("Service already registered with name: " + name);
        }
    }
}
