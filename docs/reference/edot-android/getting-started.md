---
navigation_title: Get started
description: Set up the Elastic Distribution of OpenTelemetry Android (EDOT Android) to send data to Elastic.
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
  - https://www.elastic.co/guide/en/apm/agent/android/current/setup.html
---

# Get started with EDOT Android

Set up the Elastic Distribution of OpenTelemetry Android (EDOT Android) in your app and explore your app's data in {{kib}}.

## Requirements

| Requirement                                       | Minimum version                                                                                           |
|---------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| [{{stack}}](https://www.elastic.co/elastic-stack) | 8.18                                                                                                      |
| Android API level                                 | 26 (or 21 with [desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring)) |

:::{important}
If your application's [minSdk](https://developer.android.com/studio/publish/versioning#minsdk) value is lower than 26, you must add [Java 8 desugaring support](https://developer.android.com/studio/write/java8-support#library-desugaring). Refer to [Troubleshooting](docs-content://troubleshoot/ingest/opentelemetry/edot-sdks/android/index.md#why-desugaring) for more information.
:::

## Gradle setup

Add the [EDOT Android agent plugin](https://plugins.gradle.org/plugin/co.elastic.otel.android.agent) to your application’s `build.gradle[.kts]` file:

```kotlin
plugins {
    id("com.android.application")
    id("co.elastic.otel.android.agent") version "[latest_version]" // <1>
}
```

1. You can find the latest version in [the plugin portal](https://plugins.gradle.org/plugin/co.elastic.otel.android.agent).

## Agent setup

After you've configured Gradle, initialize the agent within your app's code:

```kotlin
val agent = ElasticApmAgent.builder(application) // <1>
    .setServiceName("My app name") // <2>
    .setExportUrl("http://10.0.2.2:4318") // <3>
    .setExportAuthentication(Authentication.ApiKey("my-api-key")) // <4>
    .build()
```

1. Your [Application](https://developer.android.com/reference/android/app/Application) object. [Get your application object](docs-content://troubleshoot/ingest/opentelemetry/edot-sdks/android/index.md#get-application).
2. In OpenTelemetry, _service_ means _an entity that produces telemetry_, so this is where your application name should go. Refer to [Troubleshooting](docs-content://troubleshoot/ingest/opentelemetry/edot-sdks/android/index.md#why-service) for more information.
3. This is the Elastic endpoint where all your telemetry will be exported. [Get your Elastic endpoint](docs-content://troubleshoot/ingest/opentelemetry/edot-sdks/android/index.md#get-export-endpoint).
4. Use an API key to connect the agent to the {{stack}}. [Create an API key](docs-content://troubleshoot/ingest/opentelemetry/edot-sdks/android/index.md#create-api-key).

:::{tip}
If you'd like to provide these values from outside of your code, using an environment variable or a properties file for example, refer to [Provide config values outside of your code](configuration.md#provide-config-values-from-outside-of-your-code).
:::

## Start sending telemetry

With EDOT Android fully initialized, you can start sending telemetry to your {{stack}}.

### Generate telemetry

The following snippet shows how to generate telemetry through [manual instrumentation](manual-instrumentation.md):

```kotlin
val agent = ElasticApmAgent.builder(application)
    .setServiceName("My app name")
    //...
    .build()


agent.span("My Span") {
    Thread.sleep(500) // <1>
    agent.span("My nested Span") { // <2>
        Thread.sleep(500)
    }
}
```
1. Simulates some code execution for which we want to measure the time it takes to complete.
2. Demonstrates what span hierarchies look like in {{kib}}.

### Visualize telemetry

After your app has sent telemetry data, either [manually](manual-instrumentation.md) or [automatically](automatic-instrumentation.md), view it in {{kib}} by navigating to **Applications**, **Service Inventory**, or by searching for `Service Inventory` in the [global search field](docs-content://explore-analyze/find-and-organize/find-apps-and-objects.md). You should find your application listed there.

:::{image} images/span-visualization/1.png
:alt: Services
:width: 350px
:screenshot:
:::

When you open it, go to the **Transactions** tab, where you should see your app's "outermost" spans listed.

:::{image} images/span-visualization/2.png
:alt: Transactions tab
:width: 350px
:screenshot:
:::

After clicking on the span, you should see it in detail.

:::{image} images/span-visualization/3.png
:alt: Trace sample
:screenshot:
:::

## What’s next? [whats-next]

- This guide uses the minimum configuration options needed to initialize EDOT Android. If you'd like to explore what else you can customize, take a look at the [configuration page](configuration.md).

- In the example, you've manually sent a span, so you've created some [manual instrumentation](manual-instrumentation.md) for your app. While this is helpful and flexible, EDOT Android can also create automatic instrumentations. This means that by simply initializing EDOT Android, it will start sending telemetry data on your behalf without you having to write code. For more details, refer to [Automatic instrumentation](automatic-instrumentation.md).

- [Spans](https://opentelemetry.io/docs/concepts/signals/traces/#spans) are a great way to measure how long some method, part of a method, or even some broader transaction that involves multiple methods takes to complete. However, spans aren't the only type of [signal](https://opentelemetry.io/docs/concepts/signals/) that you can send using the agent. You can send [logs](https://opentelemetry.io/docs/concepts/signals/logs/) and [metrics](https://opentelemetry.io/docs/concepts/signals/metrics/) too! For more details, refer to [Manual instrumentation](manual-instrumentation.md).
