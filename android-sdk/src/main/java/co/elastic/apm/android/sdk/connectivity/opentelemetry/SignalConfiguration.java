package co.elastic.apm.android.sdk.connectivity.opentelemetry;

import co.elastic.apm.android.sdk.ElasticApmAgent;
import co.elastic.apm.android.sdk.connectivity.opentelemetry.custom.CustomSignalConfiguration;
import co.elastic.apm.android.sdk.connectivity.opentelemetry.custom.CustomSignalExporterConfiguration;
import co.elastic.apm.android.sdk.internal.services.Service;
import co.elastic.apm.android.sdk.internal.services.metadata.ApmMetadataService;
import co.elastic.apm.android.sdk.internal.utilities.providers.LazyProvider;
import co.elastic.apm.android.sdk.internal.utilities.providers.Provider;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;

/**
 * Provides an OpenTelemetry objects which handle the APM backend connectivity for all signals.
 */
public interface SignalConfiguration {
    /**
     * This function provides a convenient way of creating a common {@link SignalConfiguration} object that
     * requires and endpoint URL, and optionally a server secret token for Bearer authentication purposes.
     * <p>
     * An example of using this function to create a configuration with a secret token:
     *
     * <pre>
     *  {@code SignalConfiguration myConfiguration = SignalConfiguration.create("https://my.server.url").withSecretToken("my_bearer_token");}
     * </pre>
     *
     * @param endpoint - The APM server URL.
     */
    static DefaultSignalConfiguration create(String endpoint) {
        return new DefaultSignalConfiguration(endpoint);
    }

    /**
     * This function provides a convenient way to create a {@link SignalConfiguration} with a custom OpenTelemetry's {@link SpanExporter}, {@link LogRecordExporter} and {@link MetricExporter}
     * which processors will be {@link BatchSpanProcessor}, {@link BatchLogRecordProcessor} and {@link PeriodicMetricReader} respectively.
     */
    static SignalConfiguration custom(SpanExporter spanExporter, LogRecordExporter logExporter, MetricExporter metricExporter) {
        return new CustomSignalExporterConfiguration(spanExporter, logExporter, metricExporter);
    }

    /**
     * This function provides a convenient way of creating a fully customized {@link SignalConfiguration} object by providing
     * a {@link SpanProcessor}, {@link LogRecordProcessor} and a {@link MetricReader} directly. This option might come in handy to avoid having to create a custom {@link SignalConfiguration} implementation.
     */
    static SignalConfiguration custom(SpanProcessor spanProcessor, LogRecordProcessor logProcessor, MetricReader metricReader) {
        return new CustomSignalConfiguration(spanProcessor, logProcessor, metricReader);
    }

    /**
     * This function provides a {@link SignalConfiguration} instance that uses the server parameters defined at compile time.
     */
    static Provider<SignalConfiguration> getDefault() {
        return LazyProvider.of(() -> {
            ApmMetadataService service = ElasticApmAgent.get().getService(Service.Names.METADATA);
            DefaultSignalConfiguration configuration = SignalConfiguration.create(service.getServerUrl());
            String secretToken = service.getSecretToken();
            if (secretToken != null) {
                configuration.withSecretToken(secretToken);
            }
            return configuration;
        });
    }

    SpanProcessor getSpanProcessor();

    LogRecordProcessor getLogProcessor();

    MetricReader getMetricReader();
}
