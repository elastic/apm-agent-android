---
mapped_pages:
  - https://www.elastic.co/guide/en/apm/agent/android/current/supported-technologies.html
---

# Automatic instrumentation

The agent has an opt-in functionality that automatically generates telemetry on your behalf. This allows you to get telemetry data for supported targets without having to write [manual instrumentation](manual-instrumentation.md).

## How it works

### Installation

#### Supported instrumentations [supported-instrumentations-installation]

Install the automatic instrumentations you'd like to use.

Specific targets are supported for automatic instrumentation, each with its own Gradle plugin for installation. To install a supported automatic instrumentation, follow these steps:

1. Choose a [supported instrumentation](#supported-instrumentations).
2. Add its Gradle plugin to your project in the same location where the [agent](getting-started.md#gradle-setup) is added.
3. [Initialize the agent](getting-started.md#agent-setup) the same way you would without using automatic instrumentation. Automatic instrumentations will get installed during the agent initialization without having to write extra code.

#### OpenTelemetry Android instrumentations

```{applies_to}
product: beta
```

You can use instrumentations from [OpenTelemetry Android](https://github.com/open-telemetry/opentelemetry-android/tree/main/instrumentation) with the Elastic agent. Learn how to do so [here](#opentelemetry-android-instrumentation-adapter).

```{important}
This is an [experimental](component-stability.md#experimental) feature.
```

### Compilation behavior

Some automatic instrumentations perform "byte code instrumentation" (also called byte code weaving), where your application's code (including code from the libraries it uses) is modified **at compile-time**. This automates code changes that you would otherwise need to make manually.

Byte code instrumentation is a common technique which may already be used in your project for use cases such as [code optimization](https://developer.android.com/build/shrink-code#optimization) through R8. While useful, code instrumentation can make compilation take longer to complete. Because of this, the agent provides [a way to exclude](#automatic-instrumentation-configuration) specific build types in your app from byte code changes, similar to what [isMinifyEnabled](https://developer.android.com/build/shrink-code#enable) does with R8 functionalities.

### Configuration [automatic-instrumentation-configuration]

For some large projects (including dependencies), you can avoid the added compilation time caused by the [compilation behavior](#compilation-behavior) by excluding build types that don't need the functionality. Use the following configuration to do so:

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

1. By default, the `disableForBuildTypes` list is empty. Add any [build type](https://developer.android.com/build/build-variants#build-types) names for which you want to disable byte code instrumentation.

:::{note}
Disabling byte code instrumentation will cause the [automatic instrumentations](#supported-instrumentations) that need it to not work properly on the affected build type. This shouldn't cause issues to your app's functionality in general, it will only affect the agent's ability to automatically collect telemetry.
:::

## Supported instrumentations

### OkHttp

Creates spans for outgoing HTTP requests that are made using the [OkHttp](https://square.github.io/okhttp/) library. This also includes tools that rely on OkHttp to work, such as [Retrofit](https://square.github.io/retrofit/).

#### Gradle plugin

```kotlin
plugins {
    id("co.elastic.otel.android.instrumentation.okhttp") version "[latest_version]" // <1>
}
```

1. You can find the latest version [here](https://plugins.gradle.org/plugin/co.elastic.otel.android.instrumentation.okhttp).

## OpenTelemetry Android Instrumentation Adapter

**Status**: [experimental](component-stability.md#experimental)

If there's an instrumentation that you can't find in [supported instrumentations](#supported-instrumentations), you can instead search for it in the OpenTelemetry Android [available instrumentations](https://github.com/open-telemetry/opentelemetry-android/tree/main/instrumentation) and use it with the Elastic agent via its OTel instrumentation adapter by following the steps below.

### Add the adapter to your project

The OTel Android instrumentation adapter is a Gradle plugin, which you can find [here](https://plugins.gradle.org/plugin/co.elastic.otel.android.instrumentation.oteladapter). To add it to your project, include it in your app's `plugins` block—the same block where the [agent's plugin](getting-started.md#gradle-setup) should also be added, as shown below:

```kotlin
plugins {
    id("co.elastic.otel.android.instrumentation.oteladapter") version "[latest_version]" // <1>
}
```

1. You can find the latest version [here](https://plugins.gradle.org/plugin/co.elastic.otel.android.instrumentation.oteladapter).

### Use an OTel Android instrumentation

[OTel Android instrumentations](https://github.com/open-telemetry/opentelemetry-android/tree/main/instrumentation) are designed to work independently of the OTel Android agent. This is why they can be used not only with the Elastic agent but also with any other agent based on OpenTelemetry Java.

With that in mind, after [including the adapter](#add-the-adapter-to-your-project) in your project, you can install any OTel Android instrumentation by following its installation instructions from its README file, just as you would if you were using the OTel Android agent.

#### Example Use Case

For example, let's install the [HttpURLConnection instrumentation](https://github.com/open-telemetry/opentelemetry-android/tree/main/instrumentation/httpurlconnection), which automatically instruments HTTP requests made with [HttpURLConnection](https://developer.android.com/reference/java/net/HttpURLConnection).

* First, ensure that [the adapter](#add-the-adapter-to-your-project) is added to your project.
* Next, refer to the HttpURLConnection instrumentation README for instructions on [how to include it in your project](https://github.com/open-telemetry/opentelemetry-android/tree/main/instrumentation/httpurlconnection#add-these-dependencies-to-your-project).
* Finally, follow those instructions, which usually involve adding one or more Gradle dependencies. Once those dependencies are in place, the adapter will take care of the rest.

Based on the above, your app's `build.gradle.kts` file should look like this:

```kotlin
plugins {
    // ...
    id("co.elastic.otel.android.instrumentation.oteladapter") // <1>
}

// ...

dependencies {
    // ...
    implementation("io.opentelemetry.android.instrumentation:httpurlconnection-library:AUTO_HTTP_URL_INSTRUMENTATION_VERSION") // <2>
    byteBuddy("io.opentelemetry.android.instrumentation:httpurlconnection-agent:AUTO_HTTP_URL_INSTRUMENTATION_VERSION")
}
```

1. More info [here](#add-the-adapter-to-your-project).
2. You can find the latest versions in the official instructions for this instrumentation [here](https://github.com/open-telemetry/opentelemetry-android/tree/main/instrumentation/httpurlconnection#add-these-dependencies-to-your-project).

And that's it! Now the Elastic agent will use the HttpURLConnection instrumentation to automatically instrument those kinds of HTTP requests on your behalf.

:::{note}
Notice that unusual `byteBuddy` dependency we added in our example? Some instrumentations require this in order to perform byte code weaving, as mentioned earlier in [Compilation behavior](#compilation-behavior). This functionality is enabled by the [ByteBuddy Android Gradle plugin](https://github.com/raphw/byte-buddy/tree/master/byte-buddy-gradle-plugin/android-plugin), which must be present in your project for byte code weaving to work.

You don't need to worry about adding the ByteBuddy plugin manually though, as the Elastic agent takes care of that for you. So feel free to ignore any installation instructions that mention adding it, such as those in [the HttpURLConnection instructions](https://github.com/open-telemetry/opentelemetry-android/tree/main/instrumentation/httpurlconnection#byte-buddy-compilation-plugin), for example.
:::
