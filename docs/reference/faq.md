---
mapped_pages:
  - https://www.elastic.co/guide/en/apm/agent/android/current/faq.html
---

# Frequently asked questions [faq]

## Why is needed to enable desugaring support on apps with minSdk below 26? [why-desugaring]

Android devices with an API level < 26 (older than [Android 8.0](https://developer.android.com/about/versions/oreo/android-8.0)) have limited support for Java 8 features and types, which can cause your app to crash when using those types while running on those older-than-8.0 devices. For example, if one of your app's dependencies uses the [Base64](https://docs.oracle.com/javase/8/docs/api/java/util/Base64.html) type ([added in API level 26](https://developer.android.com/reference/java/util/Base64)) and then your app is installed on an Android device with OS version 7.0 ([API level 24](https://developer.android.com/about/versions/nougat/android-7.0)) then a crash will happen when the code that uses said type is executed due to a "class not found" error.

To prevent these kinds of issues on devices using Android OS older than 8.0, you must add [Java 8 desugaring support](https://developer.android.com/studio/write/java8-support#library-desugaring) to your app. This requirement is inherited from the [OpenTelemetry Java SDK](https://github.com/open-telemetry/opentelemetry-java/blob/main/VERSIONING.md#language-version-compatibility), which this project is built upon, where several of the unsupported types for Android < 8.0 are used.

## Why does my app have to be referred to as "service"? [why-service]

TL;DR: It's complicated to change it to another name because of its widespread adoption within the OpenTelemetry community.

For historic reasons, `service` has been the default way of referring to "an entity that produces telemetry". This term made its way into OpenTelemetry to a point where it was marked as one of the first "stable" resource names, meaning that it was no longer possible/feasible to make a change to another name that would better represent any kind of telemetry source. This has been debated several times within the community, with one of the latest ones being an attempt to [explain in the `service` description](https://github.com/open-telemetry/semantic-conventions/pull/630) what it should represent, as an effort to reduce the confusion it may cause, but so far there doesn't seem to be a consensus.

## How does the agent work? [faq-how-does-it-work]

The agent auto-instruments known frameworks and libraries and records interesting events, like HTTP requests. To do this, it leverages the capability of the JVM to instrument the bytecode of classes. This means that no code changes are required for supported technologies.

The agent automatically and safely injects small pieces of code before and after interesting events to measure their duration and metadata, like HTTP-related information, including the URL, parameters, and headers.

These events, called Spans, are sent to the APM Server which converts them to a format suitable for Elasticsearch, and sends them to an Elasticsearch cluster. You can then use the APM app in Kibana to gain insight into latency issues and error culprits within your application.

The instrumentation happens at compile time, using the [Android Gradle plugin Instrumentation](https://developer.android.com/reference/tools/gradle-api/7.2/com/android/build/api/variant/Instrumentation) API.


## Is the agent doing bytecode instrumentation? [faq-bytecode-instrumentation]

Yes


## How safe is bytecode instrumentation? [faq-bytecode-instrumentation-safety]

Elastic APM uses the popular bytecode instrumentation library [Byte Buddy](http://bytebuddy.net:), which takes care of the heavy lifting of dealing with actual bytecode and lets us write the instrumentation in pure Java.

Byte Buddy is widely used in popular Open Source projects, for example, Hibernate, Jackson, Mockito and is also commonly used by APM vendors. It is created by a Java Champion, awarded with the Dukes Choice award and currently downloaded over 75 million times a year.

Unlike other bytecode instrumentation libraries, Byte Buddy is designed so that it is impossible to corrupt the bytecode of instrumented classes. It also respects other transformations applied to your application at the same time.

## What if the agent doesn’t support the technologies I’m using? [faq-unsupported-technologies]

The agent is built on top of [OpenTelemetry](https://opentelemetry.io/docs/instrumentation/java/getting-started/) You can use its public API to create custom spans and transactions.

