---
mapped_pages:
  - https://www.elastic.co/guide/en/apm/agent/android/current/supported-technologies.html
---

# Automatic instrumentation

The agent has an opt-in functionality that automatically generates telemetry on your behalf, which allows you to get telemetry data (for the supported targets) without having to write [manual instrumentation](manual-instrumentation.md).

## How it works

### Installation

You need to **install** the automatic instrumentations you'd like to use.

Specific targets are supported for automatic instrumentation, each with its own Gradle plugin for installation. To install a supported automatic instrumentation, follow these steps:

1. Choose a [supported instrumentation](#supported-instrumentations).
2. Add its Gradle plugin to your project in the same location where the [agent](getting-started.md#gradle-setup) is added.
3. [Initialize the agent](getting-started.md#agent-setup), the same way you would without having any automatic instrumentation. There are no special steps needed during the agent initialization to make the automatic instrumentations work.

### Compilation behavior

Some automatic instrumentations perform "byte code instrumentation" (also called byte code weaving), where essentially your application's code (including code from the libraries it uses) is modified **at compile-time**. This is needed in many cases to be able of providing a solution for which you don't need to write code to make it work, as the otherwise "manual code changes" would be done on your behalf.

Byte code instrumentation is a common technique which is probably already used in your project for other use cases, such as for [code optimization](https://developer.android.com/build/shrink-code#optimization) via R8, for example. It's a very useful technique, although there's one problem with it (which you've probably spotted with R8 optimizations) which is that **it can make the compilation take longer** to complete. Because of it, the agent provides [a way to select](#automatic-instrumentation-configuration) which build types of your app to exclude from getting these byte code changes, similarly to what [isMinifyEnabled](https://developer.android.com/build/shrink-code#enable) does in the case of R8 functionalities.

### Configuration [automatic-instrumentation-configuration]

For some projects (and depending on their size, including dependencies) the side effects of the [compilation behavior](#compilation-behavior) explained above are something that they'd like to avoid on certain types of builds for which said functionality is not needed. You can use the following configuration to do so:

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

1. By default, the `disableForBuildTypes` list is empty. You can add as many [build type](https://developer.android.com/build/build-variants#build-types) names as needed for which you'd like to disable byte code instrumentation.

:::{note}
Disabling byte code instrumentation will cause that the [automatic instrumentations](#supported-instrumentations) that need it won't be able to work properly on the affected build type. This shouldn't cause issues to your app's functionality in general, it will only affect the agent's ability to automatically collect telemetry.
:::

## Supported instrumentations

### OkHttp

Creates spans for outgoing HTTP requests that are made via the [OkHttp](https://square.github.io/okhttp/) library, this also includes tools that rely on OkHttp to work, such as [Retrofit](https://square.github.io/retrofit/), for example.

#### Gradle plugin

```kotlin
plugins {
    id("co.elastic.otel.android.instrumentation.okhttp") version "[latest_version]" // <1>
}
```

1. You can find the latest version [here](https://plugins.gradle.org/plugin/co.elastic.otel.android.instrumentation.okhttp).
