---
mapped_pages:
  - https://www.elastic.co/guide/en/apm/agent/android/current/supported-technologies.html
---

# Automatic instrumentation [automatic-instrumentation]

::::{warning}
This functionality is in technical preview and may be changed or removed in a future release. Elastic will work to fix any issues, but features in technical preview are not subject to the support SLA of official GA features.
::::


The Elastic APM Android agent is built on top of the [OpenTelemetry Java SDK](https://opentelemetry.io) — extending its functionality while also automatically instrumenting various APIs and frameworks. This section lists all supported technologies.

* [Android Gradle Plugin versions](#supported-agp-versions)
* [Android runtime versions](#supported-android-runtime-versions)
* [Languages](#supported-languages)
* [UI frameworks](#supported-ui-frameworks)
* [Networking frameworks](#supported-networking-frameworks)


## Android Gradle Plugin versions [supported-agp-versions]

| Supported versions |
| --- |
| >= 7.4.0 |


## Android runtime versions [supported-android-runtime-versions]

| Supported versions |
| --- |
| API >= 21 |

::::{note}
If your minSdk version is lower than 26, then you must add [Java 8+ desugaring support](https://developer.android.com/studio/write/java8-support#library-desugaring) to your application.
::::



## Languages [supported-languages]

The Java version is for the supported JDK, which is aligned with the JDK version supported by the Android Gradle plugin. The Kotlin version refers to the Kotlin gradle plugin versions, also aligned with the versions supported by the Android Gradle plugin.

| Language | Supported versions |
| --- | --- |
| Java | 11 |
| Kotlin | 1.8+ |


## UI frameworks [supported-ui-frameworks]

| Class | Notes | Since |
| --- | --- | --- |
| [Activity](https://developer.android.com/reference/android/app/Activity) | Comes from the Android SDK | 0.1.0 |
| [Fragment](https://developer.android.com/reference/androidx/fragment/app/Fragment.html) | Comes from the [Android Jetpack tools](https://developer.android.com/jetpack) | 0.1.0 |


## Networking frameworks [supported-networking-frameworks]

Distributed tracing will only work if you are using one of the supported networking frameworks.

For the supported HTTP libraries, the agent automatically creates spans for outgoing HTTP requests and propagates tracing headers. The spans are named after the schema `<method> <host>`, for example `GET elastic.co`.

| Framework | Supported versions | Note | Since |
| --- | --- | --- | --- |
| OkHttp | 3.11+ | OkHttp-managed threads and Kotlin coroutine related calls are automatically traced. Calls from tools using OkHttp (such as Retrofit) are automatically traced as well. | 0.1.0 |

