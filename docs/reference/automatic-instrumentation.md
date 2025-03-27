---
mapped_pages:
  - https://www.elastic.co/guide/en/apm/agent/android/current/supported-technologies.html
---

# Automatic instrumentation

The agent has an opt-in functionality that automatically generates telemetry on your behalf. This allows you to get telemetry data for supported targets without having to write [manual instrumentation](manual-instrumentation.md).

## How it works

### Installation

Install the automatic instrumentations you'd like to use.

Specific targets are supported for automatic instrumentation, each with its own Gradle plugin for installation. To install a supported automatic instrumentation, follow these steps:

1. Choose a [supported instrumentation](#supported-instrumentations).
2. Add its Gradle plugin to your project in the same location where the [agent](getting-started.md#gradle-setup) is added.
3. [Initialize the agent](getting-started.md#agent-setup) the same way you would without using automatic instrumentation. Automatic instrumentations will get installed during the agent initialization without having to write extra code.

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
