# Getting started

## Requirements

| Requirement           | Minimum version                                                                                           |
|-----------------------|-----------------------------------------------------------------------------------------------------------|
| {{stack}}             | 8.18                                                                                                      |
| Android Gradle plugin | 7.4.0                                                                                                     |
| Android API level     | 26 (or 21 with [desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring)) |

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

1. You need to pass your application's instance. Check out [how to get it](how-to.md#get-application).
2. "Service" is OpenTelemetry's jargon for "entity that produces telemetry", so here's where your application name should go. More info on [FAQs](faq.md#why-service-name).
3. This is the Elastic endpoint where all your telemetry will be exported to. If you don't have one yet, check out [how to get it](how-to.md#get-export-endpoint).
4. Using an API Key is the recommended authentication method for the agent to connect to your {{stack}}. If you don't have one yet, check out [how to create one](how-to.md#create-api-key).
