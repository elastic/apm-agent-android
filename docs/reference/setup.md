---
mapped_pages:
  - https://www.elastic.co/guide/en/apm/agent/android/current/setup.html
---

# Set up the Agent [setup]

::::{warning}
This functionality is in technical preview and may be changed or removed in a future release. Elastic will work to fix any issues, but features in technical preview are not subject to the support SLA of official GA features.
::::


Follow these steps to start reporting your Android application’s performance to Elastic APM:

1. [Set up Gradle](#gradle-setup).
2. [(Optional) Manual set up](#manual-setup).
3. [Compile and run](#compile-and-run).
4. [What’s next?](#whats-next)


## Set up Gradle [gradle-setup]


### Requirements [gradle-requirements]

| Requirement | Minimum version |
| --- | --- |
| Android Gradle plugin | 7.4.0 |
| Android API level | 21 |


### For projects using minSdkVersion < 26 [minsdk-21-support]

Due to Android’s limited support for Java 8 features on devices with an API level < 26, or in other words, older than Android 8.0, you must add [Java 8+ desugaring support](https://developer.android.com/studio/write/java8-support#library-desugaring) to apps with a `minSdkVersion` less than 26. If you don’t, your app can crash when running on devices using Android OS versions older than 8.0. This is because the [OpenTelemetry Java SDK](https://github.com/open-telemetry/opentelemetry-java), which this SDK is built upon, uses Java 8 features.

You can skip this step if your `minSdkVersion` is 26 or higher.


### Add the Elastic Agent Gradle plugin [adding-gradle-plugin]

To automatically instrument [Supported Technologies](/reference/automatic-instrumentation.md), add the [Elastic APM agent plugin](https://plugins.gradle.org/plugin/co.elastic.apm.android/0.20.0) to your application’s `build.gradle` file as shown below:

```groovy
// Android app's build.gradle file
plugins {
    id "com.android.application"
    id "co.elastic.apm.android" version "0.20.0"
}
```

After adding the agent plugin, configure it. A minimal configuration sets the Elastic APM Server endpoint as shown below:

```groovy
// Android app's build.gradle file
plugins {
    //...
    id "co.elastic.apm.android" version "[latest_version]" <1>
}

elasticApm {
    // Minimal configuration
    serverUrl = "https://your.elastic.server"

    // Optional
    serviceName = "your app name" <2>
    serviceVersion = "0.0.0" <3>
    apiKey = "your server api key" <4>
    secretToken = "your server auth token" <5>
}
```

1. You can find the latest version in the [Gradle plugin portal](https://plugins.gradle.org/plugin/co.elastic.apm.android).
2. Defaults to your `android.defaultConfig.applicationId` value.
3. Defaults to your `android.defaultConfig.versionName` value.
4. Defaults to null. More info on API Keys [here](https://www.elastic.co/docs/api/doc/elasticsearch/operation/operation-security-create-api-key).
5. Defaults to null.


::::{note}
When both `secretToken` and `apiKey` are provided, apiKey has priority and secretToken is ignored.
::::



### Set up your application [application-setup]

After syncing your project with the Gradle changes above, the Elastic APM agent needs to be initialized within your [Application class](https://developer.android.com/reference/android/app/Application). This example shows the simplest way to configure the agent:

```java
// Your Application class

class MyApp extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ElasticApmAgent.initialize(this); <1>
    }
}
```

1. Initialize the Elastic APM agent once.



## (Optional) Manual set up [manual-setup]

If you can’t add the Elastic Agent Gradle plugin to your application as shown above, complete the following steps to set up the Elastic SDK manually.


### Add the SDK dependency [gradle-dependencies]

Add the Elastic APM agent SDK to your application’s `build.gradle` file as shown below:

```groovy
// Android app's build.gradle file
dependencies {
    implementation "co.elastic.apm:android-sdk:0.20.0"
}
```


### Configure your app’s info and connectivity parameters [manual-configuration]

* Set your app name, version, and environment name, as explained [here](/reference/configuration.md#app-id-configuration).
* Set your server connectivity parameters, as explained [here](/reference/configuration.md#app-server-connectivity).

::::{note}
Without the Gradle plugin, the Elastic SDK won’t be able to provide automatic instrumentations for its [*Supported technologies*](/reference/automatic-instrumentation.md).
::::



## Compile and run [compile-and-run]

All that’s left is to compile and run your application. That’s it!


## What’s next? [whats-next]

After initializing the agent (by using the gradle plugin), your application will automatically create traces for all OkHttp network requests (including those created by tools that make use of OkHttp, like Retrofit) and all [Activity](https://developer.android.com/reference/android/app/Activity) and [Fragment](https://developer.android.com/reference/androidx/fragment/app/Fragment) starting methods.

Apart from the automatic instrumentation helped by the Gradle plugin, you’ll get automatic crash reports when an unexpected error occurs in your app, regardless of whether the Gradle plugin is available or not.

All of these events will contain a [Session](https://opentelemetry.io/docs/specs/semconv/general/session/) ID that links related events together—allowing you to make sense of and diagnose any issues that arise. Head to the **APM app in {{kib}}** to start exploring your data.

If you need to customize the Elastic APM agent to your project’s needs, see [configuration](/reference/configuration.md). If you need to create your own custom transactions, see [manual instrumentation](/reference/manual-instrumentation.md).
