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
package co.elastic.apm.android.sdk;

import android.app.Application;
import android.content.Context;

import androidx.annotation.RestrictTo;

import co.elastic.apm.android.common.internal.logging.Elog;
import co.elastic.apm.android.sdk.attributes.AttributesCreator;
import co.elastic.apm.android.sdk.attributes.AttributesVisitor;
import co.elastic.apm.android.sdk.attributes.common.CarrierHttpAttributesVisitor;
import co.elastic.apm.android.sdk.attributes.common.ConnectionHttpAttributesVisitor;
import co.elastic.apm.android.sdk.attributes.common.SessionAttributesVisitor;
import co.elastic.apm.android.sdk.attributes.impl.ComposeAttributesVisitor;
import co.elastic.apm.android.sdk.attributes.resources.DeviceIdVisitor;
import co.elastic.apm.android.sdk.attributes.resources.DeviceInfoVisitor;
import co.elastic.apm.android.sdk.attributes.resources.OsDescriptorVisitor;
import co.elastic.apm.android.sdk.attributes.resources.RuntimeDescriptorVisitor;
import co.elastic.apm.android.sdk.attributes.resources.SdkIdVisitor;
import co.elastic.apm.android.sdk.attributes.resources.ServiceIdVisitor;
import co.elastic.apm.android.sdk.connectivity.Connectivity;
import co.elastic.apm.android.sdk.instrumentation.Instrumentations;
import co.elastic.apm.android.sdk.internal.api.Initializable;
import co.elastic.apm.android.sdk.internal.configuration.Configurations;
import co.elastic.apm.android.sdk.internal.exceptions.ElasticExceptionHandler;
import co.elastic.apm.android.sdk.internal.features.launchtime.LaunchTimeActivityCallback;
import co.elastic.apm.android.sdk.internal.injection.AgentDependenciesInjector;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.ServiceManager;
import co.elastic.apm.android.sdk.internal.services.appinfo.AppInfoService;
import co.elastic.apm.android.sdk.internal.services.metadata.ApmMetadataService;
import co.elastic.apm.android.sdk.internal.services.network.NetworkService;
import co.elastic.apm.android.sdk.internal.services.preferences.PreferencesService;
import co.elastic.apm.android.sdk.internal.time.ntp.NtpManager;
import co.elastic.apm.android.sdk.internal.utilities.logging.AndroidLoggerFactory;
import co.elastic.apm.android.sdk.internal.utilities.otel.Flusher;
import co.elastic.apm.android.sdk.internal.utilities.providers.LazyProvider;
import co.elastic.apm.android.sdk.internal.utilities.providers.Provider;
import co.elastic.apm.android.sdk.internal.utilities.providers.SimpleProvider;
import co.elastic.apm.android.sdk.logs.ElasticLogRecordProcessor;
import co.elastic.apm.android.sdk.traces.otel.processor.ElasticSpanProcessor;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.GlobalLoggerProvider;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;

public final class ElasticApmAgent {

    public final ElasticApmConfiguration configuration;
    private static ElasticApmAgent instance;
    private final Provider<Connectivity> connectivityProvider;
    private final ServiceManager serviceManager;
    private final NtpManager ntpManager;
    private final Flusher flusher;

    public static ElasticApmAgent get() {
        verifyInitialization();
        return instance;
    }

    public static ElasticApmAgent initialize(Context context) {
        return initialize(context, null, null);
    }

    public static ElasticApmAgent initialize(Context context, ElasticApmConfiguration configuration) {
        return initialize(context, configuration, null);
    }

    public static ElasticApmAgent initialize(Context context, Connectivity connectivity) {
        return initialize(context, null, connectivity);
    }

    public synchronized static ElasticApmAgent initialize(Context context, ElasticApmConfiguration configuration, Connectivity connectivity) {
        if (instance != null) {
            throw new IllegalStateException("Already initialized");
        }
        Elog.init(new AndroidLoggerFactory());
        Provider<Connectivity> connectivityProvider;
        if (connectivity != null) {
            connectivityProvider = new SimpleProvider<>(connectivity);
        } else {
            connectivityProvider = Connectivity.getDefault();
        }
        ElasticApmConfiguration apmConfiguration;
        if (configuration != null) {
            apmConfiguration = configuration;
        } else {
            apmConfiguration = ElasticApmConfiguration.getDefault();
        }
        instance = new ElasticApmAgent(context, connectivityProvider, apmConfiguration);
        instance.onInitializationFinished(context);
        return instance;
    }

    public synchronized static boolean isInitialized() {
        return instance != null;
    }

    private static void verifyInitialization() {
        if (instance == null) {
            throw new IllegalStateException("ElasticApmAgent hasn't been initialized");
        }
    }

    public static void resetForTest() {
        ElasticExceptionHandler.resetForTest();
        Configurations.resetForTest();
        if (instance != null) {
            instance.serviceManager.stop();
            instance = null;
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
    public <T extends Service> T getService(String name) {
        return serviceManager.getService(name);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
    public static <T extends Service> Provider<T> getServiceProvider(String name) {
        return LazyProvider.of(() -> get().getService(name));
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
    public Flusher getFlusher() {
        return flusher;
    }

    private ElasticApmAgent(Context context, Provider<Connectivity> connectivityProvider, ElasticApmConfiguration configuration) {
        Context appContext = context.getApplicationContext();
        AgentDependenciesInjector injector = AgentDependenciesInjector.get(context);
        this.connectivityProvider = connectivityProvider;
        this.configuration = configuration;
        flusher = new Flusher();
        ntpManager = injector.getNtpManager();
        serviceManager = new ServiceManager();
        serviceManager.addService(new NetworkService(appContext));
        serviceManager.addService(new AppInfoService(appContext));
        serviceManager.addService(new ApmMetadataService(appContext));
        serviceManager.addService(new PreferencesService(appContext));
    }

    private void onInitializationFinished(Context context) {
        ntpManager.initialize();
        serviceManager.start();
        initializeDynamicConfiguration();
        initializeOpentelemetry();
        initializeCrashReports();
        initializeSessionIdProvider();
        initializeLaunchTimeTracker(context);
    }

    private void initializeDynamicConfiguration() {
        Configurations.Builder builder = Configurations.builder();
        builder.register(configuration.instrumentationConfiguration);
        configuration.instrumentationConfiguration.instrumentations.forEach(builder::register);
        builder.buildAndRegisterGlobal();
    }

    private void initializeSessionIdProvider() {
        if (configuration.sessionIdProvider instanceof Initializable) {
            ((Initializable) configuration.sessionIdProvider).initialize();
        }
    }

    private void initializeLaunchTimeTracker(Context context) {
        ((Application) context).registerActivityLifecycleCallbacks(new LaunchTimeActivityCallback());
    }

    private void initializeCrashReports() {
        if (Instrumentations.isCrashReportingEnabled()) {
            Thread.setDefaultUncaughtExceptionHandler(ElasticExceptionHandler.getInstance());
        }
    }

    private void initializeOpentelemetry() {
        Attributes resourceAttrs = AttributesCreator.from(getResourceAttributesVisitor()).create();
        AttributesVisitor globalAttributesVisitor = new SessionAttributesVisitor();
        Resource resource = Resource.getDefault()
                .merge(Resource.create(resourceAttrs));

        SdkMeterProvider meterProvider = getMeterProvider(resource);
        SdkLoggerProvider loggerProvider = getLoggerProvider(resource, globalAttributesVisitor);

        flusher.setMeterDelegator(meterProvider::forceFlush);
        flusher.setLoggerDelegator(loggerProvider::forceFlush);

        OpenTelemetrySdk.builder()
                .setTracerProvider(getTracerProvider(resource, globalAttributesVisitor))
                .setLoggerProvider(loggerProvider)
                .setMeterProvider(meterProvider)
                .setPropagators(getContextPropagator())
                .buildAndRegisterGlobal();
    }

    private AttributesVisitor getResourceAttributesVisitor() {
        return AttributesVisitor.compose(
                new DeviceIdVisitor(),
                new DeviceInfoVisitor(),
                new OsDescriptorVisitor(),
                new RuntimeDescriptorVisitor(),
                new SdkIdVisitor(),
                new ServiceIdVisitor()
        );
    }

    private SdkTracerProvider getTracerProvider(Resource resource, AttributesVisitor commonAttrVisitor) {
        SpanProcessor spanProcessor = connectivityProvider.get().getSpanProcessor();
        ComposeAttributesVisitor spanAttributesVisitor = AttributesVisitor.compose(
                commonAttrVisitor,
                new CarrierHttpAttributesVisitor(),
                new ConnectionHttpAttributesVisitor()
        );
        ElasticSpanProcessor processor = new ElasticSpanProcessor(spanProcessor, spanAttributesVisitor);
        processor.addAllExclusionRules(configuration.httpTraceConfiguration.exclusionRules);

        return SdkTracerProvider.builder()
                .setClock(ntpManager.getClock())
                .addSpanProcessor(processor)
                .setResource(resource)
                .build();
    }

    private SdkLoggerProvider getLoggerProvider(Resource resource, AttributesVisitor commonAttrVisitor) {
        LogRecordProcessor logProcessor = connectivityProvider.get().getLogProcessor();
        ElasticLogRecordProcessor elasticProcessor = new ElasticLogRecordProcessor(logProcessor, commonAttrVisitor);
        SdkLoggerProvider loggerProvider = SdkLoggerProvider.builder()
                .setResource(resource)
                .setClock(ntpManager.getClock())
                .addLogRecordProcessor(elasticProcessor)
                .build();
        GlobalLoggerProvider.set(loggerProvider);
        return loggerProvider;
    }

    private SdkMeterProvider getMeterProvider(Resource resource) {
        return SdkMeterProvider.builder()
                .setClock(ntpManager.getClock())
                .registerMetricReader(connectivityProvider.get().getMetricReader())
                .setResource(resource)
                .build();
    }

    private ContextPropagators getContextPropagator() {
        return ContextPropagators.create(W3CTraceContextPropagator.getInstance());
    }
}