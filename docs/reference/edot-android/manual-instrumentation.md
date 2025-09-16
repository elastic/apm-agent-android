---
navigation_title: Manual instrumentation
description: Learn how to manually instrument Android applications using the Elastic Distribution of OpenTelemetry SDK to capture spans, add attributes, and send trace data to Elastic.
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
  - https://www.elastic.co/guide/en/apm/agent/android/current/manual-instrumentation.html
---

# Manual instrumentation for Android with EDOT SDK

Learn how to manually instrument Android applications using the Elastic Distribution of OpenTelemetry SDK to capture spans, add attributes, and send trace data to Elastic.

You can create your custom spans, metrics, and logs, using the [OpenTelemetry SDK APIs](https://opentelemetry.io/docs/languages/java/api/#opentelemetry-api), which you can find in the [OpenTelemetry](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-api/latest/io/opentelemetry/api/OpenTelemetry.html) object provided through the `getOpenTelemetry()` method. 

Alternatively, for common operations, you can use the [convenience EDOT Android extensions](#convenience-extensions) to create telemetry in a less verbose way.

## OpenTelemetry APIs

After completing the [setup](getting-started.md) process, EDOT Android has configured an [OpenTelemetry](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-api/latest/io/opentelemetry/api/OpenTelemetry.html) object for you, which is available through the `getOpenTelemetry()` method. 

Here's an example of how to create telemetry using the OpenTelemetry Java APIs:

```kotlin
fun myMethod() {
    val agent: ElasticApmAgent

    // Span example
    val tracer = agent.getOpenTelemetry().getTracer("my-tracer-scope")
    val span = tracer.spanBuilder("spanName").startSpan()
    // ...
    span.end()

    // Metric example
    val counter = agent.getOpenTelemetry().meterBuilder("meterScope").build().counterBuilder("myCounter").build()
    counter.add(1)

    // Logs example
    val logger = agent.getOpenTelemetry().logsBridge["logScope"]
    logger.logRecordBuilder().setBody("Log body").emit()
}
```

For more details on creating signals using the OpenTelemetry APIs, refer to the following pages:

- [Manually create spans](https://opentelemetry.io/docs/languages/java/api/#span).
- [Manually create metrics](https://opentelemetry.io/docs/languages/java/api/#meter).
- [Manually create logs](https://opentelemetry.io/docs/languages/java/api/#logger).

## Convenience extensions

For common use cases, such as spans and logs creation, EDOT Android provides a couple of Kotlin extension methods to allow you to create telemetry in a less verbose way.

The convenience methods make use of the same [OpenTelemetry APIs](#opentelemetry-apis) internally to create telemetry. While they're not the only way to create the following signals, they make it easier to create them.

### Spans

The following example shows how to manually create a span using the convenience extension:

```kotlin

fun myMethod() {
    val agent: ElasticApmAgent

    agent.span("spanName") { // <1>
        // The span body.
    }
}
```

1. The span name and its body are the only mandatory parameters from this method. However, there are other optional parameters, such as one to set custom `attributes`, that you can provide if needed. Refer to the [method definition](https://github.com/elastic/apm-agent-android/blob/main/agent-sdk/src/main/java/co/elastic/otel/android/extensions/ElasticOtelAgentExtensions.kt) for more details.

### Logs

The following example shows how to manually create a log record using the convenience extension:

```kotlin

fun myMethod() {
    val agent: ElasticApmAgent

    agent.log("My log body") // <1>
}
```

1. The log record body is the only mandatory parameter from this method. However, there are other optional parameters (such as one to set custom `attributes`), that you can provide if needed. Refer to the [method definition](https://github.com/elastic/apm-agent-android/blob/main/agent-sdk/src/main/java/co/elastic/otel/android/extensions/ElasticOtelAgentExtensions.kt) for more details.