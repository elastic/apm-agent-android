---
mapped_pages:
  - https://www.elastic.co/guide/en/apm/agent/android/current/manual-instrumentation.html
---

# Manual instrumentation

You can create your custom spans, metrics, and logs, via the [OpenTelemetry SDK APIs](https://opentelemetry.io/docs/languages/java/api/#opentelemetry-api), which you can find in the [OpenTelemetry](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-api/latest/io/opentelemetry/api/OpenTelemetry.html) object provided by the agent via its `getOpenTelemetry()` method. Alternatively, for common operations, you might be able to take advantage of the [convenience agent extensions](#convenience-extensions) to create telemetry in a less verbose way.

## OpenTelemetry APIs

After completing the [setup](/reference/getting-started.md) process, the agent will have configured an [OpenTelemetry](https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-api/latest/io/opentelemetry/api/OpenTelemetry.html) object for you, which is available via its `getOpenTelemetry()` method. Here's an example of how to create manual telemetry with it.

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

To find more details on how to create signals using the OpenTelemetry APIs, you can have a look at the following pages:

- [This is the guide](https://opentelemetry.io/docs/languages/java/api/#span) for manually creating **spans**.
- [This is the guide](https://opentelemetry.io/docs/languages/java/api/#meter) for manually creating **metrics**.
- [This is the guide](https://opentelemetry.io/docs/languages/java/api/#logger) for manually creating **logs**.

## Convenience extensions

For common use-cases, in regards to spans and logs creation, the agent provides a couple of Kotlin extension methods to allow you to create telemetry in a less verbose way.

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

1. The span name and its body are the only mandatory parameters from this method, however, there are other, optional ones (such as one to set custom `attributes`), that you can provide if needed. Take a look at the [method definition](https://github.com/elastic/apm-agent-android/blob/main/agent-sdk/src/main/java/co/elastic/otel/android/extensions/ElasticOtelAgentExtensions.kt) to find out more.

### Logs

```kotlin

fun myMethod() {
    val agent: ElasticApmAgent

    agent.log("My log body") // <1>
}
```

1. The log record body is the only mandatory parameter from this method, however, there are other, optional ones (such as one to set custom `attributes`), that you can provide if needed. Take a look at the [method definition](https://github.com/elastic/apm-agent-android/blob/main/agent-sdk/src/main/java/co/elastic/otel/android/extensions/ElasticOtelAgentExtensions.kt) to find out more.