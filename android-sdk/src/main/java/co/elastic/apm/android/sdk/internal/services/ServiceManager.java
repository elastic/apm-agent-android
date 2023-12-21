/*
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package co.elastic.apm.android.sdk.internal.services;

import android.content.Context;

import androidx.annotation.RestrictTo;

import java.util.HashMap;

import co.elastic.apm.android.sdk.internal.services.appinfo.AppInfoService;
import co.elastic.apm.android.sdk.internal.services.network.NetworkService;
import co.elastic.apm.android.sdk.internal.services.periodicwork.PeriodicWorkService;
import co.elastic.apm.android.sdk.internal.services.preferences.PreferencesService;
import co.elastic.apm.android.sdk.internal.utilities.providers.LazyProvider;
import co.elastic.apm.android.sdk.internal.utilities.providers.Provider;

public final class ServiceManager implements Lifecycle {
    private final HashMap<String, Service> services = new HashMap<>();
    private static ServiceManager INSTANCE;
    private static InitializationCallback initializationCallback;

    private ServiceManager() {
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
    public static void initialize(Context appContext) {
        INSTANCE = new ServiceManager();
        INSTANCE.addService(NetworkService.create(appContext));
        INSTANCE.addService(new AppInfoService(appContext));
        INSTANCE.addService(new PreferencesService(appContext));
        INSTANCE.addService(new PeriodicWorkService());
        notifyInitializationFinished();
        cleanUp();
    }

    private static void cleanUp() {
        initializationCallback = null;
    }

    private static void notifyInitializationFinished() {
        if (initializationCallback != null) {
            initializationCallback.onInitializationFinished();
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
    public static ServiceManager get() {
        if (INSTANCE == null) {
            throw new IllegalStateException("Services haven't been initialized");
        }
        return INSTANCE;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
    public static void setInitializationCallback(InitializationCallback initializationCallback) {
        ServiceManager.initializationCallback = initializationCallback;
    }

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

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
    @SuppressWarnings("unchecked")
    public <T extends Service> T getService(String name) {
        Service service = services.get(name);
        if (service == null) {
            throw new IllegalArgumentException("Service not found: " + name);
        }

        return (T) service;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
    public static <T extends Service> Provider<T> getServiceProvider(String name) {
        return LazyProvider.of(() -> get().getService(name));
    }

    public static void resetForTest() {
        if (INSTANCE != null) {
            INSTANCE.stop();
        }
        INSTANCE = null;
    }

    private void verifyNotExisting(String name) {
        if (services.containsKey(name)) {
            throw new IllegalArgumentException("Service already registered with name: " + name);
        }
    }

    public interface InitializationCallback {
        void onInitializationFinished();
    }
}
