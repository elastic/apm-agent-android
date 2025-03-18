---
mapped_pages:
  - https://www.elastic.co/guide/en/apm/agent/android/current/configuration.html
---

# Configuration [configuration]

## Initialization configuration [_runtime_configuration]

Available from the Elastic agent builder shown in [Agent setup](getting-started.md#agent-setup), the following are its available parameters.

### Application info [application-info]

Providing your application name, version, and environment:

```kotlin
class MyApp : android.app.Application {

    override fun onCreate() {
        super.onCreate()
        agent = ElasticApmAgent.builder(this)
            .setServiceName("My app name") // <1>
            .setServiceVersion("1.0.0") // <2>
            .setDeploymentEnvironment("prod") // <3>
            // ...
            .build()
    }
}
```

1. This will be the name used by {{kib}} when listing your application on the [Services](https://www.elastic.co/guide/en/observability/current/apm-services.html) page, defaults to `unknown`. See, [why your app is referred to as a "service"](faq.md#why-service-name).
2. Your app's version name, defaults to the version provided [here](https://developer.android.com/reference/android/content/pm/PackageInfo#versionName).
3. Typically your app's build type, flavor, backend environment it points to, or maybe a combination of them. Any helpful distinction for you to better analyze your app's data later on in {{kib}}.

### Export connectivity [export-connectivity]

Configuring where your app's telemetry will be exported to.

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
2. Your authentication method. You can use either an [API Key](https://www.elastic.co/guide/en/observability/current/apm-api-key.html), a [Secret token](https://www.elastic.co/guide/en/observability/current/apm-secret-token.html), or none; defaults to `None`. API Keys are the recommended method, if you don't have one yet, check out [how to create one](how-tos.md#create-api-key).
3. The protocol used to communicate with your endpoint. It can be either `HTTP` or `gRPC`, defaults to `HTTP`.

### Intercepting attributes

You can provide global interceptors for all spans and logs [attributes](https://opentelemetry.io/docs/specs/otel/common/#attribute), where you can read/modify them if needed.

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

### Intercepting exporters

The agent configures exporters for each signal ([spans](https://opentelemetry.io/docs/languages/java/sdk/#spanexporter), [logs](https://opentelemetry.io/docs/languages/java/sdk/#logrecordexporter) and [metrics](https://opentelemetry.io/docs/languages/java/sdk/#metricexporter)), to manage features like [disk buffering](index.md#disk-buffering) and also to establish a connection with the Elastic export endpoint based on the provided [export connectivity](#export-connectivity) values. You can intercept these to add your own logic on top, such as logging each signal that gets exported, or filtering some items that don't make sense for you to export.

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

### Internal logging policy [internal-logging-policy]

::::{note}
Not to be confused with OpenTelemetry's [log signals](https://opentelemetry.io/docs/concepts/signals/logs/). The internal logging policy is about the agent's internal logs that you should see in [logcat](https://developer.android.com/studio/debug/logcat) only.
::::

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

### Intercepting resources

You can provide your own [resource](https://opentelemetry.io/docs/languages/java/resources/) object which will be used for all of the OpenTelemetry signals (Spans, Metrics and Logs) as shown below:

```java
class MyApp extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Resource myResource = Resource.create(Attributes.builder().put(RESOURCE_KEY, RESOURCE_VALUE).build());
        ElasticApmConfiguration configuration = ElasticApmConfiguration.builder()
                .setResource(myResource)
                .build();
        ElasticApmAgent.initialize(this, configuration);
    }
}
```


### APM Server export protocol [server-export-protocol]

Signals are exported using the gRPC protocol by default. You can change the export protocol to HTTP by using the ExportProtocol configuration as shown below:

```java
class MyApp extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ElasticApmConfiguration configuration = ElasticApmConfiguration.builder()
                .setExportProtocol(ExportProtocol.HTTP) <1>
                .build();
        ElasticApmAgent.initialize(this, configuration);
    }
}
```

1. Note that this configuration may not work if a [custom signal configuration](#custom-signal-configuration) is set.

::::{note}
OTLP over HTTP is supported in APM server versions 8.3.0+. OTLP over gRPC is supported in APM Server versions 7.12.0+.
::::

### Persistence configuration [persistence-configuration]

By default, all APM data is sent to the backend right away. However, this may not be possible or feasible for an Android application. For example, an Android device may run into network issues or the device may need to handle resources in a particular way due to mobile data connectivity and battery life. To prevent these issues, the agent provides disk persistence or local cache support. This enables you to store APM data in disk first, and define how often data should be exported to the backend.

The example below shows how to enable and configure the persistence feature.

```java
class MyApp extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PersistenceConfiguration persistenceConfiguration = PersistenceConfiguration.builder()
                .setEnabled(true) <1>
                .setMaxCacheSize(60 * 1024 * 1024) <2>
                .setExportScheduler(ExportScheduler.getDefault(60 * 1000)) <3>
                .build();
        ElasticApmConfiguration configuration = ElasticApmConfiguration.builder()
                .setPersistenceConfiguration(persistenceConfiguration)
                .build();
        ElasticApmAgent.initialize(this, configuration);
    }
}
```

1. Defaults to `false`.
2. Defaults to 60 MB.
3. Defaults to one minute. The default `ExportScheduler` will run only when the host app is running, though you can create your own implementation of it in order to provide a better-suited scheduler for your app.


### Sample rate configuration [sample-rate-configuration]

Sample rates are applied to [sessions](https://opentelemetry.io/docs/specs/semconv/general/session/), meaning that, if the sample rate value is `0.5`, then only half of the sessions would get sampled. You can set a session sample rate that will be evaluated on every new session creation to determine whether the full session is exported or ignored. Sessions are currently time-based and will kept alive for at least 30 mins. A `session.id` attribute will be sent on every signal until the timer ends, resetting the timer whenever a new signal is created.

When the time’s up, a new session ID will be generated and the sample rate will be evaluated to determine whether the new session’s signals will get exported or ignored.

You can set the sample rate value at runtime either programmatically, as shown below, or remotely through the [Central configuration](#dynamic-configuration). Values set through Central configuration will override the ones set programmatically.

```java
class MyApp extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ElasticApmConfiguration configuration = ElasticApmConfiguration.builder()
                .setSampleRate(0.5) <1>
                .build();
        ElasticApmAgent.initialize(this, configuration);
    }
}
```

1. Only values between 0 and 1.



### Signal filtering [_signal_filtering]

You can provide your own filters to specify which spans, logs, and metrics are allowed to be exported to the backend. With this tool, you could essentially turn some of these signals (or all) on and off at runtime depending on your own business logic.

In order to do so, you need to provide your own filters for each signal in the agent configuration as shown below:

```java
class MyApp extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ElasticApmConfiguration configuration = ElasticApmConfiguration.builder()
                .addLogFilter(new LogFilter(){/*...*/})
                .addMetricFilter(new MetricFilter(){/*...*/})
//                .addMetricFilter(new MetricFilter(){/*...*/}) You can add multiple filters per signal.
                .addSpanFilter(new SpanFilter() {
                    @Override
                    public boolean shouldInclude(ReadableSpan readableSpan) {
                        if (thisSpanIsAllowedToContinue(readableSpan)) {
                            return true;
                        }
                        return false;
                    }
                })
                .build();
        ElasticApmAgent.initialize(this, configuration);
    }
}
```

Each filter will contain a `shouldInclude` function which provides the signal item to be evaluated. This function must return a boolean value--`true` when the provided item is allowed to continue or `false` when it must be discarded.

You can add multiple filters per signal which will be iterated over (in the order they were added) until all the filters are checked or until one of them decides to discard the signal item provided.


### Automatic instrumentation enabling/disabling [_automatic_instrumentation_enablingdisabling]

The agent provides automatic instrumentation for its [*Supported technologies*](/reference/automatic-instrumentation.md) which are all enabled by default. You can choose which ones to keep enabled, as well as and disabling those you don’t need, at runtime, like so:

```java
class MyApp extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // When building an InstrumentationConfiguration object using `InstrumentationConfiguration.builder()`
        // all of the instrumentations are disabled by default, so you only need to enable the ones you need.
        InstrumentationConfiguration instrumentations = InstrumentationConfiguration.builder()
            .enableHttpTracing(true)
            .build();
        ElasticApmConfiguration configuration = ElasticApmConfiguration.builder()
                .setInstrumentationConfiguration(instrumentations)
                .build();
        ElasticApmAgent.initialize(this, configuration);
    }
}
```

::::{note}
When building an InstrumentationConfiguration object using `InstrumentationConfiguration.builder()`, all instrumentations are disabled by default. Only enable the instrumentations you need using the builder setter methods.
::::



### HTTP Configuration [_http_configuration]

The agent provides a configuration object for HTTP-related spans named `HttpTraceConfiguration`. You can pass an instance of it to the `ElasticApmConfiguration` object when initializing the agent in order to customize how the HTTP spans should be handled.


#### Filtering HTTP requests from getting traced [_filtering_http_requests_from_getting_traced]

By default, all of your app’s HTTP requests will get traced. You can avoid some requests from getting traced by creating your own `HttpExclusionRule`. For example, this is an exclusion rule that prevents all requests with the host `127.0.0.1` from getting traced:

```java
class MyHttpExclusionRule extends HttpExclusionRule {

    @Override
    public boolean exclude(HttpRequest request) {
        return request.url.getHost().equals("127.0.0.1");
    }
}
```

Then you’d need to add it to Elastic’s Agent config through its `HttpTraceConfiguration`, like so:

```java
class MyApp extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        HttpTraceConfiguration httpConfig = HttpTraceConfiguration.builder()
            .addExclusionRule(new MyHttpExclusionRule())
            .build();
        ElasticApmConfiguration configuration = ElasticApmConfiguration.builder()
                .setHttpTraceConfiguration(httpConfig)
                .build();
        ElasticApmAgent.initialize(this, configuration);
    }
}
```


#### Adding extra attributes to your HTTP requests' spans [_adding_extra_attributes_to_your_http_requests_spans]

If the HTTP span attributes [provided by default](https://github.com/elastic/apm/tree/main/specs/agents/mobile) aren’t enough, you can attach your own `HttpAttributesVisitor` to add extra params to each HTTP request being traced. For example:

```java
class MyHttpAttributesVisitor implements HttpAttributesVisitor {

    public void visit(AttributesBuilder attrsBuilder, HttpRequest request) {
        attrsBuilder.put("my_custom_attr_key", "my_custom_attr_value");
    }
}
```

Then you’d need to add it to Elastic’s Agent config through its `HttpTraceConfiguration`, like so:

```java
class MyApp extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        HttpTraceConfiguration httpConfig = HttpTraceConfiguration.builder()
        .addHttpAttributesVisitor(new MyHttpAttributesVisitor())
        .build();
        ElasticApmConfiguration configuration = ElasticApmConfiguration.builder()
                .setHttpTraceConfiguration(httpConfig)
                .build();
        ElasticApmAgent.initialize(this, configuration);
    }
}
```


### Trace spans attributes notes [_trace_spans_attributes_notes]

There are [common attributes](https://github.com/elastic/apm/tree/main/specs/agents/mobile) that the Elastic APM agent gathers for every Span. However, due to the nature of Android’s OS, to collect some device-related data some of the above-mentioned resources require the Host app (your app) to have specific runtime permissions granted. If the corresponding permissions aren’t granted, then the device data won’t be collected, and nothing will be sent for those attributes. This table outlines the attributes and their corresponding permissions:

| Attribute | Used in | Requires permission |
| --- | --- | --- |
| `net.host.connection.subtype` | All Spans | [READ_PHONE_STATE](https://developer.android.com/reference/android/Manifest.permission#READ_PHONE_STATE) |


## Advanced configurable options [_advanced_configurable_options]


### Custom SignalConfiguration [custom-signal-configuration]

A SignalConfiguration object contains OpenTelemetry’s processors for all signals: spans, metrics, and logs. The agent takes care of creating a default SignalConfiguration implementation based on the [Connectivity parameters](#app-server-connectivity) passed during the agent’s initialization, as well as the [export protocol](#server-export-protocol) chosen for sending data to the APM Server. However, if you need more control over OpenTelemetry’s processors and exporters, you can override the default SignalConfiguration object and provide your own with custom processors and/or exporters, as shown below:

```java
class MyApp extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SpanExporter mySpanExporter;
        LogRecordExporter myLogRecordExporter;
        MetricExporter myMetricExporter;

        // You could also pass processors instead of exporters.
        SignalConfiguration mySignalConfiguration = SignalConfiguration.custom(mySpanExporter, myLogRecordExporter, myMetricExporter); <1>

        ElasticApmConfiguration.builder()
                .setSignalConfiguration(mySignalConfiguration)
                .build();
        ElasticApmAgent.initialize(this, configuration);
    }
}
```

1. You can either create your own implementation for the SignalConfiguration interface, or you can use the `SignalConfiguration.custom` function and pass your implementations for OpenTelemetry’s processors and/or exporters.

## Dynamic configuration [dynamic-configuration]

Configuration options marked with Dynamic true can be changed at runtime when set from Kibana’s [central configuration](docs-content://solutions/observability/apps/apm-agent-central-configuration.md).