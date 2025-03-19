---
mapped_pages:
  - https://www.elastic.co/guide/en/apm/agent/android/current/supported-technologies.html
---

# Automatic instrumentation

The agent has an opt-in functionality that automatically generates telemetry on your behalf, which allows you to get telemetry data (for the supported targets) without having to write [manual instrumentation](manual-instrumentation.md).

## How it works

You need to **install** the automatic instrumentations you'd like to use.

There are specific targets that are supported for automatic instrumentation, each one has its own Gradle plugin for it to be installed. Based on that, the overall steps to install a supported automatic instrumentation, are as follows:

1. Choose a [supported instrumentation](#supported-instrumentations).
2. Add its Gradle plugin to your project (the same where the [agent](getting-started.md#gradle-setup) is added too).
3. [Initialize the agent](getting-started.md#agent-setup), the same way you would without having any automatic instrumentation. There are no special steps needed during the agent initialization to make the automatic instrumentations work.

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
