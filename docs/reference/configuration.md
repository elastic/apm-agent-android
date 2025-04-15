---
mapped_pages:
  - https://www.elastic.co/guide/en/apm/agent/android/current/configuration.html
---

# Configuration [configuration]

This page includes a comprehensive list of all the configurable parameters available for the agent, including those you can set during initialization and those you can adjust dynamically afterward.

**Just getting started?** Start with [Getting started](getting-started.md). 

## Initialization configuration 

Available from the Elastic agent builder shown in [Agent setup](getting-started.md#agent-setup), the following are its available parameters.

### Application info

Providing your application name, version, and environment:

```kotlin
class MyApp : android.app.Application {

    override fun onCreate() {
        super.onCreate()
        val agent = ElasticApmAgent.builder(this)
            .setServiceName("My app name") // <1>
            .setServiceVersion("1.0.0") // <2>
            .setDeploymentEnvironment("prod") // <3>
            // ...
            .build()
    }
}
```

1. This will be the name used by {{kib}} when listing your application on the [Services](docs-content://solutions/observability/apm/services.md) page. Defaults to `unknown`. See [why your app is referred to as a "service"](faq.md#why-service).
2. Your app's version name. Defaults to the version provided [here](https://developer.android.com/reference/android/content/pm/PackageInfo#versionName).
3. Typically your app's build type, flavor, backend environment, or maybe a combination of these. Any helpful distinction for you to better analyze your app's data later in {{kib}}.

### Export connectivity

Configuring where your app's telemetry will be exported.

```kotlin
class MyApp : android.app.Application {

    override fun onCreate() {
        super.onCreate()
        val agent = ElasticApmAgent.builder(this)
            // ...
            .setExportUrl("https://my-elastic-apm-collector.endpoint") // <1>
            .setExportAuthentication(Authentication.ApiKey("my-api-key")) // <2>
            .setExportProtocol(ExportProtocol.HTTP) // <3>
            .build()
    }
}
```

1. Your endpoint URL. If you don't have one yet, check out [how to find it](how-tos.md#get-export-endpoint).
2. Your authentication method. You can use either an [API Key](docs-content://solutions/observability/apm/api-keys.md), a [Secret token](docs-content://solutions/observability/apm/secret-token.md), or none; defaults to `None`. API Keys are the recommended method, if you don't have one yet, check out [how to create one](how-tos.md#create-api-key).
3. The protocol used to communicate with your endpoint. It can be either `HTTP` or `gRPC`. Defaults to `HTTP`.

:::{include} _snippets/tip-provide-values-from-outside.md
:::

### Intercepting export request headers

You can provide an interceptor for the signals' export request headers, where you can read/modify them if needed.

```kotlin
class MyApp : android.app.Application {

    override fun onCreate() {
        super.onCreate()
        val agent = ElasticApmAgent.builder(this)
            // ...
            .setExportHeadersInterceptor(interceptor)
            .build()
    }
}
```

### Intercepting attributes

You can provide global interceptors for all spans and logs [attributes](https://opentelemetry.io/docs/specs/otel/common/#attribute), which will be executed on every span or log creation, where you can read/modify them if needed.

This is useful for setting dynamic global attributes. If you'd like to set static global attributes (which are also applied to metrics) take a look at [intercepting resources](#intercepting-resources).

```kotlin
class MyApp : android.app.Application {

    override fun onCreate() {
        super.onCreate()
        val agent = ElasticApmAgent.builder(this)
            // ...
            .addSpanAttributesInterceptor(interceptor)
            .addLogRecordAttributesInterceptor(interceptor)
            .build()
    }
}
```

### Intercepting resources

The agent creates a [resource](https://opentelemetry.io/docs/specs/otel/overview/#resources) for your signals, which is essentially a set of static global attributes. These attributes help {{kib}} properly display your application's data.

You can intercept these resources and read/modify them as shown below.

:::{note}
The resource interceptor is only applied during initialization, as this is the only time where resource attributes can be modified. If you'd like to set _dynamic_ global attributes instead, take a look at [intercepting attributes](#intercepting-attributes).
:::

```kotlin
class MyApp : android.app.Application {

    override fun onCreate() {
        super.onCreate()
        val agent = ElasticApmAgent.builder(this)
            // ...
            .setResourceInterceptor(interceptor)
            .build()
    }
}
```

### Intercepting exporters

The agent configures exporters for each signal ([spans](https://opentelemetry.io/docs/languages/java/sdk/#spanexporter), [logs](https://opentelemetry.io/docs/languages/java/sdk/#logrecordexporter), and [metrics](https://opentelemetry.io/docs/languages/java/sdk/#metricexporter)), to manage features like [disk buffering](index.md#disk-buffering) and also to establish a connection with the Elastic export endpoint based on the provided [export connectivity](#export-connectivity) values. You can intercept these to add your own logic on top, such as logging each signal that gets exported, or filtering some items that don't make sense for you to export.

```kotlin
class MyApp : android.app.Application {

    override fun onCreate() {
        super.onCreate()
        val agent = ElasticApmAgent.builder(this)
            // ...
            .addSpanExporterInterceptor(interceptor)
            .addLogRecordExporterInterceptor(interceptor)
            .addMetricExporterInterceptor(interceptor)
            .build()
    }
}
```

### Intercepting HTTP spans

This is a convenience tool to intercept HTTP-related spans. By default, the agent enhances HTTP span names to include domain:port when only an HTTP verb is set, which is [often the case](https://opentelemetry.io/docs/specs/semconv/http/http-spans/#name) for HTTP client span names.

You can override this behavior by setting your own interceptor (or you can choose to set it to `null` to just disable it all).

```kotlin
class MyApp : android.app.Application {

    override fun onCreate() {
        super.onCreate()
        val agent = ElasticApmAgent.builder(this)
            // ...
            .setHttpSpanInterceptor(interceptor)
            .build()
    }
}
```

### Providing processors

Part of the work that the agent does when configuring the [OpenTelemetry SDK](https://github.com/open-telemetry/opentelemetry-java) on your behalf, is to provide processors, which are needed to delegate data to the exporters. For spans, the agent provides a [BatchSpanProcessor](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-trace/latest/io/opentelemetry/sdk/trace/export/BatchSpanProcessor.html); for logs, a [BatchLogRecordProcessor](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-logs/latest/io/opentelemetry/sdk/logs/export/BatchLogRecordProcessor.html); whereas for metrics, it's a [PeriodicMetricReader](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-metrics/latest/io/opentelemetry/sdk/metrics/export/PeriodicMetricReader.html) (which is analogous to a processor, despite not having that word included on its name).

In case you wanted to provide your own ones, you can do so by setting a custom [ProcessorFactory](https://github.com/elastic/apm-agent-android/blob/main/agent-sdk/src/main/java/co/elastic/otel/android/processors/ProcessorFactory.kt), as shown below. The factory will be called once during initialization and will need to provide a processor per signal. Each processor-provider-method within the factory will contain the pre-configured exporter for that signal as an argument so that it's included into the processor as its delegate exporter.

```kotlin
class MyApp : android.app.Application {

   override fun onCreate() {
      super.onCreate()
      val agent = ElasticApmAgent.builder(this)
         // ...
         .setProcessorFactory(factory)
         .build()
   }
}
```

### Internal logging policy

:::{note}
Not to be confused with OpenTelemetry's [log signals](https://opentelemetry.io/docs/concepts/signals/logs/). The internal logging policy is about the agent's internal logs that you should see in [logcat](https://developer.android.com/studio/debug/logcat) only.
:::

The agent creates logs, by using [Android's Log](https://developer.android.com/reference/android/util/Log) type, to notify about its internal events so that you can check them out in [logcat](https://developer.android.com/studio/debug/logcat) for debugging purposes. By default, all of the logs are printed for a debuggable app build, however, in the case of non-debuggable builds, only logs at the INFO level and above are printed.

If you would like to show some specific logs from the agent, or even disable them altogether, you can do so by providing your own `LoggingPolicy` configuration. The following example shows how to allow all logs of level WARN and higher to be printed, whereas those below WARN will be ignored.

```kotlin
class MyApp : android.app.Application {

    override fun onCreate() {
        super.onCreate()
        val agent = ElasticApmAgent.builder(this)
            // ...
            .setInternalLoggingPolicy(LoggingPolicy.enabled(LogLevel.WARN))
            .build()
    }
}
```

## Dynamic configuration

These are available from an already built [agent](https://github.com/elastic/apm-agent-android/blob/main/agent-sdk/src/main/java/co/elastic/otel/android/ElasticApmAgent.kt).

### Update export connectivity

You can change any of the configuration values provided as part of the [export connectivity](#export-connectivity) setters, at any time, by setting a new [ExportEndpointConfiguration](https://github.com/elastic/apm-agent-android/blob/main/agent-sdk/src/main/java/co/elastic/otel/android/connectivity/ExportEndpointConfiguration.kt) object, which will override them all.

```kotlin
class MyApp : android.app.Application {

    override fun onCreate() {
        super.onCreate()
        val agent = ElasticApmAgent.builder(this)
            // ...
            .build()
        agent.setExportEndpointConfiguration(configuration)
    }
}
```
