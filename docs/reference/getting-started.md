# Getting started

## Requirements

| Requirement                                       | Minimum version                                                                                           |
|---------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| [{{stack}}](https://www.elastic.co/elastic-stack) | 8.18                                                                                                      |
| Android Gradle plugin                             | 7.4.0                                                                                                     |
| Android API level                                 | 26 (or 21 with [desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring)) |

:::{important} - Apps with minSdk below 26
If your application's [minSdk](https://developer.android.com/studio/publish/versioning#minsdk) value is lower than 26, you **must** add [Java 8 desugaring support](https://developer.android.com/studio/write/java8-support#library-desugaring).

More info on [FAQs](faq.md#why-desugaring).
:::

## Gradle setup

Add the [Elastic OTel agent plugin](https://plugins.gradle.org/plugin/co.elastic.otel.android.agent) to your application’s `build.gradle[.kts]` file, as shown below:

```kotlin
plugins {
    id("com.android.application")
    id("co.elastic.otel.android.agent") version "[latest_version]" // <1>
}
```

1. You can find the latest version [here](https://plugins.gradle.org/plugin/co.elastic.otel.android.agent).

## Agent setup

Once the gradle setup is done, you'll need to initialize the agent within your app's code, as shown below:

```kotlin
val agent = ElasticApmAgent.builder(application) // <1>
    .setServiceName("My app name") // <2>
    .setExportUrl("http://10.0.2.2:4318") // <3>
    .setExportAuthentication(Authentication.ApiKey("my-api-key")) // <4>
    .build()
```

1. Your [Application](https://developer.android.com/reference/android/app/Application) instance. Check out [how to get it](how-to.md#get-application).
2. "Service" is OpenTelemetry's jargon for "entity that produces telemetry", so here's where your application name should go. More info on [FAQs](faq.md#why-service-name).
3. This is the Elastic endpoint where all your telemetry will be exported to. If you don't have one yet, check out [how to get it](how-to.md#get-export-endpoint).
4. Using an API Key is the recommended authentication method for the agent to connect to your {{stack}}. If you don't have one yet, check out [how to create one](how-to.md#create-api-key).

## Hello World!

The agent is fully initialized, so now you can start sending telemetry to your {{stack}}! Here's a quick example of manually creating a [span](https://opentelemetry.io/docs/concepts/signals/traces/#spans) and finding it in {{kib}}:

### Generate telemetry

```kotlin
val agent = ElasticApmAgent.builder(application)
    //...
    .build()


agent.span("My Span") {
    Thread.sleep(500) // <1>
    agent.span("My nested Span") { // <2>
        Thread.sleep(500) 
    }
}
```
1. This is to simulate some code execution for which we want to measure the time it takes to complete.
2. This is to demonstrate how does span hierarchies look like in {{kib}}.

### Visualize telemetry

Once your app has sent telemetry data, either manually or automatically, you should be able to visualize it in {{kib}} by navigating to **Applications -> Service Inventory** in the main menu, or alternatively, searching for "Service Inventory" in the [global search field](https://www.elastic.co/guide/en/kibana/current/introduction.html#kibana-navigation-search).

You should find your application listed there, as shown below:

:::{image} ../images/span-visualization/1.png
:screenshot:
:width: 350px
:::

When you open it, **go to the "Transactions" tab** where you should see your app's "outermost" span listed, as shown below:

:::{image} ../images/span-visualization/2.png
:screenshot:
:width: 350px
:::

And after clicking on our span, we should see it in detail:

:::{image} ../images/span-visualization/3.png
:screenshot:
:::
