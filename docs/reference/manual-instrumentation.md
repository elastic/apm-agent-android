---
mapped_pages:
  - https://www.elastic.co/guide/en/apm/agent/android/current/manual-instrumentation.html
---

# Manual Instrumentation [manual-instrumentation]

::::{warning}
This functionality is in technical preview and may be changed or removed in a future release. Elastic will work to fix any issues, but features in technical preview are not subject to the support SLA of official GA features.
::::


The Elastic APM Android Agent automatically instruments [*Supported technologies*](/reference/automatic-instrumentation.md), creating spans for interesting events for each case, and some of those automated spans can be [configured](/reference/configuration.md) to better suit different app’s needs. However, if you need to create your own, custom spans, metrics and logs, you can do so by accessing the [OpenTelemetry Java SDK APIs](https://opentelemetry.io/docs/instrumentation/java/manual/) that this agent is built on top.


## OpenTelemetry Entrypoint [opentelemetry-entrypoint]

After completing the [setup](/reference/setup.md) process, the Agent will have configured the OpenTelemetry entrypoint for you and made it globally accessible. In order to access to the configured OpenTelemetry instance you need to use the `GlobalOpenTelemetry` class as shown below.

```java
class MyClass {

    // Example of how to obtain an OpenTelemetry tracer, meter, and logger to create custom Spans, Metrics and Logs.
    public void myMethod() {
        // Span example
        Tracer tracer = GlobalOpenTelemetry.getTracer("my-tracer-scope-name");
        Span span = tracer.spanBuilder("spanName").startSpan();
        //...
        span.end();

        // Metric example
        LongCounter counter = GlobalOpenTelemetry.meterBuilder("meterScope").build().counterBuilder("myCounter").build();
        counter.add(1);

        // Logs example
        Logger logger = GlobalOpenTelemetry.get().getLogsBridge().get("logScope");
        logger.logRecordBuilder().setBody("Log body").emit();
    }
}
```

You can find more details on how to create and customize all kinds of [signals](https://opentelemetry.io/docs/concepts/signals/) by following [OpenTelemetry’s Java SDK guide](https://opentelemetry.io/docs/languages/java/instrumentation/) on manually creating signals.

