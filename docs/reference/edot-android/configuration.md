---
navigation_title: Configuration
description: Comprehensive list of configuration parameters for the Elastic Distribution of OpenTelemetry Android (EDOT Android).
applies_to:
  stack:
  serverless:
    observability:
  product:
    edot_android: ga
products:
  - id: cloud-serverless
  - id: observability
  - id: edot-sdk
mapped_pages:
  - https://www.elastic.co/guide/en/apm/agent/android/current/configuration.html
---

# Configure the EDOT Android SDK [configuration]

This section contains a comprehensive list of all the configurable parameters available for EDOT Android, including those you can set during initialization and those you can adjust dynamically afterward.

## Initialization configuration 

Initialization configuration is available from the EDOT Android builder shown in [Agent setup](getting-started.md#agent-setup). The following are its available parameters.

### Application metadata

Provide your application name, version, and environment. For example:

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

1. Name used by {{kib}} when listing your application on the [Services](docs-content://solutions/observability/apm/services.md) page. Defaults to `unknown`. Refer to [Why your app is referred to as a "service"](docs-content://troubleshoot/ingest/opentelemetry/edot-sdks/android/index.md#why-service).
2. Your app's version name. Defaults to the version provided [in the PackageInfo](https://developer.android.com/reference/android/content/pm/PackageInfo#versionName).
3. Your app's build type, flavor, backend environment, or a combination of these. Helpful to better analyze your app's data later in {{kib}}.

### Export connectivity

Configure where your app's telemetry is exported:

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

1. Your endpoint URL. If you don't have one yet, refer to [Get the export endpoint](docs-content://troubleshoot/ingest/opentelemetry/edot-sdks/android/index.md#get-export-endpoint).
2. Your authentication method. You can use either an [API key](docs-content://solutions/observability/apm/api-keys.md), a [Secret token](docs-content://solutions/observability/apm/secret-token.md), or none. Defaults to `None`. API keys are the recommended method, if you don't have one yet, refer to [Create an API key](docs-content://troubleshoot/ingest/opentelemetry/edot-sdks/android/index.md#create-api-key).
3. The protocol used to communicate with your endpoint. It can be either `HTTP` or `gRPC`. Defaults to `HTTP`.

:::{tip}
To provide these values from outside of your code, using an environment variable or a properties file for example, refer to [Provide config values outside of your code](configuration.md#provide-config-values-from-outside-of-your-code).
:::

### Intercept export request headers

You can provide an interceptor for the signals' export request headers, where you can read or modify them if needed.

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

### Intercept attributes

You can provide global interceptors for all spans and logs [attributes](https://opentelemetry.io/docs/specs/otel/common/#attribute). Interceptors are executed on every span or log creation, where you can read or modify them if needed.

This is useful for setting dynamic global attributes. If you'd like to set static global attributes, which are also applied to metrics, take a look at [Intercept resources](#intercept-resources).

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

### Session behavior

You can configure how [sessions](index.md#sessions) work. For example:

```kotlin
class MyApp : android.app.Application {

    override fun onCreate() {
        super.onCreate()
        val agent = ElasticApmAgent.builder(this)
            .setSessionSampleRate(1.0) // <1>
            // ...
            .build()
    }
}
```

1. From version 1.1.0, you can provide your sample rate value. The value gets evaluated on every new session creation to determine whether the next session is sampled or not. When a session is not sampled, none of its signals are exported. Default value is `1.0`.

### Disk buffering behavior

You can configure how [disk buffering](index.md#disk-buffering) works. For example:

```kotlin
class MyApp : android.app.Application {

    override fun onCreate() {
        super.onCreate()
        val agent = ElasticApmAgent.builder(this)
            .setDiskBufferingConfiguration(DiskBufferingConfiguration.enabled()) // <1>
            // ...
            .build()
    }
}
```

1. From version 1.1.0, you can configure whether the disk-buffering feature is turned on or off. It's turned on by default.

### Intercept resources

EDOT Android creates a [resource](https://opentelemetry.io/docs/specs/otel/overview/#resources) for your signals, which is a set of static global attributes. These attributes help {{kib}} properly display your application's data.

You can intercept these resources and read or modify them as shown in the following example.

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

:::{note}
The resource interceptor is only applied during initialization, as this is the only time where resource attributes can be modified. If you'd like to set _dynamic_ global attributes instead, take a look at [Intercept attributes](#intercept-attributes).
:::

### Intercept exporters

EDOT Android configures exporters for each signal ([spans](https://opentelemetry.io/docs/languages/java/sdk/#spanexporter), [logs](https://opentelemetry.io/docs/languages/java/sdk/#logrecordexporter), and [metrics](https://opentelemetry.io/docs/languages/java/sdk/#metricexporter)) to manage features like [disk buffering](index.md#disk-buffering) and also to establish a connection with the Elastic export endpoint based on the provided [export connectivity](#export-connectivity) values.

You can intercept exporters to add your own logic, such as logging each signal that gets exported, or filtering some items that don't make sense for you to export. For example:

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

### Intercept HTTP spans

This is a convenience tool to intercept HTTP-related spans. By default, EDOT Android enhances HTTP span names to include `domain:port` when only an HTTP verb is set. This is [often the case](https://opentelemetry.io/docs/specs/semconv/http/http-spans/#name) for HTTP client span names.

You can override this behavior by setting your own interceptor, or you can choose to set it to `null` to just turn it off. For example:

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

### Provide processors

Part of the work that EDOT Android does when configuring the OpenTelemetry SDK on your behalf is to provide processors, which are needed to delegate data to the exporters. For spans, EDOT Android provides a [BatchSpanProcessor](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-trace/latest/io/opentelemetry/sdk/trace/export/BatchSpanProcessor.html); for logs, a [BatchLogRecordProcessor](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-logs/latest/io/opentelemetry/sdk/logs/export/BatchLogRecordProcessor.html); whereas for metrics, it's a [PeriodicMetricReader](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-metrics/latest/io/opentelemetry/sdk/metrics/export/PeriodicMetricReader.html), which is analogous to a processor.

If you want to provide your own processors, you can do so by setting a custom [ProcessorFactory](https://github.com/elastic/apm-agent-android/blob/main/agent-sdk/src/main/java/co/elastic/otel/android/processors/ProcessorFactory.kt), as shown in the example:

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

The factory is called once during initialization and needs to provide a processor per signal. Each processor-provider method within the factory contains the configured exporter for that signal as an argument so that it's included into the processor as its delegate exporter.

### Internal logging policy

:::{note}
Not to be confused with OpenTelemetry's [log signals](https://opentelemetry.io/docs/concepts/signals/logs/). The internal logging policy is about EDOT Android's internal logs that you should see in [logcat](https://developer.android.com/studio/debug/logcat) only.
:::

EDOT Android creates logs using [Android's Log](https://developer.android.com/reference/android/util/Log) type to notify about its internal events, so that you can check them out in [logcat](https://developer.android.com/studio/debug/logcat) for debugging purposes. By default, all logs are printed for a debuggable app build. However, in the case of non-debuggable builds, only logs at the `INFO` level and higher are printed.

If you want to show specific logs from EDOT Android, or even turn off logs altogether, you can do so by providing your own `LoggingPolicy` configuration. The following example shows how to allow all logs of level `WARN` and higher to be printed, whereas those lower than `WARN` are ignored.

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

Dynamic configuration settings are available from an already built [agent](https://github.com/elastic/apm-agent-android/blob/main/agent-sdk/src/main/java/co/elastic/otel/android/ElasticApmAgent.kt).

### Update export connectivity

You can change any of the configuration values provided as part of the [export connectivity](#export-connectivity) setters, at any time by setting a new [ExportEndpointConfiguration](https://github.com/elastic/apm-agent-android/blob/main/agent-sdk/src/main/java/co/elastic/otel/android/connectivity/ExportEndpointConfiguration.kt) object, which overrides them all. For example:

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

## Central configuration

```{applies_to}
serverless: unavailable
stack: preview 9.1 
product:
  edot_android: preview 1.2.0
```

Starting from version `1.2.0`, you can remotely manage the EDOT Android behavior through [Central configuration](opentelemetry://reference/central-configuration.md).

### Activate central configuration

The remote management is turned off by default. To turn it on, provide your central configuration endpoint when initializing EDOT Android, as shown here:

```kotlin
class MyApp : android.app.Application {

    override fun onCreate() {
        super.onCreate()
        val agent = ElasticApmAgent.builder(this)
            // ...
            .setManagementUrl("https://...") // <1>
            .setManagementAuthentication(Authentication.ApiKey("my-api-key")) // <2>
            .build()
    }
}
```

1. Provide your EDOT Collector OpAMP endpoint. Refer to [Central configuration](opentelemetry://reference/central-configuration.md) for more details.
2. In case your OpAMP endpoint [requires authentication](elastic-agent://reference/edot-collector/config/default-config-standalone.md#authentication-settings), this is how you can provide your API Key value.

### Available settings

You can modify the following settings for EDOT Android through the Central Configuration:

| Setting | Central configuration name | Description | Type |
|---------|----------------------------|-------------|------|
| Recording | `recording` | Whether EDOT Android should record and export telemetry or not. By default it's enabled, disabling it is effectively turning EDOT Android off where only the central configuration polling will be performed. | Dynamic |
| Session sample rate | `session_sample_rate` | To reduce overhead and storage requirements, you can set the sample rate to a value between 0.0 and 1.0. Data will be sampled per session, this is so context in a given session isn't lost. | Dynamic |

Dynamic settings can be changed without having to restart the application.

## Provide config values from outside of your code

You might need to get values such as an endpoint URL or API key or secret token from a local file in your project directory or an environment variable, or both. You can do this through the Android Gradle plugin and its [build config fields](https://developer.android.com/build/gradle-tips#share-custom-fields-and-resource-values-with-your-app-code), which provide a way to share Gradle info with your app's Kotlin/Java code.

### Provide data from an environment variable

The following example shows how to obtain configuration values from environment variables:

```kotlin
// Your app's build.gradle.kts file
plugins {
    // ...
}

val url = System.getenv("MY_ENV_WITH_MY_URL") // <1>
val apiKey = System.getenv("MY_ENV_WITH_MY_KEY")

android {
    // ...
    buildFeatures.buildConfig = true // <2>

    defaultConfig { // <3>
        // ...
        buildConfigField("String", "MY_EXPORT_URL", "\"$url\"")
        buildConfigField("String", "MY_EXPORT_API_KEY", "\"$apiKey\"")
    }
}
```

1. Because `build.gradle.kts` files are written in [Kotlin](https://kotlinlang.org/), you can get your environment variables the same way you would with regular Kotlin sources. The same applies to `build.gradle` files, which work with [Groovy](https://groovy-lang.org/) instead.
2. Ensure Android's `buildConfig` feature is turned on.
3. Adding build config fields to the `android.defaultConfig` block ensures they're available for all of your app's build variants. To provide different values per variant, you can create fields with the same name but different values for each of your build variants, as shown in Android's [official docs](https://developer.android.com/build/gradle-tips#share-custom-fields-and-resource-values-with-your-app-code).

You've properly created build config fields from environment variables. To use them in code, take a look at how to [read build config fields](#read-build-config-fields) in code.

### Provide data from a properties file

[Properties](https://docs.oracle.com/javase/8/docs/api/java/util/Properties.html) are a common way to provide values to JVM apps through files. Here's an example of how you could use them to provide config values to EDOT Android.

Given the following example properties file:

```properties
my.url=http://localhost
my.api.key=somekey
```

This is what your `build.gradle.kts` configuration should look like:

```kotlin
// Your app's build.gradle.kts file
import java.util.Properties

plugins {
    // ...
}

val myPropertiesFile = project.file("myfile.properties") // <1>
val myProperties = Properties().apply {
    myPropertiesFile.inputStream().use { load(it) }
}

val url = myProperties["my.url"]
val apiKey = myProperties["my.api.key"]

android {
    // ...
    buildFeatures.buildConfig = true // <2>

    defaultConfig { // <3>
        // ...
        buildConfigField("String", "MY_EXPORT_URL", "\"$url\"")
        buildConfigField("String", "MY_EXPORT_API_KEY", "\"$apiKey\"")
    }
}
```

1. Your file path is relative to your `build.gradle.kts` file. In this example, both files, `build.gradle.kts` and `myfile.properties`, are located in the same directory.
2. Make sure Android's `buildConfig` feature is turned on.
3. Adding your build config fields to the `android.defaultConfig` block ensures they're available for all of your app's build variants. To provide different values per variant, you can also create fields with the same name but different values for each of your build variants, as shown in Android's [official docs](https://developer.android.com/build/gradle-tips#share-custom-fields-and-resource-values-with-your-app-code).

You've properly created build config fields from a properties file. To use them in code, refer to [read build config fields](#read-build-config-fields) in code.

### Read build config fields in code [read-build-config-fields]

After adding [build config fields](https://developer.android.com/build/gradle-tips#share-custom-fields-and-resource-values-with-your-app-code) in your `build.gradle.kts` file, you can now use them within your app's Kotlin or Java code by following these steps:

1. Compile your project. The build config fields are generated during compilation, so this step is required so that you can find them in your code later.
2. Find them within your app's `BuildConfig` generated type, as shown in the following example:

```kotlin
import my.app.namespace.BuildConfig // <1>
// ...

fun myMethod() {
    val agent = ElasticApmAgent.builder(application)
        // ...
        .setExportUrl(BuildConfig.MY_EXPORT_URL)
        .setExportAuthentication(Authentication.ApiKey(BuildConfig.MY_EXPORT_API_KEY))
        .build()
}
```

1. There might be multiple `BuildConfig` types available to use in your code. The one that belongs to your app has a package name equal to your [app's namespace](https://developer.android.com/build/configure-app-module#set-namespace) value. So, for example, if your app's namespace is set to `com.my.app`, then the `BuildConfig` type you must use would be `com.my.app.BuildType`.
