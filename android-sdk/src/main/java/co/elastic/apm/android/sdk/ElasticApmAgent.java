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
import androidx.lifecycle.ProcessLifecycleOwner;

import java.io.IOException;

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
import co.elastic.apm.android.sdk.connectivity.opentelemetry.SignalConfiguration;
import co.elastic.apm.android.sdk.connectivity.opentelemetry.exporters.VisitableExporters;
import co.elastic.apm.android.sdk.features.persistence.SignalDiskExporter;
import co.elastic.apm.android.sdk.instrumentation.Instrumentations;
import co.elastic.apm.android.sdk.internal.api.filter.ComposableFilter;
import co.elastic.apm.android.sdk.internal.configuration.Configurations;
import co.elastic.apm.android.sdk.internal.exceptions.ElasticExceptionHandler;
import co.elastic.apm.android.sdk.internal.features.centralconfig.initializer.CentralConfigurationInitializer;
import co.elastic.apm.android.sdk.internal.features.centralconfig.poll.ConfigurationPollManager;
import co.elastic.apm.android.sdk.internal.features.launchtime.LaunchTimeApplicationListener;
import co.elastic.apm.android.sdk.internal.features.lifecycle.ElasticProcessLifecycleObserver;
import co.elastic.apm.android.sdk.internal.features.persistence.PersistenceInitializer;
import co.elastic.apm.android.sdk.internal.features.sampling.SampleRateManager;
import co.elastic.apm.android.sdk.internal.injection.AgentDependenciesInjector;
import co.elastic.apm.android.sdk.internal.injection.DefaultAgentDependenciesInjector;
import co.elastic.apm.android.sdk.internal.opentelemetry.processors.logs.ElasticLogRecordProcessor;
import co.elastic.apm.android.sdk.internal.opentelemetry.processors.metrics.ElasticMetricReader;
import co.elastic.apm.android.sdk.internal.opentelemetry.processors.spans.ElasticSpanProcessor;
import co.elastic.apm.android.sdk.internal.opentelemetry.tools.Flusher;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.ServiceManager;
import co.elastic.apm.android.sdk.internal.services.periodicwork.PeriodicWorkService;
import co.elastic.apm.android.sdk.internal.time.ntp.NtpManager;
import co.elastic.apm.android.sdk.internal.utilities.logging.AndroidLoggerFactory;
import co.elastic.apm.android.sdk.session.SessionManager;
import io.opentelemetry.android.OpenTelemetryRum;
import io.opentelemetry.android.config.OtelRumConfig;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.events.GlobalEventLoggerProvider;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.internal.SdkEventLoggerProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.SpanProcessor;

public final class ElasticApmAgent {
    public final ElasticApmConfiguration configuration;
    private static ElasticApmAgent instance;
    private final Flusher flusher;
    private NtpManager ntpManager;

    public static ElasticApmAgent get() {
        verifyInitialization();
        return instance;
    }

    /**
     * Initializes the Elastic Agent.
     *
     * @param context The Application context.
     * @deprecated Use {@link ElasticApmAgent#initialize(Application)} instead. This method will be
     * removed in the next major version release.
     */
    @Deprecated
    public static ElasticApmAgent initialize(Context context) {
        return initialize(context, null, null);
    }

    /**
     * Initializes the Elastic Agent.
     *
     * @param context       The Application context.
     * @param configuration The Elastic configuration.
     * @deprecated Use {@link ElasticApmAgent#initialize(Application, ElasticApmConfiguration)} instead. This method will be
     * removed in the next major version release.
     */
    @Deprecated
    public static ElasticApmAgent initialize(Context context, ElasticApmConfiguration configuration) {
        return initialize(context, configuration, null);
    }

    /**
     * Initializes the Elastic Agent.
     *
     * @param context      The Application context.
     * @param connectivity The APM server connectivity config.
     * @deprecated Use {@link ElasticApmAgent#initialize(Application, Connectivity)} instead. This method will be
     * removed in the next major version release.
     */
    @Deprecated
    public static ElasticApmAgent initialize(Context context, Connectivity connectivity) {
        return initialize(context, null, connectivity);
    }

    /**
     * Initializes the Elastic Agent.
     *
     * @param context       The Application context.
     * @param configuration The Elastic configuration.
     * @param connectivity  The APM server connectivity config.
     * @deprecated Use {@link ElasticApmAgent#initialize(Application, ElasticApmConfiguration, Connectivity)} instead. This method will be
     * removed in the next major version release.
     */
    @Deprecated
    public static ElasticApmAgent initialize(Context context, ElasticApmConfiguration configuration, Connectivity connectivity) {
        return initialize((Application) context, configuration, connectivity, null);
    }

    public static ElasticApmAgent initialize(Application application) {
        return initialize(application, null, null);
    }

    public static ElasticApmAgent initialize(Application application, ElasticApmConfiguration configuration) {
        return initialize(application, configuration, null);
    }

    public static ElasticApmAgent initialize(Application application, Connectivity connectivity) {
        return initialize(application, null, connectivity);
    }

    public static ElasticApmAgent initialize(Application application, ElasticApmConfiguration configuration, Connectivity connectivity) {
        return initialize(application, configuration, connectivity, null);
    }

    private synchronized static ElasticApmAgent initialize(Application application, ElasticApmConfiguration configuration, Connectivity connectivity, AgentDependenciesInjector.Interceptor interceptor) {
        if (instance != null) {
            throw new IllegalStateException("Already initialized");
        }
        ElasticApmConfiguration finalConfiguration = (configuration == null) ? ElasticApmConfiguration.getDefault() : configuration;
        Elog.init(new AndroidLoggerFactory(finalConfiguration.libraryLoggingPolicy));
        ServiceManager.initialize(application);
        ServiceManager.get().start();
        Connectivity finalConnectivity = (connectivity == null) ? Connectivity.getDefault() : connectivity;
        AgentDependenciesInjector injector = process(new DefaultAgentDependenciesInjector(application, finalConfiguration, finalConnectivity), interceptor);
        instance = new ElasticApmAgent(finalConfiguration);
        instance.onInitializationFinished(application, injector);
        initializePeriodicWork();
        return instance;
    }

    private static AgentDependenciesInjector process(AgentDependenciesInjector injector, AgentDependenciesInjector.Interceptor interceptor) {
        if (interceptor != null) {
            return interceptor.intercept(injector);
        }
        return injector;
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
        ConfigurationPollManager.resetForTest();
        Configurations.resetForTest();
        SessionManager.resetForTest();
        ConnectionHttpAttributesVisitor.resetForTest();
        ServiceManager.resetForTest();
        GlobalOpenTelemetry.resetForTest();
        GlobalEventLoggerProvider.resetForTest();
        instance = null;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
    public Flusher getFlusher() {
        return flusher;
    }

    private ElasticApmAgent(ElasticApmConfiguration configuration) {
        this.configuration = configuration;
        flusher = new Flusher();
    }

    private void onInitializationFinished(Application application, AgentDependenciesInjector injector) {
        initializeNtpManager(injector);
        initializeSessionManager(injector);
        initializeConfigurations(injector);
        initializeOpentelemetry(application, injector);
        initializeCrashReports();
        initializeLifecycleObserver();
    }

    private static void initializePeriodicWork() {
        getPeriodicWorkService().initialize();
    }

    private void initializeLifecycleObserver() {
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new ElasticProcessLifecycleObserver());
    }

    private void initializeNtpManager(AgentDependenciesInjector injector) {
        ntpManager = injector.getNtpManager();
        ntpManager.initialize();
        getPeriodicWorkService().addTask(ntpManager);
    }

    private void initializeConfigurations(AgentDependenciesInjector injector) {
        CentralConfigurationInitializer centralConfigInitializer = injector.getCentralConfigurationInitializer();
        ConfigurationPollManager.set(centralConfigInitializer.getPollManager());
        Configurations.Builder builder = Configurations.builder();
        builder.addSource(centralConfigInitializer.getManager());

        builder.registerAll(injector.getConfigurationsProvider().provideConfigurations());
        configuration.instrumentationConfiguration.instrumentations.forEach(builder::register);
        builder.buildAndRegisterGlobal();

        getPeriodicWorkService().addTask(centralConfigInitializer);
    }

    private void initializeSessionManager(AgentDependenciesInjector injector) {
        SessionManager sessionManager = injector.getSessionManager();
        sessionManager.initialize();
        SessionManager.set(sessionManager);
    }

    private void initializeCrashReports() {
        if (Instrumentations.isCrashReportingEnabled()) {
            Thread.setDefaultUncaughtExceptionHandler(ElasticExceptionHandler.getInstance());
        }
    }

    private void initializeOpentelemetry(Application app, AgentDependenciesInjector injector) {
        SignalConfiguration signalConfiguration;
        if (configuration.signalConfiguration == null) {
            signalConfiguration = SignalConfiguration.getDefault();
        } else {
            signalConfiguration = configuration.signalConfiguration;
        }
        SampleRateManager sampleRateManager = new SampleRateManager();
        PersistenceInitializer persistenceInitializer = tryInitializePersistence(signalConfiguration, injector);
        Attributes resourceAttrs = AttributesCreator.from(getResourceAttributesVisitor()).create();
        AttributesVisitor globalAttributesVisitor = new SessionAttributesVisitor();
        Resource resource = configuration.resource
                .merge(Resource.create(resourceAttrs));

        OtelRumConfig rumConfig = new OtelRumConfig();
        rumConfig.disableNetworkAttributes();
        rumConfig.disableNetworkChangeMonitoring();
        OpenTelemetryRum rum = OpenTelemetryRum.builder(app, rumConfig)
                .addTracerProviderCustomizer((sdkTracerProviderBuilder, application) -> configureTracerProviderBuilder(sdkTracerProviderBuilder, signalConfiguration, resource, globalAttributesVisitor, sampleRateManager))
                .addMeterProviderCustomizer((sdkMeterProviderBuilder, application) -> configureMeterProviderBuilder(sdkMeterProviderBuilder, signalConfiguration, resource, sampleRateManager))
                .addLoggerProviderCustomizer((sdkLoggerProviderBuilder, application) -> configureLoggerProviderBuilder(sdkLoggerProviderBuilder, signalConfiguration, resource, globalAttributesVisitor, sampleRateManager))
                .addInstrumentation(instrumentedApplication -> {
                    // Adding screen spans
                    new AndroidLifecycleInstrumentationBuilder()
                            .setVisibleScreenTracker(new VisibleScreenTracker())
                            .setStartupTimer(new AppStartupTimer())
                            .build().installOn(instrumentedApplication);

                    // Adding launch time metrics
                    instrumentedApplication.registerApplicationStateListener(new LaunchTimeApplicationListener());
                })
                .build();

        OpenTelemetry openTelemetry = rum.getOpenTelemetry();
        GlobalOpenTelemetry.set(openTelemetry);
        GlobalEventLoggerProvider.set(SdkEventLoggerProvider.create(openTelemetry.getLogsBridge(), ntpManager.getClock()));

        if (persistenceInitializer != null) {
            SignalDiskExporter.set(persistenceInitializer.createSignalDiskExporter());
            configuration.persistenceConfiguration.exportScheduler.onPersistenceEnabled();
        } else {
            configuration.persistenceConfiguration.exportScheduler.onPersistenceDisabled();
        }
    }

    private PersistenceInitializer tryInitializePersistence(SignalConfiguration signalConfiguration, AgentDependenciesInjector injector) {
        if (configuration.persistenceConfiguration.enabled && signalConfiguration instanceof VisitableExporters) {
            Elog.getLogger().debug("Initializing the persistence feature");
            try {
                PersistenceInitializer persistenceInitializer = injector.getPersistenceInitializer();
                persistenceInitializer.prepare();
                ((VisitableExporters) signalConfiguration).setExporterVisitor(persistenceInitializer);
                return persistenceInitializer;
            } catch (IOException e) {
                Elog.getLogger().error("Could not initialize the persistence feature", e);
            }
        }
        return null;
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

    private SdkTracerProviderBuilder configureTracerProviderBuilder(SdkTracerProviderBuilder builder,
                                                                    SignalConfiguration signalConfiguration,
                                                                    Resource resource,
                                                                    AttributesVisitor commonAttrVisitor, SampleRateManager sampleRateManager) {
        SpanProcessor spanProcessor = signalConfiguration.getSpanProcessor();
        ComposeAttributesVisitor spanAttributesVisitor = AttributesVisitor.compose(
                commonAttrVisitor,
                ConnectionHttpAttributesVisitor.getInstance(),
                new CarrierHttpAttributesVisitor()
        );
        ElasticSpanProcessor processor = new ElasticSpanProcessor(spanProcessor, spanAttributesVisitor);
        ComposableFilter<ReadableSpan> filter = new ComposableFilter<>();
        filter.addFilter(sampleRateManager.spanFilter);
        filter.addAllFilters(configuration.spanFilters);
        filter.addAllFilters(configuration.httpTraceConfiguration.httpFilters);
        processor.setFilter(filter);

        return builder
                .setClock(ntpManager.getClock())
                .addSpanProcessor(processor)
                .setResource(resource);
    }

    private SdkLoggerProviderBuilder configureLoggerProviderBuilder(SdkLoggerProviderBuilder builder,
                                                                    SignalConfiguration signalConfiguration,
                                                                    Resource resource,
                                                                    AttributesVisitor commonAttrVisitor,
                                                                    SampleRateManager sampleRateManager) {
        LogRecordProcessor logProcessor = signalConfiguration.getLogProcessor();
        ComposeAttributesVisitor logAttributes = AttributesVisitor.compose(
                commonAttrVisitor,
                ConnectionHttpAttributesVisitor.getInstance()
        );
        ElasticLogRecordProcessor elasticProcessor = new ElasticLogRecordProcessor(logProcessor, logAttributes);
        ComposableFilter<LogRecordData> logFilter = new ComposableFilter<>();
        logFilter.addFilter(sampleRateManager.logFilter);
        logFilter.addAllFilters(configuration.logFilters);
        elasticProcessor.setFilter(logFilter);

        flusher.setLoggerDelegator(elasticProcessor::forceFlush);

        return builder
                .setResource(resource)
                .setClock(ntpManager.getClock())
                .addLogRecordProcessor(elasticProcessor);
    }

    private SdkMeterProviderBuilder configureMeterProviderBuilder(
            SdkMeterProviderBuilder builder,
            SignalConfiguration signalConfiguration,
            Resource resource,
            SampleRateManager sampleRateManager) {
        ElasticMetricReader elasticMetricReader = new ElasticMetricReader(signalConfiguration.getMetricReader());
        ComposableFilter<MetricData> metricFilter = new ComposableFilter<>();
        metricFilter.addFilter(sampleRateManager.metricFilter);
        metricFilter.addAllFilters(configuration.metricFilters);
        elasticMetricReader.setFilter(metricFilter);

        flusher.setMeterDelegator(elasticMetricReader::forceFlush);

        return builder
                .setClock(ntpManager.getClock())
                .registerMetricReader(elasticMetricReader)
                .setResource(resource);
    }

    private static PeriodicWorkService getPeriodicWorkService() {
        return ServiceManager.get().getService(Service.Names.PERIODIC_WORK);
    }
}