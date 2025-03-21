---
mapped_pages:
  - https://www.elastic.co/guide/en/apm/agent/android/current/configuration.html
---

# Configuration [configuration]

::::{warning}
This functionality is in technical preview and may be changed or removed in a future release. Elastic will work to fix any issues, but features in technical preview are not subject to the support SLA of official GA features.
::::



## Gradle configuration [_gradle_configuration]

Configure your application at compile time within your application’s `build.gradle` file:

```groovy
// Android app's build.gradle file
plugins {
    //...
    id "co.elastic.apm.android" version "[latest_version]" <1>
}

elasticApm {
    // Minimal configuration
    serverUrl = "https://your.elastic.server"

    // Optional
    serviceName = "your app name" <2>
    serviceVersion = "0.0.0" <3>
    apiKey = "your server api key" <4>
    secretToken = "your server auth token" <5>
}
```

1. You can find the latest version in the [Gradle plugin portal](https://plugins.gradle.org/plugin/co.elastic.apm.android).
2. Defaults to your `android.defaultConfig.applicationId` value.
3. Defaults to your `android.defaultConfig.versionName` value.
4. Defaults to null. More info on API Keys [here](https://www.elastic.co/docs/api/doc/elasticsearch/operation/operation-security-create-api-key).
5. Defaults to null.


::::{note}
When both `secretToken` and `apiKey` are provided, apiKey has priority and secretToken is ignored.
::::


All of the values provided in the Gradle configuration can be overridden with the following environment variables:

| Config | Associated Environment variable |
| --- | --- |
| serviceName | `ELASTIC_APM_SERVICE_NAME` |
| serviceVersion | `ELASTIC_APM_SERVICE_VERSION` |
| serverUrl | `ELASTIC_APM_SERVER_URL` |
| apiKey | `ELASTIC_APM_API_KEY` |
| secretToken | `ELASTIC_APM_SECRET_TOKEN` |


## Runtime configuration [_runtime_configuration]

The runtime configuration is provided within your [Application](https://developer.android.com/reference/android/app/Application) class when initializing the Elastic agent. This configuration overrides any previously-set compile time configuration.

Runtime configuration works by providing your own instance of the `ElasticApmConfiguration` class as shown below:

```java
// Application class

class MyApp extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ElasticApmAgent.initialize(this, ElasticApmConfiguration.builder().build());
    }
}
```


### APM Server connectivity [app-server-connectivity]

The APM Server connectivity parameters can be provided at compile time, either by using the Gradle DSL configuration or by providing the APM Server connectivity-related environment variables as mentioned above. Later on, when the app is running, the connectivity parameters can be overridden by providing a custom `Connectivity` instance when initializing the Elastic agent.

Once you’ve created your `Connectivity` instance, you can set it into the agent’s initialization as show below:

```java
class MyApp extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Connectivity myCustomConnectivity = Connectivity.simple(/*params*/);
        ElasticApmAgent.initialize(this, myCustomConnectivity);

        // Optionally if you also define a custom configuration:
        // ElasticApmAgent.initialize(this, ElasticApmConfiguration.builder().build(), myCustomConnectivity);
    }
}
```


### OpenTelemetry resource [opentelemetry-resource]

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



### Application ID configuration [app-id-configuration]

You can provide your application name, version, and environment dynamically when building your `ElasticApmConfiguration` instance as shown below:

```java
class MyApp extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ElasticApmConfiguration configuration = ElasticApmConfiguration.builder()
                .setServiceName("my-custom-name")
                .setServiceVersion("1.0.0")
                .setDeploymentEnvironment("debug")
                .build();
        ElasticApmAgent.initialize(this, configuration);
    }
}
```


### Sample rate configuration [sample-rate-configuration]

Sample rates are applied to [sessions](https://opentelemetry.io/docs/specs/semconv/general/session/), meaning that, if the sample rate value is `0.5`, then only half of the sessions would get sampled. You can set a session sample rate that will be evaluated on every new session creation to determine whether the full session is exported or ignored. Sessions are currently time-based and will kept alive for at least 30 mins. A `session.id` attribute will be sent on every signal until the timer ends, resetting the timer whenever a new signal is created.

When the time’s up, a new session ID will be generated and the sample rate will be evaluated to determine whether the new session’s signals will get exported or ignored.

You can set the sample rate value at runtime either programmatically, as shown below, or remotely through the [Central configuration](#configuration-dynamic). Values set through Central configuration will override the ones set programmatically.

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

The agent provides automatic instrumentation for its [*Supported technologies*](/reference/supported-technologies.md) which are all enabled by default. You can choose which ones to keep enabled, as well as and disabling those you don’t need, at runtime, like so:

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


### Internal logging policy [_internal_logging_policy]

By default, all logs created by this library are printed for a debuggable app build. In the case of non-debuggable builds, only logs at the INFO level and above are printed.

If you would like to create a custom log policy or even disable all of the logs from this library altogether, you can do so by providing your own `LoggingPolicy` configuration. The below example policy will allow all logs of level WARN and higher to be printed. Levels below WARN will be ignored.

```java
class MyApp extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // This example policy will allow all logs of level WARN and higher to get printed, ignoring levels below it.
        LoggingPolicy loggingPolicy = LoggingPolicy.enabled(LogLevel.WARN);

        ElasticApmConfiguration.builder()
                .setLibraryLoggingPolicy(loggingPolicy)
                .build();
        ElasticApmAgent.initialize(this, configuration);
    }
}
```


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



### Further configurations from the OpenTelemetry SDK. [opentelemetry-sdk-configuration]

The configurable parameters provided by the Elastic APM agent aim to help configuring common use cases in an easy way, in most of the cases it means to act as a facade between your application and the OpenTelemetry Java SDK that this agent is built on top. If your project requires to configure more advanced aspects of the overall APM processes, you could directly apply that configuration using the [OpenTelemetry SDK](https://opentelemetry.io/docs/instrumentation/java/getting-started/), which becomes available for you to use within your project by adding the Elastic agent plugin, as explained in [the agent setup guide](/reference/setup.md). Said configuration will be used by the Elastic agent for the [signals](https://opentelemetry.io/docs/concepts/signals/) it sends out of the box.


## Dynamic configuration ![dynamic config](../images/dynamic-config.svg "") [configuration-dynamic]

Configuration options marked with Dynamic true can be changed at runtime when set from Kibana’s [central configuration](docs-content://solutions/observability/apps/apm-agent-central-configuration.md).


## Option reference [_option_reference]

This is a list of all configuration options.


### `recording` ([0.4.0]) [config-recording]

A boolean specifying if the agent should be recording or not. When recording, the agent instruments incoming HTTP requests, tracks errors and collects and sends metrics. When not recording, the agent works as a noop, not collecting data and not communicating with the APM sever, except for polling the central configuration endpoint. As this is a reversible switch, agent threads are not being killed when inactivated, but they will be mostly idle in this state, so the overhead should be negligible.

You can use this setting to dynamically disable Elastic APM at runtime.

[![dynamic config](../images/dynamic-config.svg "") ](#configuration-dynamic)

| Default | Type | Dynamic |
| --- | --- | --- |
| `true` | Boolean | true |


### `session_sample_rate` ([0.9.0]) [config-session-sample-rate]

By default, the agent will sample all signals generated by your application (e.g. spans, metrics, and logs). To reduce overhead and storage requirements, you can set the sample rate to a value between 0.0 and 1.0. When reduced below 1.0, data will be sampled per session. This is so context in a given session isn’t lost. You can use this setting to dynamically disable Elastic APM at runtime by setting the sample rate to `0`.

[![dynamic config](../images/dynamic-config.svg "") ](#configuration-dynamic)

| Default | Type | Dynamic |
| --- | --- | --- |
| `1.0` | Float | true |

