---
navigation_title: Automatic instrumentation
description: Instrument Android applications automatically using EDOT Android.
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
  - https://www.elastic.co/guide/en/apm/agent/android/current/supported-technologies.html
---

# Automatic instrumentation for Android with EDOT SDK

EDOT Android can automatically generate telemetry on your behalf. This allows you to get telemetry data for supported targets without having to write [manual instrumentation](manual-instrumentation.md).

## Installation [supported-instrumentations-installation]

All automatic instrumentations are optional and aren't bundled with the Android agent. You need to install the instrumentations you want to use. Specific targets are supported for automatic instrumentation, each with its own Gradle plugin for installation. 

To install a supported automatic instrumentation, follow these steps:

1. Choose a [supported instrumentation](#supported-instrumentations).
2. Add its Gradle plugin to your project in the same location where the [agent](getting-started.md#gradle-setup) is added.
3. [Initialize EDOT Android](getting-started.md#agent-setup) the same way you would without using automatic instrumentation.

Automatic instrumentations will get installed during EDOT Android's initialization.

```{tip}
You can also use instrumentations from [OpenTelemetry Android](https://github.com/open-telemetry/opentelemetry-android/tree/main/instrumentation) through the [OTel Android instrumentation adapter](#adapter-for-otel-android-instrumentations).
```

## Compilation behavior

Some automatic instrumentations perform bytecode instrumentation, also known as _bytecode weaving_, where your application's code, including code from the libraries it uses, is modified at compile-time. This is similar to what `isMinifyEnabled` does with R8 functionalities, automating code changes that you would otherwise need to make manually. 

Bytecode instrumentation is a common technique which may already be used in your project for use cases such as [code optimization](https://developer.android.com/build/shrink-code#optimization) through R8. While useful, bytecode instrumentation can make compilation take longer to complete. Because of this, EDOT Android provides [a way to exclude](#automatic-instrumentation-configuration) specific build types in your app from byte code changes.

## Configuration [automatic-instrumentation-configuration]

For large projects, you can avoid the added compilation time caused by the [compilation behavior](#compilation-behavior) by excluding build types that don't need the functionality. 

Use the following configuration to exclude build types:

```kotlin
// Your app's build.gradle.kts file
plugins {
    // ...
    id("co.elastic.otel.android.agent")
}

// ...

elasticAgent {
    bytecodeInstrumentation.disableForBuildTypes.set(listOf("debug")) // <1>
}
```

1. By default, the `disableForBuildTypes` list is empty. Add any [build type](https://developer.android.com/build/build-variants#build-types) names for which you want to turn off byte code instrumentation.

:::{note}
Turning off byte-code instrumentation might affect the ability of some [automatic instrumentations](#supported-instrumentations) to generate telemetry.
:::

## Supported instrumentations [supported-instrumentations]

The following automatic instrumentations are supported.

### OkHttp

Creates spans for outgoing HTTP requests that are made using the [OkHttp](https://square.github.io/okhttp/) library. This also includes tools that rely on OkHttp to work, such as [Retrofit](https://square.github.io/retrofit/).

#### Gradle plugin

```kotlin
plugins {
    id("co.elastic.otel.android.instrumentation.okhttp") version "[latest_version]" // <1>
}
```

1. You can find the latest version [here](https://plugins.gradle.org/plugin/co.elastic.otel.android.instrumentation.okhttp).

## Adapter for OTel Android instrumentations

You can use any instrumentation from the OpenTelemetry Android [available instrumentations](https://github.com/open-telemetry/opentelemetry-android/tree/main/instrumentation) through the OTel instrumentation adapter gradle plugin. The adapter is an [extended](opentelemetry://reference/compatibility/nomenclature.md#extended-components) component.

### Add the adapter to your project

Add the OTel Android instrumentation adapter by including it in your app's `plugins` block. This is the same block where the [agent's plugin](getting-started.md#gradle-setup) should also be added.

```kotlin
plugins {
    id("co.elastic.otel.android.instrumentation.oteladapter") version "[latest_version]" // <1>
}
```

1. To find the latest version, refer to [the plugin portal](https://plugins.gradle.org/plugin/co.elastic.otel.android.instrumentation.oteladapter).

### Use an OTel Android instrumentation

After including the adapter in your project, choose an instrumentation from [this list](https://github.com/open-telemetry/opentelemetry-android/tree/main/instrumentation) and follow the installation instructions from its README file.

### Example use case

For example, consider the [HttpURLConnection instrumentation](https://github.com/open-telemetry/opentelemetry-android/tree/main/instrumentation/httpurlconnection), which automatically instruments HTTP requests made with HttpURLConnection.

To have it fully installed, your app's `build.gradle.kts` file should look like this:

```kotlin
plugins {
    // ...
    id("co.elastic.otel.android.instrumentation.oteladapter") // <1>
}

// ...

dependencies {  // <2>
    // ... 
    implementation("io.opentelemetry.android.instrumentation:httpurlconnection-library:AUTO_HTTP_URL_INSTRUMENTATION_VERSION") 
    byteBuddy("io.opentelemetry.android.instrumentation:httpurlconnection-agent:AUTO_HTTP_URL_INSTRUMENTATION_VERSION") // <3>
}
```

1. Make sure the adapter is added.
2. You can find the dependencies needed in the [instrumentation's README file](https://github.com/open-telemetry/opentelemetry-android/tree/main/instrumentation/httpurlconnection#project-dependencies). The same will be the case for any other instrumentation.
3. The instrumentations that require a `byteBuddy` dependency, do bytecode weaving, as explained in [compilation behavior](#compilation-behavior). An extra plugin named `net.bytebuddy.byte-buddy-gradle-plugin` is required to make this work, as shown [here](https://github.com/open-telemetry/opentelemetry-android/tree/main/instrumentation/httpurlconnection#byte-buddy-compilation-plugin). However, EDOT Android installs this extra plugin on your behalf, so there's no need for you to do so manually.

### Compilation performance

As explained in [compilation behavior](#compilation-behavior), the instrumentations that perform bytecode weaving might increase the
compilation times. Because of this, EDOT Android provides a [configuration param](#automatic-instrumentation-configuration)
that allows to select specific build types for which the bytecode instrumentation will be turned off. This configuration
only works for EDOT Android's [supported instrumentations](#supported-instrumentations), so it won't have any effect on
instrumentations added through the OTel Android instrumentations adapter.

To select the build variant to run the bytecode instrumentation for an OTel Android instrumentation, you must
add its `byteBuddy` dependency using the variant-specific byteBuddy name, such as `releaseByteBuddy` or `debugByteBuddy`,
depending on which variant you'd like to enable the bytecode instrumentation for.

Following the previous example, if you want to install the `HttpURLConnection` instrumentation only for your app's `release` build type,
you can change the way we add its byteBuddy dependency as shown here:

```kotlin

dependencies { 
    // ... 
    releaseByteBuddy("io.opentelemetry.android.instrumentation:httpurlconnection-agent:AUTO_HTTP_URL_INSTRUMENTATION_VERSION") // <1>
}
```

1. Will only install the instrumentation for the `release` build type of the app, avoiding to increase the compilation time for other types, such as `debug`, for example.
## Understanding auto-instrumentation scope

Auto-instrumentation automatically captures telemetry for the frameworks and libraries listed on this page. However, it cannot instrument:

- Custom or proprietary frameworks and libraries
- Closed-source components without instrumentation support
- Application-specific business logic

If your application uses technologies not covered by auto-instrumentation, you have two options:

1. **Native OpenTelemetry support** — Some frameworks and libraries include built-in OpenTelemetry instrumentation provided by the vendor.
2. **Manual instrumentation** — Use the [OpenTelemetry API](https://opentelemetry.io/docs/languages/java/instrumentation/) to add custom spans, metrics, and logs for unsupported components.