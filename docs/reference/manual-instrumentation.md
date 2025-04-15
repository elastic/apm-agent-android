---
mapped_pages:
  - https://www.elastic.co/guide/en/apm/agent/android/current/manual-instrumentation.html
---

# Manual instrumentation

You can create your custom spans, metrics, and logs, via the [OpenTelemetry SDK APIs](https://opentelemetry.io/docs/languages/java/api/#opentelemetry-api), which you can find in the [OpenTelemetry](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-api/latest/io/opentelemetry/api/OpenTelemetry.html) object provided by the agent via its `getOpenTelemetry()` method. Alternatively, for common operations, you might be able to take advantage of the [convenience agent extensions](#convenience-extensions) to create telemetry in a less verbose way.

## OpenTelemetry APIs

After completing the [setup](getting-started.md) process, the agent will have configured an [OpenTelemetry](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-api/latest/io/opentelemetry/api/OpenTelemetry.html) object for you, which is available via its `getOpenTelemetry()` method. Here's an example of how to create telemetry with it.

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

- [Manually create **spans**](https://opentelemetry.io/docs/languages/java/api/#span).
- [Manually create **metrics**](https://opentelemetry.io/docs/languages/java/api/#meter).
- [Manually create **logs**](https://opentelemetry.io/docs/languages/java/api/#logger).

## Convenience extensions

For common use cases, in regards to spans and logs creation, the agent provides a couple of Kotlin extension methods to allow you to create telemetry in a less verbose way.

:::{note}
The convenience methods make use of the same [OpenTelemetry APIs](#opentelemetry-apis) internally to create telemetry. So they are not the only way to create the following signals, they are only making them more straightforward to create.
:::

### Spans

```kotlin

fun myMethod() {
    val agent: ElasticApmAgent

    agent.span("spanName") { // <1>
        // The span body.
    }
}
```

1. The span name and its body are the only mandatory parameters from this method. However, there are other optional parameters (such as one to set custom `attributes`) that you can provide if needed. Take a look at the [method definition](https://github.com/elastic/apm-agent-android/blob/main/agent-sdk/src/main/java/co/elastic/otel/android/extensions/ElasticOtelAgentExtensions.kt) to find out more.

### Logs

```kotlin

fun myMethod() {
    val agent: ElasticApmAgent

    agent.log("My log body") // <1>
}
```

1. The log record body is the only mandatory parameter from this method. However, there are other optional parameters (such as one to set custom `attributes`), that you can provide if needed. Take a look at the [method definition](https://github.com/elastic/apm-agent-android/blob/main/agent-sdk/src/main/java/co/elastic/otel/android/extensions/ElasticOtelAgentExtensions.kt) to find out more.