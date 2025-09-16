---
navigation_title: EDOT Android
description: The Elastic Distribution of OpenTelemetry Android (EDOT Android) is an APM agent based on OpenTelemetry. It provides built-in tools and configurations to make the OpenTelemetry SDK work with Elastic using as little code as possible while fully leveraging the combined forces of Elasticsearch and Kibana for your Android application.
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
  - https://www.elastic.co/guide/en/apm/agent/android/current/intro.html
  - https://www.elastic.co/guide/en/apm/agent/android/current/index.html
---

# Elastic Distribution of OpenTelemetry Android

The Elastic Distribution of OpenTelemetry Android (EDOT Android) is an APM agent based on [OpenTelemetry](https://opentelemetry.io/). EDOT Android provides built-in tools and configurations to make the [OpenTelemetry SDK](https://opentelemetry.io/docs/languages/java/) work with your {{stack}} using as little code as possible.

All the features provided by the [OpenTelemetry SDK](https://github.com/open-telemetry/opentelemetry-java) are available for you to use and are pre-configured by EDOT Android to work properly out of the box. This allows you to fulfill various use cases to better understand your app's performance, such as:

- [Distributed tracing](#distributed-tracing)
- [Session review](#session-review)
- [Alerts and dashboards](#alerts-and-dashboards)

## Distributed tracing [distributed-tracing]

Distributed tracing allows you to get the full picture of how long your application has to wait for your backend services to provide a response, and why, as shown in the following example:

:::{image} images/intro/distributed-tracing.png
:screenshot:
:alt: Distributed tracing
:::

The previous image shows three spans from {{kib}}:

- The first span is created in your application to track an HTTP request to your backend service.
- The second span is created within your backend service and shows how long it took to receive the client request and provide a response.
- The last span is also created within your backend service when it queries other resources internally. In this case, it makes an HTTP request to another service, but in other cases, it might involve querying a database.

You can select any of the spans to view their full details if you need more contextual information from each.

:::{note}
For distributed tracing to work properly, configure your backend services to send telemetry to the {{stack}} as well.
:::

## Session review [session-review]

EDOT Android attaches [session](#sessions) information to each span and log generated from your application. This allows you to create queries that group all the telemetry that belongs to a session and form a session event timeline. This is useful to identify the most common actions performed by your users, as well as tracing the steps leading up to errors they might encounter.

For example, let's say you have a screen "A" in your app that can be opened from other screens, such as "B". If you create a log event when the user clicks on a button on screen "B" that takes them to screen "A", along with a log when screen "A" opens (or a span if you'd like to measure how long it takes for screen "A" to fully load), both items will contain a session.id attribute with the same value per session. This allows you to create Elasticsearch queries, for example in Kibana's Discover tool, to list all events during that session and better understand a user's journey within your application.

## Alerts, dashboards, and more [alerts-and-dashboards]

Because EDOT Android also provides [direct access](manual-instrumentation.md) to the [OpenTelemetry SDK](https://opentelemetry.io/docs/languages/java/), you can generate your own data in ways that best suit your needs and take advantage of {{stack}}'s tools, such as:

 * [Create alerts](docs-content://explore-analyze/alerts-cases.md) when something interesting happens; for example, when an error is recorded.
 * [Build custom dashboards](docs-content://explore-analyze/dashboards.md) to display your data the way you need to see it

## Features

EDOT Android provides additional features on top of those that come with the [OpenTelemetry SDK](https://opentelemetry.io/docs/languages/java/).

### Disk buffering [disk-buffering]

Your application's telemetry data is stored locally before being sent to the {{stack}}. Then it's removed either after being successfully exported or to make room for new telemetry data if needed. This minimizes the risk of data loss due to internet connectivity issues.

### Central configuration

You can remotely manage how EDOT Android behaves through {{kib}}. Refer to [Central configuration](configuration.md#central-configuration) for more details.

### Real time [real-time]

For [distributed tracing](#distributed-tracing) to work properly, your application's time should be in sync with the [coordinated universal time](https://en.wikipedia.org/wiki/Coordinated_Universal_Time). This is sometimes an issue for Android applications, as the time provided by the OS is often not accurate enough. EDOT Android aims to synchronize telemetry timestamps with the universal time to ensure a reliable view of event timelines.

### Sessions [sessions]

A session groups telemetry data within a time frame when your application is active by adding a `session.id` attribute to all the spans and logs that come out of your application. This helps limit the volume of data you need to look at when investigating the steps that led to an error within your app. It can also give you insight into the common actions performed on each use.

A session is created when no previous session exists or when the previous one has expired. Sessions expire after 30 minutes of inactivity. You can extend this for up to 4 hours.

### Dynamic configuration [dynamic-configuration]

EDOT Android allows you to modify some values after its initialization has finished. Refer to [configuration](configuration.md).

### Automatic instrumentation [automatic-instrumentation]

EDOT Android provides extensions that automatically generate telemetry for common tools and use cases. Refer to [Automatic Instrumentation](automatic-instrumentation.md) for more details.

## Try it out

You can follow the [Sample application guide](https://github.com/elastic/apm-agent-android/tree/main/sample-app) to set up a test environment and take a quick look at the agent's functionalities. Or if you're ready to get started with your own app, continue with [Getting started](getting-started.md).