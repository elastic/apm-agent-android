---
navigation_title: 'Elastic OTel Android agent'
mapped_pages:
  - https://www.elastic.co/guide/en/apm/agent/android/current/intro.html
  - https://www.elastic.co/guide/en/apm/agent/android/current/index.html
---

# Introduction

The Elastic OTel Android agent is an [APM](https://en.wikipedia.org/wiki/Application_performance_management) agent based on [OpenTelemetry](https://opentelemetry.io/) ![alt](../images/opentelemetry-logo.png "OpenTelemetry =16x16") which provides built-in tools and configurations to make the [OpenTelemetry SDK](https://opentelemetry.io/docs/languages/java/) work with your {{stack}} with as little code as possible while fully harnessing the combined forces of [Elasticsearch](docs-content://get-started/index.md) and [Kibana](docs-content://get-started/the-stack.md) for your Android application.

## What can I do with it?

All the features provided by the [OpenTelemetry SDK](https://github.com/open-telemetry/opentelemetry-java) are available for you to use and are configured by the agent to work properly out of the box. This enables you to fulfil different use-cases that will help better understand your app's performance, some of them are:

### Distributed tracing

Allows you to see the full picture of **how long does your application have to wait for your backend** services to provide an answer (and why), as shown in the example below:

:::{image} ../images/intro/distributed-tracing.png
:screenshot:
:::

In the image above we can see **3 spans** from {{kib}}:

- The first one was created in your application to track an HTTP request to your backend service.
- The second one was created within your backend service, and it shows how long it took between receiving that client request and providing a response for it.
- The last one was also created within your backend service for when it had to query other resources internally. In this case, it had to make an HTTP request to another service, in other cases it could be querying from a database as well.

You may click on any of those spans to see their full details, in case you need more contextual information from each.

:::{note}
For distributed tracing to work properly, your backend services have to be configured to send telemetry to the {{stack}} as well.
:::

### Session review

The agent attaches [session](#session) info to each span and log generated from your application, allowing you to create queries where you can group all the telemetry that belongs to a session and form a session event timeline, useful for understanding what are the most common actions performed by your users, as well as tracing their steps towards errors that they might encounter.

For example, let's say you have a screen "A" in your app that can be opened from other screens, such as "B", and let's say you've created a log event for when the user clicks on a button from screen "B" that takes them to screen "A", as well as a log created when screen "A" is opened (or maybe a span instead, in case you'd like to measure how long it takes for screen "A" to fully load). Both items will contain an attribute named `session.id` with the same value per session. This can let you create {{es}} queries, say in {{kib}}'s [Discover tool](https://www.elastic.co/guide/en/kibana/current/discover.html) for example, to list all the events that happened during that session and to better understand your user's journey within your application.

### More

The examples above show a couple of use-cases that you can achieve with the agent and the {{stack}}, however, since the agent not only configures the [OpenTelemetry SDK](https://opentelemetry.io/docs/languages/java/) but also gives you [direct access](manual-instrumentation.md) to its features, it means that you can generate your own data in ways that better suit your needs and take advantage of {{stack}}'s tools, such as [creating alerts](https://www.elastic.co/guide/en/kibana/current/alerting-getting-started.html) for when something interesting for you happens (maybe when an error is recorded), as well as [custom dashboards](https://www.elastic.co/guide/en/kibana/current/dashboard.html) to display your data the way you need to see it, and [much more](https://www.elastic.co/guide/en/kibana/current/introduction.html).

## Features

On top of the features that come with the [OpenTelemetry SDK](https://opentelemetry.io/docs/languages/java/), the agent also adds the following ones.

### Disk buffering

Your application's telemetry data is stored locally before getting sent to your {{stack}} and removed once it's successfully exported, or to make room for new telemetry data, if needed. This is done to minimize the risk of losing data due to internet connectivity issues.

### Real time

For [distributed tracing](#distributed-tracing) to work properly, your application's time should be in sync with the [coordinated universal time](https://en.wikipedia.org/wiki/Coordinated_Universal_Time). This is sometimes an issue for Android applications, as the time provided by the OS is often not accurate enough. The agent aims to synchronize telemetry timestamps with the universal time to ensure a reliable view of event timelines.

### Session

A session aims to group telemetry data into a timeframe where your application was active, presumably to carry out a single task/operation/goal at a given time, by adding a `session.id` attribute to all the spans and logs that come out of your application. This is useful to be able of narrowing down the amount of data that you should take a look at for cases in which you'd want to investigate the steps that ended up in an error within your app, or just to know what are the common actions that are performed within your app on each use.

A session is created when there's no previous one, or when the previous one has expired. They expire after 30 minutes of idle time (meaning that for those 30 minutes there was no telemetry generated), which can be extended up to 4 hours in case that there's always telemetry data getting generated during that time.

### Dynamic configuration

The agent allows you to modify some values, provided during initialization, after the initialization has finished. More info on the [configuration](configuration.md) page.

### Automatic instrumentation

The agent provides extensions that generate telemetry automatically for common tools and use-cases. More info on the [automatic instrumentation](automatic-instrumentation.md) page.

## How does the Agent work? [how-it-works]

The Agent auto-instruments [*Supported technologies*](/reference/automatic-instrumentation.md) and records interesting events, like spans for outgoing HTTP requests and UI rendering processes. To do this, it leverages the capability of the Android Gradle plugin API to instrument the bytecode of classes. This means that for supported technologies, there are no code changes required.

Spans are grouped in transactions — by default, one for each outgoing HTTP request or UI rendering process. It’s also possible to create custom transactions, as well as logs and metrics, with the [OpenTelemetry Java API](https://opentelemetry.io/docs/instrumentation/java/manual/), which is automatically provided to the Agent’s host app. Spans, Logs and Metrics are sent to the APM Server, where they’re converted to a format suitable for Elasticsearch. You can then use the APM app in Kibana to gain insight into latency issues and error culprits within your application.

::::{note}
The metrics aggregation strategy used by the agent is [DELTA](https://github.com/open-telemetry/opentelemetry-java/blob/976edfde504193f84d19936b97e2eb8d8cf060e2/sdk/metrics/src/main/java/io/opentelemetry/sdk/metrics/data/AggregationTemporality.java#L15).
::::


More detailed information on how the Agent works can be found in the [FAQ](/reference/faq.md#faq-how-does-it-work).


## Additional components [additional-components]

APM Agents work in conjunction with the [APM Server](docs-content://solutions/observability/apps/application-performance-monitoring-apm.md), [Elasticsearch](docs-content://get-started/index.md), and [Kibana](docs-content://get-started/the-stack.md). The [APM Guide](docs-content://solutions/observability/apps/application-performance-monitoring-apm.md) provides details on how these components work together, and provides a matrix outlining [Agent and Server compatibility](docs-content://solutions/observability/apps/apm-agent-compatibility.md).

