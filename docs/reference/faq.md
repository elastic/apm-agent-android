---
mapped_pages:
  - https://www.elastic.co/guide/en/apm/agent/android/current/faq.html
---

# Frequently asked questions [faq]

::::{warning}
This functionality is in technical preview and may be changed or removed in a future release. Elastic will work to fix any issues, but features in technical preview are not subject to the support SLA of official GA features.
::::

## Why is needed to enable desugaring support on apps with minSdk below 26? [why-desugaring]

Due to Android’s limited support for Java 8 features on devices with an API level < 26, or in other words, older than Android 8.0, you must add [Java 8 desugaring support](https://developer.android.com/studio/write/java8-support#library-desugaring) to apps with a `minSdk` value of less than 26. If you don’t, your app can crash when running on devices using Android OS versions older than 8.0. 

This requirement is inherited from the [OpenTelemetry Java SDK](https://github.com/open-telemetry/opentelemetry-java/blob/main/VERSIONING.md#language-version-compatibility), which this project is built upon.

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


## How can I configure SSL/TLS? [faq-ssl]

Please note that the Elastic Agent does not handle SSL/TLS configs internally, therefore, the recommended way to manage these types of configurations is by doing so as part of your app’s network security configurations, as explained in Android’s official [security guidelines](https://developer.android.com/privacy-and-security/security-ssl). Below we show a set of common use-cases and quick tips on what could be done on each one, however, each case might be different, so please refer to Android’s [official docs](https://developer.android.com/privacy-and-security/security-config) on this topic in case you need more details.


### Connecting to Elastic Cloud [faq-ssl-elastic-cloud]

If your Elastic Stack is hosted in Elastic Cloud, you shouldn’t need to add any SSL/TLS config changes in your app, it should work out of the box.


### Connecting to an on-prem server [faq-ssl-on-prem]

If your Elastic Stack is hosted on-prem, then it depends on the type of CA your host uses to sign its certificates, if it’s a commonly trusted CA, then you shouldn’t have to worry about changing your app’s SSL/TLS configuration as it all should work well out of the box, however, if your CAs are unknown/private or your server uses a self-signed certificate, then you would need to configure your app to trust custom CAs by following [Android’s guide on it](https://developer.android.com/privacy-and-security/security-config).


### Debugging purposes [faq-ssl-debug]

If you’re running a local server and need to connect to it without using https in order to run a quick test, then you could temporarily [enable cleartext traffic](https://developer.android.com/guide/topics/manifest/application-element#usesCleartextTraffic) within your `AndroidManifest.xml` file, inside the `<application>` tag. As shown below:

```xml
<application
    ...
    android:usesCleartextTraffic="true">
    ...
</application>
```

::::{note}
You should only enable cleartext traffic for debugging purposes and not for production code.
::::


If enabling cleartext traffic isn’t a valid option for your debugging use-case, you should refer to Android’s guide on [configuring CAs for debugging](https://developer.android.com/privacy-and-security/security-config#TrustingDebugCa).

For more information on how Android handles network security, please refer to the official [Android docs on it](https://developer.android.com/privacy-and-security/security-ssl).


## What if the agent doesn’t support the technologies I’m using? [faq-unsupported-technologies]

The agent is built on top of [OpenTelemetry](https://opentelemetry.io/docs/instrumentation/java/getting-started/) You can use its public API to create custom spans and transactions.

