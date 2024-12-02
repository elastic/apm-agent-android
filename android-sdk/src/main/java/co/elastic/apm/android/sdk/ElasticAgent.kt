package co.elastic.apm.android.sdk

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.logs.SdkLoggerProvider
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.semconv.ResourceAttributes

class ElasticAgent internal constructor(private val openTelemetry: OpenTelemetry) {

    fun getOpenTelemetry(): OpenTelemetry {
        return openTelemetry
    }

    companion object {
        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }

    class Builder internal constructor() {
        private var serviceName: String = ""
        private var serviceVersion: String = ""
        private var deploymentEnvironment: String = ""

        fun setServiceName(value: String) = apply {
            serviceName = value
        }

        fun setServiceVersion(value: String) = apply {
            serviceVersion = value
        }

        fun setDeploymentEnvironment(value: String) = apply {
            deploymentEnvironment = value
        }

        fun build(): ElasticAgent {
            val resource = Resource.builder()
                .put(ResourceAttributes.SERVICE_NAME, serviceName)
                .put(ResourceAttributes.SERVICE_VERSION, serviceVersion)
                .put(ResourceAttributes.DEPLOYMENT_ENVIRONMENT, deploymentEnvironment)
                .build()
            val openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(
                    SdkTracerProvider.builder()
                        .setResource(resource)
                        .build()
                )
                .setLoggerProvider(
                    SdkLoggerProvider.builder()
                        .setResource(resource)
                        .build()
                )
                .setMeterProvider(
                    SdkMeterProvider.builder()
                        .setResource(resource)
                        .build()
                )
                .build()
            return ElasticAgent(openTelemetry)
        }
    }
}