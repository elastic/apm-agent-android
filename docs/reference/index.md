---
navigation_title: 'OTel Android agent'
mapped_pages:
  - https://www.elastic.co/guide/en/apm/agent/android/current/intro.html
  - https://www.elastic.co/guide/en/apm/agent/android/current/index.html
---

# Introduction

## What it is

The Elastic OTel Android agent is an [APM](https://en.wikipedia.org/wiki/Application_performance_management) agent based on [OpenTelemetry](https://opentelemetry.io/) ![alt](../images/opentelemetry-logo.png "OpenTelemetry =16x16") which provides built-in tools and configurations to make [OpenTelemetry Java](https://github.com/open-telemetry/opentelemetry-java) work with the {{stack}} with as little code as possible while fully harnessing the combined forces of [Elasticsearch](docs-content://get-started/index.md) and [Kibana](docs-content://get-started/the-stack.md) for your Android application.

## What can I do with it?

The Elastic APM Android Agent automatically measures the performance of your application and tracks errors. It has a default configuration that suits most common use cases and built-in support for popular frameworks and technologies. The agent is built on top of [OpenTelemetry](https://opentelemetry.io/), enabling you to add custom instrumentation with the [OpenTelemetry Java API](https://opentelemetry.io/docs/instrumentation/java/manual/).

## How does the Agent work? [how-it-works]

The Agent auto-instruments [*Supported technologies*](/reference/automatic-instrumentation.md) and records interesting events, like spans for outgoing HTTP requests and UI rendering processes. To do this, it leverages the capability of the Android Gradle plugin API to instrument the bytecode of classes. This means that for supported technologies, there are no code changes required.

Spans are grouped in transactions — by default, one for each outgoing HTTP request or UI rendering process. It’s also possible to create custom transactions, as well as logs and metrics, with the [OpenTelemetry Java API](https://opentelemetry.io/docs/instrumentation/java/manual/), which is automatically provided to the Agent’s host app. Spans, Logs and Metrics are sent to the APM Server, where they’re converted to a format suitable for Elasticsearch. You can then use the APM app in Kibana to gain insight into latency issues and error culprits within your application.

::::{note}
The metrics aggregation strategy used by the agent is [DELTA](https://github.com/open-telemetry/opentelemetry-java/blob/976edfde504193f84d19936b97e2eb8d8cf060e2/sdk/metrics/src/main/java/io/opentelemetry/sdk/metrics/data/AggregationTemporality.java#L15).
::::


More detailed information on how the Agent works can be found in the [FAQ](/reference/faq.md#faq-how-does-it-work).


## Additional components [additional-components]

APM Agents work in conjunction with the [APM Server](docs-content://solutions/observability/apps/application-performance-monitoring-apm.md), [Elasticsearch](docs-content://get-started/index.md), and [Kibana](docs-content://get-started/the-stack.md). The [APM Guide](docs-content://solutions/observability/apps/application-performance-monitoring-apm.md) provides details on how these components work together, and provides a matrix outlining [Agent and Server compatibility](docs-content://solutions/observability/apps/apm-agent-compatibility.md).

