---
mapped_pages:
  - https://www.elastic.co/guide/en/apm/agent/android/current/faq.html
---

# Frequently asked questions

## Why is enabling desugaring support on apps with minSdk below 26 necessary?

Android devices with an API level below 26 (older than [Android 8.0](https://developer.android.com/about/versions/oreo/android-8.0)) have limited support for Java 8 features and types, which can cause your app to crash when using those types while running on those older-than-8.0 devices. For example, if one of your app's dependencies uses the [Base64](https://docs.oracle.com/javase/8/docs/api/java/util/Base64.html) type ([added in API level 26](https://developer.android.com/reference/java/util/Base64)), and your app is installed on an Android device with OS version 7.0 ([API level 24](https://developer.android.com/about/versions/nougat/android-7.0)), a crash will happen when the code that uses said type is executed due to a "class not found" error.

To prevent these kinds of issues on devices using Android OS older than 8.0, you must add [Java 8 desugaring support](https://developer.android.com/studio/write/java8-support#library-desugaring) to your app. This requirement is inherited from the [OpenTelemetry Java SDK](https://github.com/open-telemetry/opentelemetry-java/blob/main/VERSIONING.md#language-version-compatibility), which this project is built upon, where several of the unsupported types for Android versions older than 8.0 are used.

## Why does my app have to be referred to as "service"?

TL;DR: It's complicated to change it to another name because of its widespread adoption within the OpenTelemetry community.

For historic reasons, `service` has been the default way of referring to "an entity that produces telemetry". This term made its way into OpenTelemetry to a point where it was marked as one of the first "stable" resource names, meaning that it was no longer possible/feasible to make a change to another name that would better represent any kind of telemetry source. This has been debated several times within the community. A recent discussion attempts to [explain the `service` description](https://github.com/open-telemetry/semantic-conventions/pull/630) and what it should represent in an effort to reduce confusion. However, there doesn't seem to be a consensus.